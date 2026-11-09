package dev.domaincentric.sample.ecommerce.pricing.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.pricing.application.shared.ProcessedEvents;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * PostgreSQL/JDBC implementation of {@link ProcessedEvents}: one row per effect and event id, in
 * the same database and transaction as the effect it guards — an effect and its record commit or
 * roll back together.
 */
@Component("pricingProcessedEvents")
public class JdbcProcessedEvents implements ProcessedEvents {

  private final JdbcTemplate jdbcTemplate;

  public JdbcProcessedEvents(final DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public boolean contains(final String effect, final UUID eventId) {
    return Boolean.TRUE.equals(
        jdbcTemplate.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM pricing_processed_events WHERE effect = ? AND event_id = ?)",
            Boolean.class,
            effect,
            eventId));
  }

  @Override
  public void markProcessed(final String effect, final UUID eventId) {
    jdbcTemplate.update(
        "INSERT INTO pricing_processed_events (effect, event_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
        effect,
        eventId);
  }
}
