package dev.domaincentric.sample.ecommerce.account.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * CSRF strategy (ADR-035): browser forms carry the token from the {@code XSRF-TOKEN} cookie; the
 * Bearer-only API is exempt because cookies never authenticate it.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own instead of polluting the repository tests.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:csrf_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class CsrfProtectionIntegrationTest {

  private static final Pattern PRODUCT_LINK = Pattern.compile("href=\"/products/([0-9a-f-]{36})\"");
  private static final Pattern CSRF_FIELD = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Web POST without CSRF token is rejected")
  void webPostWithoutTokenIsRejected() throws Exception {
    mockMvc
        .perform(post("/cart/add-product").param("productId", "x").param("quantity", "1"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Web POST with the token from the page is accepted")
  void webPostWithTokenFollowsTheNormalFlow() throws Exception {
    final MvcResult products =
        mockMvc.perform(get("/products")).andExpect(status().isOk()).andReturn();
    final Matcher matcher = PRODUCT_LINK.matcher(products.getResponse().getContentAsString());
    assertThat(matcher.find()).isTrue();
    final String productId = matcher.group(1);

    // The detail page renders the add-to-cart form — and with it the token (cookie + hidden field)
    final MvcResult detail =
        mockMvc.perform(get("/products/" + productId)).andExpect(status().isOk()).andReturn();
    final Matcher field = CSRF_FIELD.matcher(detail.getResponse().getContentAsString());
    assertThat(field.find()).as("form carries the _csrf field").isTrue();
    assertThat(cookieNamed(detail.getResponse(), "XSRF-TOKEN")).isNotNull();

    mockMvc
        .perform(
            post("/cart/add-product")
                .cookie(detail.getResponse().getCookies())
                .param("_csrf", field.group(1))
                .param("productId", productId)
                .param("quantity", "1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", "/cart"));
  }

  @Test
  @DisplayName("API POST without cookies or token is not blocked by CSRF")
  void apiPostIsExemptFromCsrf() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nobody@example.com\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized()); // reached the resource; 403 would mean CSRF
  }

  @Test
  @DisplayName("API ignores browser cookies — a session cookie neither authenticates nor is issued")
  void apiIgnoresCookies() throws Exception {
    final MvcResult web = mockMvc.perform(get("/products")).andReturn();
    final Cookie[] browserCookies = web.getResponse().getCookies();
    assertThat(browserCookies).isNotEmpty(); // the web side did hand out an identity cookie

    final MvcResult api =
        mockMvc
            .perform(post("/api/carts").cookie(browserCookies).param("customerId", "c-1"))
            .andExpect(status().isCreated())
            .andReturn();

    assertThat(api.getResponse().getCookies()).isEmpty();
    assertThat(api.getResponse().getHeaders("Set-Cookie")).isEmpty();
  }

  private static Cookie cookieNamed(final MockHttpServletResponse response, final String name) {
    return Arrays.stream(response.getCookies())
        .filter(c -> name.equals(c.getName()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no cookie " + name));
  }
}
