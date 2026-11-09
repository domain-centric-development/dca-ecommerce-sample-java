package dev.domaincentric.sample.ecommerce.infrastructure.database;

import java.util.Map;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

/**
 * Points every Spring context a test starts at the database {@code shop} of {@link
 * PostgresTestDatabase}, and sets the defaults every test context shares.
 *
 * <p>All contexts of a JVM share the one database; {@link DatabaseReset} gives each test the seeded
 * state. The scheduled redelivery is off by default: a context the cache keeps would otherwise
 * resubmit the failed deliveries another test arranged. A test about the scheduled redelivery turns
 * it on and closes its context afterwards.
 */
public final class PostgresTestDatabaseInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  /** The one database every test context connects to. */
  public static final String DATABASE = "shop";

  @Override
  public void initialize(final ConfigurableApplicationContext context) {
    final var sources = context.getEnvironment().getPropertySources();
    sources.addFirst(
        new MapPropertySource(
            "postgresTestDatabase",
            Map.of(
                "spring.datasource.url",
                PostgresTestDatabase.urlOf(DATABASE),
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
    sources.addLast(
        new MapPropertySource("testContextDefaults", Map.of("dca.events.retry.enabled", "false")));
  }
}
