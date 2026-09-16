package dev.domaincentric.sample.ecommerce.account.application.changepassword;

import static dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.CURRENT_PASSWORD;
import static dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.NEW_PASSWORD;
import static dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.USER_ID;
import static dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.accountWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;
import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordResult.Outcome;
import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.TestAccountRepository;
import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.TestDomainEventPublisher;
import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.TestPasswordHasher;
import dev.domaincentric.sample.ecommerce.account.application.shared.IdentitySession;
import dev.domaincentric.sample.ecommerce.account.application.shared.TokenService;
import dev.domaincentric.sample.ecommerce.account.domain.event.AccountPasswordChanged;
import dev.domaincentric.sample.ecommerce.account.domain.model.Account;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountStatus;
import dev.domaincentric.sample.ecommerce.account.domain.model.HashedPassword;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unit tests for {@link ChangePasswordUseCase}.
 *
 * <p>Covers: a wrong current password, a rejected new password and the precedence between them; a
 * successful change with its save and a single event published after the save with the aggregate
 * cleared; an unchanged password being allowed; an inaccessible account; {@code @Transactional} on
 * {@code execute}; {@code ChangePasswordCommand} validation; and the input port's shape and
 * location.
 */
@DisplayName("ChangePasswordUseCase")
class ChangePasswordUseCaseTest {

  private List<String> interactions;
  private TestPasswordHasher passwordHasher;
  private TestAccountRepository accountRepository;
  private TestDomainEventPublisher eventPublisher;
  private ChangePasswordUseCase useCase;

  @BeforeEach
  void setUp() {
    interactions = new ArrayList<>();
    passwordHasher = new TestPasswordHasher();
    accountRepository = new TestAccountRepository(interactions);
    eventPublisher = new TestDomainEventPublisher(interactions);
    useCase = new ChangePasswordUseCase(accountRepository, passwordHasher, eventPublisher);
  }

  private Account givenStoredAccount(final AccountStatus status) {
    final Account account = accountWith(status, CURRENT_PASSWORD, passwordHasher);
    account.clearDomainEvents();
    accountRepository.store(account);
    return account;
  }

  private ChangePasswordResult execute(final String currentPassword, final String newPassword) {
    return useCase.execute(new ChangePasswordCommand(USER_ID, currentPassword, newPassword));
  }

  // ---------------------------------------------------------------- success

  @Test
  @DisplayName("correct current password and strong new password yields CHANGED")
  void changesPassword() {
    givenStoredAccount(AccountStatus.ACTIVE);

    final ChangePasswordResult result = execute(CURRENT_PASSWORD, NEW_PASSWORD);

    assertEquals(Outcome.CHANGED, result.outcome());
    assertTrue(result.errorMessage().isEmpty(), "a successful change carries no error message");
  }

  @Test
  @DisplayName("a successful change saves the new password hash")
  void savesNewPasswordHash() {
    givenStoredAccount(AccountStatus.ACTIVE);

    execute(CURRENT_PASSWORD, NEW_PASSWORD);

    assertEquals(1, accountRepository.saveCount(), "the account is saved exactly once");
    final Account saved = accountRepository.savedAccounts().getFirst();
    assertEquals(
        HashedPassword.of(passwordHasher.hash(NEW_PASSWORD)),
        saved.password(),
        "the saved account must carry the hash of the new password");
    assertTrue(
        saved.checkPassword(NEW_PASSWORD, passwordHasher),
        "the saved account must accept the new password");
    assertFalse(
        saved.checkPassword(CURRENT_PASSWORD, passwordHasher),
        "the saved account must no longer accept the old password");
  }

