package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.BuyerInfo;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItemId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionStatus;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutTotals;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.DeliveryAddress;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentSelection;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import dev.domaincentric.sample.ecommerce.infrastructure.database.PostgresTestDatabase;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("JdbcCheckoutSessionRepository")
class JdbcCheckoutSessionRepositoryTest {

  private static final DataSource DATA_SOURCE =
      PostgresTestDatabase.migrated("checkout-session-repository");

  private final JdbcCheckoutSessionRepository sessions =
      new JdbcCheckoutSessionRepository(DATA_SOURCE);

  @BeforeEach
  void emptyTables() {
    new JdbcTemplate(DATA_SOURCE).execute("TRUNCATE checkout_line_items, checkout_sessions");
  }

  @Test
  @DisplayName("A session with every step completed reads back as it was saved, without events")
  void aCompletedSessionReadsBack() {
    final CheckoutSession saved = sessionWithEveryStep(CheckoutSessionStatus.CONFIRMED);
    sessions.save(saved);

    final CheckoutSession loaded = sessions.findById(saved.id()).orElseThrow();

    assertThat(loaded.cartId()).isEqualTo(saved.cartId());
    assertThat(loaded.customerId()).isEqualTo(saved.customerId());
    assertThat(loaded.lineItems()).isEqualTo(saved.lineItems());
    assertThat(loaded.totals()).isEqualTo(saved.totals());
    assertThat(loaded.currentStep()).isEqualTo(CheckoutStep.CONFIRMATION);
    assertThat(loaded.status()).isEqualTo(CheckoutSessionStatus.CONFIRMED);
    assertThat(loaded.buyerInfo()).isEqualTo(saved.buyerInfo());
    assertThat(loaded.deliveryAddress()).isEqualTo(saved.deliveryAddress());
    assertThat(loaded.shippingOption()).isEqualTo(saved.shippingOption());
    assertThat(loaded.paymentSelection()).isEqualTo(saved.paymentSelection());
    assertThat(loaded.orderReference()).isEqualTo("ORDER-1");
    assertThat(loaded.domainEvents()).isEmpty();
  }

  @Test
  @DisplayName("A started session keeps its steps empty and its lines in order")
  void aStartedSessionKeepsItsLinesInOrder() {
    final CheckoutSession started =
        CheckoutSession.start(
            CartId.generate(),
            CustomerId.of("customer-1"),
            List.of(line("Zeta", 1), line("Alpha", 2), line("Mu", 3)),
            Money.euro(60));
    sessions.save(started);

    final CheckoutSession loaded = sessions.findById(started.id()).orElseThrow();

    assertThat(loaded.lineItems())
        .extracting(CheckoutLineItem::productName)
        .containsExactly("Zeta", "Alpha", "Mu");
    assertThat(loaded.buyerInfo()).isNull();
    assertThat(loaded.deliveryAddress()).isNull();
    assertThat(loaded.shippingOption()).isNull();
    assertThat(loaded.paymentSelection()).isNull();
  }

  @Test
  @DisplayName("A cart's active session is found by cart and by customer; a confirmed one is not")
  void theActiveSessionIsFoundByCartAndCustomer() {
    final CheckoutSession confirmed = sessionWithEveryStep(CheckoutSessionStatus.CONFIRMED);
    sessions.save(confirmed);
    final CheckoutSession active =
        CheckoutSession.start(
            confirmed.cartId(), confirmed.customerId(), List.of(line("Pin", 1)), Money.euro(10));
    sessions.save(active);

    assertThat(sessions.findActiveByCartId(confirmed.cartId()).orElseThrow().id())
        .isEqualTo(active.id());
    assertThat(sessions.findActiveByCustomerId(confirmed.customerId()).orElseThrow().id())
        .isEqualTo(active.id());
    assertThat(
            sessions
                .findConfirmedOrCompletedByCustomerId(confirmed.customerId())
                .orElseThrow()
                .id())
        .isEqualTo(confirmed.id());
    assertThat(sessions.findByIdForCustomer(active.id(), CustomerId.of("someone-else"))).isEmpty();
  }

  @Test
  @DisplayName("A change survives only through save")
  void aChangeSurvivesOnlyThroughSave() {
    final CheckoutSession started =
        CheckoutSession.start(
            CartId.generate(),
            CustomerId.of("customer-1"),
            List.of(line("Pin", 1)),
            Money.euro(10));
    sessions.save(started);

    sessions.findById(started.id()).orElseThrow().supersede();

    assertThat(sessions.findById(started.id()).orElseThrow().status())
        .isEqualTo(CheckoutSessionStatus.ACTIVE);
  }

  private static CheckoutSession sessionWithEveryStep(final CheckoutSessionStatus status) {
    return CheckoutSession.reconstitute(
        CheckoutSessionId.generate(),
        CartId.generate(),
        CustomerId.of("customer-1"),
        List.of(line("Pin", 2)),
        CheckoutTotals.of(Money.euro(20), Money.euro(5), Money.euro(4), Money.euro(25)),
        CheckoutStep.CONFIRMATION,
        status,
        BuyerInfo.of("jane@example.com", "Jane", "Doe", "+49 30 1234567"),
        DeliveryAddress.of("Main Street 1", "Floor 2", "Berlin", "10115", "DE", "BE"),
        ShippingOption.of("standard", "Standard", "3-5 days", Money.euro(5)),
        new PaymentSelection(PaymentProviderId.of("card"), "PAY-1", Money.euro(25)),
        "ORDER-1");
  }

  private static CheckoutLineItem line(final String name, final int quantity) {
    return new CheckoutLineItem(
        CheckoutLineItemId.generate(),
        ProductId.generate(),
        name,
        Money.euro(10),
        quantity,
        "/images/" + name + ".webp",
        "{\"units\":" + quantity + "}");
  }
}
