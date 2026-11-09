package dev.domaincentric.sample.ecommerce.infrastructure.database;

import java.util.Map;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

/**
 * Points every Spring context a test starts at a database of {@link PostgresTestDatabase}.
 *
 * <p>A test class that names a database of its own — {@code test.database=<name>} — gets a database
 * of that name, empty of any other class's rows; every other context shares the database {@code
 * shop}.
 */
public final class PostgresTestDatabaseInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final String URL = "spring.datasource.url";
  private static final String DATABASE = "test.database";

  @Override
  public void initialize(final ConfigurableApplicationContext context) {
    final var environment = context.getEnvironment();
    final String database = environment.getProperty(DATABASE, "shop");
    environment
        .getPropertySources()
        .addFirst(
            new MapPropertySource(
                "postgresTestDatabase",
                Map.of(
                    URL,
                    PostgresTestDatabase.urlOf(database),
                    "spring.datasource.username",
                    PostgresTestDatabase.username(),
                    "spring.datasource.password",
                    PostgresTestDatabase.password(),
                    // A test sends one request at a time; a small pool keeps every cached context
                    // within the container's connections
                    "spring.datasource.hikari.maximum-pool-size",
                    "4",
                    "spring.datasource.hikari.minimum-idle",
                    "1")));
  }
}
