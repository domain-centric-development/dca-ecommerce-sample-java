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
 * The login after a customer changed their password on the change-password page, in the wired
 * application: from then on only the new password logs in.
 */
// MockMvc requests commit, and the shared H2 database outlives the Spring context — so this class
// gets a database of its own, in which the password is changed once for all its tests.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:changed_password_login_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Login after a password change")
class ChangedPasswordLoginIntegrationTest {

  private static final String EMAIL = "anna.schmidt@example.com";
  private static final String OLD_PASSWORD = "Secret123";
  private static final String NEW_PASSWORD = "Better456";

  /** The class shares one database, so the password is changed once for all its tests. */
  private static boolean passwordChanged;

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("New password logs in")
  void theNewPasswordLogsInAndOpensTheCart() throws Exception {
    changeThePassword();
    final Visitor visitor = new Visitor(mockMvc);

    final String afterLogin = visitor.logIn("/login", EMAIL, NEW_PASSWORD);

    assertThat(afterLogin).isEqualTo("/cart?notice=welcome-back");
    assertThat(element(visitor.page(afterLogin), "user-greeting").text())
        .isEqualTo("Welcome, " + EMAIL);
  }

  @Test
  @DisplayName("Old password no longer logs in")
  void theOldPasswordIsRefusedWithTheGenericMessage() throws Exception {
    changeThePassword();
    final Visitor visitor = new Visitor(mockMvc);

    final String loginPage = visitor.refusedLogIn("/login", EMAIL, OLD_PASSWORD);

    assertThat(element(loginPage, "login-error-message").text())
        .isEqualTo("Invalid email or password");
  }

  /**
   * Registers the scenario's account and changes its password on the change-password page, from a
   * browser of its own, once for this database.
   */
  private void changeThePassword() throws Exception {
    if (passwordChanged) {
      return;
    }
    new Visitor(mockMvc).register(EMAIL, OLD_PASSWORD);
    final Visitor customer = new Visitor(mockMvc);
    customer.logIn("/login", EMAIL, OLD_PASSWORD);
    customer.submit(
        "/account/change-password",
        Map.of(
            "currentPassword", OLD_PASSWORD,
            "newPassword", NEW_PASSWORD,
            "confirmPassword", NEW_PASSWORD));
    passwordChanged = true;
  }
}
