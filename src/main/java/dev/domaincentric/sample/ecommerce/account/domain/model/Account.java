package dev.domaincentric.sample.ecommerce.account.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.BaseAggregateRoot;
import dev.domaincentric.sample.ecommerce.account.domain.event.AccountClosed;
import dev.domaincentric.sample.ecommerce.account.domain.event.AccountEmailChanged;
import dev.domaincentric.sample.ecommerce.account.domain.event.AccountLinkedToIdentity;
import dev.domaincentric.sample.ecommerce.account.domain.event.AccountLoggedIn;
import dev.domaincentric.sample.ecommerce.account.domain.event.AccountOwnerDateOfBirthChanged;
import dev.domaincentric.sample.ecommerce.account.domain.event.AccountPasswordChanged;
import dev.domaincentric.sample.ecommerce.account.domain.event.AccountReactivated;
import dev.domaincentric.sample.ecommerce.account.domain.event.AccountRegistered;
import dev.domaincentric.sample.ecommerce.account.domain.event.AccountSuspended;
import dev.domaincentric.sample.ecommerce.account.domain.gateway.PasswordHasher;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Account Aggregate Root.
 *
 * <p>Represents a registered user account with credentials and profile information. An Account is
 * linked to a UserId which is shared across all bounded contexts.
 *
 * <p><b>Key Concepts:</b>
 *
 * <ul>
 *   <li>AccountId - The aggregate root identity (internal)
 *   <li>UserId - The cross-context identity (shared, in JWT)
 *   <li>Email - The login credential (unique)
 *   <li>Owner - The person the account belongs to (name and date of birth)
 *   <li>HashedPassword - BCrypt hashed password (never stored as plaintext)
 * </ul>
 *
 * <p><b>Business Rules:</b>
 *
 * <ul>
 *   <li>Email must be unique across all accounts
 *   <li>Password must meet strength requirements (validated by domain)
 *   <li>Each account is linked to exactly one UserId
 *   <li>Cannot login if account is suspended or closed
 *   <li>The owner's name is captured at registration and never changes; only the date of birth can
 *       be corrected afterwards
 * </ul>
 *
 * <p><b>Domain Events:</b>
 *
 * <ul>
 *   <li>{@link AccountRegistered} - when account is created
 *   <li>{@link AccountLinkedToIdentity} - when UserId is linked
 *   <li>{@link AccountLoggedIn} - when a user logs in
 *   <li>{@link AccountPasswordChanged} - when password is changed
 *   <li>{@link AccountEmailChanged} - when the email address is changed
 *   <li>{@link AccountOwnerDateOfBirthChanged} - when the owner's date of birth is corrected
 *   <li>{@link AccountSuspended} - when account is suspended
 *   <li>{@link AccountReactivated} - when account is reactivated
 *   <li>{@link AccountClosed} - when account is permanently closed
 * </ul>
 */
public final class Account extends BaseAggregateRoot<Account, AccountId> {

  private final AccountId id;
  private Email email;
  private Owner owner;
  private final UserId linkedUserId;
  private HashedPassword password;
  private AccountStatus status;
  private Set<String> roles;
  private final Instant createdAt;
  private Instant lastLoginAt;

  private Account(
      final AccountId id,
      final Email email,
      final Owner owner,
      final UserId linkedUserId,
      final HashedPassword password,
      final Set<String> roles,
      final Instant createdAt) {
    this.id = id;
    this.email = email;
    // Every account belongs to somebody: an owner-less account could never satisfy the rule that
    // the owner's name is fixed, because there would be no name to fix.
    this.owner = Objects.requireNonNull(owner, "An account must have an owner");
    this.linkedUserId = linkedUserId;
    this.password = password;
    this.status = AccountStatus.ACTIVE;
    this.roles = new HashSet<>(roles);
    this.createdAt = createdAt;
  }

