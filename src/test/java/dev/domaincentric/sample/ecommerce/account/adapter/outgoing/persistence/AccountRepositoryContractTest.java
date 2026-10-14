package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The behaviour every {@link AccountRepository} adapter owes its callers, run against each
 * implementation (ADR-031).
 *
 * <p>The contract exists because the two adapters used to disagree on the one thing use cases
 * depend on: a store hands out <em>copies</em>, so a mutation only survives if it is saved. The
 * in-memory adapter returned its stored instance and made forgotten saves invisible — a bug that
 * would only have surfaced against a database.
 */
abstract class AccountRepositoryContractTest {

  static final AccountId ACCOUNT_ID = AccountId.of("account-1");
  static final String USER_ID = "user-4711";
  static final String EMAIL = "jane.doe@example.com";
  static final String NEW_EMAIL = "jane.new@example.com";
  static final Owner OWNER = Owner.of("Jane", "Doe", LocalDate.of(1990, 5, 17));

  private AccountRepository repository;

  /**
   * Provides the adapter under test, freshly emptied.
   *
   * @return the repository implementation
   */
  abstract AccountRepository createRepository();

  @BeforeEach
  void setUpRepository() {
    repository = createRepository();
    repository.save(accountWith(EMAIL));
  }

  static Account accountWith(final String email) {
    return Account.reconstitute(
        ACCOUNT_ID,
        Email.of(email),
        OWNER,
        UserId.of(USER_ID),
        HashedPassword.of("hashed:OldPassw0rd"),
        AccountStatus.ACTIVE,
        Set.of(Role.CUSTOMER),
        Instant.parse("2026-01-01T00:00:00Z"),
        Instant.parse("2026-07-31T08:15:30Z"));
  }

  @Test
  @DisplayName("finds a saved account by its email address")
  void findsAccountByEmail() {
    assertEquals(ACCOUNT_ID, repository.findByEmail(Email.of(EMAIL)).orElseThrow().id());
  }

  @Test
  @DisplayName("restores every attribute of a saved account")
  void restoresEveryAttribute() {
    final Account loaded = repository.findById(ACCOUNT_ID).orElseThrow();

    assertEquals(Email.of(EMAIL), loaded.email());
    assertEquals(OWNER, loaded.owner());
    assertEquals(UserId.of(USER_ID), loaded.linkedUserId());
    assertEquals(HashedPassword.of("hashed:OldPassw0rd"), loaded.password());
    assertEquals(AccountStatus.ACTIVE, loaded.status());
    assertEquals(Set.of(Role.CUSTOMER), loaded.roles());
    assertEquals(Instant.parse("2026-01-01T00:00:00Z"), loaded.createdAt());
    assertEquals(Instant.parse("2026-07-31T08:15:30Z"), loaded.lastLoginAt());
  }

  @Test
  @DisplayName("restores an account that has never logged in")
  void restoresAccountWithoutLogin() {
    final Account neverLoggedIn =
        Account.reconstitute(
            AccountId.of("account-3"),
            Email.of("newcomer@example.com"),
            OWNER,
            UserId.of("user-0001"),
            HashedPassword.of("hashed:Passw0rd"),
            AccountStatus.ACTIVE,
            Set.of(Role.CUSTOMER),
            Instant.parse("2026-01-01T00:00:00Z"),
            null);
    repository.save(neverLoggedIn);

    assertEquals(null, repository.findById(AccountId.of("account-3")).orElseThrow().lastLoginAt());
  }

  @Test
  @DisplayName("hands out a copy, so an unsaved mutation does not reach the store")
  void handsOutACopy() {
    final Account loaded = repository.findById(ACCOUNT_ID).orElseThrow();
    loaded.changeEmail(Email.of(NEW_EMAIL));

    final Account reloaded = repository.findById(ACCOUNT_ID).orElseThrow();

    assertNotSame(loaded, reloaded, "the store must not hand out the instance it holds");
    assertEquals(
        Email.of(EMAIL),
        reloaded.email(),
        "a mutation that was never saved must not be visible to the next reader");
  }

  @Test
  @DisplayName("finds the account under its new address after the email changed")
  void findsAccountUnderNewEmail() {
    repository.save(accountWith(NEW_EMAIL));

    assertEquals(ACCOUNT_ID, repository.findByEmail(Email.of(NEW_EMAIL)).orElseThrow().id());
  }

  @Test
  @DisplayName("the superseded email address no longer resolves to the account")
  void supersededEmailNoLongerResolves() {
    repository.save(accountWith(NEW_EMAIL));

    assertTrue(
        repository.findByEmail(Email.of(EMAIL)).isEmpty(),
        "the store must drop the address the account no longer uses");
  }

  @Test
  @DisplayName("the superseded address is dropped when the loaded account itself changed it")
  void supersededEmailNoLongerResolvesAfterInPlaceChange() {
    final Account stored = repository.findByEmail(Email.of(EMAIL)).orElseThrow();
    stored.changeEmail(Email.of(NEW_EMAIL));

    repository.save(stored);

    assertTrue(
        repository.findByEmail(Email.of(EMAIL)).isEmpty(),
        "the old address must be gone even when the changed account is the one that was loaded");
    assertEquals(ACCOUNT_ID, repository.findByEmail(Email.of(NEW_EMAIL)).orElseThrow().id());
  }

  @Test
  @DisplayName("still finds the account by its linked userId after the email changed")
  void findsAccountByLinkedUserIdAfterEmailChange() {
    repository.save(accountWith(NEW_EMAIL));

    assertEquals(ACCOUNT_ID, repository.findByLinkedUserId(UserId.of(USER_ID)).orElseThrow().id());
  }

  @Test
  @DisplayName("a deleted account resolves under neither its id nor its email")
  void deletedAccountIsGone() {
    repository.deleteById(ACCOUNT_ID);

    assertTrue(repository.findById(ACCOUNT_ID).isEmpty());
    assertTrue(repository.findByEmail(Email.of(EMAIL)).isEmpty());
    assertTrue(repository.findByLinkedUserId(UserId.of(USER_ID)).isEmpty());
  }
}