  @Test
  @DisplayName("publishes exactly one AccountPasswordChanged after the save and clears events")
  void publishesPasswordChangedAfterSave() {
    final Account account = givenStoredAccount(AccountStatus.ACTIVE);

    execute(CURRENT_PASSWORD, NEW_PASSWORD);

    final List<DomainEvent> published = eventPublisher.publishedEvents();
    assertEquals(1, published.size(), "exactly one domain event is published: " + published);
    final DomainEvent event = published.getFirst();
    assertTrue(
        event instanceof AccountPasswordChanged, "expected AccountPasswordChanged, was: " + event);
    assertEquals(
        account.id(), ((AccountPasswordChanged) event).accountId(), "the event names the account");
    assertEquals(
        List.of("save", "publish"),
        interactions,
        "events are published only after the aggregate was saved");
    assertTrue(account.domainEvents().isEmpty(), "the aggregate's pending events must be cleared");
  }

  @Test
  @DisplayName("a new password equal to the current one succeeds")
  void allowsUnchangedPassword() {
    givenStoredAccount(AccountStatus.ACTIVE);

    assertEquals(Outcome.CHANGED, execute(CURRENT_PASSWORD, CURRENT_PASSWORD).outcome());
  }

  // ---------------------------------------------------------------- current password

  @Test
  @DisplayName("a wrong current password yields CURRENT_PASSWORD_INVALID with its message")
  void rejectsWrongCurrentPassword() {
    givenStoredAccount(AccountStatus.ACTIVE);

    final ChangePasswordResult result = execute("WrongPassw0rd", NEW_PASSWORD);

    assertEquals(Outcome.CURRENT_PASSWORD_INVALID, result.outcome());
    assertEquals("Current password is not correct", result.errorMessage().orElse(null));
  }

  @Test
  @DisplayName("a wrong current password changes nothing, saves nothing, publishes nothing")
  void wrongCurrentPasswordHasNoEffect() {
    final Account account = givenStoredAccount(AccountStatus.ACTIVE);

    execute("WrongPassw0rd", NEW_PASSWORD);

    assertTrue(
        account.checkPassword(CURRENT_PASSWORD, passwordHasher),
        "the stored password must be unchanged");
    assertEquals(0, accountRepository.saveCount(), "nothing may be saved");
    assertTrue(eventPublisher.publishedEvents().isEmpty(), "no event may be published");
    assertTrue(account.domainEvents().isEmpty(), "no event may be registered on the aggregate");
  }

  // ---------------------------------------------------------------- new password strength

  @ParameterizedTest
  @ValueSource(strings = {"Short1", "alllowercase1", "ALLUPPERCASE1", "NoDigitsAtAll"})
  @DisplayName("a weak new password yields NEW_PASSWORD_REJECTED with the domain's message")
  void rejectsWeakNewPassword(final String weakPassword) {
    givenStoredAccount(AccountStatus.ACTIVE);

    final ChangePasswordResult result = execute(CURRENT_PASSWORD, weakPassword);

    assertEquals(Outcome.NEW_PASSWORD_REJECTED, result.outcome());
    assertEquals(
        domainRejectionMessage(weakPassword),
        result.errorMessage().orElse(null),
        "the domain's rejection message must be reported verbatim");
  }

  @Test
  @DisplayName("a rejected new password changes nothing, saves nothing, publishes nothing")
  void rejectedNewPasswordHasNoEffect() {
    final Account account = givenStoredAccount(AccountStatus.ACTIVE);

    execute(CURRENT_PASSWORD, "weak");

    assertTrue(
        account.checkPassword(CURRENT_PASSWORD, passwordHasher),
        "the stored password must be unchanged");
    assertEquals(0, accountRepository.saveCount(), "nothing may be saved");
    assertTrue(eventPublisher.publishedEvents().isEmpty(), "no event may be published");
    assertTrue(account.domainEvents().isEmpty(), "no event may be registered on the aggregate");
  }

  @Test
  @DisplayName("a wrong current password wins over a weak new password")
  void wrongCurrentPasswordWinsOverWeakNewPassword() {
    givenStoredAccount(AccountStatus.ACTIVE);

    final ChangePasswordResult result = execute("WrongPassw0rd", "weak");

    assertEquals(Outcome.CURRENT_PASSWORD_INVALID, result.outcome());
    assertEquals("Current password is not correct", result.errorMessage().orElse(null));
  }

  // ---------------------------------------------------------------- accessibility

