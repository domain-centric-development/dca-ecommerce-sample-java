package dev.domaincentric.sample.ecommerce.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountCommand;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountInputPort;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountResult;
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
 * A form post that did not come from the shop's own page: the browser holds the shop's cookies, the
 * post lacks the form token the page carries, and the wired application refuses it (ADR-035).
 */
// MockMvc requests commit, and the shared H2 database outlives the Spring context — so this class
// gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:form_token_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Form token")
class FormTokenIntegrationTest {

  private static final String PASSWORD = "Secret123";
  private static final Pattern CSRF_FIELD = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");
  private static final Pattern GREETING =
      Pattern.compile("<\\w+[^>]*\\bdata-test=\"user-greeting\"[^>]*>([^<]*)");

  @Autowired private MockMvc mockMvc;
  @Autowired private AuthenticateAccountInputPort authenticateAccount;

  @Test
  @DisplayName("A registration post without the form token is refused")
  void aRegistrationPostWithoutTheFormTokenCreatesNoAccount() throws Exception {
    final Map<String, Cookie> cookies = new LinkedHashMap<>();
    page("/register", cookies);

    mockMvc
        .perform(
            post("/register")
                .cookie(cookies.values().toArray(Cookie[]::new))
                .param("firstName", "Grace")
                .param("lastName", "Hopper")
                .param("dateOfBirth", "1990-05-17")
                .param("email", "grace@example.com")
                .param("password", PASSWORD)
                .param("confirmPassword", PASSWORD))
        .andExpect(status().isForbidden());

    final AuthenticateAccountResult login =
        authenticateAccount.execute(new AuthenticateAccountCommand("grace@example.com", PASSWORD));
    assertThat(login.success()).as("grace@example.com can log in").isFalse();
    assertThat(login.errorMessage()).isEqualTo("Invalid email or password");
  }

  @Test
  @DisplayName("A logout post without the form token is refused")
  void aLogoutPostWithoutTheFormTokenKeepsTheCustomerLoggedIn() throws Exception {
    final Map<String, Cookie> cookies = new LinkedHashMap<>();
    register("ada@example.com", "Ada", "Lovelace");
    logIn("ada@example.com", cookies);
    assertThat(text(GREETING, page("/products", cookies))).isEqualTo("Welcome, ada@example.com");

    final MockHttpServletResponse refused =
        mockMvc
            .perform(post("/logout").cookie(cookies.values().toArray(Cookie[]::new)))
            .andReturn()
            .getResponse();
    keep(cookies, refused);

    assertThat(refused.getStatus()).isEqualTo(403);
    assertThat(text(GREETING, page("/products", cookies))).isEqualTo("Welcome, ada@example.com");
  }

  /** Registers an account from a browser of its own, through the registration form. */
  private void register(final String email, final String firstName, final String lastName)
      throws Exception {
    final Map<String, Cookie> cookies = new LinkedHashMap<>();
    final String form = page("/register", cookies);
    mockMvc
        .perform(
            post("/register")
                .cookie(cookies.values().toArray(Cookie[]::new))
                .param("_csrf", text(CSRF_FIELD, form))
                .param("firstName", firstName)
                .param("lastName", lastName)
                .param("dateOfBirth", "1990-05-17")
                .param("email", email)
                .param("password", PASSWORD)
                .param("confirmPassword", PASSWORD))
        .andExpect(status().is3xxRedirection());
  }

  /** Logs in through the login form, with its token, and follows the merge page's redirect. */
  private void logIn(final String email, final Map<String, Cookie> cookies) throws Exception {
    final String form = page("/login", cookies);
    final MockHttpServletResponse loggedIn =
        mockMvc
            .perform(
                post("/login")
                    .cookie(cookies.values().toArray(Cookie[]::new))
                    .param("_csrf", text(CSRF_FIELD, form))
                    .param("email", email)
                    .param("password", PASSWORD))
            .andExpect(status().is3xxRedirection())
            .andReturn()
            .getResponse();
    keep(cookies, loggedIn);
    final MockHttpServletResponse merged =
        mockMvc
            .perform(
                get(URI.create(loggedIn.getRedirectedUrl()))
                    .cookie(cookies.values().toArray(Cookie[]::new)))
            .andExpect(status().is3xxRedirection())
            .andReturn()
            .getResponse();
    keep(cookies, merged);
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

  private static String text(final Pattern pattern, final String html) {
    final Matcher matcher = pattern.matcher(html);
    assertThat(matcher.find()).as("%s in the rendered page", pattern).isTrue();
    return HtmlUtils.htmlUnescape(matcher.group(1)).strip();
  }
}
