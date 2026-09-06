package dev.domaincentric.sample.ecommerce.checkout.application.synccheckoutwithcart;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CartData;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CartDataPort;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.ProductInfoPort;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItemId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.TaxCalculator;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Use case for synchronizing a checkout session with current cart state.
 *
 * <p>This use case is triggered by cart change events (item added, removed, quantity changed) to
 * keep the active checkout session in sync with the cart contents.
 *
 * <p><b>Why This Matters:</b> Since carts remain ACTIVE during checkout, users can modify their
 * cart while in the checkout flow. This use case ensures the checkout session reflects those
 * changes.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link SyncCheckoutWithCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 *
 * <p><b>Bounded Context Isolation:</b> This use case accesses:
 *
 * <ul>
 *   <li>Cart data through {@link CartDataPort} output port
 *   <li>Product names through {@link ProductInfoPort} output port
 * </ul>
 *
 * This isolates the Checkout context from direct coupling to other contexts' domain models.
 */
@Service
public class SyncCheckoutWithCartUseCase implements SyncCheckoutWithCartInputPort {

  private static final Logger logger = LoggerFactory.getLogger(SyncCheckoutWithCartUseCase.class);

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final CartDataPort cartDataPort;
  private final TaxCalculator taxCalculator;
  private final ProductInfoPort productInfoPort;
  private final DomainEventPublisher eventPublisher;
  private final TransactionBoundary transactionBoundary;

  public SyncCheckoutWithCartUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final CartDataPort cartDataPort,
      final TaxCalculator taxCalculator,
      final ProductInfoPort productInfoPort,
      final DomainEventPublisher eventPublisher,
      final TransactionBoundary transactionBoundary) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.cartDataPort = cartDataPort;
    this.taxCalculator = taxCalculator;
    this.productInfoPort = productInfoPort;
    this.eventPublisher = eventPublisher;
    this.transactionBoundary = transactionBoundary;
  }

  @Override
  public SyncCheckoutWithCartResult execute(final SyncCheckoutWithCartCommand command) {
    final CartId cartId = CartId.of(command.cartId());

    // Find active checkout session for this cart
    final Optional<CheckoutSession> activeSession =
        checkoutSessionRepository.findActiveByCartId(cartId);
    if (activeSession.isEmpty()) {
      logger.debug("No active checkout session for cart {}, skipping sync", command.cartId());
      return SyncCheckoutWithCartResult.noActiveSession();
    }
    final CheckoutSessionId sessionId = activeSession.get().id();

    // Cart contents and product data come from other contexts (remote-capable) - outside the
    // transaction
    // No caller here - the session names the customer this runs on behalf of.
    final CartData cart =
        cartDataPort
            .findById(cartId, activeSession.get().customerId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Cart not found for active session: " + command.cartId()));
    if (cart.items().isEmpty()) {
      logger.warn(
          "Cart {} is empty but has active checkout session {}, skipping sync",
          command.cartId(),
          sessionId.value());
      return SyncCheckoutWithCartResult.noActiveSession();
    }
    final List<CheckoutLineItem> newLineItems = new ArrayList<>();
    Money subtotal = Money.euro(0.0);
    for (final CartData.CartItemData cartItem : cart.items()) {
      final String productName =
          productInfoPort
              .getProductName(cartItem.productId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Product not found: " + cartItem.productId().value()));
      final String imageUrl = productInfoPort.getProductImageUrl(cartItem.productId()).orElse(null);
      final CheckoutLineItem lineItem =
          CheckoutLineItem.of(
              CheckoutLineItemId.generate(),
              cartItem.productId(),
              productName,
              cartItem.priceAtAddition().value(),
              cartItem.quantity(),
              imageUrl);
      newLineItems.add(lineItem);
      subtotal = subtotal.add(lineItem.lineTotal());
    }
    final Money total = subtotal;

    // Short transaction: reload, sync, save, publish
    final SyncCheckoutWithCartResult result =
        transactionBoundary.inTransaction(
            () -> {
              final CheckoutSession session =
                  checkoutSessionRepository
                      .findById(sessionId)
                      .orElseThrow(
                          () ->
                              new IllegalStateException(
                                  "Checkout session vanished: " + sessionId.value()));
              session.syncLineItems(newLineItems, total, taxCalculator);
              checkoutSessionRepository.save(session);
              eventPublisher.publishAndClearEvents(session);
              return SyncCheckoutWithCartResult.synced(
                  session.id().value().toString(), newLineItems.size());
            });
    logger.info(
        "Synced checkout session {} with cart {} - {} items, subtotal: {}",
        sessionId.value(),
        command.cartId(),
        newLineItems.size(),
        total);
    return result;
  }
}
