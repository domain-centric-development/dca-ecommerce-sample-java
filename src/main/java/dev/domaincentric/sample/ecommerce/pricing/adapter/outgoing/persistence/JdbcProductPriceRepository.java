package dev.domaincentric.sample.ecommerce.pricing.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.pricing.application.shared.ProductPriceRepository;
import dev.domaincentric.sample.ecommerce.pricing.domain.model.PriceId;
import dev.domaincentric.sample.ecommerce.pricing.domain.model.ProductPrice;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * PostgreSQL/JDBC implementation of {@link ProductPriceRepository}: one row per product, read back
 * through {@link ProductPrice#reconstitute}, so every load hands out a fresh instance.
 */
@Repository
public class JdbcProductPriceRepository implements ProductPriceRepository {

  private static final String SELECT_PRICE =
      "SELECT id, product_id, amount, currency, effective_from FROM product_prices";

  private final JdbcTemplate jdbcTemplate;

  public JdbcProductPriceRepository(final DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public Optional<ProductPrice> findById(final PriceId id) {
    return jdbcTemplate.query(SELECT_PRICE + " WHERE id = ?", rowMapper(), id.value()).stream()
        .findFirst();
  }

  @Override
  public Optional<ProductPrice> findByProductId(final ProductId productId) {
    return jdbcTemplate
        .query(SELECT_PRICE + " WHERE product_id = ?", rowMapper(), productId.value())
        .stream()
        .findFirst();
  }

  @Override
  public List<ProductPrice> findByProductIds(final Collection<ProductId> productIds) {
    if (productIds.isEmpty()) {
      return List.of();
    }
    return jdbcTemplate.query(
        SELECT_PRICE + " WHERE product_id = ANY (?)",
        rowMapper(),
        (Object) productIds.stream().map(ProductId::value).toArray(String[]::new));
  }

  @Override
  public ProductPrice save(final ProductPrice productPrice) {
    final Money money = productPrice.currentPrice().value();
    jdbcTemplate.update(
        """
        INSERT INTO product_prices (id, product_id, amount, currency, effective_from)
        VALUES (?, ?, ?, ?, ?)
        ON CONFLICT (id) DO UPDATE SET
          product_id = EXCLUDED.product_id, amount = EXCLUDED.amount,
          currency = EXCLUDED.currency, effective_from = EXCLUDED.effective_from
        """,
        productPrice.id().value(),
        productPrice.productId().value(),
        money.amount(),
        money.currency().getCurrencyCode(),
        Timestamp.from(productPrice.effectiveFrom()));
    return productPrice;
  }

  @Override
  public void deleteById(final PriceId id) {
    jdbcTemplate.update("DELETE FROM product_prices WHERE id = ?", id.value());
  }

  private static RowMapper<ProductPrice> rowMapper() {
    return (rs, rowNum) ->
        ProductPrice.reconstitute(
            PriceId.of(rs.getString("id")),
            ProductId.of(rs.getString("product_id")),
            Price.of(
                Money.of(
                    rs.getBigDecimal("amount"), Currency.getInstance(rs.getString("currency")))),
            rs.getTimestamp("effective_from").toInstant());
  }
}
