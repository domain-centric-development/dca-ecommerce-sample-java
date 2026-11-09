package dev.domaincentric.sample.ecommerce.infrastructure.database;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Gives every test the database as the start left it: the seeded catalogue, nothing else.
 *
 * <p>Before each test it waits for the deliveries the previous test set off, empties every table
 * and writes the seeded rows back. The seeded rows are taken once per database, the first time a
 * test runs after a context started on it — the state the seeder and the start's event deliveries
 * left. Restoring them is the seeder run again, without paying for its round through three contexts
 * before every test.
 */
public final class DatabaseReset implements TestExecutionListener, Ordered {

  private static final String SNAPSHOT = "test_seed";
  private static final Duration DELIVERY_TIMEOUT = Duration.ofSeconds(15);
  private static final Set<String> SNAPSHOT_TAKEN = ConcurrentHashMap.newKeySet();

  @Override
  public int getOrder() {
    // After dependency injection, before the test's own @BeforeEach
    return 2_500;
  }

  @Override
  public void beforeTestMethod(final TestContext testContext) {
    final ApplicationContext context = testContext.getApplicationContext();
    final DataSource dataSource = context.getBeanProvider(DataSource.class).getIfAvailable();
    if (dataSource == null) {
      return;
    }
    context
        .getBeanProvider(EventPublicationRegistry.class)
        .ifAvailable(DatabaseReset::awaitDelivery);
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    final List<String> tables =
        jdbc.queryForList(
            """
            SELECT table_name FROM information_schema.tables
            WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
              AND table_name <> 'flyway_schema_history'
            ORDER BY table_name
            """,
            String.class);
    final String database = jdbc.queryForObject("SELECT current_database()", String.class);
    if (SNAPSHOT_TAKEN.add(database)) {
      takeSnapshot(jdbc, tables);
    } else {
      restoreSnapshot(jdbc, tables);
    }
    context.getBeansOfType(Resettable.class).values().forEach(Resettable::reset);
  }

  private static void takeSnapshot(final JdbcTemplate jdbc, final List<String> tables) {
    jdbc.execute("DROP SCHEMA IF EXISTS " + SNAPSHOT + " CASCADE");
    jdbc.execute("CREATE SCHEMA " + SNAPSHOT);
    for (final String table : tables) {
      jdbc.execute("CREATE TABLE " + SNAPSHOT + "." + table + " AS SELECT * FROM public." + table);
    }
  }

  private static void restoreSnapshot(final JdbcTemplate jdbc, final List<String> tables) {
    final StringBuilder sql = new StringBuilder("BEGIN;");
    // The rows go back as they were taken, parents and children in any order
    sql.append("SET LOCAL session_replication_role = replica;");
    sql.append("TRUNCATE ").append(String.join(", ", tables)).append(";");
    for (final String table : tables) {
      sql.append("INSERT INTO public.")
          .append(table)
          .append(" SELECT * FROM ")
          .append(SNAPSHOT)
          .append('.')
          .append(table)
          .append(';');
    }
    sql.append("COMMIT;");
    jdbc.execute(sql.toString());
  }

  private static void awaitDelivery(final EventPublicationRegistry registry) {
    final Instant deadline = Instant.now().plus(DELIVERY_TIMEOUT);
    while (registry.findIncompletePublications().stream()
        .anyMatch(publication -> publication.getStatus() != EventPublication.Status.FAILED)) {
      if (Instant.now().isAfter(deadline)) {
        return; // a test that left a delivery hanging finds out from its own assertions
      }
      try {
        Thread.sleep(20);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }
}
