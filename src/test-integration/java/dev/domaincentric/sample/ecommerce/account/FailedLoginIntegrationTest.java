package dev.domaincentric.sample.ecommerce.account;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountCommand;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountInputPort;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountResult;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A login the wired application refuses: whatever was wrong, the refusal says the same, so the
 * login gives nothing away about which addresses have an account.
 */
// The shared H2 database outlives the Spring context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:failed_login_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@DisplayName("Failed login")
class FailedLoginIntegrationTest {

  private static final String GENERIC_REFUSAL = "Invalid email or password";

  @Autowired private AuthenticateAccountInputPort authenticateAccount;

  @Test
  @DisplayName("Unknown address is refused")
  void anAddressWithoutAnAccountIsRefusedWithTheGenericMessage() {
    final AuthenticateAccountResult result =
        authenticateAccount.execute(
            new AuthenticateAccountCommand("nobody@example.com", "Secret123"));

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).isEqualTo(GENERIC_REFUSAL);
  }

  @Test
  @DisplayName("Malformed address is refused")
  void aMalformedAddressIsRefusedWithTheGenericMessage() {
    final AuthenticateAccountResult result =
        authenticateAccount.execute(
            new AuthenticateAccountCommand("anna.schmidt@example", "Secret123"));

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).isEqualTo(GENERIC_REFUSAL);
  }
}
