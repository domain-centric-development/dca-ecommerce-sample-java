package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.account.application.shared.AccountRepository;
import dev.domaincentric.sample.ecommerce.account.domain.model.Account;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountId;
import dev.domaincentric.sample.ecommerce.account.domain.model.Email;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of AccountRepository, active under the {@code inmemory} profile.
 *
 * <p>{@link JdbcAccountRepository} is the default; this adapter exists to run the account context
 * without a database. Note that the {@code inmemory} profile is only partial today: the cart still
 * persists via JPA, so the application as a whole still needs its datasource.
 *
 * <p>It behaves as though a database were behind it (ADR-031): every account is copied on the way
 * in and on the way out, so callers never share an instance with the store. Handing out the stored
 * aggregate would let a use case that forgets to {@code save} appear to work here and fail against
 * any real persistence.
 */
@Profile("inmemory")
@Repository
public class InMemoryAccountRepository implements AccountRepository {

  private final ConcurrentHashMap<AccountId, Account> accounts = new ConcurrentHashMap<>();

  // Secondary indexes for efficient lookups
  private final ConcurrentHashMap<String, AccountId> emailIndex = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, AccountId> userIdIndex = new ConcurrentHashMap<>();

  @Override
  public Optional<Account> findById(final AccountId id) {
    return Optional.ofNullable(accounts.get(id)).map(InMemoryAccountRepository::copyOf);
  }

  @Override
  public Optional<Account> findByEmail(final Email email) {
    final AccountId accountId = emailIndex.get(email.value());
    if (accountId == null) {
      return Optional.empty();
    }
    return findById(accountId);
  }

  @Override
  public Optional<Account> findByLinkedUserId(final UserId userId) {
    final AccountId accountId = userIdIndex.get(userId.value());
    if (accountId == null) {
      return Optional.empty();
    }
    return findById(accountId);
  }

  /**
   * @throws IllegalStateException if the email address or the linked user id already belongs to
   *     another account — the answer the {@code UNIQUE} columns of the schema give, so a caller
   *     written against this adapter works unchanged against the relational one
   */
  @Override
  public Account save(final Account account) {
    claim(emailIndex, account.email().value(), account.id(), "Email address");
    claim(userIdIndex, account.linkedUserId().value(), account.id(), "Linked user id");

    // Stored as a copy so that a later mutation of the caller's instance does not reach the store
    // without a save, the way it would not reach a database either.
    accounts.put(account.id(), copyOf(account));

    // Drop every address this account no longer uses: an account that changed its email must stop
    // resolving under the old one, otherwise it would keep logging in under both and would occupy
    // the address for everyone else.
    emailIndex
        .entrySet()
        .removeIf(
            entry ->
                entry.getValue().equals(account.id())
                    && !entry.getKey().equals(account.email().value()));

    return account;
  }

  /** Claims a unique value for one account, or refuses when somebody else holds it. */
  private static void claim(
      final ConcurrentHashMap<String, AccountId> index,
      final String value,
      final AccountId accountId,
      final String what) {

    final AccountId holder = index.putIfAbsent(value, accountId);
    if (holder != null && !holder.equals(accountId)) {
      throw new IllegalStateException(what + " " + value + " already belongs to another account");
    }
  }

  /**
   * Round-trips an account through {@link Account#reconstitute}, the same way loading a row would.
   *
   * <p>Registered-but-unpublished domain events are deliberately not carried over: a stored account
   * is a fact, and re-reading it must not replay what the writer already published.
   */
  private static Account copyOf(final Account account) {
    return Account.reconstitute(
        account.id(),
        account.email(),
        account.owner(),
        account.linkedUserId(),
        account.password(),
        account.status(),
        account.roles(),
        account.createdAt(),
        account.lastLoginAt());
  }

  @Override
  public void deleteById(final AccountId id) {
    final Account account = accounts.remove(id);
    if (account != null) {
      emailIndex.remove(account.email().value());
      userIdIndex.remove(account.linkedUserId().value());
    }
  }
}
