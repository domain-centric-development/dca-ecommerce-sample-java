package dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.completecart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.AggregateRoot;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence.InMemoryProcessedEvents;
import dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence.InMemoryShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** An event completes a cart once, however often it is delivered. */
class CompleteCartUseCaseTest {

  private final InMemoryShoppingCartRepository carts = new InMemoryShoppingCartRepository();
  private final InMemoryProcessedEvents processedEvents = new InMemoryProcessedEvents();
  private final CountingEventPublisher publisher = new CountingEventPublisher();
  private final CompleteCartUseCase useCase =
      new CompleteCartUseCase(carts, publisher, processedEvents);

  @Test
  @DisplayName("The same event delivered twice completes the cart once")
  void aRedeliveryCompletesOnce() {
    final CartId cartId = storedCart();
    final UUID eventId = UUID.randomUUID();

    final CompleteCartResult first =
        useCase.execute(
            new CompleteCartCommand(cartId.value().toString(), "session-1", List.of(), eventId));
    final CompleteCartResult second =
        useCase.execute(
            new CompleteCartCommand(cartId.value().toString(), "session-1", List.of(), eventId));

    assertFalse(first.alreadyCompleted());
    assertTrue(second.alreadyCompleted());
    assertEquals(1, publisher.publications);
  }

  @Test
  @DisplayName("Two effects receiving the same event id are recorded apart")
  void effectsAreRecordedApart() {
    final UUID eventId = UUID.randomUUID();

    processedEvents.markProcessed("complete-cart", eventId);

    assertTrue(processedEvents.contains("complete-cart", eventId));
    assertFalse(processedEvents.contains("another-effect", eventId));
  }

  private CartId storedCart() {
    final CartId cartId = CartId.generate();
    carts.save(ShoppingCart.create(cartId, CustomerId.of("customer-" + UUID.randomUUID())));
    return cartId;
  }

  private static final class CountingEventPublisher implements DomainEventPublisher {
    private int publications;

    @Override
    public void publish(final DomainEvent event) {}

    @Override
    public void publishAndClearEvents(final AggregateRoot<?, ?> aggregate) {
      publications++;
      aggregate.clearDomainEvents();
    }
  }
}
