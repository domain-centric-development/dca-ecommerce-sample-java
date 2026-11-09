package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.infrastructure.database.PostgresTestDatabase;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@DisplayName("JdbcProcessedEvents")
class JdbcProcessedEventsTest {

  private static final DataSource DATA_SOURCE = PostgresTestDatabase.migrated("processed-events");

  private final JdbcProcessedEvents processed = new JdbcProcessedEvents(DATA_SOURCE);

  @Test
  @DisplayName("An event counts as processed per effect, and marking it twice is no failure")
  void anEventCountsAsProcessedPerEffect() {
    final UUID event = UUID.randomUUID();

    processed.markProcessed("complete-cart", event);
    processed.markProcessed("complete-cart", event);

    assertThat(processed.contains("complete-cart", event)).isTrue();
    assertThat(processed.contains("another-effect", event)).isFalse();
  }

  @Test
  @DisplayName("A record marked in a rolled-back transaction is not kept")
  void aRolledBackMarkIsNotKept() {
    final UUID event = UUID.randomUUID();

    new TransactionTemplate(new DataSourceTransactionManager(DATA_SOURCE))
        .executeWithoutResult(
            status -> {
              processed.markProcessed("complete-cart", event);
              status.setRollbackOnly();
            });

    assertThat(processed.contains("complete-cart", event)).isFalse();
  }
}
