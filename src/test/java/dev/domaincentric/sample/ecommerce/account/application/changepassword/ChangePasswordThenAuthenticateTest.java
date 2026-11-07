package dev.domaincentric.sample.ecommerce.account.application.changepassword;

import static dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.CURRENT_PASSWORD;
import static dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.EMAIL;
import static dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.NEW_PASSWORD;
import static dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.USER_ID;
import static dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.accountWith;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountCommand;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountResult;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountUseCase;
import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.TestAccountRepository;
import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.TestDomainEventPublisher;
import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.TestPasswordHasher;
import dev.domaincentric.sample.ecommerce.account.domain.model.Account;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountStatus;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Use case collaboration test: after a successful password change, authentication succeeds with the
 * new password and fails with the old one.
 *
 * <p>Runs {@link ChangePasswordUseCase} and {@link AuthenticateAccountUseCase} against the same
 * in-memory repository double and the same round-tripping password hasher double.
 */
@DisplayName("Change password, then authenticate")
class ChangePasswordThenAuthenticateTest {

  private TestPasswordHasher passwordHasher;
  private TestAccountRepository accountRepository;
  private ChangePasswordUseCase changePassword;
  private AuthenticateAccountUseCase authenticate;

  @BeforeEach
  void setUp() {
    final List<String> interactions = new ArrayList<>();
    passwordHasher = new TestPasswordHasher();
    accountRepository = new TestAccountRepository(interactions);
    changePassword =
        new ChangePasswordUseCase(
            accountRepository, passwordHasher, new TestDomainEventPublisher(interactions));
    authenticate =
        new AuthenticateAccountUseCase(
            accountRepository, passwordHasher, new TestDomainEventPublisher(interactions));

    final Account account = accountWith(AccountStatus.ACTIVE, CURRENT_PASSWORD, passwordHasher);
    account.clearDomainEvents();
    accountRepository.store(account);
  }

  @Test
  @DisplayName("the new password authenticates, the old one no longer does")
  void newPasswordAuthenticatesAfterChange() {
    assertInstanceOf(
        ChangePasswordResult.Changed.class,
        changePassword.execute(new ChangePasswordCommand(USER_ID, CURRENT_PASSWORD, NEW_PASSWORD)),
        "precondition: the password change must succeed");

    final AuthenticateAccountResult withNewPassword =
        authenticate.execute(new AuthenticateAccountCommand(EMAIL, NEW_PASSWORD));
    assertInstanceOf(
        AuthenticateAccountResult.Authenticated.class,
        withNewPassword,
        "the new password must authenticate");

    final AuthenticateAccountResult withOldPassword =
        authenticate.execute(new AuthenticateAccountCommand(EMAIL, CURRENT_PASSWORD));
    assertInstanceOf(
        AuthenticateAccountResult.CredentialsRefused.class,
        withOldPassword,
        "the old password must no longer authenticate");
  }
}
