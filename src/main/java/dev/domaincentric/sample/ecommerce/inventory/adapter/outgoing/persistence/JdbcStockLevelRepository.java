package dev.domaincentric.sample.ecommerce.inventory.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.inventory.application.shared.StockLevelRepository;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevelId;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockQuantity;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * PostgreSQL/JDBC implementation of {@link StockLevelRepository}: one row per product, read back
 * through {@link StockLevel#reconstitute}.
 *
 * <p>A stock level read for one product inside a transaction is read {@code FOR UPDATE}: a
 * reservation, a release or a reduction checks the units and then changes them, and the row lock
 * holds every other one back until this transaction ends — across processes, where the
 * one-at-a-time of {@link StockLevelRepository#inStockLevels} reaches only this one.
 */
@Repository
public class JdbcStockLevelRepository implements StockLevelRepository {

  private static final String SELECT_STOCK =
      "SELECT id, product_id, available_quantity, reserved_quantity FROM stock_levels";

  private final JdbcTemplate jdbcTemplate;

  public JdbcStockLevelRepository(final DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public Optional<StockLevel> findById(final StockLevelId id) {
    return jdbcTemplate.query(SELECT_STOCK + " WHERE id = ?", rowMapper(), id.value()).stream()
        .findFirst();
  }

  @Override
  public Optional<StockLevel> findByProductId(final ProductId productId) {
    final String lock =
        TransactionSynchronizationManager.isActualTransactionActive() ? " FOR UPDATE" : "";
    return jdbcTemplate
        .query(SELECT_STOCK + " WHERE product_id = ?" + lock, rowMapper(), productId.value())
        .stream()
        .findFirst();
  }

  @Override
  public List<StockLevel> findByProductIds(final Collection<ProductId> productIds) {
    if (productIds.isEmpty()) {
      return List.of();
    }
    return jdbcTemplate.query(
        SELECT_STOCK + " WHERE product_id = ANY (?)",
        rowMapper(),
        (Object) productIds.stream().map(ProductId::value).toArray(String[]::new));
  }

  @Override
  public List<StockLevel> findAll() {
    return jdbcTemplate.query(SELECT_STOCK + " ORDER BY product_id", rowMapper());
  }

  @Override
  public StockLevel save(final StockLevel stockLevel) {
    jdbcTemplate.update(
        """
        INSERT INTO stock_levels (id, product_id, available_quantity, reserved_quantity)
        VALUES (?, ?, ?, ?)
        ON CONFLICT (id) DO UPDATE SET
          product_id = EXCLUDED.product_id,
          available_quantity = EXCLUDED.available_quantity,
          reserved_quantity = EXCLUDED.reserved_quantity
        """,
        stockLevel.id().value(),
        stockLevel.productId().value(),
        stockLevel.availableQuantity().value(),
        stockLevel.reservedQuantity().value());
    return stockLevel;
  }

  @Override
  public void deleteById(final StockLevelId id) {
    jdbcTemplate.update("DELETE FROM stock_levels WHERE id = ?", id.value());
  }

  private static RowMapper<StockLevel> rowMapper() {
    return (rs, rowNum) ->
        StockLevel.reconstitute(
            StockLevelId.of(rs.getString("id")),
            ProductId.of(rs.getString("product_id")),
            StockQuantity.of(rs.getInt("available_quantity")),
            StockQuantity.of(rs.getInt("reserved_quantity")));
  }
}
