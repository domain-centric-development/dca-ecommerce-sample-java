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
 * The password policy as the change-password page words it, in the wired application: a new
 * password that breaks a rule is refused with that rule's sentence.
 */
// MockMvc requests commit, and the shared H2 database outlives the Spring context — so this class
// gets a database of its own. No test here changes the password.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:password_policy_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Password policy on the change-password page")
class PasswordPolicyIntegrationTest {

  private static final String EMAIL = "anna.schmidt@example.com";
  private static final String PASSWORD = "Secret123";
  private static final String CHANGE_PASSWORD = "/account/change-password";

  /** The class shares one database, so the account is registered once for all its tests. */
  private static boolean accountRegistered;

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Short new password is refused")
  void aNewPasswordUnderEightCharactersIsRefusedWithTheLengthRule() throws Exception {
    final String page = refusedChange("Abc1234");

    assertThat(element(page, "change-password-error-message").text())
        .isEqualTo("Password must be at least 8 characters long.");
  }

  @Test
  @DisplayName("Length is checked before the characters")
  void aShortPasswordMissingCharacterClassesIsRefusedForItsLength() throws Exception {
    final String page = refusedChange("abcdefg");

    assertThat(element(page, "change-password-error-message").text())
        .isEqualTo("Password must be at least 8 characters long.");
  }

  @Test
  @DisplayName("Password over 72 bytes is refused")
  void aNewPasswordOfSeventyThreeBytesIsRefusedWithTheByteRule() throws Exception {
    final String page = refusedChange("Aa1" + "ä".repeat(35));

    assertThat(element(page, "change-password-error-message").text())
        .isEqualTo("Password must not be longer than 72 bytes.");
  }

  @Test
  @DisplayName("New password without lowercase is refused")
  void aNewPasswordWithoutLowercaseLetterIsRefused() throws Exception {
    final String page = refusedChange("SECRET123");

    assertThat(element(page, "change-password-error-message").text())
        .isEqualTo("Password must contain a lowercase letter.");
  }

  @Test
  @DisplayName("New password without digit is refused")
  void aNewPasswordWithoutDigitIsRefused() throws Exception {
    final String page = refusedChange("SecretPass");

    assertThat(element(page, "change-password-error-message").text())
        .isEqualTo("Password must contain a digit.");
  }

  /** The logged-in customer submits the new password, confirmed; returns the refusal page. */
  private String refusedChange(final String newPassword) throws Exception {
    final Visitor customer = loggedInCustomer();
    return customer.refused(
        CHANGE_PASSWORD,
        Map.of(
            "currentPassword", PASSWORD,
            "newPassword", newPassword,
            "confirmPassword", newPassword));
  }

  /** A browser of its own in which the scenario's customer has logged in. */
  private Visitor loggedInCustomer() throws Exception {
    if (!accountRegistered) {
      new Visitor(mockMvc).register(EMAIL, PASSWORD);
      accountRegistered = true;
    }
    final Visitor customer = new Visitor(mockMvc);
    customer.logIn("/login", EMAIL, PASSWORD);
    return customer;
  }
}
