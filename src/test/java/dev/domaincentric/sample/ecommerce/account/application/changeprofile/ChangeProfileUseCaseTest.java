package dev.domaincentric.sample.ecommerce.account.application.changeprofile;

import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.DATE_OF_BIRTH;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.EMAIL;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.FIRST_NAME;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.LAST_NAME;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.NEW_DATE_OF_BIRTH;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.NEW_EMAIL;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.USER_ID;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.accountWith;
import static dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.otherAccountWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileResult.Outcome;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.TestAccountRepository;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.TestDomainEventPublisher;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileTestFixtures.TestPasswordHasher;
import dev.domaincentric.sample.ecommerce.account.domain.event.AccountEmailChanged;
import dev.domaincentric.sample.ecommerce.account.domain.event.AccountOwnerDateOfBirthChanged;
import dev.domaincentric.sample.ecommerce.account.domain.model.Account;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountId;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountStatus;
import dev.domaincentric.sample.ecommerce.account.domain.model.Email;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Unit tests for {@link ChangeProfileUseCase}.
 *
 * <p>Covers the accessibility gate, storing a new email and a corrected date of birth, the email
 * uniqueness check skipping the caller's own address, the rejection outcomes, that a rejected
 * submission stores nothing and publishes nothing, and that the owner's name survives every change.
 */
@DisplayName("ChangeProfileUseCase")
class ChangeProfileUseCaseTest {

  private TestPasswordHasher passwordHasher;
  private TestAccountRepository accountRepository;
  private TestDomainEventPublisher eventPublisher;
  private ChangeProfileUseCase changeProfile;

  @BeforeEach
  void setUp() {
    passwordHasher = new TestPasswordHasher();
    accountRepository = new TestAccountRepository();
    eventPublisher = new TestDomainEventPublisher();
    changeProfile = new ChangeProfileUseCase(accountRepository, eventPublisher);
  }

  private Account givenAccount(final AccountStatus status) {
    final Account account = accountWith(status, passwordHasher);
    accountRepository.store(account);
    return account;
  }

  private Account givenActiveAccount() {
    return givenAccount(AccountStatus.ACTIVE);
  }

  private ChangeProfileResult submit(final String email, final LocalDate dateOfBirth) {
    return changeProfile.execute(new ChangeProfileCommand(USER_ID, email, dateOfBirth));
  }

  private Account storedAccount() {
    final Optional<Account> account = accountRepository.findById(AccountId.of("account-1"));
    assertTrue(account.isPresent(), "the account under test must still exist");
    return account.get();
  }

  // ---------------------------------------------------------------- accessibility

  @ParameterizedTest
  @EnumSource(
      value = AccountStatus.class,
      names = {"SUSPENDED", "CLOSED"})
  @DisplayName("an account that cannot log in is not accessible and is left untouched")
  void inaccessibleAccountIsLeftUntouched(final AccountStatus status) {
    givenAccount(status);

    final ChangeProfileResult result = submit(NEW_EMAIL, NEW_DATE_OF_BIRTH);

    assertEquals(Outcome.ACCOUNT_NOT_ACCESSIBLE, result.outcome());
    assertEquals(Email.of(EMAIL), storedAccount().email(), "nothing may be changed");
    assertEquals(0, accountRepository.saveCount());
    assertTrue(eventPublisher.publishedEvents().isEmpty());
  }

  @Test
  @DisplayName("a userId without an account is not accessible")
  void unknownUserIdIsNotAccessible() {
    assertEquals(Outcome.ACCOUNT_NOT_ACCESSIBLE, submit(NEW_EMAIL, NEW_DATE_OF_BIRTH).outcome());
  }

  // ---------------------------------------------------------------- email

  @Test
  @DisplayName("a valid, unused email is stored and reported in the result")
  void storesNewEmail() {
    givenActiveAccount();

    final ChangeProfileResult result = submit(NEW_EMAIL, DATE_OF_BIRTH);

    assertEquals(Outcome.CHANGED, result.outcome());
    assertEquals(NEW_EMAIL, result.profile().orElseThrow().email());
    assertEquals(Email.of(NEW_EMAIL), storedAccount().email());
    assertEquals(1, accountRepository.saveCount(), "the changed account is saved exactly once");
  }

  @Test
  @DisplayName("an email change publishes the email changed event exactly once")
  void publishesEmailChangedOnce() {
    givenActiveAccount();

    submit(NEW_EMAIL, DATE_OF_BIRTH);

    final List<DomainEvent> events = emailChangedEvents();
    assertEquals(1, events.size(), "exactly one email changed event is published");
    final AccountEmailChanged event =
        assertInstanceOf(AccountEmailChanged.class, events.getFirst());
    assertEquals(AccountId.of("account-1"), event.accountId());
    assertEquals(Email.of(EMAIL), event.previousEmail());
    assertEquals(Email.of(NEW_EMAIL), event.newEmail());
  }

