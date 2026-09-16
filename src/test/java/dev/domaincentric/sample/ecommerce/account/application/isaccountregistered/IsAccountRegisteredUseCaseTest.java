package dev.domaincentric.sample.ecommerce.account.application.isaccountregistered;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.account.application.shared.AccountRepository;
import dev.domaincentric.sample.ecommerce.account.domain.model.Account;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountId;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountStatus;
import dev.domaincentric.sample.ecommerce.account.domain.model.Email;
import dev.domaincentric.sample.ecommerce.account.domain.model.HashedPassword;
import dev.domaincentric.sample.ecommerce.account.domain.model.Owner;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link IsAccountRegisteredUseCase}.
 *
 * <p>Pins the one fact the authentication filter needs: whether the account a session token names
 * still exists. Status is deliberately not part of the answer — a suspended account is registered,
 * it just cannot log in — and the query performs no write.
 */
@DisplayName("IsAccountRegisteredUseCase")
class IsAccountRegisteredUseCaseTest {

  private static final String USER_ID = "user-4711";

  private TestAccountRepository accountRepository;
  private IsAccountRegisteredUseCase isAccountRegistered;

  @BeforeEach
  void setUp() {
    accountRepository = new TestAccountRepository();
    isAccountRegistered = new IsAccountRegisteredUseCase(accountRepository);
  }

  @ParameterizedTest
  @EnumSource(AccountStatus.class)
  @DisplayName("an existing account is registered whatever its status")
  void existingAccountIsRegistered(final AccountStatus status) {
    accountRepository.store(accountWith(status));

    final IsAccountRegisteredResult result =
        isAccountRegistered.execute(new IsAccountRegisteredQuery(USER_ID));

    assertTrue(result.registered());
    assertFalse(accountRepository.saved(), "a query must not write");
  }

  @Test
  @DisplayName("a user id without an account is not registered")
  void unknownUserIsNotRegistered() {
    final IsAccountRegisteredResult result =
        isAccountRegistered.execute(new IsAccountRegisteredQuery("user-nobody"));

    assertFalse(result.registered());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  "})
  @DisplayName("the query refuses a blank user id")
  void queryRefusesBlankUserId(final String userId) {
    assertThrows(IllegalArgumentException.class, () -> new IsAccountRegisteredQuery(userId));
  }

  private static Account accountWith(final AccountStatus status) {
    return Account.reconstitute(
        AccountId.of("account-1"),
        Email.of("jane.doe@example.com"),
        Owner.of("Jane", "Doe", LocalDate.of(1990, 5, 17)),
        UserId.of(USER_ID),
        HashedPassword.of("hashed:OldPassw0rd"),
        status,
        Set.of("CUSTOMER"),
        Instant.parse("2026-01-01T00:00:00Z"),
        Instant.parse("2026-07-31T08:15:30Z"));
  }

  private static final class TestAccountRepository implements AccountRepository {

    private final Map<AccountId, Account> accounts = new LinkedHashMap<>();
    private boolean saved;

    void store(final Account account) {
      accounts.put(account.id(), account);
    }

    boolean saved() {
      return saved;
    }

    @Override
    public Optional<Account> findById(final AccountId id) {
      return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public Account save(final Account aggregate) {
      saved = true;
      accounts.put(aggregate.id(), aggregate);
      return aggregate;
    }

    @Override
    public void deleteById(final AccountId id) {
      accounts.remove(id);
    }

    @Override
    public Optional<Account> findByEmail(final Email email) {
      return accounts.values().stream()
          .filter(account -> account.email().equals(email))
          .findFirst();
    }

    @Override
    public Optional<Account> findByLinkedUserId(final UserId userId) {
      return accounts.values().stream()
          .filter(account -> account.linkedUserId().equals(userId))
          .findFirst();
    }
  }
}