  @Test
  @DisplayName("an unknown UserId yields ACCOUNT_NOT_ACCESSIBLE and changes nothing")
  void unknownUserIdIsNotAccessible() {
    final ChangePasswordResult result = execute(CURRENT_PASSWORD, NEW_PASSWORD);

    assertEquals(Outcome.ACCOUNT_NOT_ACCESSIBLE, result.outcome());
    assertEquals(0, accountRepository.saveCount());
    assertTrue(eventPublisher.publishedEvents().isEmpty());
  }

  @ParameterizedTest
  @ValueSource(strings = {"SUSPENDED", "CLOSED"})
  @DisplayName("an account that cannot log in yields ACCOUNT_NOT_ACCESSIBLE")
  void inaccessibleStatusIsNotAccessible(final String status) {
    final Account account = givenStoredAccount(AccountStatus.valueOf(status));

    final ChangePasswordResult result = execute(CURRENT_PASSWORD, NEW_PASSWORD);

    assertEquals(Outcome.ACCOUNT_NOT_ACCESSIBLE, result.outcome());
    assertTrue(
        account.checkPassword(CURRENT_PASSWORD, passwordHasher),
        "the stored password must be unchanged");
    assertEquals(0, accountRepository.saveCount(), "nothing may be saved");
    assertTrue(eventPublisher.publishedEvents().isEmpty(), "no event may be published");
  }

  // ---------------------------------------------------------------- structure

  @Test
  @DisplayName("execute is annotated @Transactional")
  void executeIsTransactional() throws NoSuchMethodException {
    final Method execute =
        ChangePasswordUseCase.class.getDeclaredMethod("execute", ChangePasswordCommand.class);

    assertTrue(
        execute.isAnnotationPresent(Transactional.class),
        "the change password use case must run in a transaction");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t"})
  @DisplayName("ChangePasswordCommand rejects a null or blank userId")
  void commandRejectsBlankUserId(final String userId) {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ChangePasswordCommand(userId, CURRENT_PASSWORD, NEW_PASSWORD));
  }

  @Test
  @DisplayName("the input port extends UseCase<ChangePasswordCommand, ChangePasswordResult>")
  void inputPortExtendsUseCase() {
    assertTrue(
        UseCase.class.isAssignableFrom(ChangePasswordInputPort.class),
        "the input port must extend UseCase");
    final ParameterizedType useCaseType =
        (ParameterizedType)
            Arrays.stream(ChangePasswordInputPort.class.getGenericInterfaces())
                .filter(ParameterizedType.class::isInstance)
                .map(ParameterizedType.class::cast)
                .filter(type -> UseCase.class.equals(type.getRawType()))
                .findFirst()
                .orElseThrow(
                    () -> new AssertionError("ChangePasswordInputPort does not extend UseCase<,>"));
    assertEquals(
        List.of(ChangePasswordCommand.class, ChangePasswordResult.class),
        List.of(useCaseType.getActualTypeArguments()));
    assertEquals(
        "dev.domaincentric.sample.ecommerce.account.application.changepassword",
        ChangePasswordInputPort.class.getPackageName());
    assertTrue(
        ChangePasswordInputPort.class.isAssignableFrom(ChangePasswordUseCase.class),
        "the use case implements its input port");
  }

  @Test
  @DisplayName("the use case depends on neither TokenService nor IdentitySession")
  void useCaseIssuesNoToken() {
    final List<Class<?>> parameterTypes =
        Arrays.stream(ChangePasswordUseCase.class.getDeclaredConstructors())
            .flatMap(constructor -> Arrays.stream(constructor.getParameterTypes()))
            .toList();

    assertFalse(
        parameterTypes.contains(TokenService.class),
        "changing a password must not issue a new token");
    assertFalse(
        parameterTypes.contains(IdentitySession.class),
        "changing a password must not touch the identity session");
  }

  private static String domainRejectionMessage(final String weakPassword) {
    try {
      HashedPassword.validatePasswordStrength(weakPassword);
    } catch (final IllegalArgumentException e) {
      return e.getMessage();
    }
    throw new AssertionError("Expected '" + weakPassword + "' to be rejected by the domain");
  }
}
