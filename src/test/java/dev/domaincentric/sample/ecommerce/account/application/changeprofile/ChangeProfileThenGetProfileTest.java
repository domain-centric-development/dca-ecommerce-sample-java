package dev.domaincentric.sample.ecommerce.account.application.changeprofile;

import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.DATE_OF_BIRTH;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.EMAIL;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.FIRST_NAME;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.LAST_NAME;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.NEW_DATE_OF_BIRTH;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.NEW_EMAIL;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.USER_ID;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.accountWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.TestAccountRepository;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.TestDomainEventPublisher;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.TestPasswordHasher;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileQuery;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileResult;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileResult.Profile;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileUseCase;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Use case collaboration test: what was stored through the profile change is what the profile page
 * shows afterwards.
 *
 * <p>Runs {@link ChangeProfileUseCase} and {@link GetProfileUseCase} against the same in-memory
 * repository double.
 */
@DisplayName("Change profile, then read it back")
class ChangeProfileThenGetProfileTest {

  private final TestPasswordHasher passwordHasher = new TestPasswordHasher();
  private final TestAccountRepository accountRepository = new TestAccountRepository();
  private final ChangeProfileUseCase changeProfile =
      new ChangeProfileUseCase(accountRepository, new TestDomainEventPublisher());
  private final GetProfileUseCase getProfile = new GetProfileUseCase(accountRepository);

  private Profile changeThenRead(final String email, final LocalDate dateOfBirth) {
    accountRepository.store(accountWith(AccountStatus.ACTIVE, passwordHasher));

    assertInstanceOf(
        ChangeProfileResult.Changed.class,
        changeProfile.execute(new ChangeProfileCommand(USER_ID, email, dateOfBirth)),
        "precondition: the profile change must succeed");

    return assertInstanceOf(
            GetProfileResult.Found.class, getProfile.execute(new GetProfileQuery(USER_ID)))
        .profile();
  }

  @Test
  @DisplayName("the new email is the one shown afterwards")
  void newEmailIsShownAfterwards() {
    assertEquals(NEW_EMAIL, changeThenRead(NEW_EMAIL, DATE_OF_BIRTH).email());
  }

  @Test
  @DisplayName("the corrected date of birth is the one shown afterwards")
  void correctedDateOfBirthIsShownAfterwards() {
    assertEquals(NEW_DATE_OF_BIRTH, changeThenRead(EMAIL, NEW_DATE_OF_BIRTH).dateOfBirth());
  }

  @Test
  @DisplayName("the name shown afterwards is still the registered one")
  void nameIsUnchangedAfterwards() {
    final Profile profile = changeThenRead(NEW_EMAIL, NEW_DATE_OF_BIRTH);

    assertEquals(FIRST_NAME, profile.firstName());
    assertEquals(LAST_NAME, profile.lastName());
  }
}
