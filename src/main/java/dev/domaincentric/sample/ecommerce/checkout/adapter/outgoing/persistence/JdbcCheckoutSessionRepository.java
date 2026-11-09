package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.BuyerInfo;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItemId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionStatus;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutTotals;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.DeliveryAddress;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentSelection;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * PostgreSQL/JDBC implementation of {@link CheckoutSessionRepository}.
 *
 * <p>A session is one row — the data of each step in columns of its own, null until the step is
 * completed — plus its line items in order. Loading maps both back through {@link
 * CheckoutSession#reconstitute}, so every load hands out a fresh instance.
 *
 * <p>A session read by its cart inside a transaction is read {@code FOR UPDATE}: confirming and
 * replacing a cart's session check its state and then change it, and the row lock holds the other
 * back until this transaction ends — across processes, where the one-at-a-time of {@link
 * CheckoutSessionRepository#inCartSession} reaches only this one.
 */
@Repository
public class JdbcCheckoutSessionRepository implements CheckoutSessionRepository {

  private static final String SELECT_SESSION = "SELECT * FROM checkout_sessions";

  private final JdbcTemplate jdbcTemplate;

  public JdbcCheckoutSessionRepository(final DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public Optional<CheckoutSession> findById(final CheckoutSessionId id) {
    return findOne(" WHERE id = ?", id.value());
  }

  @Override
  public Optional<CheckoutSession> findByIdForCustomer(
      final CheckoutSessionId id, final CustomerId customerId) {
    return findOne(" WHERE id = ? AND customer_id = ?", id.value(), customerId.value());
  }

  @Override
  public Optional<CheckoutSession> findByCartId(final CartId cartId) {
    return findOne(" WHERE cart_id = ? ORDER BY updated_at" + lockInTransaction(), cartId.value());
  }

  @Override
  public Optional<CheckoutSession> findActiveByCartId(final CartId cartId) {
    return findOne(
        " WHERE cart_id = ? AND status = ?" + lockInTransaction(),
        cartId.value(),
        CheckoutColumns.valueOf(CheckoutSessionStatus.ACTIVE));
  }

  @Override
  public Optional<CheckoutSession> findActiveByCustomerId(final CustomerId customerId) {
    return findOne(
        " WHERE customer_id = ? AND status = ?",
        customerId.value(),
        CheckoutColumns.valueOf(CheckoutSessionStatus.ACTIVE));
  }

  @Override
  public List<CheckoutSession> findExpiredSessions() {
    return findMany(" WHERE status = ?", CheckoutColumns.valueOf(CheckoutSessionStatus.EXPIRED));
  }

  @Override
  public List<CheckoutSession> findAll() {
    return findMany(" ORDER BY updated_at");
  }

  @Override
  public Optional<CheckoutSession> findConfirmedOrCompletedByCustomerId(
      final CustomerId customerId) {
    return findOne(
        " WHERE customer_id = ? AND status IN (?, ?)",
        customerId.value(),
        CheckoutColumns.valueOf(CheckoutSessionStatus.CONFIRMED),
        CheckoutColumns.valueOf(CheckoutSessionStatus.COMPLETED));
  }

  @Override
  @Transactional
  public CheckoutSession save(final CheckoutSession session) {
    final CheckoutTotals totals = session.totals();
    final @Nullable BuyerInfo buyer = session.buyerInfo();
    final @Nullable DeliveryAddress address = session.deliveryAddress();
    final @Nullable ShippingOption shipping = session.shippingOption();
    final @Nullable PaymentSelection payment = session.paymentSelection();
    final @Nullable Money authorized = payment == null ? null : payment.authorizedAmount();
    jdbcTemplate.update(
        """
        INSERT INTO checkout_sessions (
          id, cart_id, customer_id, current_step, status,
          subtotal_amount, shipping_amount, tax_amount, total_amount, currency,
          buyer_email, buyer_first_name, buyer_last_name, buyer_phone,
          delivery_street, delivery_street_line2, delivery_city, delivery_postal_code,
          delivery_country, delivery_state,
          shipping_option_id, shipping_option_name, shipping_option_estimated_delivery,
          shipping_option_cost_amount, shipping_option_cost_currency,
          payment_provider_id, payment_provider_reference,
          payment_authorized_amount, payment_authorized_currency,
          order_reference, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                ?, CURRENT_TIMESTAMP)
        ON CONFLICT (id) DO UPDATE SET
          cart_id = EXCLUDED.cart_id, customer_id = EXCLUDED.customer_id,
          current_step = EXCLUDED.current_step, status = EXCLUDED.status,
          subtotal_amount = EXCLUDED.subtotal_amount, shipping_amount = EXCLUDED.shipping_amount,
          tax_amount = EXCLUDED.tax_amount, total_amount = EXCLUDED.total_amount,
          currency = EXCLUDED.currency,
          buyer_email = EXCLUDED.buyer_email, buyer_first_name = EXCLUDED.buyer_first_name,
          buyer_last_name = EXCLUDED.buyer_last_name, buyer_phone = EXCLUDED.buyer_phone,
          delivery_street = EXCLUDED.delivery_street,
          delivery_street_line2 = EXCLUDED.delivery_street_line2,
          delivery_city = EXCLUDED.delivery_city,
          delivery_postal_code = EXCLUDED.delivery_postal_code,
          delivery_country = EXCLUDED.delivery_country, delivery_state = EXCLUDED.delivery_state,
          shipping_option_id = EXCLUDED.shipping_option_id,
          shipping_option_name = EXCLUDED.shipping_option_name,
          shipping_option_estimated_delivery = EXCLUDED.shipping_option_estimated_delivery,
          shipping_option_cost_amount = EXCLUDED.shipping_option_cost_amount,
          shipping_option_cost_currency = EXCLUDED.shipping_option_cost_currency,
          payment_provider_id = EXCLUDED.payment_provider_id,
          payment_provider_reference = EXCLUDED.payment_provider_reference,
          payment_authorized_amount = EXCLUDED.payment_authorized_amount,
          payment_authorized_currency = EXCLUDED.payment_authorized_currency,
          order_reference = EXCLUDED.order_reference, updated_at = EXCLUDED.updated_at
        """,
        session.id().value(),
        session.cartId().value(),
        session.customerId().value(),
        CheckoutColumns.valueOf(session.currentStep()),
        CheckoutColumns.valueOf(session.status()),
        totals.subtotal().amount(),
        totals.shipping().amount(),
        totals.tax().amount(),
        totals.total().amount(),
        totals.total().currency().getCurrencyCode(),
        buyer == null ? null : buyer.email(),
        buyer == null ? null : buyer.firstName(),
        buyer == null ? null : buyer.lastName(),
        buyer == null ? null : buyer.phone(),
        address == null ? null : address.street(),
        address == null ? null : address.streetLine2(),
        address == null ? null : address.city(),
        address == null ? null : address.postalCode(),
        address == null ? null : address.country(),
        address == null ? null : address.state(),
        shipping == null ? null : shipping.id(),
        shipping == null ? null : shipping.name(),
        shipping == null ? null : shipping.estimatedDelivery(),
        shipping == null ? null : shipping.cost().amount(),
        shipping == null ? null : shipping.cost().currency().getCurrencyCode(),
        payment == null ? null : payment.providerId().value(),
        payment == null ? null : payment.providerReference(),
        authorized == null ? null : authorized.amount(),
        authorized == null ? null : authorized.currency().getCurrencyCode(),
        session.orderReference());

    // Replaced wholesale: the lines are a list, and their order is part of the session
    jdbcTemplate.update(
        "DELETE FROM checkout_line_items WHERE session_id = ?", session.id().value());
    final List<CheckoutLineItem> lines = session.lineItems();
    for (int position = 0; position < lines.size(); position++) {
      final CheckoutLineItem line = lines.get(position);
      jdbcTemplate.update(
          """
          INSERT INTO checkout_line_items (session_id, position, id, product_id, product_name,
            unit_price_amount, unit_price_currency, quantity, image_url, position_snapshot)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
          """,
          session.id().value(),
          position,
          line.id().value(),
          line.productId().value(),
          line.productName(),
          line.unitPrice().amount(),
          line.unitPrice().currency().getCurrencyCode(),
          line.quantity(),
          line.imageUrl(),
          line.positionSnapshot());
    }
    return session;
  }

  @Override
  public void deleteById(final CheckoutSessionId id) {
    jdbcTemplate.update("DELETE FROM checkout_sessions WHERE id = ?", id.value());
  }

  private static String lockInTransaction() {
    return TransactionSynchronizationManager.isActualTransactionActive() ? " FOR UPDATE" : "";
  }

  private Optional<CheckoutSession> findOne(final String where, final Object... params) {
    final String limited =
        where.endsWith(" FOR UPDATE")
            ? where.substring(0, where.length() - " FOR UPDATE".length()) + " LIMIT 1 FOR UPDATE"
            : where + " LIMIT 1";
    return findMany(limited, params).stream().findFirst();
  }

  private List<CheckoutSession> findMany(final String where, final Object... params) {
    return jdbcTemplate.query(SELECT_SESSION + where, this::toDomain, params);
  }

  private CheckoutSession toDomain(final ResultSet rs, final int rowNum) throws SQLException {
    final Currency currency = Currency.getInstance(rs.getString("currency"));
    final String id = rs.getString("id");
    return CheckoutSession.reconstitute(
        CheckoutSessionId.of(id),
        CartId.of(rs.getString("cart_id")),
        CustomerId.of(rs.getString("customer_id")),
        lineItemsOf(id),
        CheckoutTotals.of(
            Money.of(rs.getBigDecimal("subtotal_amount"), currency),
            Money.of(rs.getBigDecimal("shipping_amount"), currency),
            Money.of(rs.getBigDecimal("tax_amount"), currency),
            Money.of(rs.getBigDecimal("total_amount"), currency)),
        CheckoutColumns.stepOf(rs.getString("current_step")),
        CheckoutColumns.statusOf(rs.getString("status")),
        rs.getString("buyer_email") == null
            ? null
            : BuyerInfo.of(
                rs.getString("buyer_email"),
                rs.getString("buyer_first_name"),
                rs.getString("buyer_last_name"),
                rs.getString("buyer_phone")),
        rs.getString("delivery_street") == null
            ? null
            : DeliveryAddress.of(
                rs.getString("delivery_street"),
                rs.getString("delivery_street_line2"),
                rs.getString("delivery_city"),
                rs.getString("delivery_postal_code"),
                rs.getString("delivery_country"),
                rs.getString("delivery_state")),
        rs.getString("shipping_option_id") == null
            ? null
            : ShippingOption.of(
                rs.getString("shipping_option_id"),
                rs.getString("shipping_option_name"),
                rs.getString("shipping_option_estimated_delivery"),
                money(rs, "shipping_option_cost_amount", "shipping_option_cost_currency")),
        rs.getString("payment_provider_id") == null
            ? null
            : new PaymentSelection(
                PaymentProviderId.of(rs.getString("payment_provider_id")),
                rs.getString("payment_provider_reference"),
                money(rs, "payment_authorized_amount", "payment_authorized_currency")),
        rs.getString("order_reference"));
  }

  private List<CheckoutLineItem> lineItemsOf(final String sessionId) {
    final RowMapper<CheckoutLineItem> line =
        (rs, rowNum) ->
            new CheckoutLineItem(
                CheckoutLineItemId.of(rs.getString("id")),
                ProductId.of(rs.getString("product_id")),
                rs.getString("product_name"),
                money(rs, "unit_price_amount", "unit_price_currency"),
                rs.getInt("quantity"),
                rs.getString("image_url"),
                rs.getString("position_snapshot"));
    return new ArrayList<>(
        jdbcTemplate.query(
            "SELECT * FROM checkout_line_items WHERE session_id = ? ORDER BY position",
            line,
            sessionId));
  }

  private static @Nullable Money money(
      final ResultSet rs, final String amountColumn, final String currencyColumn)
      throws SQLException {
    final BigDecimal amount = rs.getBigDecimal(amountColumn);
    return amount == null
        ? null
        : Money.of(amount, Currency.getInstance(rs.getString(currencyColumn)));
  }
}
