package dev.domaincentric.sample.ecommerce.account;

import static dev.domaincentric.sample.ecommerce.account.Visitor.element;
import static dev.domaincentric.sample.ecommerce.account.Visitor.text;
import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.account.Visitor.Landing;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.HtmlUtils;

/**
 * The change-password page of the account area, rendered by the wired application: the form it
 * shows, a change that keeps the password, and the login it asks a visitor for.
 */
// MockMvc requests commit, and the shared H2 database outlives the Spring context — so this class
// gets a database of its own. No test here changes the password to another one.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:change_password_page_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Change password page")
class ChangePasswordPageIntegrationTest {

  private static final String EMAIL = "anna.schmidt@example.com";
  private static final String PASSWORD = "Secret123";
  private static final String CHANGE_PASSWORD = "/account/change-password";

  private static final Pattern FORM =
      Pattern.compile("data-test=\"change-password-form\">(.*?)</form>", Pattern.DOTALL);
  private static final Pattern LABEL =
      Pattern.compile("<label[^>]*\\bfor=\"([^\"]*)\"[^>]*>([^<]*)</label>");
  private static final Pattern NEW_PASSWORD_HINT =
      Pattern.compile(
          "data-test=\"change-password-new-password-input\"[^>]*>\\s*<p class=\"form__hint\">([^<]*)</p>");

  /** The class shares one database, so the account is registered once for all its tests. */
  private static boolean accountRegistered;

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Change password page shows the form")
  void accountNavigationOpensTheEmptyChangePasswordForm() throws Exception {
    final Visitor customer = loggedInCustomer();
    final String target =
        element(customer.page("/account"), "account-nav-change-password").attribute("href");

    final String page = customer.page(target);

    assertThat(target).isEqualTo(CHANGE_PASSWORD);
    assertThat(element(page, "change-password-title").text()).isEqualTo("Change Password");
    final Visitor.Element current = element(page, "account-nav-change-password");
    assertThat(current.text()).isEqualTo("Change Password");
    assertThat(current.attribute("aria-current")).isEqualTo("page");

    final Matcher labels = LABEL.matcher(text(FORM, page));
    final Map<String, String> fieldsByLabel = new LinkedHashMap<>();
    while (labels.find()) {
      fieldsByLabel.put(HtmlUtils.htmlUnescape(labels.group(2)).strip(), labels.group(1));
    }
    assertThat(fieldsByLabel)
        .containsExactly(
            Map.entry("Current Password", "currentPassword"),
            Map.entry("New Password", "newPassword"),
            Map.entry("Confirm New Password", "confirmPassword"));
    for (final String field : new String[] {"current", "new", "confirm"}) {
      final Visitor.Element input = element(page, "change-password-" + field + "-password-input");
      assertThat(input.attribute("type")).isEqualTo("password");
      assertThat(input.has("value")).as("the %s password field is empty", field).isFalse();
    }
    assertThat(element(page, "change-password-submit-button").text()).isEqualTo("Change Password");

    assertThat(text(NEW_PASSWORD_HINT, page))
        .isEqualTo(
            "Password must be at least 8 characters and contain an uppercase letter, a lowercase"
                + " letter and a digit");

    assertThat(element(page, "change-password-new-password-input").attribute("maxlength"))
        .isEqualTo("72");
    assertThat(element(page, "change-password-confirm-password-input").attribute("maxlength"))
        .isEqualTo("72");
  }

  @Test
  @DisplayName("Keeping the same password is allowed")
  void submittingTheCurrentPasswordAsTheNewOneIsConfirmed() throws Exception {
    final Visitor customer = loggedInCustomer();

    final Landing landing =
        customer.submit(
            CHANGE_PASSWORD,
            Map.of(
                "currentPassword", PASSWORD,
                "newPassword", PASSWORD,
                "confirmPassword", PASSWORD));

    assertThat(element(landing.page(), "change-password-success-message").text())
        .isEqualTo("Your password has been changed.");
  }

  @Test
  @DisplayName("Change password sends a visitor to the login")
  void changePasswordSendsAVisitorWithoutCookiesToTheLogin() throws Exception {
    final Visitor visitor = new Visitor(mockMvc);

    assertThat(visitor.redirectOf(CHANGE_PASSWORD))
        .isEqualTo("/login?returnUrl=%2Faccount%2Fchange-password");
  }

  /** A browser of its own in which the scenario's customer has logged in. */
  private Visitor loggedInCustomer() throws Exception {
    registerTheAccount();
    final Visitor customer = new Visitor(mockMvc);
    customer.logIn("/login", EMAIL, PASSWORD);
    return customer;
  }

  /** Registers the scenario's account from a browser of its own, once for this database. */
  private void registerTheAccount() throws Exception {
    if (accountRegistered) {
      return;
    }
    new Visitor(mockMvc).register(EMAIL, PASSWORD);
    accountRegistered = true;
  }
}
