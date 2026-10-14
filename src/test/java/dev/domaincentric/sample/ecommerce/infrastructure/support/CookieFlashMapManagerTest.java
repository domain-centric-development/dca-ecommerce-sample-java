package dev.domaincentric.sample.ecommerce.infrastructure.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.FlashMap;

@DisplayName("The flash maps travel in a sealed cookie")
class CookieFlashMapManagerTest {

  private static final byte[] KEY = new byte[32];
  private static final byte[] OTHER_KEY = new byte[32];

  static {
    Arrays.fill(KEY, (byte) 7);
    Arrays.fill(OTHER_KEY, (byte) 9);
  }

  private final CookieFlashMapManager manager = new CookieFlashMapManager(KEY, false, "Lax");

  @Test
  @DisplayName("the page the redirect leads to reads the notice, once")
  void thePageItIsAddressedToReadsItOnce() {
    final Cookie sealed = redirectTo("/cart", "notice.cart", "product-added");

    final MockHttpServletResponse first = new MockHttpServletResponse();
    final FlashMap read = manager.retrieveAndUpdate(get("/cart", sealed), first);

    assertThat(asMap(read)).containsEntry("notice.cart", "product-added").containsEntry("count", 3);
    assertThat(first.getCookie(CookieFlashMapManager.COOKIE_NAME).getMaxAge()).isZero();
  }

  @Test
  @DisplayName("another page leaves it for the page it is meant for")
  void anotherPageLeavesIt() {
    final Cookie sealed = redirectTo("/cart", "notice.cart", "product-added");

    final MockHttpServletResponse elsewhere = new MockHttpServletResponse();
    assertThat(asMap(manager.retrieveAndUpdate(get("/checkout/buyer", sealed), elsewhere)))
        .isNull();
    assertThat(elsewhere.getCookie(CookieFlashMapManager.COOKIE_NAME)).isNull();

    assertThat(
            asMap(manager.retrieveAndUpdate(get("/cart", sealed), new MockHttpServletResponse())))
        .containsEntry("notice.cart", "product-added");
  }

  @Test
  @DisplayName("the cookie does not show what it says")
  void theCookieIsEncrypted() {
    final Cookie sealed = redirectTo("/cart", "notice.cart", "product-added");

    assertThat(sealed.getValue()).doesNotContain("product").doesNotContain("cart");
    assertThat(sealed.isHttpOnly()).isTrue();
  }

  @Test
  @DisplayName("a changed cookie, or one sealed with another key, says nothing")
  void aForgedCookieSaysNothing() {
    final Cookie sealed = redirectTo("/cart", "notice.cart", "product-added");
    final char[] value = sealed.getValue().toCharArray();
    value[value.length / 2] = value[value.length / 2] == 'A' ? 'B' : 'A';

    assertThat(
            asMap(
                manager.retrieveAndUpdate(
                    get("/cart", new Cookie(CookieFlashMapManager.COOKIE_NAME, new String(value))),
                    new MockHttpServletResponse())))
        .isNull();
    assertThat(
            asMap(
                new CookieFlashMapManager(OTHER_KEY, false, "Lax")
                    .retrieveAndUpdate(get("/cart", sealed), new MockHttpServletResponse())))
        .isNull();
    assertThat(
            asMap(
                manager.retrieveAndUpdate(
                    get("/cart", new Cookie(CookieFlashMapManager.COOKIE_NAME, "not-base64!")),
                    new MockHttpServletResponse())))
        .isNull();
  }

  @Test
  @DisplayName("a second notice in the same request keeps the first one's removal")
  void aRedirectAfterReadingKeepsOnlyTheNewNotice() {
    final Cookie sealed = redirectTo("/checkout/delivery", "notice.checkout-step", "x");
    final MockHttpServletRequest request = get("/checkout/delivery", sealed);
    final MockHttpServletResponse response = new MockHttpServletResponse();
    manager.retrieveAndUpdate(request, response);

    final FlashMap next = new FlashMap();
    next.put("notice.checkout-step", "y");
    next.setTargetRequestPath("/checkout/buyer");
    manager.saveOutputFlashMap(next, request, response);

    final Cookie last = response.getCookies()[response.getCookies().length - 1];
    assertThat(
            asMap(
                manager.retrieveAndUpdate(
                    get("/checkout/delivery", last), new MockHttpServletResponse())))
        .isNull();
    assertThat(
            asMap(
                manager.retrieveAndUpdate(
                    get("/checkout/buyer", last), new MockHttpServletResponse())))
        .containsEntry("notice.checkout-step", "y");
  }

  @Test
  @DisplayName("a later notice for a page replaces one it left unread")
  void aLaterNoticeReplacesAnUnreadOne() {
    final Cookie first = redirectTo("/cart", "notice.cart", "product-added");
    final FlashMap later = new FlashMap();
    later.put("notice.cart", "carts-merged");
    later.setTargetRequestPath("/cart");
    final MockHttpServletResponse response = new MockHttpServletResponse();
    manager.saveOutputFlashMap(later, get("/cart/merge", first), response);

    final Cookie sealed = response.getCookie(CookieFlashMapManager.COOKIE_NAME);
    final MockHttpServletResponse read = new MockHttpServletResponse();
    assertThat(asMap(manager.retrieveAndUpdate(get("/cart", sealed), read)))
        .containsEntry("notice.cart", "carts-merged");
    assertThat(read.getCookie(CookieFlashMapManager.COOKIE_NAME).getMaxAge()).isZero();
  }

  @Test
  @DisplayName("only a code and a number go into the cookie")
  void onlyStringsAndIntegers() {
    final FlashMap flashMap = new FlashMap();
    flashMap.put("notice", LocalDate.now());
    flashMap.setTargetRequestPath("/cart");

    assertThatThrownBy(
            () ->
                manager.saveOutputFlashMap(
                    flashMap, get("/cart/add-product", null), new MockHttpServletResponse()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("a key that is not AES-sized is refused")
  void refusesAShortKey() {
    assertThatThrownBy(() -> new CookieFlashMapManager(new byte[5], false, "Lax"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private Cookie redirectTo(final String path, final String name, final String code) {
    final FlashMap flashMap = new FlashMap();
    flashMap.put(name, code);
    flashMap.put("count", 3);
    flashMap.setTargetRequestPath(path);
    final MockHttpServletResponse response = new MockHttpServletResponse();
    manager.saveOutputFlashMap(flashMap, get("/somewhere", null), response);
    return response.getCookie(CookieFlashMapManager.COOKIE_NAME);
  }

  /**
   * A flash map seen as the map it is — it is also Comparable, which AssertJ cannot choose between.
   */
  private static @Nullable Map<String, Object> asMap(final @Nullable FlashMap flashMap) {
    return flashMap;
  }

  private static MockHttpServletRequest get(final String path, final @Nullable Cookie cookie) {
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
    if (cookie != null) {
      request.setCookies(cookie);
    }
    return request;
  }
}
