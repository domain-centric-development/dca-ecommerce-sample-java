package dev.domaincentric.sample.ecommerce.backoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * The header of a back-office page, rendered by the wired application with both real security
 * chains, for a browser that is logged in to the shop and to the back office at once.
 */
// The shopper registers an address of their own, so the class gets a database of its own: no other
// test can have registered the same address first.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:backoffice_header_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Backoffice header")
class BackofficeHeaderIntegrationTest {

  private static final String SHOPPER = "anna.schmidt@example.com";

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("The back office header shows no shopper")
  void eventLogHeaderShowsNothingOfTheShopperLoggedInToTheShop() throws Exception {
    final Map<String, Cookie> cookies = new LinkedHashMap<>();
    final MockHttpSession session = new MockHttpSession();
    final Document registration = keep(cookies, perform(withCookies(get("/register"), cookies)));
    keep(
        cookies,
        perform(
            withCookies(post("/register"), cookies)
                .param("_csrf", csrf(registration))
                .param("firstName", "Anna")
                .param("lastName", "Schmidt")
                .param("dateOfBirth", "1990-05-17")
                .param("email", SHOPPER)
                .param("password", "Secret123")
                .param("confirmPassword", "Secret123")));
    assertThat(perform(withCookies(get("/account"), cookies)).getStatus())
        .as("the shopper is logged in to the shop")
        .isEqualTo(200);
    final Document loginForm =
        keep(cookies, perform(withCookies(get("/backoffice/login"), cookies).session(session)));
    assertThat(
            perform(
                    withCookies(post("/backoffice/login"), cookies)
                        .session(session)
                        .param("_csrf", csrf(loginForm))
                        .param("username", "admin")
                        .param("password", "admin"))
                .getRedirectedUrl())
        .as("the operator is logged in to the back office")
        .isEqualTo("/backoffice/events");

    final MockHttpServletResponse eventLog =
        perform(withCookies(get("/backoffice/events"), cookies).session(session));

    assertThat(eventLog.getStatus()).isEqualTo(200);
    final Element header =
        Jsoup.parse(eventLog.getContentAsString()).selectFirst("header[data-test=site-header]");
    assertThat(header).as("the page's header").isNotNull();
    assertThat(header.select("[data-test=user-greeting]")).isEmpty();
    assertThat(header.text()).doesNotContain("Welcome, " + SHOPPER);
    assertThat(header.select("[data-test=nav-my-account-link]")).isEmpty();
    assertThat(header.text()).doesNotContain("My Account");
    assertThat(header.select("[data-test=mini-basket]")).isEmpty();
    assertThat(header.select("a:matchesOwn(^Cart$), a:has(span:matchesOwn(^Cart$))")).isEmpty();
  }

  private MockHttpServletResponse perform(final MockHttpServletRequestBuilder request)
      throws Exception {
    return mockMvc.perform(request).andReturn().getResponse();
  }

  /** Sends the cookies the browser holds, as it would; a fresh browser sends none. */
  private static MockHttpServletRequestBuilder withCookies(
      final MockHttpServletRequestBuilder request, final Map<String, Cookie> cookies) {
    return cookies.isEmpty() ? request : request.cookie(cookies.values().toArray(Cookie[]::new));
  }

  /** Keeps the cookies a response sets, as the browser would; returns the response's page. */
  private static Document keep(
      final Map<String, Cookie> cookies, final MockHttpServletResponse response) throws Exception {
    for (final Cookie cookie : response.getCookies()) {
      if (cookie.getMaxAge() == 0) {
        cookies.remove(cookie.getName());
      } else {
        cookies.put(cookie.getName(), cookie);
      }
    }
    return Jsoup.parse(response.getContentAsString());
  }

  private static String csrf(final Document page) {
    final Element field = page.selectFirst("input[name=_csrf]");
    assertThat(field).as("the form's CSRF field").isNotNull();
    return field.attr("value");
  }
}
