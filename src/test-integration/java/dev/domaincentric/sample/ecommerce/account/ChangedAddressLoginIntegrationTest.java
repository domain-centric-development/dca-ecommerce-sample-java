package dev.domaincentric.sample.ecommerce.account;

import static dev.domaincentric.sample.ecommerce.account.Visitor.element;
import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The login of the wired application after a customer changed their address on "My Profile": the
 * new address logs in, the old one no longer does.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@AutoConfigureMockMvc
@DisplayName("Login after an address change")
class ChangedAddressLoginIntegrationTest {

  private static final String OLD_EMAIL = "anna.schmidt@example.com";
  private static final String NEW_EMAIL = "anna.meyer@example.com";
  private static final String PASSWORD = "Secret123";

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("New address logs in")
  void theNewAddressLogsInAndOpensTheCart() throws Exception {
    changeTheAddress();
    final Visitor visitor = new Visitor(mockMvc);

    final String afterLogin = visitor.logIn("/login", NEW_EMAIL, PASSWORD);

    assertThat(afterLogin).isEqualTo("/cart");
    assertThat(element(visitor.page(afterLogin), "user-greeting").text())
        .isEqualTo("Welcome, " + NEW_EMAIL);
  }

  @Test
  @DisplayName("Old address no longer logs in")
  void theOldAddressIsRefusedWithTheGenericMessage() throws Exception {
    changeTheAddress();
    final Visitor visitor = new Visitor(mockMvc);

    final String loginPage = visitor.refusedLogIn(OLD_EMAIL, PASSWORD);

    assertThat(element(loginPage, "login-error-message").text())
        .isEqualTo("Invalid email or password");
  }

  /** Registers the account and changes its address on the profile page. */
  private void changeTheAddress() throws Exception {
    final Visitor customer = new Visitor(mockMvc);
    // Registering signs the visitor in.
    customer.register(OLD_EMAIL, PASSWORD);
    customer.saveProfile(NEW_EMAIL, "1990-05-17");
  }
}
