package dev.domaincentric.sample.ecommerce.specification;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.*;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.dca.spring.InMemoryTransactionBoundary;
import dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.persistence.InMemoryCheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.cartsync.synccheckoutwithcart.*;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.*;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.*;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.*;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.CheckoutConfirmed;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.*;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.*;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import org.junit.jupiter.api.*;

class CheckoutSpecificationTest {
  @TestFactory
  Stream<DynamicTest> vectors() throws Exception {
    var tests = new ArrayList<DynamicTest>();
    for (var v :
        SharedSpecificationTest.JSON.readTree(
            SharedSpecificationTest.ROOT.resolve("vectors/checkout.json").toFile())) {
      String id = v.path("id").asText();
      tests.add(DynamicTest.dynamicTest(id, () -> run(id)));
    }
    return tests.stream();
  }

  void run(String id) throws Exception {
    var f = new Fixture();
    var session = f.start();
    ready(session);
    session.clearDomainEvents();
    switch (id) {
      case "checkout.snapshot.unchanged-after-cart-edit",
          "checkout.cart-edit.does-not-create-session" -> {
        var snapshot = List.copyOf(session.lineItems());
        f.cart.addItem(
            f.product,
            dev.domaincentric.sample.ecommerce.cart.domain.model.Quantity.of(3),
            Price.of(Money.euro(10)));
        var sync =
            new SyncCheckoutWithCartUseCase(
                f.repository,
                f,
                new TaxCalculator(),
                new ProductInfoPort() {
                  public Optional<String> getProductName(ProductId id) {
                    return Optional.of("Thing");
                  }

                  public Optional<String> getProductImageUrl(ProductId id) {
                    return Optional.empty();
                  }
                },
                f.events,
                new InMemoryTransactionBoundary());
        assertFalse(sync.execute(new SyncCheckoutWithCartCommand(f.cart.id().value())).synced());
        assertEquals(snapshot, session.lineItems());
        assertEquals(
            session.id(), f.repository.findActiveByCartId(session.cartId()).orElseThrow().id());
      }
      case "checkout.restart.supersedes-open-session", "checkout.superseded.confirm-rejected" -> {
        var next = f.start();
        assertNotEquals(session.id(), next.id());
        assertEquals(CheckoutSessionStatus.SUPERSEDED, session.status());
        assertThrows(IllegalStateException.class, () -> f.confirm(session));
        assertTrue(session.domainEvents().isEmpty());
      }
      case "checkout.abandon.preserves-cart" -> {
        String snapshot = f.cart.items().get(0).positionSnapshot();
        session.abandon();
        assertEquals(snapshot, f.cart.items().get(0).positionSnapshot());
        assertNotEquals(session.id(), f.start().id());
        assertEquals(CheckoutSessionStatus.ABANDONED, session.status());
      }
      case "checkout.completed.not-superseded" -> {
        f.confirm(session);
        session.complete("order");
        f.start();
        assertEquals(CheckoutSessionStatus.COMPLETED, session.status());
      }
      case "checkout.confirm.price-changed", "checkout.confirm.out-of-stock" -> {
        var totals = session.totals();
        int events = f.events.published.size();
        if (id.endsWith("price-changed")) f.price = Money.euro(11);
        else f.stock = 1;
        var failure = assertThrows(CheckoutValidationException.class, () -> f.confirm(session));
        assertEquals(f.product, failure.validation().errors().get(0).productId());
        assertEquals(1, failure.validation().errors().size());
        assertEquals(CheckoutSessionStatus.ACTIVE, session.status());
        assertEquals(totals, session.totals());
        assertTrue(session.domainEvents().isEmpty());
        assertEquals(events, f.events.published.size());
      }
      case "checkout.confirm.unchanged", "checkout.confirmed-event.total" -> {
        f.confirm(session);
        var confirmed = (CheckoutConfirmed) f.events.published.getLast();
        assertEquals(Money.euro(20), session.totals().total());
        assertEquals(session.totals().total(), confirmed.totalAmount());
      }
      case "checkout.replacement.confirmation-wins" -> race(f, session, true);
      case "checkout.replacement.replacement-wins" -> race(f, session, false);
      default -> fail("Vector has no adapter: " + id);
    }
  }

