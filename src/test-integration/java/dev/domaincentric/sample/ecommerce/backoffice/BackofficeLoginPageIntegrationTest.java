package dev.domaincentric.sample.ecommerce.backoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.HtmlUtils;

/**
 * The back office's own login page, rendered by the wired application with its real security chain:
 * the form an operator without cookies sees, and where wrong credentials leave them.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@AutoConfigureMockMvc
@DisplayName("Backoffice login page")
class BackofficeLoginPageIntegrationTest {

  private static final String LOGIN_PAGE = "/backoffice/login";

  private static final Pattern HEADING = Pattern.compile("<h2 class=\"auth__title\">([^<]*)</h2>");
  private static final Pattern FORM =
      Pattern.compile("data-test=\"backoffice-login-form\">(.*?)</form>", Pattern.DOTALL);
  private static final Pattern LABEL =
      Pattern.compile("<label[^>]*\\bfor=\"([^\"]*)\"[^>]*>([^<]*)</label>");
  private static final Pattern CSRF_FIELD = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Backoffice login page")
  void loginPageShowsTheUsernameAndPasswordForm() throws Exception {
    final String page = page(LOGIN_PAGE, new MockHttpSession());

    assertThat(text(HEADING, page)).isEqualTo("Backoffice Login");
    final Matcher labels = LABEL.matcher(text(FORM, page));
    final Map<String, String> fieldsByLabel = new LinkedHashMap<>();
    while (labels.find()) {
      fieldsByLabel.put(HtmlUtils.htmlUnescape(labels.group(2)).strip(), labels.group(1));
    }
    assertThat(fieldsByLabel.keySet()).containsExactly("Username", "Password");
    fieldsByLabel.forEach(
        (label, id) ->
            assertThat(page)
                .as("an input for %s", label)
                .containsPattern("<input[^>]*\\bid=\"" + id + "\""));
    assertThat(text(dataTest("backoffice-login-submit"), page)).isEqualTo("Login");
  }

  @Test
  @DisplayName("Wrong password is refused")
  void wrongPasswordKeepsTheOperatorOnTheLoginPageWithOneMessage() throws Exception {
    final MockHttpSession session = new MockHttpSession();

    final String landed = logIn("admin", "wrong", session);

    assertThat(landed).isEqualTo(LOGIN_PAGE + "?error=true");
    assertThat(text(dataTest("login-error-message"), page(landed, session)))
        .isEqualTo("Invalid username or password.");
  }

  @Test
  @DisplayName("Unknown username is refused")
  void unknownUsernameKeepsTheOperatorOnTheLoginPageWithOneMessage() throws Exception {
    final MockHttpSession session = new MockHttpSession();

    final String landed = logIn("operator", "admin", session);

    assertThat(URI.create(landed).getPath()).isEqualTo(LOGIN_PAGE);
    assertThat(text(dataTest("login-error-message"), page(landed, session)))
        .isEqualTo("Invalid username or password.");
  }

  /** Submits the login form the page shows with the given credentials; returns where it leads. */
  private String logIn(final String username, final String password, final MockHttpSession session)
      throws Exception {
    final String form = page(LOGIN_PAGE, session);
    return mockMvc
        .perform(
            post(LOGIN_PAGE)
                .session(session)
                .param("_csrf", text(CSRF_FIELD, form))
                .param("username", username)
                .param("password", password))
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
