package dev.domaincentric.sample.ecommerce.account.application.changeprofile;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.AggregateRoot;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.account.application.shared.AccountRepository;
import dev.domaincentric.sample.ecommerce.account.domain.gateway.PasswordHasher;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Test doubles shared by the change-profile application tests.
 *
 * <p>All doubles implement ports only — no production adapter is pulled into an application-layer
 * test.
 */
final class ChangeProfileTestFixtures {

  static final String USER_ID = "user-4711";
  static final String EMAIL = "jane.doe@example.com";
  static final String NEW_EMAIL = "jane.new@example.com";
  static final String PASSWORD = "OldPassw0rd";
  static final String FIRST_NAME = "Jane";
  static final String LAST_NAME = "Doe";
  static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 17);
  static final LocalDate NEW_DATE_OF_BIRTH = LocalDate.of(1990, 5, 18);
  static final Owner OWNER = Owner.of(FIRST_NAME, LAST_NAME, DATE_OF_BIRTH);

  private ChangeProfileTestFixtures() {}

  /**
   * Creates an account with the given status.
   *
   * @param status the account status
   * @param hasher the round-tripping test hasher
   * @return the account
   */
  static Account accountWith(final AccountStatus status, final PasswordHasher hasher) {
    final Account account =
        Account.reconstitute(
            AccountId.of("account-1"),
            Email.of(EMAIL),
            OWNER,
            UserId.of(USER_ID),
            HashedPassword.fromPlaintext(PASSWORD, hasher),
            status,
            Set.of(Role.CUSTOMER),
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-07-31T08:15:30Z"));
    return account;
  }

  /**
   * Creates a second, unrelated account holding the given email address.
   *
   * @param email the email address that account occupies
   * @param hasher the round-tripping test hasher
   * @return the other account
   */
  static Account otherAccountWith(final String email, final PasswordHasher hasher) {
    final Account account =
        Account.reconstitute(
            AccountId.of("account-2"),
            Email.of(email),
            Owner.of("John", "Other", DATE_OF_BIRTH),
            UserId.of("user-0815"),
            HashedPassword.fromPlaintext("OtherPassw0rd", hasher),
            AccountStatus.ACTIVE,
            Set.of(Role.CUSTOMER),
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-07-31T08:15:30Z"));
    return account;
  }

  /** Round-tripping password hasher: reversible, deterministic, no BCrypt. */
  static final class TestPasswordHasher implements PasswordHasher {

    private static final String PREFIX = "hashed:";

    @Override
    public String hash(final String plaintext) {
      return PREFIX + plaintext;
    }

    @Override
    public boolean matches(final String plaintext, final String hash) {
      return hash.equals(PREFIX + plaintext);
    }
  }

  /** In-memory account repository double recording every save. */
  static final class TestAccountRepository implements AccountRepository {

    private final Map<AccountId, Account> accounts = new LinkedHashMap<>();
    private final List<Account> savedAccounts = new ArrayList<>();

    void store(final Account account) {
      accounts.put(account.id(), account);
    }

    int saveCount() {
      return savedAccounts.size();
    }

    @Override
    public Optional<Account> findById(final AccountId id) {
      return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public Account save(final Account aggregate) {
      savedAccounts.add(aggregate);
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

  /** Recording domain event publisher. */
  static final class TestDomainEventPublisher implements DomainEventPublisher {

    private final List<DomainEvent> publishedEvents = new ArrayList<>();

    List<DomainEvent> publishedEvents() {
      return List.copyOf(publishedEvents);
    }

    @Override
    public void publish(final DomainEvent event) {
      publishedEvents.add(event);
    }

    @Override
    public void publishAndClearEvents(final AggregateRoot<?, ?> aggregate) {
      publishedEvents.addAll(aggregate.domainEvents());
      aggregate.clearDomainEvents();
    }
  }
}