  /**
   * Factory method to register a new account.
   *
   * <p>This method:
   *
   * <ol>
   *   <li>Validates password strength
   *   <li>Hashes the password via the {@link PasswordHasher} domain gateway
   *   <li>Generates a new AccountId
   *   <li>Links the existing UserId (preserving cart and checkout session)
   *   <li>Raises AccountRegistered and AccountLinkedToIdentity events
   * </ol>
   *
   * <p>The UserId remains unchanged during registration. This ensures continuity of cart items and
   * checkout session for users who register during checkout.
   *
   * @param email the user's email address (login credential)
   * @param owner the person the account belongs to; their name is fixed from here on
   * @param plainPassword the plaintext password (will be validated and hashed)
   * @param currentUserId the UserId to link to (from the user's JWT)
   * @param passwordHasher the password hashing domain gateway
   * @return a new Account instance
   * @throws IllegalArgumentException if email or password is invalid
   */
  public static Account register(
      final Email email,
      final Owner owner,
      final String plainPassword,
      final UserId currentUserId,
      final PasswordHasher passwordHasher) {

    final HashedPassword hashedPassword =
        HashedPassword.fromPlaintext(plainPassword, passwordHasher);
    final AccountId accountId = AccountId.generate();

    // UserId remains unchanged - no prefix conversion needed
    // This preserves cart and checkout session data
    final Set<String> defaultRoles = Set.of("CUSTOMER");

    final Account account =
        new Account(
            accountId, email, owner, currentUserId, hashedPassword, defaultRoles, Instant.now());

    // Raise domain events
    account.registerEvent(AccountRegistered.now(accountId, email, owner, currentUserId));
    account.registerEvent(AccountLinkedToIdentity.now(accountId, currentUserId));

    return account;
  }

  /**
   * Reconstructs an Account from persistence.
   *
   * <p>Used by repositories when loading accounts from storage.
   *
   * @param id the account ID
   * @param email the email
   * @param owner the person the account belongs to
   * @param linkedUserId the linked user ID
   * @param hashedPassword the BCrypt password hash
   * @param status the account status
   * @param roles the account roles
   * @param createdAt when the account was created
   * @param lastLoginAt when the user last logged in
   * @return the reconstructed Account
   */
  public static Account reconstitute(
      final AccountId id,
      final Email email,
      final Owner owner,
      final UserId linkedUserId,
      final HashedPassword hashedPassword,
      final AccountStatus status,
      final Set<String> roles,
      final Instant createdAt,
      final Instant lastLoginAt) {
    final Account account =
        new Account(id, email, owner, linkedUserId, hashedPassword, roles, createdAt);
    account.status = status;
    account.lastLoginAt = lastLoginAt;
    return account;
  }

  @Override
  public AccountId id() {
    return id;
  }

  public Email email() {
    return email;
  }

  public Owner owner() {
    return owner;
  }

  public UserId linkedUserId() {
    return linkedUserId;
  }

  public AccountStatus status() {
    return status;
  }

