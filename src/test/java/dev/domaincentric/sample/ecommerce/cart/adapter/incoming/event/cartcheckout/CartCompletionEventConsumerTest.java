package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.event.cartcheckout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.completecart.CompleteCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.completecart.CompleteCartResult;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;
import dev.domaincentric.sample.ecommerce.cart.events.CartCompletionTrigger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The cart completion consumer turns the trigger into a command carrying the cart's owner and the
 * event's id.
 */
class CartCompletionEventConsumerTest {

  @Test
  @DisplayName("The trigger becomes a completion command carrying the owner and the event id")
  void theCommandCarriesTheEventId() {
    final List<CompleteCartCommand> executed = new ArrayList<>();
    final CartCompletionEventConsumer consumer =
        new CartCompletionEventConsumer(
            command -> {
              executed.add(command);
              return new CompleteCartResult.Completed(command.cartId(), CartStatus.ACTIVE, false);
            });
    final UUID eventId = UUID.randomUUID();

    consumer.on(
        new Trigger(eventId, Instant.now(), "cart-1", "customer-1", "session-1", List.of("p1")));

    assertEquals(
        List.of(
            new CompleteCartCommand("cart-1", "customer-1", "session-1", List.of("p1"), eventId)),
        executed);
  }

  private record Trigger(
      UUID eventId,
      Instant occurredOn,
      String cartId,
      String customerId,
      String sessionId,
      List<String> purchasedPositions)
      implements CartCompletionTrigger {}
}
