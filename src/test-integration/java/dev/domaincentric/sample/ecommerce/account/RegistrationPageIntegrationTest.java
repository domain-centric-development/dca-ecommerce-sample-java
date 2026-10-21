package dev.domaincentric.sample.ecommerce.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import java.util.LinkedHashMap;
import java.util.List;
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
 * The registration page a visitor without cookies opens, rendered by the wired application: the way
 * to it from the header, the form it shows, and where a registration leads.
 */
// MockMvc requests commit, and the shared H2 database outlives the Spring context — so this class
// gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:registration_page_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Registration page")
class RegistrationPageIntegrationTest {

  private static final Pattern HEADING = Pattern.compile("<h2 class=\"auth__title\">([^<]*)</h2>");
  private static final Pattern FORM =
      Pattern.compile("data-test=\"register-form\">(.*?)</form>", Pattern.DOTALL);
  private static final Pattern LABEL =
      Pattern.compile("<label[^>]*\\bfor=\"([^\"]*)\"[^>]*>([^<]*)</label>");
  private static final Pattern HINT = Pattern.compile("<p class=\"form__hint\">([^<]*)</p>");
  private static final Pattern CSRF_FIELD = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");
  private static final Pattern RETURN_URL_FIELD =
      Pattern.compile("name=\"returnUrl\" value=\"([^\"]*)\"");
  private static final Pattern SUCCESS_MESSAGE =
      Pattern.compile("data-test=\"success-message\"[^>]*>([^<]*)<");

  /** The register page's one sentence for a value its owner, address or date refuses. */
  private static final String CHECK_YOUR_ENTRIES =
      "Please check the email address, your name and your date of birth.";

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Header offers register")
  void headerOffersARegisterLinkToTheRegistrationPage() throws Exception {
    final Element link = element(page("/products"), "nav-register-link");

    assertThat(link.text()).isEqualTo("Register");
    assertThat(link.attribute("href")).isEqualTo("/register");
  }

  @Test
  @DisplayName("Register page shows the form")
  void registerPageShowsTheOwnerAddressAndPasswordForm() throws Exception {
    final String page = page("/register");

    assertThat(text(HEADING, page)).isEqualTo("Create Account");
    final Matcher labels = LABEL.matcher(text(FORM, page));
    final Map<String, String> fieldsByLabel = new LinkedHashMap<>();
    while (labels.find()) {
      fieldsByLabel.put(HtmlUtils.htmlUnescape(labels.group(2)).strip(), labels.group(1));
    }
    assertThat(fieldsByLabel.keySet())
        .containsExactly(
            "First Name", "Last Name", "Date of Birth", "Email", "Password", "Confirm Password");
    fieldsByLabel.forEach(
        (label, id) ->
            assertThat(page)
                .as("an input for %s", label)
                .containsPattern("<input[^>]*\\bid=\"" + id + "\""));
    assertThat(element(page, "register-submit-button").text()).isEqualTo("Create Account");
    assertThat(all(HINT, page))
        .contains("Your name cannot be changed later", "Password must be at least 8 characters");
    assertThat(element(page, "register-first-name-input").attribute("maxlength")).isEqualTo("100");
    assertThat(element(page, "register-last-name-input").attribute("maxlength")).isEqualTo("100");
    assertThat(element(page, "register-password-input").attribute("maxlength")).isEqualTo("72");
    assertThat(element(page, "register-confirm-password-input").attribute("maxlength"))
        .isEqualTo("72");
    final Element login = element(page, "register-login-link");
    assertThat(login.text()).isEqualTo("Login here");
    assertThat(login.attribute("href")).isEqualTo("/login");
  }

  @Test
  @DisplayName("Registration returns to the given page")
  void registrationReturnsToThePageTheVisitorCameFrom() throws Exception {
    final Map<String, Cookie> cookies = new LinkedHashMap<>();
    final String form = page("/register?returnUrl=/products", cookies);

    final MockHttpServletResponse submitted =
        mockMvc
            .perform(
                post("/register")
                    .cookie(cookies.values().toArray(Cookie[]::new))
                    .param("_csrf", text(CSRF_FIELD, form))
                    .param("returnUrl", text(RETURN_URL_FIELD, form))
                    .param("firstName", "Anna")
                    .param("lastName", "Schmidt")
                    .param("dateOfBirth", "1990-05-17")
                    .param("email", "anna.schmidt@example.com")
                    .param("password", "Secret123")
                    .param("confirmPassword", "Secret123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/products"))
            .andReturn()
            .getResponse();
    keep(cookies, submitted);

    final String catalogue = page("/products", cookies);
    assertThat(element(catalogue, "user-greeting").text())
        .isEqualTo("Welcome, anna.schmidt@example.com");
  }

  @Test
  @DisplayName("Login link keeps the return page")
  void loginLinkCarriesTheReturnPageOn() throws Exception {
    final String page = page("/register?returnUrl=/products");

    assertThat(element(page, "register-login-link").attribute("href"))
        .isEqualTo("/login?returnUrl=/products");
  }

