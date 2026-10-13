package dev.domaincentric.sample.ecommerce.backoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.util.HtmlUtils;

/**
 * Who the back office lets in, run against the wired application with both real security chains: a
 * shop customer is not let in, an operator login always lands on the event log, and a logout closes
 * the back office again.
 */
// The customer registers an address of their own, so the class gets a database of its own: no
// other test can have registered the same address first.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:backoffice_access_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Backoffice access")
class BackofficeAccessIntegrationTest {

  private static final String LOGIN_PAGE = "/backoffice/login";
  private static final String EVENT_LOG = "/backoffice/events";

  private static final Pattern HEADING = Pattern.compile("<h1>([^<]*)</h1>");
  private static final Pattern CSRF_FIELD = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("A customer login does not open the back office")
  void aCustomerLoggedInToTheShopIsSentToTheBackofficeLogin() throws Exception {
    final Map<String, Cookie> cookies = new LinkedHashMap<>();
    final String registration = keep(cookies, perform(withCookies(get("/register"), cookies)));
    keep(
        cookies,
        perform(
            withCookies(post("/register"), cookies)
                .param("_csrf", text(CSRF_FIELD, registration))
                .param("firstName", "Anna")
                .param("lastName", "Schmidt")
                .param("dateOfBirth", "1990-05-17")
                .param("email", "anna.schmidt@example.com")
                .param("password", "Secret123")
                .param("confirmPassword", "Secret123")));
    assertThat(perform(withCookies(get("/account"), cookies)).getStatus())
        .as("the customer is logged in to the shop")
        .isEqualTo(200);

    final MockHttpServletResponse eventLog = perform(withCookies(get(EVENT_LOG), cookies));

    assertThat(redirectPath(eventLog)).isEqualTo(LOGIN_PAGE);
  }

  @Test
  @DisplayName("Login after a redirect opens the event log")
  void operatorLoginAfterARedirectFromAnotherPageOpensTheEventLog() throws Exception {
    final MockHttpSession session = new MockHttpSession();
    assertThat(redirectPath(perform(get("/backoffice/settings").session(session))))
        .isEqualTo(LOGIN_PAGE);

    final String landed = logIn(session);

    assertThat(URI.create(landed).getPath()).isEqualTo(EVENT_LOG);
    assertThat(text(HEADING, page(landed, session))).isEqualTo("Event Publication Log");
  }

  @Test
  @DisplayName("Logout confirms on the login page")
  void logoutLeadsToTheLoginPageWithItsConfirmation() throws Exception {
    final MockHttpSession session = new MockHttpSession();
    logIn(session);

    final String landed = logOut(session);

    assertThat(landed).isEqualTo(LOGIN_PAGE + "?logout=true");
    assertThat(text(dataTest("logout-message"), page(landed, new MockHttpSession())))
        .isEqualTo("You have been logged out.");
  }

  @Test
  @DisplayName("Event log is closed after logout")
  void eventLogSendsTheOperatorToTheLoginAgainAfterTheLogout() throws Exception {
    final MockHttpSession session = new MockHttpSession();
    logIn(session);
    logOut(session);

    final MockHttpServletResponse eventLog = perform(get(EVENT_LOG).session(session));

    assertThat(redirectPath(eventLog)).isEqualTo(LOGIN_PAGE);
  }

  /** Logs the operator in on the login page with the demo credentials; returns where it leads. */
  private String logIn(final MockHttpSession session) throws Exception {
    final String form = page(LOGIN_PAGE, session);
    return mockMvc
        .perform(
            post(LOGIN_PAGE)
                .session(session)
                .param("_csrf", text(CSRF_FIELD, form))
                .param("username", "admin")
                .param("password", "admin"))
        .andExpect(status().is3xxRedirection())
        .andReturn()
        .getResponse()
        .getRedirectedUrl();
  }

  /** Presses "Logout" on the event log; returns where it leads. */
  private String logOut(final MockHttpSession session) throws Exception {
    final String eventLog = page(EVENT_LOG, session);
    return mockMvc
        .perform(
            post("/backoffice/logout").session(session).param("_csrf", text(CSRF_FIELD, eventLog)))
        .andExpect(status().is3xxRedirection())
        .andReturn()
        .getResponse()
        .getRedirectedUrl();
  }

  /** A page as the browser gets it within the operator's session. */
  private String page(final String path, final MockHttpSession session) throws Exception {
    return mockMvc
        .perform(get(URI.create(path)).session(session))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  private MockHttpServletResponse perform(final MockHttpServletRequestBuilder request)
      throws Exception {
    return mockMvc.perform(request).andReturn().getResponse();
  }

  /** The path a response sends the browser to; fails when it sends it nowhere. */
  private static String redirectPath(final MockHttpServletResponse response) {
    assertThat(response.getStatus()).as("a redirect").isBetween(300, 399);
    return URI.create(response.getRedirectedUrl()).getPath();
  }

  /** Sends the cookies the browser holds, as it would; a fresh browser sends none. */
  private static MockHttpServletRequestBuilder withCookies(
      final MockHttpServletRequestBuilder request, final Map<String, Cookie> cookies) {
    return cookies.isEmpty() ? request : request.cookie(cookies.values().toArray(Cookie[]::new));
  }

  /** Keeps the cookies a response sets, as the browser would; returns the response's page. */
  private static String keep(
      final Map<String, Cookie> cookies, final MockHttpServletResponse response) throws Exception {
    for (final Cookie cookie : response.getCookies()) {
      if (cookie.getMaxAge() == 0) {
        cookies.remove(cookie.getName());
      } else {
        cookies.put(cookie.getName(), cookie);
      }
    }
    return response.getContentAsString();
  }

  private static Pattern dataTest(final String dataTest) {
    return Pattern.compile(
        "<\\w+[^>]*\\bdata-test=\"" + Pattern.quote(dataTest) + "\"[^>]*>([^<]*)<");
  }

  private static String text(final Pattern pattern, final String html) {
    final Matcher matcher = pattern.matcher(html);
    assertThat(matcher.find()).as("%s in the rendered page", pattern).isTrue();
    return HtmlUtils.htmlUnescape(matcher.group(1)).strip();
  }
}
