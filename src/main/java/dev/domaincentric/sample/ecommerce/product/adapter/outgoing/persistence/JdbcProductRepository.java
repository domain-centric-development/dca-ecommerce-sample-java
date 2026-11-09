package dev.domaincentric.sample.ecommerce.product.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import dev.domaincentric.sample.ecommerce.product.domain.model.Category;
import dev.domaincentric.sample.ecommerce.product.domain.model.ImageUrl;
import dev.domaincentric.sample.ecommerce.product.domain.model.Product;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductDescription;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductName;
import dev.domaincentric.sample.ecommerce.product.domain.model.SKU;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * PostgreSQL/JDBC implementation of {@link ProductRepository}.
 *
 * <p>Every load maps its row back through {@link Product#reconstitute}, so a caller gets a fresh
 * instance and a change survives only through {@code save}. Listings are ordered by name in the
 * {@code "C"} collation — byte order, the ordinal comparison the port promises.
 */
@Repository
public class JdbcProductRepository implements ProductRepository {

  private static final String SELECT_PRODUCT =
      "SELECT id, sku, name, description, category, image_url FROM products";
  private static final String BY_NAME = " ORDER BY name COLLATE \"C\"";
  private static final String SKU_CONSTRAINT = "uq_products_sku";

  private final JdbcTemplate jdbcTemplate;

  public JdbcProductRepository(final DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public Optional<Product> findById(final ProductId id) {
    return jdbcTemplate.query(SELECT_PRODUCT + " WHERE id = ?", rowMapper(), id.value()).stream()
        .findFirst();
  }

  @Override
  public Optional<Product> findBySku(final SKU sku) {
    return jdbcTemplate.query(SELECT_PRODUCT + " WHERE sku = ?", rowMapper(), sku.value()).stream()
        .findFirst();
  }

  @Override
  public List<Product> findByCategory(final Category category) {
    return jdbcTemplate.query(
        SELECT_PRODUCT + " WHERE category = ?" + BY_NAME, rowMapper(), category.name());
  }

  @Override
  public List<Product> findAll() {
    return jdbcTemplate.query(SELECT_PRODUCT + BY_NAME, rowMapper());
  }

  /**
   * @throws IllegalStateException if another product already carries this SKU — the unique
   *     constraint refuses the row, and only that refusal is translated
   */
  @Override
  public Product save(final Product product) {
    try {
      jdbcTemplate.update(
          """
          INSERT INTO products (id, sku, name, description, category, image_url)
          VALUES (?, ?, ?, ?, ?, ?)
          ON CONFLICT (id) DO UPDATE SET
            sku = EXCLUDED.sku, name = EXCLUDED.name, description = EXCLUDED.description,
            category = EXCLUDED.category, image_url = EXCLUDED.image_url
          """,
          product.id().value(),
          product.sku().value(),
          product.name().value(),
          product.description().value(),
          product.category().name(),
          product.imageUrl().value());
    } catch (final DuplicateKeyException violation) {
      if (!refusedBySkuConstraint(violation)) {
        throw violation;
      }
      throw new IllegalStateException("SKU " + product.sku().value() + " is already taken");
    }
    return product;
  }

  @Override
  public void deleteById(final ProductId id) {
    jdbcTemplate.update("DELETE FROM products WHERE id = ?", id.value());
  }

  @Override
  public boolean existsBySku(final SKU sku) {
    return Boolean.TRUE.equals(
        jdbcTemplate.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM products WHERE sku = ?)", Boolean.class, sku.value()));
  }

  private static boolean refusedBySkuConstraint(final Throwable failure) {
    for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
      final String message = cause.getMessage();
      if (message != null && message.toLowerCase(Locale.ROOT).contains(SKU_CONSTRAINT)) {
        return true;
      }
    }
    return false;
  }

  private static RowMapper<Product> rowMapper() {
    return (rs, rowNum) ->
        Product.reconstitute(
            ProductId.of(rs.getString("id")),
            SKU.of(rs.getString("sku")),
            ProductName.of(rs.getString("name")),
            ProductDescription.of(rs.getString("description")),
            Category.of(rs.getString("category")),
            ImageUrl.of(rs.getString("image_url")));
  }
}
