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
@SpringBootTest(classes = EcommerceSampleApplication.class)
@DisplayName("Failed login")
class FailedLoginIntegrationTest {

  @Autowired private AuthenticateAccountInputPort authenticateAccount;

  @Test
  @DisplayName("Unknown address is refused")
  void anAddressWithoutAnAccountIsRefusedWithTheGenericMessage() {
    final AuthenticateAccountResult result =
        authenticateAccount.execute(
            new AuthenticateAccountCommand("nobody@example.com", "Secret123"));

    assertThat(result).isInstanceOf(AuthenticateAccountResult.CredentialsRefused.class);
  }

  @Test
  @DisplayName("Malformed address is refused")
  void aMalformedAddressIsRefusedWithTheGenericMessage() {
    final AuthenticateAccountResult result =
        authenticateAccount.execute(
            new AuthenticateAccountCommand("anna.schmidt@example", "Secret123"));

    assertThat(result).isInstanceOf(AuthenticateAccountResult.CredentialsRefused.class);
  }
}
