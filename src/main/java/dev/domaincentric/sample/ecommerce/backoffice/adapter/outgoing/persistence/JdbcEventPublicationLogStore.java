package dev.domaincentric.sample.ecommerce.backoffice.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.backoffice.application.shared.EventPublicationLogStore;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * JDBC-based implementation of {@link EventPublicationLogStore}.
 *
 * <p>Reads directly from the {@code EVENT_PUBLICATION} table managed by Spring Modulith's JDBC
 * event publication registry. This adapter is read-only — event lifecycle is managed by Spring
 * Modulith.
 */
@Component
public class JdbcEventPublicationLogStore implements EventPublicationLogStore {

  private static final String FIND_ALL_SQL =
      "SELECT ID, EVENT_TYPE, SERIALIZED_EVENT, LISTENER_ID, PUBLICATION_DATE, COMPLETION_DATE, STATUS, COMPLETION_ATTEMPTS "
          + "FROM EVENT_PUBLICATION ORDER BY PUBLICATION_DATE DESC";

  private final JdbcTemplate jdbcTemplate;

  public JdbcEventPublicationLogStore(final JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<EventPublicationEntry> findAll() {
    return jdbcTemplate.query(FIND_ALL_SQL, this::mapRow);
  }

  private EventPublicationEntry mapRow(final ResultSet rs, final int rowNum) throws SQLException {
    final Timestamp completionTimestamp = rs.getTimestamp("COMPLETION_DATE");

    return new EventPublicationEntry(
        UUID.fromString(rs.getString("ID")),
        rs.getString("EVENT_TYPE"),
        "Status: "
            + rs.getString("STATUS")
            + ", attempts: "
            + rs.getInt("COMPLETION_ATTEMPTS")
            + "\n"
            + rs.getString("SERIALIZED_EVENT"),
        rs.getString("LISTENER_ID"),
        rs.getTimestamp("PUBLICATION_DATE").toInstant(),
        completionTimestamp != null ? completionTimestamp.toInstant() : null);
  }
}
