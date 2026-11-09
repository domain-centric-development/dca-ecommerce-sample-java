package dev.domaincentric.sample.ecommerce.infrastructure;

import dev.domaincentric.sample.ecommerce.infrastructure.database.PostgresTestDatabase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * The shop on this machine with a PostgreSQL of its own: {@code ./gradlew bootTestRun} starts a
 * container and connects the shop to it, so nothing has to run beforehand. The data lives as long
 * as the container, which stops with the shop.
 */
public final class LocalShopApplication {

  private LocalShopApplication() {}

  public static void main(final String[] args) {
    SpringApplication.from(EcommerceSampleApplication::main).with(LocalDatabase.class).run(args);
  }

  /** The container the local shop connects to. */
  @TestConfiguration(proxyBeanMethods = false)
  static class LocalDatabase {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
      return new PostgreSQLContainer(PostgresTestDatabase.IMAGE);
    }
  }
}
