package dev.domaincentric.sample.ecommerce.account;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.HtmlUtils;

/**
 * The login page a visitor without cookies opens, rendered by the wired application: the way to it
 * from the header, the form it shows, where a login leads, and the page a logout ends on.
 */
// MockMvc requests commit, and the shared H2 database outlives the Spring context — so this class
// gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:login_page_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Login page")
class LoginPageIntegrationTest {

  private static final String EMAIL = "anna.schmidt@example.com";
  private static final String PASSWORD = "Secret123";

  private static final Pattern HEADING = Pattern.compile("<h2 class=\"auth__title\">([^<]*)</h2>");
  private static final Pattern FORM =
      Pattern.compile("data-test=\"login-form\">(.*?)</form>", Pattern.DOTALL);
  private static final Pattern LABEL =
      Pattern.compile("<label[^>]*\\bfor=\"([^\"]*)\"[^>]*>([^<]*)</label>");
  private static final Pattern CSRF_FIELD = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");
  private static final Pattern RETURN_URL_FIELD =
      Pattern.compile("name=\"returnUrl\" value=\"([^\"]*)\"");

  /** The class shares one database, so the account is registered once for all its tests. */
  private static boolean accountRegistered;

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Header offers login")
  void headerOffersALoginLinkNextToTheRegisterLink() throws Exception {
    final String page = page("/products");

    final Element login = element(page, "nav-login-link");
    assertThat(login.text()).isEqualTo("Login");
    assertThat(login.attribute("href")).isEqualTo("/login");
    assertThat(element(page, "nav-register-link").text()).isEqualTo("Register");
    assertThat(page.indexOf("data-test=\"nav-register-link\""))
        .as("the register link follows the login link")
        .isGreaterThan(page.indexOf("data-test=\"nav-login-link\""));
  }

  @Test
  @DisplayName("Login page shows the form")
  void loginPageShowsTheEmailAndPasswordForm() throws Exception {
    final String page = page("/login");

    assertThat(text(HEADING, page)).isEqualTo("Login");
    final Matcher labels = LABEL.matcher(text(FORM, page));
    final Map<String, String> fieldsByLabel = new LinkedHashMap<>();
    while (labels.find()) {
      fieldsByLabel.put(HtmlUtils.htmlUnescape(labels.group(2)).strip(), labels.group(1));
    }
    assertThat(fieldsByLabel.keySet()).containsExactly("Email", "Password");
    fieldsByLabel.forEach(
        (label, id) ->
            assertThat(page)
                .as("an input for %s", label)
                .containsPattern("<input[^>]*\\bid=\"" + id + "\""));
    assertThat(element(page, "login-submit-button").text()).isEqualTo("Login");
    final Element register = element(page, "login-register-link");
    assertThat(register.text()).isEqualTo("Register here");
    assertThat(register.attribute("href")).isEqualTo("/register");
  }

  @Test
  @DisplayName("Login returns to the given page")
  void loginReturnsToThePageTheVisitorCameFrom() throws Exception {
    registerTheAccount();
    final Map<String, Cookie> cookies = new LinkedHashMap<>();
    final String form = page("/login?returnUrl=/products", cookies);

    final String afterLogin = logIn(form, cookies);

    final MockHttpServletResponse landed = follow(afterLogin, cookies);
    assertThat(landed.getRedirectedUrl()).isEqualTo("/products");
    final String catalogue = page(landed.getRedirectedUrl(), cookies);
    assertThat(element(catalogue, "user-greeting").text()).isEqualTo("Welcome, " + EMAIL);
  }

  @Test
  @DisplayName("Register link keeps the return page")
  void registerLinkCarriesTheReturnPageOn() throws Exception {
    final String page = page("/login?returnUrl=/products");

    assertThat(element(page, "login-register-link").attribute("href"))
        .isEqualTo("/register?returnUrl=/products");
  }

  @Test
  @DisplayName("Logout confirms on the login page")
  void logoutEndsTheSessionAndConfirmsOnTheLoginPage() throws Exception {
    registerTheAccount();
    final Map<String, Cookie> cookies = new LinkedHashMap<>();
    final String form = page("/login", cookies);
    keep(cookies, follow(logIn(form, cookies), cookies));
    final String loggedIn = page("/products", cookies);
    assertThat(element(loggedIn, "logout-button").text()).isEqualTo("Logout");

    final MockHttpServletResponse loggedOut =
        mockMvc
            .perform(
                post("/logout")
                    .cookie(cookies.values().toArray(Cookie[]::new))
                    .param("_csrf", text(CSRF_FIELD, loggedIn)))
            .andExpect(status().is3xxRedirection())
            .andReturn()
            .getResponse();
    keep(cookies, loggedOut);

    assertThat(loggedOut.getRedirectedUrl()).isEqualTo("/login?logout=true");
    final String loginPage = page(loggedOut.getRedirectedUrl(), cookies);
    assertThat(element(loginPage, "logout-success-message").text())
        .isEqualTo("You have been successfully logged out.");
    assertThat(element(loginPage, "nav-login-link").text()).isEqualTo("Login");
    assertThat(element(loginPage, "nav-register-link").text()).isEqualTo("Register");
    assertThat(loginPage).doesNotContain("data-test=\"user-greeting\"");
  }

