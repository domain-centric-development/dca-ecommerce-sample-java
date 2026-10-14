package dev.domaincentric.sample.ecommerce.account.application.getaccountoverview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewResult.AccountOverview;
import dev.domaincentric.sample.ecommerce.account.application.shared.AccountRepository;
import dev.domaincentric.sample.ecommerce.account.domain.model.Account;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountId;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountStatus;
import dev.domaincentric.sample.ecommerce.account.domain.model.Email;
import dev.domaincentric.sample.ecommerce.account.domain.model.HashedPassword;
import dev.domaincentric.sample.ecommerce.account.domain.model.Owner;
import dev.domaincentric.sample.ecommerce.account.domain.model.Role;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link GetAccountOverviewUseCase}.
 *
 * <p>Covers the found projection, the not-found result, the read-only property, and the
 * account-lifecycle rule: an account that cannot log in is not accessible.
 */
@DisplayName("GetAccountOverviewUseCase")
class GetAccountOverviewUseCaseTest {

  private static final String LINKED_USER_ID = "user-4711";
  private static final String UNKNOWN_USER_ID = "user-does-not-exist";
  private static final String EMAIL = "jane.doe@example.com";
  private static final String PASSWORD_HASH = "$2a$10$abcdefghijklmnopqrstuv";
  private static final Owner OWNER = Owner.of("Jane", "Doe", LocalDate.of(1990, 5, 17));
  private static final Instant LAST_LOGIN =
      Instant.parse("2026-07-31T08:15:30Z").truncatedTo(ChronoUnit.SECONDS);

  private TestAccountRepository repository;
  private GetAccountOverviewUseCase useCase;

  @BeforeEach
  void setUp() {
    repository = new TestAccountRepository();
    useCase = new GetAccountOverviewUseCase(repository);
  }

  @Test
  @DisplayName("projects email and last login of the account linked to the queried userId")
  void projectsAccountLinkedToQueriedUserId() {
    repository.save(anAccount());

    final GetAccountOverviewResult result =
        useCase.execute(new GetAccountOverviewQuery(LINKED_USER_ID));

    assertTrue(result.found(), "account linked to the queried userId must be found");
    assertEquals(EMAIL, result.account().orElseThrow().email());
    assertEquals(LAST_LOGIN, result.account().orElseThrow().lastLoginAt());
  }

  @Test
  @DisplayName("returns a not-found result without email when no account is linked")
  void returnsNotFoundWhenNoAccountIsLinked() {
    repository.save(anAccount());

    final GetAccountOverviewResult result =
        useCase.execute(new GetAccountOverviewQuery(UNKNOWN_USER_ID));

    assertFalse(result.found(), "unknown userId must yield a not-found result");
    assertTrue(result.account().isEmpty(), "not-found result must not expose a projection");
  }

  @Test
  @DisplayName("performs no write - status, password hash and lastLoginAt stay unchanged")
  void performsNoWrite() {
    final Account account = anAccount();
    repository.save(account);
    repository.resetSaveCount();

    useCase.execute(new GetAccountOverviewQuery(LINKED_USER_ID));

    assertEquals(0, repository.saveCount(), "read use case must not save the aggregate");
    assertEquals(AccountStatus.ACTIVE, account.status());
    assertEquals(PASSWORD_HASH, account.password().hash());
    assertEquals(LAST_LOGIN, account.lastLoginAt());
  }

  @ParameterizedTest
  @EnumSource(
      value = AccountStatus.class,
      names = {"SUSPENDED", "CLOSED"})
  @DisplayName("an account that cannot log in is not accessible and is reported as absent")
  void reportsAccountThatCannotLoginAsAbsent(final AccountStatus status) {
    repository.save(anAccountWith(status));

    final GetAccountOverviewResult result =
        useCase.execute(new GetAccountOverviewQuery(LINKED_USER_ID));

    assertFalse(result.found(), status + " account must not be accessible");
    assertTrue(result.account().isEmpty(), status + " account must not expose email or last login");
  }

  @Test
  @DisplayName("an active account is accessible")
  void reportsActiveAccountAsAccessible() {
    repository.save(anAccountWith(AccountStatus.ACTIVE));

    assertTrue(useCase.execute(new GetAccountOverviewQuery(LINKED_USER_ID)).found());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  @DisplayName("rejects a query without a userId")
  void rejectsQueryWithoutUserId(final String userId) {
    assertThrows(IllegalArgumentException.class, () -> new GetAccountOverviewQuery(userId));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  @DisplayName("rejects a projection without an email")
  void rejectsProjectionWithoutEmail(final String email) {
    assertThrows(IllegalArgumentException.class, () -> new AccountOverview(email, LAST_LOGIN));
  }

  private static Account anAccount() {
    return anAccountWith(AccountStatus.ACTIVE);
  }

  private static Account anAccountWith(final AccountStatus status) {
    return Account.reconstitute(
        AccountId.generate(),
        Email.of(EMAIL),
        OWNER,
        UserId.of(LINKED_USER_ID),
        HashedPassword.of(PASSWORD_HASH),
        status,
        Set.of(Role.CUSTOMER),
        Instant.parse("2026-01-01T00:00:00Z"),
        LAST_LOGIN);
  }

  /** In-memory test double counting write access. */
  private static final class TestAccountRepository implements AccountRepository {

    private final Map<AccountId, Account> accounts = new ConcurrentHashMap<>();
    private int saveCount;

    @Override
    public Optional<Account> findById(final AccountId id) {
      return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public Optional<Account> findByEmail(final Email email) {
      return accounts.values().stream().filter(a -> a.email().equals(email)).findFirst();
    }

    @Override
    public Optional<Account> findByLinkedUserId(final UserId userId) {
      return accounts.values().stream().filter(a -> a.linkedUserId().equals(userId)).findFirst();
    }

    @Override
    public Account save(final Account account) {
      saveCount++;
      accounts.put(account.id(), account);
      return account;
    }

    @Override
    public void deleteById(final AccountId id) {
      accounts.remove(id);
    }

    int saveCount() {
      return saveCount;
    }

    void resetSaveCount() {
      saveCount = 0;
    }
  }
}