  void race(Fixture f, CheckoutSession session, boolean confirmationWins) throws Exception {
    f.events.armed = true;
    try (var executor = Executors.newFixedThreadPool(2)) {
      var first =
          executor.submit(
              () -> {
                if (confirmationWins) f.confirm(session);
                else f.start();
              });
      assertTrue(f.events.entered.await(5, TimeUnit.SECONDS));
      var attempted = new CountDownLatch(1);
      var second =
          executor.submit(
              () -> {
                attempted.countDown();
                if (confirmationWins) f.start();
                else f.confirm(session);
              });
      assertTrue(attempted.await(5, TimeUnit.SECONDS));
      assertFalse(second.isDone());
      f.events.release.countDown();
      first.get(5, TimeUnit.SECONDS);
      if (confirmationWins) {
        second.get(5, TimeUnit.SECONDS);
        assertEquals(CheckoutSessionStatus.CONFIRMED, session.status());
      } else {
        assertInstanceOf(
            IllegalStateException.class,
            assertThrows(ExecutionException.class, () -> second.get(5, TimeUnit.SECONDS))
                .getCause());
        assertEquals(CheckoutSessionStatus.SUPERSEDED, session.status());
      }
    } finally {
      f.events.release.countDown();
    }
  }

  static void ready(CheckoutSession s) {
    s.submitBuyerInfo(new BuyerInfo("a@b.de", "Ada", "Lovelace", "123"));
    s.submitDelivery(
        DeliveryAddress.of("Street 1", "Town", "12345", "DE"),
        new ShippingOption("free", "Free", "Tomorrow", Money.euro(0)),
        new TaxCalculator());
    s.submitPayment(PaymentSelection.of(PaymentProviderId.of("invoice")));
  }

  static class Fixture implements CartDataPort, CheckoutArticleDataPort {
    final ProductId product = ProductId.generate();
    final dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart cart =
        new dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart(
            dev.domaincentric.sample.ecommerce.cart.domain.model.CartId.generate(),
            dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId.of("specification"));
    final InMemoryCheckoutSessionRepository repository = new InMemoryCheckoutSessionRepository();
    final Publisher events = new Publisher();
    Money price = Money.euro(10);
    int stock = 100;

    Fixture() {
      cart.addItem(
          product,
          dev.domaincentric.sample.ecommerce.cart.domain.model.Quantity.of(2),
          Price.of(price));
    }

    CheckoutSession start() {
      var result =
          new StartCheckoutUseCase(
                  this,
                  new TaxCalculator(),
                  new CheckoutCartFactory(),
                  this,
                  repository,
                  events,
                  new InMemoryTransactionBoundary())
              .execute(new StartCheckoutCommand(cart.id().value(), cart.customerId().value()));
      return repository.findById(CheckoutSessionId.of(result.sessionId())).orElseThrow();
    }

    void confirm(CheckoutSession session) {
      new ConfirmCheckoutUseCase(repository, this, events, new InMemoryTransactionBoundary())
          .execute(new ConfirmCheckoutCommand(session.id().value()));
    }

    public Optional<CartData> findById(CartId id, CustomerId customer) {
      return Optional.of(
          new CartData(
              id,
              customer,
              cart.items().stream()
                  .map(
                      i ->
                          new CartData.CartItemData(
                              i.productId(),
                              i.priceAtAddition(),
                              i.quantity().value(),
                              i.positionSnapshot()))
                  .toList(),
              cart.isActive()));
    }

    public Map<ProductId, CheckoutArticle> getArticleData(Collection<ProductId> ids) {
      return ids.stream()
          .collect(
              Collectors.toMap(
                  i -> i, i -> new CheckoutArticle(i, "Thing", price, stock, true, null)));
    }
  }

  static class Publisher implements DomainEventPublisher {
    final List<DomainEvent> published = new ArrayList<>();
    volatile boolean armed;
    final CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);

    public void publish(DomainEvent event) {
      published.add(event);
    }

    public void publishAndClearEvents(AggregateRoot<?, ?> aggregate) {
      if (armed) {
        armed = false;
        entered.countDown();
        try {
          if (!release.await(5, TimeUnit.SECONDS))
            throw new IllegalStateException("Race release timed out");
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IllegalStateException(e);
        }
      }
      published.addAll(aggregate.domainEvents());
      aggregate.clearDomainEvents();
    }
  }
}
