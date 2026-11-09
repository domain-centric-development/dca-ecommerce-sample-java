package dev.domaincentric.sample.ecommerce.account;

import static dev.domaincentric.sample.ecommerce.account.Visitor.element;
import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.account.Visitor.Landing;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The longest password the policy accepts, set on the change-password page of the wired
 * application: 72 bytes in UTF-8, however few characters they are.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@AutoConfigureMockMvc
@DisplayName("Longest password")
class LongestPasswordIntegrationTest {

  private static final String EMAIL = "anna.schmidt@example.com";
  private static final String PASSWORD = "Secret123";

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Password of 72 bytes is accepted")
  void aNewPasswordOfExactlySeventyTwoBytesIsConfirmed() throws Exception {
    new Visitor(mockMvc).register(EMAIL, PASSWORD);
    final Visitor customer = new Visitor(mockMvc);
    customer.logIn("/login", EMAIL, PASSWORD);
    final String newPassword = "Aa1" + "ä".repeat(34) + "b";

    final Landing landing =
        customer.submit(
            "/account/change-password",
            Map.of(
                "currentPassword", PASSWORD,
                "newPassword", newPassword,
                "confirmPassword", newPassword));

    assertThat(element(landing.page(), "change-password-success-message").text())
        .isEqualTo("Your password has been changed.");
  }
}