  public Set<String> roles() {
    return Set.copyOf(roles);
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant lastLoginAt() {
    return lastLoginAt;
  }

  /**
   * Returns the hashed password (used by adapters for persistence).
   *
   * <p>For authentication, prefer {@link #checkPassword(String, PasswordHasher)} which keeps the
   * verification step inside the aggregate.
   *
   * @return the hashed password
   */
  public HashedPassword password() {
    return password;
  }

  /**
   * Verifies whether a plaintext password matches the account's stored hash, using the supplied
   * {@link PasswordHasher} domain gateway.
   *
   * @param plainPassword the plaintext password to verify
   * @param passwordHasher the password hashing domain gateway
   * @return true if the password matches
   */
  public boolean checkPassword(final String plainPassword, final PasswordHasher passwordHasher) {
    return password.matches(plainPassword, passwordHasher);
  }

  /**
   * Records a successful login.
   *
   * <p>Updates the last login timestamp.
   *
   * @throws AccountNotAccessibleException if the account's status does not allow signing in
   */
  public void recordLogin() {
    if (!status.canLogin()) {
      throw new AccountNotAccessibleException(this.id, status);
    }
    this.lastLoginAt = Instant.now();
    registerEvent(AccountLoggedIn.now(this.id));
  }

  /**
   * Changes the account password. The new plaintext is validated and hashed via the supplied {@link
   * PasswordHasher} domain gateway.
   *
   * @param newPlainPassword the new plaintext password
   * @param passwordHasher the password hashing domain gateway
   * @throws AccountClosedException if the account is closed
   * @throws PasswordTooWeakException if the password does not meet the strength rules
   */
  public void changePassword(final String newPlainPassword, final PasswordHasher passwordHasher) {
    if (status.isTerminal()) {
      throw new AccountClosedException(this.id);
    }
    this.password = HashedPassword.fromPlaintext(newPlainPassword, passwordHasher);
    registerEvent(AccountPasswordChanged.now(this.id));
  }

  /**
   * Changes the account's email address, which is also its login credential.
   *
   * <p>Raises {@link AccountEmailChanged} only when the new address differs from the stored one.
   * Uniqueness across accounts is decided outside the aggregate.
   *
   * @param newEmail the new email address
   * @throws AccountClosedException if the account is closed
   */
  public void changeEmail(final Email newEmail) {
    if (status.isTerminal()) {
      throw new AccountClosedException(this.id);
    }
    if (email.equals(newEmail)) {
      return;
    }
    final Email previousEmail = this.email;
    this.email = newEmail;
    registerEvent(AccountEmailChanged.now(this.id, previousEmail, newEmail));
  }

  /**
   * Corrects the date of birth of the account's owner.
   *
   * <p>The owner's name is not touched: the corrected owner is derived via {@link
   * Owner#withDateOfBirth(LocalDate)}, which carries both names over. Raises {@link
   * AccountOwnerDateOfBirthChanged} only when the new date differs from the stored one.
   *
   * @param newDateOfBirth the corrected date of birth
   * @throws AccountClosedException if the account is closed
   * @throws IllegalArgumentException if the date lies in the future
   */
  public void changeOwnerDateOfBirth(final LocalDate newDateOfBirth) {
    if (status.isTerminal()) {
      throw new AccountClosedException(this.id);
    }
    if (owner.dateOfBirth().equals(newDateOfBirth)) {
      return;
    }
    final LocalDate previousDateOfBirth = owner.dateOfBirth();
    this.owner = owner.withDateOfBirth(newDateOfBirth);
    registerEvent(AccountOwnerDateOfBirthChanged.now(this.id, previousDateOfBirth, newDateOfBirth));
  }

  /**
   * Suspends the account.
   *
   * @throws AccountClosedException if the account is already closed
   */
  public void suspend() {
    if (status.isTerminal()) {
      throw new AccountClosedException(this.id);
    }
    this.status = AccountStatus.SUSPENDED;
    registerEvent(AccountSuspended.now(this.id));
  }

  /**
   * Reactivates a suspended account.
   *
   * @throws AccountNotSuspendedException if the account is not suspended
   */
  public void reactivate() {
    if (status != AccountStatus.SUSPENDED) {
      throw new AccountNotSuspendedException(this.id, status);
    }
    this.status = AccountStatus.ACTIVE;
    registerEvent(AccountReactivated.now(this.id));
  }

  /**
   * Closes the account permanently.
   *
   * @throws AccountClosedException if the account is already closed
   */
  public void close() {
    if (status.isTerminal()) {
      throw new AccountClosedException(this.id);
    }
    this.status = AccountStatus.CLOSED;
    registerEvent(AccountClosed.now(this.id));
  }

  /**
   * Adds a role to the account.
   *
   * @param role the role to add
   */
  public void addRole(final String role) {
    this.roles.add(role);
  }

  /**
   * Removes a role from the account.
   *
   * @param role the role to remove
   */
  public void removeRole(final String role) {
    this.roles.remove(role);
  }
}