  @Test
  @DisplayName("A scheme-relative return page leads home after registering")
  void aRegistrationLeadsToTheHomePageInsteadOfASchemeRelativeReturnPage() throws Exception {
    final Map<String, Cookie> cookies = new LinkedHashMap<>();
    final String form = page("/register?returnUrl=//elsewhere.example/", cookies);

    final MockHttpServletResponse submitted =
        mockMvc
            .perform(
                post("/register")
                    .cookie(cookies.values().toArray(Cookie[]::new))
                    .param("_csrf", text(CSRF_FIELD, form))
                    .param("returnUrl", text(RETURN_URL_FIELD, form))
                    .param("firstName", "Anna")
                    .param("lastName", "Schmidt")
                    .param("dateOfBirth", "1990-05-17")
                    // An address of its own: the class shares one database, and a taken one is
                    // refused.
                    .param("email", "anna.schmidt.elsewhere@example.com")
                    .param("password", "Secret123")
                    .param("confirmPassword", "Secret123"))
            .andExpect(status().is3xxRedirection())
            .andReturn()
            .getResponse();
    keep(cookies, submitted);

    assertThat(submitted.getRedirectedUrl()).isEqualTo("/");
    final String home = page("/", cookies);
    assertThat(all(SUCCESS_MESSAGE, home))
        .containsExactly("Account created successfully! Welcome!");
  }

  @Test
  @DisplayName("The login link drops an outside return page")
  void loginLinkLeadsToTheLoginWithoutAReturnPageOfAnotherSite() throws Exception {
    final String page = page("/register?returnUrl=https://elsewhere.example/");

    assertThat(element(page, "register-login-link").attribute("href")).isEqualTo("/login");
  }

  @Test
  @DisplayName("Short password is refused")
  void shortPasswordIsRefusedWithTheLengthRule() throws Exception {
    final String page =
        refusedRegistration(Map.of("password", "Sec123", "confirmPassword", "Sec123"));

    assertThat(element(page, "register-error-message").text())
        .isEqualTo("Password must be at least 8 characters");
  }

  @Test
  @DisplayName("Unreadable date is refused")
  void unreadableDateIsRefusedWithTheExpectedFormat() throws Exception {
    final String page = refusedRegistration(Map.of("dateOfBirth", "17.05.1990"));

    assertThat(element(page, "register-error-message").text())
        .isEqualTo("Please enter a date like 1990-05-17");
  }

  @Test
  @DisplayName("Future date is refused")
  void futureDateOfBirthIsRefusedWithTheCheckYourEntriesSentence() throws Exception {
    final String page = refusedRegistration(Map.of("dateOfBirth", "2999-01-01"));

    assertThat(element(page, "register-error-message").text()).isEqualTo(CHECK_YOUR_ENTRIES);
  }

  @Test
  @DisplayName("Malformed email is refused")
  void malformedEmailIsRefusedWithTheCheckYourEntriesSentence() throws Exception {
    final String page = refusedRegistration(Map.of("email", "anna.schmidt@example"));

    assertThat(element(page, "register-error-message").text()).isEqualTo(CHECK_YOUR_ENTRIES);
  }

  @Test
  @DisplayName("Blank first name is refused")
  void blankFirstNameIsRefusedWithTheCheckYourEntriesSentence() throws Exception {
    final String page = refusedRegistration(Map.of("firstName", "   "));

    assertThat(element(page, "register-error-message").text()).isEqualTo(CHECK_YOUR_ENTRIES);
  }

  @Test
  @DisplayName("Overlong last name is refused")
  void overlongLastNameIsRefusedWithTheCheckYourEntriesSentence() throws Exception {
    final String page = refusedRegistration(Map.of("lastName", "S".repeat(101)));

    assertThat(element(page, "register-error-message").text()).isEqualTo(CHECK_YOUR_ENTRIES);
  }

  /**
   * Submits an otherwise valid registration from a visitor without cookies, with the given fields
   * replaced, and returns the page the shop answers with.
   */
  private String refusedRegistration(final Map<String, String> replaced) throws Exception {
    final Map<String, Cookie> cookies = new LinkedHashMap<>();
    final String form = page("/register", cookies);
    final Map<String, String> fields = new LinkedHashMap<>();
    fields.put("firstName", "Anna");
    fields.put("lastName", "Schmidt");
    fields.put("dateOfBirth", "1990-05-17");
    // An address no test registers, so a refusal is never the one for a taken address.
    fields.put("email", "refused.visitor@example.com");
    fields.put("password", "Secret123");
    fields.put("confirmPassword", "Secret123");
    fields.putAll(replaced);

    final var request =
        post("/register")
            .cookie(cookies.values().toArray(Cookie[]::new))
            .param("_csrf", text(CSRF_FIELD, form));
    fields.forEach(request::param);
    return mockMvc
        .perform(request)
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  /** A page opened by a visitor without cookies. */
  private String page(final String path) throws Exception {
    return page(path, new LinkedHashMap<>());
  }

  /** A page as the browser gets it, with the cookies collected so far; keeps the ones it sets. */
  private String page(final String path, final Map<String, Cookie> cookies) throws Exception {
    final var request = get(path);
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

  private static List<String> all(final Pattern pattern, final String html) {
    return pattern
        .matcher(html)
        .results()
        .map(m -> HtmlUtils.htmlUnescape(m.group(1)).strip())
        .toList();
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
