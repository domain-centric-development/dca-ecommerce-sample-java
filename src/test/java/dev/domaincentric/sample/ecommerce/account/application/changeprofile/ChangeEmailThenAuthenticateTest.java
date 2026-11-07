package dev.domaincentric.sample.ecommerce.account.application.changeprofile;

import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.DATE_OF_BIRTH;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.EMAIL;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.NEW_EMAIL;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.PASSWORD;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.USER_ID;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.accountWith;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountCommand;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountResult;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountUseCase;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.TestAccountRepository;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.TestDomainEventPublisher;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.TestPasswordHasher;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Use case collaboration test: after the email was changed, the new address logs in with the
 * unchanged password and the old address no longer does.
 *
 * <p>The email is the login credential, so a change to it must move the credential with it.
 */
@DisplayName("Change email, then authenticate")
class ChangeEmailThenAuthenticateTest {

  private TestAccountRepository accountRepository;
  private ChangeProfileUseCase changeProfile;
  private AuthenticateAccountUseCase authenticate;

  @BeforeEach
  void setUp() {
    final TestPasswordHasher passwordHasher = new TestPasswordHasher();
    accountRepository = new TestAccountRepository();
    changeProfile = new ChangeProfileUseCase(accountRepository, new TestDomainEventPublisher());
    authenticate =
        new AuthenticateAccountUseCase(
            accountRepository, passwordHasher, new TestDomainEventPublisher());

    accountRepository.store(accountWith(AccountStatus.ACTIVE, passwordHasher));
  }

  @Test
  @DisplayName("the new email authenticates with the unchanged password, the old one does not")
  void newEmailAuthenticatesAfterChange() {
    assertInstanceOf(
        ChangeProfileResult.Changed.class,
        changeProfile.execute(new ChangeProfileCommand(USER_ID, NEW_EMAIL, DATE_OF_BIRTH)),
        "precondition: the email change must succeed");

    final AuthenticateAccountResult withNewEmail =
        authenticate.execute(new AuthenticateAccountCommand(NEW_EMAIL, PASSWORD));
    assertInstanceOf(
        AuthenticateAccountResult.Authenticated.class,
        withNewEmail,
        "the new email must authenticate");

    final AuthenticateAccountResult withOldEmail =
        authenticate.execute(new AuthenticateAccountCommand(EMAIL, PASSWORD));
    assertInstanceOf(
        AuthenticateAccountResult.CredentialsRefused.class,
        withOldEmail,
        "the superseded email must no longer authenticate");
  }
}
