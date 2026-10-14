package dev.domaincentric.sample.ecommerce.account;

import static dev.domaincentric.sample.ecommerce.account.Visitor.element;
import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Password changes the change-password page refuses, in the wired application: which refusal wins,
 * and that the old password still logs in afterwards.
 */
// MockMvc requests commit, and the shared H2 database outlives the Spring context — so this class
// gets a database of its own. No test here changes the password.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:refused_password_change_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Refused password change")
class RefusedPasswordChangeIntegrationTest {

  private static final String EMAIL = "anna.schmidt@example.com";
  private static final String PASSWORD = "Secret123";
  private static final String WRONG_PASSWORD = "Wrong999";
  private static final String CHANGE_PASSWORD = "/account/change-password";

  /** The class shares one database, so the account is registered once for all its tests. */
  private static boolean accountRegistered;

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Differing confirmation wins over a wrong current password")
  void aDifferingConfirmationIsNamedEvenWhenTheCurrentPasswordIsWrong() throws Exception {
    final String page = refusedChange(loggedInCustomer(), WRONG_PASSWORD, "Better456", "Better457");

    assertThat(element(page, "change-password-error-message").text())
        .isEqualTo("New password and confirmation do not match");
  }

  @Test
  @DisplayName("Wrong current password is refused")
  void aWrongCurrentPasswordIsRefused() throws Exception {
    final String page = refusedChange(loggedInCustomer(), WRONG_PASSWORD, "Better456", "Better456");

    assertThat(element(page, "change-password-error-message").text())
        .isEqualTo("Current password is not correct");
  }

  @Test
  @DisplayName("Wrong current password wins over a weak new one")
  void aWrongCurrentPasswordIsNamedEvenWhenTheNewOneIsWeak() throws Exception {
    final String page = refusedChange(loggedInCustomer(), WRONG_PASSWORD, "weakpass", "weakpass");

    assertThat(element(page, "change-password-error-message").text())
        .isEqualTo("Current password is not correct");
  }

  @Test
  @DisplayName("Refused change keeps the old password")
  void theOldPasswordStillLogsInAfterARefusedChange() throws Exception {
    refusedChange(loggedInCustomer(), WRONG_PASSWORD, "Better456", "Better456");
    final Visitor visitor = new Visitor(mockMvc);

    final String afterLogin = visitor.logIn("/login", EMAIL, PASSWORD);

    assertThat(afterLogin).isEqualTo("/cart?notice=welcome-back");
    assertThat(element(visitor.page(afterLogin), "user-greeting").text())
        .isEqualTo("Welcome, " + EMAIL);
  }

  /** Submits the change-password form with the three values; returns the page shown again. */
  private static String refusedChange(
      final Visitor customer,
      final String currentPassword,
      final String newPassword,
      final String confirmPassword)
      throws Exception {
    return customer.refusedSubmit(
        CHANGE_PASSWORD,
        Map.of(
            "currentPassword", currentPassword,
            "newPassword", newPassword,
            "confirmPassword", confirmPassword));
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