  @Test
  @DisplayName("re-submitting the own email in another spelling is accepted and changes nothing")
  void ownEmailIsNeverReportedAsInUse() {
    givenActiveAccount();

    final ChangeProfileResult result = submit(EMAIL.toUpperCase(Locale.ROOT), DATE_OF_BIRTH);

    assertEquals(
        Outcome.CHANGED,
        result.outcome(),
        "the caller's own address must not collide with itself, whatever its spelling");
    assertTrue(emailChangedEvents().isEmpty(), "an unchanged email publishes no event");
  }

  @Test
  @DisplayName("an email another account uses is rejected and stores nothing")
  void rejectsEmailUsedByAnotherAccount() {
    givenActiveAccount();
    accountRepository.store(otherAccountWith("taken@example.com", passwordHasher));

    final ChangeProfileResult result = submit("taken@example.com", DATE_OF_BIRTH);

    assertEquals(Outcome.EMAIL_ALREADY_IN_USE, result.outcome());
    assertEquals(Email.of(EMAIL), storedAccount().email(), "the rejected change stores nothing");
    assertEquals(0, accountRepository.saveCount());
    assertTrue(eventPublisher.publishedEvents().isEmpty());
  }

  @Test
  @DisplayName("a syntactically invalid email is rejected as EMAIL_INVALID")
  void rejectsInvalidEmail() {
    givenActiveAccount();

    final ChangeProfileResult result = submit("not-an-email", DATE_OF_BIRTH);

    assertEquals(Outcome.EMAIL_INVALID, result.outcome());
    assertEquals(Email.of(EMAIL), storedAccount().email());
    assertEquals(0, accountRepository.saveCount());
  }

  // ---------------------------------------------------------------- date of birth

  @Test
  @DisplayName("a corrected date of birth is stored and published exactly once")
  void storesCorrectedDateOfBirth() {
    givenActiveAccount();

    final ChangeProfileResult result = submit(EMAIL, NEW_DATE_OF_BIRTH);

    assertEquals(Outcome.CHANGED, result.outcome());
    assertEquals(NEW_DATE_OF_BIRTH, result.profile().orElseThrow().dateOfBirth());
    assertEquals(NEW_DATE_OF_BIRTH, storedAccount().owner().dateOfBirth());
    assertEquals(1, dateOfBirthChangedEvents().size());
  }

  @Test
  @DisplayName("an unchanged date of birth publishes no event")
  void unchangedDateOfBirthPublishesNoEvent() {
    givenActiveAccount();

    assertEquals(Outcome.CHANGED, submit(EMAIL, DATE_OF_BIRTH).outcome());
    assertTrue(dateOfBirthChangedEvents().isEmpty(), "an unchanged date of birth is not a change");
  }

  @Test
  @DisplayName("a missing date of birth is rejected and stores nothing")
  void rejectsMissingDateOfBirth() {
    givenActiveAccount();

    final ChangeProfileResult result = submit(NEW_EMAIL, null);

    assertEquals(Outcome.DATE_OF_BIRTH_INVALID, result.outcome());
    assertEquals(Email.of(EMAIL), storedAccount().email());
    assertEquals(0, accountRepository.saveCount());
  }

  @Test
  @DisplayName("a future date of birth rejects the whole submission, email included")
  void futureDateOfBirthLeavesEmailUnchanged() {
    givenActiveAccount();

    final ChangeProfileResult result = submit(NEW_EMAIL, LocalDate.now().plusDays(1));

    assertEquals(Outcome.DATE_OF_BIRTH_INVALID, result.outcome());
    assertEquals(
        Email.of(EMAIL),
        storedAccount().email(),
        "all input is validated before anything is changed");
    assertEquals(DATE_OF_BIRTH, storedAccount().owner().dateOfBirth());
    assertEquals(0, accountRepository.saveCount());
    assertTrue(eventPublisher.publishedEvents().isEmpty());
  }

  // ---------------------------------------------------------------- the name never changes

  @Test
  @DisplayName("the command carries exactly userId, email and date of birth")
  void commandCarriesNoName() {
    assertEquals(
        List.of("userId", "email", "dateOfBirth"),
        java.util.Arrays.stream(ChangeProfileCommand.class.getRecordComponents())
            .map(java.lang.reflect.RecordComponent::getName)
            .toList(),
        "the profile change must offer no name component");
  }

  @Test
  @DisplayName("a full profile change leaves both names as registered")
  void changingEverythingKeepsTheName() {
    givenActiveAccount();

    assertEquals(Outcome.CHANGED, submit(NEW_EMAIL, NEW_DATE_OF_BIRTH).outcome());

    assertEquals(FIRST_NAME, storedAccount().owner().firstName());
    assertEquals(LAST_NAME, storedAccount().owner().lastName());
  }

  // ---------------------------------------------------------------- helpers

  private List<DomainEvent> emailChangedEvents() {
    return eventPublisher.publishedEvents().stream()
        .filter(AccountEmailChanged.class::isInstance)
        .toList();
  }

  private List<DomainEvent> dateOfBirthChangedEvents() {
    return eventPublisher.publishedEvents().stream()
        .filter(AccountOwnerDateOfBirthChanged.class::isInstance)
        .toList();
  }
}
