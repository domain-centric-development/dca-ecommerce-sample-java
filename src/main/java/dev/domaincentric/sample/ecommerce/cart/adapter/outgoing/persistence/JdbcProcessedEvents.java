package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ProcessedEvents;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * PostgreSQL/JDBC implementation of {@link ProcessedEvents}: one row per effect and event id, in
 * the same database and transaction as the effect it guards — an effect and its record commit or
 * roll back together.
 */
@Profile("!inmemory")
@Component("cartProcessedEvents")
public class JdbcProcessedEvents implements ProcessedEvents {

  private final JdbcTemplate jdbcTemplate;

  public JdbcProcessedEvents(final DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public boolean contains(final String effect, final UUID eventId) {
    return Boolean.TRUE.equals(
        jdbcTemplate.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM cart_processed_events WHERE effect = ? AND event_id = ?)",
            Boolean.class,
            effect,
            eventId));
  }

  @Override
  public void markProcessed(final String effect, final UUID eventId) {
    jdbcTemplate.update(
        "INSERT INTO cart_processed_events (effect, event_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
        effect,
        eventId);
  }
}
