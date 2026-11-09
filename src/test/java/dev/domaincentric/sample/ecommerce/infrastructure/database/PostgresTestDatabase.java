package dev.domaincentric.sample.ecommerce.infrastructure.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * The PostgreSQL every test of this JVM talks to: one container, started on first use, with one
 * database per name inside it.
 *
 * <p>A database is created once and migrated by the shop's own Flyway scripts, so a test runs
 * against the schema production runs against. The container needs Docker or Podman; it stops when
 * the JVM does.
 */
public final class PostgresTestDatabase {

  /** The PostgreSQL image every test and the local start run on. */
  public static final String IMAGE = "postgres:17-alpine";

  private static final Set<String> CREATED = ConcurrentHashMap.newKeySet();

  // Spring caches up to 32 test contexts per JVM, each with a pool of its own
  @SuppressWarnings("resource") // lives as long as the JVM
  private static final PostgreSQLContainer CONTAINER =
      new PostgreSQLContainer(IMAGE).withCommand("postgres", "-c", "max_connections=300");

  private PostgresTestDatabase() {}

  /**
   * The JDBC address of a database of this name, created on first request and left without schema —
   * the application that connects to it migrates it.
   *
   * @param name any name; it is folded into a valid database name
   * @return the JDBC URL
   */
  public static String urlOf(final String name) {
    final String database = databaseName(name);
    ensureCreated(database);
    return CONTAINER.getJdbcUrl().replaceFirst("/[^/?]+(\\?|$)", "/" + database + "$1");
  }

  /** The user every database of the container belongs to. */
  public static String username() {
    start();
    return CONTAINER.getUsername();
  }

  /** The password of {@link #username()}. */
  public static String password() {
    start();
    return CONTAINER.getPassword();
  }

  /**
   * A migrated database of this name, for an adapter test that needs no Spring context.
   *
   * @param name any name; it is folded into a valid database name
   * @return a data source on that database, its schema at the newest migration
   */
  public static DataSource migrated(final String name) {
    final DataSource dataSource = new DriverManagerDataSource(urlOf(name), username(), password());
    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
    return dataSource;
  }

  private static synchronized void start() {
    if (!CONTAINER.isRunning()) {
      CONTAINER.start();
    }
  }

  private static void ensureCreated(final String database) {
    if (CREATED.contains(database)) {
      return;
    }
    start();
    synchronized (CREATED) {
      if (CREATED.contains(database)) {
        return;
      }
      try (Connection connection =
              new DriverManagerDataSource(CONTAINER.getJdbcUrl(), username(), password())
                  .getConnection();
          Statement statement = connection.createStatement()) {
        final var exists =
            statement.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + database + "'");
        if (!exists.next()) {
          statement.execute("CREATE DATABASE " + database);
        }
      } catch (final SQLException e) {
        throw new IllegalStateException("Could not create the test database " + database, e);
      }
      CREATED.add(database);
    }
  }

  private static String databaseName(final String name) {
    final String folded = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
    final String prefixed = "t_" + folded;
    return prefixed.length() > 63 ? prefixed.substring(0, 63) : prefixed;
  }
}
