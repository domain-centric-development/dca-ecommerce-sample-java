package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.account.application.shared.AccountRepository;
import dev.domaincentric.sample.ecommerce.infrastructure.database.PostgresTestDatabase;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Runs the {@link AccountRepositoryContractTest} against the JDBC adapter on PostgreSQL, migrated
 * by the production scripts.
 *
 * <p>No Spring context: the adapter takes a {@link DataSource} and nothing else, so booting the
 * application to test it would only test the wiring. The database is this class's own.
 */
@DisplayName("JdbcAccountRepository")
class JdbcAccountRepositoryTest extends AccountRepositoryContractTest {

  private static final DataSource DATA_SOURCE =
      PostgresTestDatabase.migrated("account-repository-contract");

  @Override
  AccountRepository createRepository() {
    // Each test method starts from empty tables rather than from what the previous one left.
    new JdbcTemplate(DATA_SOURCE).execute("TRUNCATE account_roles, accounts");
    return new JdbcAccountRepository(DATA_SOURCE);
  }
}
