package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence.jdbc.CartSpecToJdbc;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.*;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.PageResult;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.PagingRequest;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.CompositeSpecification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * H2/JDBC implementation of ShoppingCartRepository.
 *
 * <p>This adapter persists carts and items to an in-memory H2 database using Spring JDBC. Rows are
 * read into a {@link CartRow} plus the cart's stored lines and handed to {@code
 * ShoppingCart.reconstitute}, which restores the aggregate without raising domain events.
 */
@org.springframework.context.annotation.Profile("jdbc")
@Repository
public class JdbcShoppingCartRepository implements ShoppingCartRepository {

  private final JdbcTemplate jdbcTemplate;
  private final CartSpecToJdbc specTranslator;

  public JdbcShoppingCartRepository(
      final DataSource dataSource, final CartSpecToJdbc specTranslator) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.specTranslator = specTranslator;
  }

  @Override
  public Optional<ShoppingCart> findById(final CartId id) {
    final List<CartRow> rows =
        jdbcTemplate.query(
            "SELECT id, customer_id, status FROM carts WHERE id = ?", cartRowMapper(), id.value());
    return rows.stream().findFirst().map(this::toDomain);
  }

  @Override
  public Optional<ShoppingCart> findByIdForCustomer(
      final CartId cartId, final CustomerId customerId) {
    final List<CartRow> rows =
        jdbcTemplate.query(
            "SELECT id, customer_id, status FROM carts WHERE id = ? AND customer_id = ?",
            cartRowMapper(),
            cartId.value(),
            customerId.value());
    return rows.stream().findFirst().map(this::toDomain);
  }

  @Override
  public List<ShoppingCart> findByCustomerId(final CustomerId customerId) {
    final List<CartRow> rows =
        jdbcTemplate.query(
            "SELECT id, customer_id, status FROM carts WHERE customer_id = ? ORDER BY updated_at DESC",
            cartRowMapper(),
            customerId.value());
    return toDomain(rows);
  }

  @Override
  public Optional<ShoppingCart> findActiveCartByCustomerId(final CustomerId customerId) {
    final List<CartRow> rows =
        jdbcTemplate.query(
            "SELECT id, customer_id, status FROM carts WHERE customer_id = ? AND status = ? ORDER BY updated_at DESC LIMIT 1",
            cartRowMapper(),
            customerId.value(),
            CartStatus.ACTIVE.name());
    return rows.stream().findFirst().map(this::toDomain);
  }

  @Override
  public List<ShoppingCart> findAll() {
    final List<CartRow> rows =
        jdbcTemplate.query(
            "SELECT id, customer_id, status FROM carts ORDER BY updated_at DESC", cartRowMapper());
    return toDomain(rows);
  }

  @Override
  @Transactional
  public ShoppingCart save(final ShoppingCart cart) {
    // Upsert cart
    jdbcTemplate.update(
        "MERGE INTO carts (id, customer_id, status, updated_at) KEY(id) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
        cart.id().value(),
        cart.customerId().value(),
        cart.status().name());

    // Replace items
    jdbcTemplate.update("DELETE FROM cart_items WHERE cart_id = ?", cart.id().value());

    for (final CartItem item : cart.items()) {
      jdbcTemplate.update(
          "INSERT INTO cart_items (id, cart_id, product_id, quantity, price_amount, price_currency, position_units) VALUES (?, ?, ?, ?, ?, ?, ?)",
          item.id().value(),
          cart.id().value(),
          item.productId().value(),
          item.quantity().value(),
          item.priceAtAddition().value().amount(),
          item.priceAtAddition().value().currency().getCurrencyCode(),
          item.storedUnits());
    }

    return cart;
  }

  @Override
  @Transactional
  public void deleteById(final CartId id) {
    jdbcTemplate.update("DELETE FROM cart_items WHERE cart_id = ?", id.value());
    jdbcTemplate.update("DELETE FROM carts WHERE id = ?", id.value());
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<ShoppingCart> findBy(
      final CompositeSpecification<ShoppingCart> specification, final PagingRequest pageQuery) {
    final CartSpecToJdbc translator = requireTranslator();
    final var pred = specification.accept(translator);

    // Count total
    final String countSql = "SELECT COUNT(*) FROM carts c WHERE " + pred.sql();
    final long total = jdbcTemplate.queryForObject(countSql, pred.params().toArray(), Long.class);

    // Page content
    final String selectSql =
        "SELECT id, customer_id, status FROM carts c WHERE "
            + pred.sql()
            + " ORDER BY updated_at DESC LIMIT ? OFFSET ?";
    final Object[] params =
        appendLimitOffset(pred.params().toArray(), pageQuery.pageSize(), (int) pageQuery.offset());
    final List<CartRow> rows = jdbcTemplate.query(selectSql, cartRowMapper(), params);

    return new PageResult<>(toDomain(rows), total, pageQuery.pageNumber(), pageQuery.pageSize());
  }

  private Object[] appendLimitOffset(final Object[] base, final int limit, final int offset) {
    final Object[] arr = new Object[base.length + 2];
    System.arraycopy(base, 0, arr, 0, base.length);
    arr[base.length] = limit;
    arr[base.length + 1] = offset;
    return arr;
  }

  private CartSpecToJdbc requireTranslator() {
    if (this.specTranslator == null) {
      throw new IllegalStateException("CartSpecToJdbc translator not configured");
    }
    return this.specTranslator;
  }

  /** One row of the {@code carts} table; the cart's lines are read separately. */
  private record CartRow(CartId id, CustomerId customerId, CartStatus status) {}

  private RowMapper<CartRow> cartRowMapper() {
    return (rs, rowNum) ->
        new CartRow(
            CartId.of(rs.getString("id")),
            CustomerId.of(rs.getString("customer_id")),
            CartStatus.valueOf(rs.getString("status")));
  }

  private List<ShoppingCart> toDomain(final List<CartRow> rows) {
    return rows.stream().map(this::toDomain).toList();
  }

  /** Reads the cart's lines and lets the aggregate assemble itself from them. */
  private ShoppingCart toDomain(final CartRow row) {
    return ShoppingCart.reconstitute(
        row.id(), row.customerId(), row.status(), storedItemsOf(row.id()));
  }

  private List<ShoppingCart.StoredItem> storedItemsOf(final CartId cartId) {
    final List<Map<String, Object>> rows =
        jdbcTemplate.queryForList(
            "SELECT id, product_id, quantity, price_amount, price_currency, position_units FROM cart_items WHERE cart_id = ?",
            cartId.value());

    final List<ShoppingCart.StoredItem> storedItems = new ArrayList<>();
    for (final Map<String, Object> row : rows) {
      final String currency = (String) row.get("price_currency");
      final BigDecimal amount = (BigDecimal) row.get("price_amount");
      storedItems.add(
          new ShoppingCart.StoredItem(
              CartItemId.of((String) row.get("id")),
              ProductId.of((String) row.get("product_id")),
              Quantity.of(((Number) row.get("quantity")).intValue()),
              Price.of(Money.of(amount, java.util.Currency.getInstance(currency))),
              (String) row.get("position_units")));
    }
    return storedItems;
  }
}