  /** Registers the scenario's account from a browser of its own, once for this database. */
  private void registerTheAccount() throws Exception {
    if (accountRegistered) {
      return;
    }
    final Map<String, Cookie> cookies = new LinkedHashMap<>();
    final String form = page("/register", cookies);
    mockMvc
        .perform(
            post("/register")
                .cookie(cookies.values().toArray(Cookie[]::new))
                .param("_csrf", text(CSRF_FIELD, form))
                .param("firstName", "Anna")
                .param("lastName", "Schmidt")
                .param("dateOfBirth", "1990-05-17")
                .param("email", EMAIL)
                .param("password", PASSWORD)
                .param("confirmPassword", PASSWORD))
        .andExpect(status().is3xxRedirection());
    accountRegistered = true;
  }

  /**
   * Submits the login form the page shows with the account's credentials; returns where it leads.
   */
  private String logIn(final String form, final Map<String, Cookie> cookies) throws Exception {
    final var request =
        post("/login")
            .cookie(cookies.values().toArray(Cookie[]::new))
            .param("_csrf", text(CSRF_FIELD, form))
            .param("email", EMAIL)
            .param("password", PASSWORD);
    final Matcher returnUrl = RETURN_URL_FIELD.matcher(form);
    if (returnUrl.find()) {
      request.param("returnUrl", HtmlUtils.htmlUnescape(returnUrl.group(1)));
    }
    final MockHttpServletResponse response =
        mockMvc.perform(request).andExpect(status().is3xxRedirection()).andReturn().getResponse();
    keep(cookies, response);
    return response.getRedirectedUrl();
  }

  /** Follows a redirect as the browser would, with the cookies collected so far. */
  private MockHttpServletResponse follow(final String url, final Map<String, Cookie> cookies)
      throws Exception {
    final MockHttpServletResponse response =
        mockMvc
            .perform(get(URI.create(url)).cookie(cookies.values().toArray(Cookie[]::new)))
            .andExpect(status().is3xxRedirection())
            .andReturn()
            .getResponse();
    keep(cookies, response);
    return response;
  }

  /** A page opened by a visitor without cookies. */
  private String page(final String path) throws Exception {
    return page(path, new LinkedHashMap<>());
  }

  /** A page as the browser gets it, with the cookies collected so far; keeps the ones it sets. */
  private String page(final String path, final Map<String, Cookie> cookies) throws Exception {
    final var request = get(URI.create(path));
    if (!cookies.isEmpty()) {
      request.cookie(cookies.values().toArray(Cookie[]::new));
    }
    final MockHttpServletResponse response =
        mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse();
    keep(cookies, response);
    return response.getContentAsString();
  }

  private static void keep(final Map<String, Cookie> cookies, final MockHttpServletResponse r) {
    for (final Cookie cookie : r.getCookies()) {
      if (cookie.getMaxAge() == 0) {
        cookies.remove(cookie.getName());
      } else {
        cookies.put(cookie.getName(), cookie);
      }
    }
  }

  private static Element element(final String page, final String dataTest) {
    final Matcher matcher =
        Pattern.compile("<\\w+([^>]*\\bdata-test=\"" + Pattern.quote(dataTest) + "\"[^>]*)>([^<]*)")
            .matcher(page);
    assertThat(matcher.find()).as("element %s in the rendered page", dataTest).isTrue();
    return new Element(matcher.group(1), HtmlUtils.htmlUnescape(matcher.group(2)).strip());
  }

  private static String text(final Pattern pattern, final String html) {
    final Matcher matcher = pattern.matcher(html);
    assertThat(matcher.find()).as("%s in the rendered page", pattern).isTrue();
    return HtmlUtils.htmlUnescape(matcher.group(1)).strip();
  }

  /** One rendered element: its attributes as written and its text. */
  private record Element(String attributes, String text) {

    String attribute(final String name) {
      final Matcher matcher =
          Pattern.compile("\\b" + Pattern.quote(name) + "=\"([^\"]*)\"").matcher(attributes);
      assertThat(matcher.find()).as("attribute %s on %s", name, attributes).isTrue();
      return HtmlUtils.htmlUnescape(matcher.group(1));
    }
  }
}
