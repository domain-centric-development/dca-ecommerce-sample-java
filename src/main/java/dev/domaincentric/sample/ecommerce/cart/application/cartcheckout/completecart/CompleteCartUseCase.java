package dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.completecart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.CartNotFoundException;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ProcessedEvents;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for completing a shopping cart after checkout confirmation.
 *
 * <p>This use case is triggered when a checkout is confirmed (CheckoutConfirmed event), marking the
 * cart as completed to indicate the order has been placed.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link CompleteCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class CompleteCartUseCase implements CompleteCartInputPort {

  /** The effect's key in {@link ProcessedEvents} — stable: a rename would replay old events. */
  private static final String EFFECT = "complete-cart";

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;
  private final ProcessedEvents processedEvents;

  public CompleteCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher,
      final ProcessedEvents processedEvents) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
    this.processedEvents = processedEvents;
  }

  @Override
  public CompleteCartResult execute(final CompleteCartCommand input) {
    final CartId cartId = CartId.of(input.cartId());

    // Retrieve cart
    final ShoppingCart cart =
        shoppingCartRepository
            .findById(cartId)
            .orElseThrow(() -> new CartNotFoundException(cartId));

    final UUID eventId = input.eventId();
    if (eventId != null && processedEvents.contains(EFFECT, eventId)) {
      return new CompleteCartResult(cart.id().value().toString(), cart.status().name(), true);
    }

    // Complete cart (business logic validates status)
    cart.reconcileCheckout(input.sessionId(), input.purchasedPositions());

    // Persist
    shoppingCartRepository.save(cart);

    eventPublisher.publishAndClearEvents(cart);
    if (eventId != null) {
      processedEvents.markProcessed(EFFECT, eventId);
    }

    return new CompleteCartResult(cart.id().value().toString(), cart.status().name(), false);
  }
}
