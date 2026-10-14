package dev.domaincentric.sample.ecommerce.account.application.getprofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileResult.Profile;
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
 * Unit tests for {@link GetProfileUseCase}.
 *
 * <p>Pins the projection the profile page renders — including the owner's name, which the page
 * displays but does not offer for editing — that an account which cannot log in is reported as
 * absent, and that reading a profile performs no write.
 */
@DisplayName("GetProfileUseCase")
class GetProfileUseCaseTest {

  private static final String USER_ID = "user-4711";
  private static final String EMAIL = "jane.doe@example.com";
  private static final String FIRST_NAME = "Jane";
  private static final String LAST_NAME = "Doe";
  private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 17);

  private TestAccountRepository accountRepository;
  private GetProfileUseCase getProfile;

  @BeforeEach
  void setUp() {
    accountRepository = new TestAccountRepository();
    getProfile = new GetProfileUseCase(accountRepository);
  }

  private void givenAccount(final AccountStatus status) {
    accountRepository.store(
        Account.reconstitute(
            AccountId.of("account-1"),
            Email.of(EMAIL),
            Owner.of(FIRST_NAME, LAST_NAME, DATE_OF_BIRTH),
            UserId.of(USER_ID),
            HashedPassword.fromHash("hashed:OldPassw0rd"),
            status,
            Set.of(Role.CUSTOMER),
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-07-31T08:15:30Z")));
  }

  @Test
  @DisplayName("projects the email, both names and the date of birth of an active account")
  void projectsProfileOfActiveAccount() {
    givenAccount(AccountStatus.ACTIVE);

    final GetProfileResult result = getProfile.execute(new GetProfileQuery(USER_ID));

    assertTrue(result.found());
    final Profile profile = result.profile().orElseThrow();
    assertEquals(EMAIL, profile.email());
    assertEquals(FIRST_NAME, profile.firstName());
    assertEquals(LAST_NAME, profile.lastName());
    assertEquals(FIRST_NAME + " " + LAST_NAME, profile.fullName());
    assertEquals(DATE_OF_BIRTH, profile.dateOfBirth());
  }

  @ParameterizedTest
  @EnumSource(
      value = AccountStatus.class,
      names = {"SUSPENDED", "CLOSED"})
  @DisplayName("an account that cannot log in is reported as absent")
  void accountThatCannotLoginIsAbsent(final AccountStatus status) {
    givenAccount(status);

    final GetProfileResult result = getProfile.execute(new GetProfileQuery(USER_ID));

    assertFalse(result.found());
    assertTrue(result.profile().isEmpty());
  }

  @Test
  @DisplayName("a userId without an account is reported as absent")
  void unknownUserIdIsAbsent() {
    assertFalse(getProfile.execute(new GetProfileQuery("user-unknown")).found());
  }

  @Test
  @DisplayName("reading a profile performs no write")
  void readingPerformsNoWrite() {
    givenAccount(AccountStatus.ACTIVE);

    getProfile.execute(new GetProfileQuery(USER_ID));

    assertEquals(0, accountRepository.saveCount(), "a query must not write");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  @DisplayName("rejects a query without a userId")
  void rejectsQueryWithoutUserId(final String userId) {
    assertThrows(IllegalArgumentException.class, () -> new GetProfileQuery(userId));
  }

  /** In-memory account repository double counting write access. */
  private static final class TestAccountRepository implements AccountRepository {

    private final Map<AccountId, Account> accounts = new LinkedHashMap<>();
    private int saveCount;

    void store(final Account account) {
      accounts.put(account.id(), account);
    }

    int saveCount() {
      return saveCount;
    }

    @Override
    public Optional<Account> findById(final AccountId id) {
      return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public Account save(final Account aggregate) {
      saveCount++;
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
