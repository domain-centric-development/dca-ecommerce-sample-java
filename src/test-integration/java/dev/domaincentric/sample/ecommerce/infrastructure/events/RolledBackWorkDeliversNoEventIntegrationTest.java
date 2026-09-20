package dev.domaincentric.sample.ecommerce.infrastructure.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEvent;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Work that rolls back leaves no trace of its integration event — in every profile, including
 * {@code inmemory}.
 *
 * <p>Delivery itself is safe either way: an {@code @ApplicationModuleListener} fires after commit,
 * so a rolled-back publication is never handed to a listener in the same run. What is not safe is
 * the row the event publication registry writes: it belongs to the transaction of the work that
 * published the event, and a transaction manager that only pretends to open one lets that row
 * commit on its own. The work is gone, the row remains — and the next recovery run delivers it.
 */
@ActiveProfiles("inmemory")
@SpringBootTest(
    classes = {
      EcommerceSampleApplication.class,
      RolledBackWorkDeliversNoEventIntegrationTest.Listener.class
    },
    properties =
        "spring.datasource.url=jdbc:h2:mem:rollback_events;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
class RolledBackWorkDeliversNoEventIntegrationTest {

  @Autowired private ApplicationEventPublisher publisher;
  @Autowired private PlatformTransactionManager transactions;
  @Autowired private Delivered delivered;
  @Autowired private JdbcTemplate jdbc;

  @Test
  @DisplayName("An event published by work that then fails is never delivered")
  void aRolledBackUseCaseDeliversNothing() {
    final UUID rolledBack = UUID.randomUUID();

    assertThatThrownBy(
            () ->
                new TransactionTemplate(transactions)
                    .executeWithoutResult(
                        status -> {
                          publisher.publishEvent(new Probe(rolledBack, Instant.now()));
                          throw new IllegalStateException("the work fails after publishing");
                        }))
        .isInstanceOf(IllegalStateException.class);

    // A committed event proves the listener works at all, and gives the rolled-back one every
    // chance to arrive before the assertion below.
    final UUID committed = UUID.randomUUID();
    new TransactionTemplate(transactions)
        .executeWithoutResult(
            status -> publisher.publishEvent(new Probe(committed, Instant.now())));

    Awaitility.await()
        .atMost(Duration.ofSeconds(10))
        .until(() -> delivered.eventIds.contains(committed));

    assertThat(delivered.eventIds)
        .as("a rolled-back unit of work must not deliver its integration event")
        .doesNotContain(rolledBack);

    assertThat(publicationsFor(rolledBack))
        .as("and must leave no publication behind for a later recovery run to deliver")
        .isZero();
    assertThat(publicationsFor(committed))
        .as("while the committed one was registered, so the query above looks where it should")
        .isEqualTo(1);
  }

  /** How many rows the event publication registry holds for one probe, completed or not. */
  private long publicationsFor(final UUID eventId) {
    final Long rows =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM event_publication WHERE serialized_event LIKE ?",
            Long.class,
            "%" + eventId + "%");
    return rows == null ? 0 : rows;
  }

  public record Probe(UUID eventId, Instant occurredOn) implements IntegrationEvent {}

  static final class Delivered {
    final List<UUID> eventIds = new CopyOnWriteArrayList<>();
  }

  @TestConfiguration
  static class Listener {

    @Bean
    Delivered delivered() {
      return new Delivered();
    }

    @Bean
    ProbeListener probeListener(final Delivered delivered) {
      return new ProbeListener(delivered);
    }
  }

  static class ProbeListener {

    private final Delivered delivered;

    ProbeListener(final Delivered delivered) {
      this.delivered = delivered;
    }

    @ApplicationModuleListener
    public void onProbe(final Probe event) {
      delivered.eventIds.add(event.eventId());
    }
  }
}
