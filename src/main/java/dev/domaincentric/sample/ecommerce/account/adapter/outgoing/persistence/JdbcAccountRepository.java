package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.account.application.shared.AccountRepository;
import dev.domaincentric.sample.ecommerce.account.domain.model.Account;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountId;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountStatus;
import dev.domaincentric.sample.ecommerce.account.domain.model.Email;
import dev.domaincentric.sample.ecommerce.account.domain.model.HashedPassword;
import dev.domaincentric.sample.ecommerce.account.domain.model.Owner;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Role;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * H2/JDBC implementation of {@link AccountRepository} and the default account persistence.
 *
 * <p>Loading an account maps its row back through {@link Account#reconstitute} and therefore hands
 * out a fresh instance every time (ADR-031). A caller that mutates the aggregate and forgets to
 * {@code save} loses the change here — which is the point: the in-memory alternative used to return
 * the stored instance itself, and silently made the omission work.
 *
 * <p>The roles live in their own table, each by its {@code Role#code()}, because a set has no
 * natural column. They are replaced wholesale on save, in the same transaction as the account row.
 */
@Profile("!inmemory")
@Repository
public class JdbcAccountRepository implements AccountRepository {

  private static final String SELECT_ACCOUNT =
      """
      SELECT id, email, first_name, last_name, date_of_birth, linked_user_id,
             password_hash, status, created_at, last_login_at
      FROM accounts
      """;

  private final JdbcTemplate jdbcTemplate;

  public JdbcAccountRepository(final DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public Optional<Account> findById(final AccountId id) {
    return findOne(SELECT_ACCOUNT + " WHERE id = ?", id.value());
  }

  @Override
  public Optional<Account> findByEmail(final Email email) {
    return findOne(SELECT_ACCOUNT + " WHERE email = ?", email.value());
  }

  @Override
  public Optional<Account> findByLinkedUserId(final UserId userId) {
    return findOne(SELECT_ACCOUNT + " WHERE linked_user_id = ?", userId.value());
  }

  @Override
  @Transactional
  public Account save(final Account account) {
    jdbcTemplate.update(
        """
        MERGE INTO accounts (id, email, first_name, last_name, date_of_birth, linked_user_id,
                             password_hash, status, created_at, last_login_at)
        KEY(id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        account.id().value(),
        account.email().value(),
        account.owner().firstName(),
        account.owner().lastName(),
        account.owner().dateOfBirth(),
        account.linkedUserId().value(),
        account.password().hash(),
        columnOf(account.status()),
        Timestamp.from(account.createdAt()),
        account.lastLoginAt() == null ? null : Timestamp.from(account.lastLoginAt()));

    // Replaced wholesale rather than diffed: roles are a set, and a set has no update semantics
    // beyond "these are the members now".
    jdbcTemplate.update("DELETE FROM account_roles WHERE account_id = ?", account.id().value());
    for (final Role role : account.roles()) {
      jdbcTemplate.update(
          "INSERT INTO account_roles (account_id, role) VALUES (?, ?)",
          account.id().value(),
          role.code());
    }

    return account;
  }

  @Override
  @Transactional
  public void deleteById(final AccountId id) {
    jdbcTemplate.update("DELETE FROM account_roles WHERE account_id = ?", id.value());
    jdbcTemplate.update("DELETE FROM accounts WHERE id = ?", id.value());
  }

  private Optional<Account> findOne(final String sql, final Object... params) {
    final List<AccountRow> rows = jdbcTemplate.query(sql, accountRowMapper(), params);
    return rows.stream().findFirst().map(this::toDomain);
  }

  /** One row of the {@code accounts} table; the account's roles are read separately. */
  private record AccountRow(
      AccountId id,
      Email email,
      Owner owner,
      UserId linkedUserId,
      HashedPassword password,
      AccountStatus status,
      Instant createdAt,
      @Nullable Instant lastLoginAt) {}

  private RowMapper<AccountRow> accountRowMapper() {
    return (rs, rowNum) ->
        new AccountRow(
            AccountId.of(rs.getString("id")),
            Email.of(rs.getString("email")),
            Owner.of(
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getDate("date_of_birth").toLocalDate()),
            UserId.of(rs.getString("linked_user_id")),
            HashedPassword.fromHash(rs.getString("password_hash")),
            statusOf(rs.getString("status")),
            rs.getTimestamp("created_at").toInstant(),
            toInstant(rs.getTimestamp("last_login_at")));
  }

  private static @Nullable Instant toInstant(final @Nullable Timestamp timestamp) {
    return timestamp == null ? null : timestamp.toInstant();
  }

  private Account toDomain(final AccountRow row) {
    return Account.reconstitute(
        row.id(),
        row.email(),
        row.owner(),
        row.linkedUserId(),
        row.password(),
        row.status(),
        rolesOf(row.id()),
        row.createdAt(),
        row.lastLoginAt());
  }

  private Set<Role> rolesOf(final AccountId accountId) {
    return jdbcTemplate
        .queryForList(
            "SELECT role FROM account_roles WHERE account_id = ?", String.class, accountId.value())
        .stream()
        .map(Role::fromCode)
        .collect(Collectors.toSet());
  }

  /** The stored spelling of a status, written out per value as every stored account carries it. */
  private static String columnOf(final AccountStatus status) {
    return switch (status) {
      case ACTIVE -> "ACTIVE";
      case SUSPENDED -> "SUSPENDED";
      case CLOSED -> "CLOSED";
    };
  }

  private static AccountStatus statusOf(final String value) {
    return switch (value) {
      case "ACTIVE" -> AccountStatus.ACTIVE;
      case "SUSPENDED" -> AccountStatus.SUSPENDED;
      case "CLOSED" -> AccountStatus.CLOSED;
      default -> throw new IllegalStateException("Unknown stored account status: " + value);
    };
  }
}
