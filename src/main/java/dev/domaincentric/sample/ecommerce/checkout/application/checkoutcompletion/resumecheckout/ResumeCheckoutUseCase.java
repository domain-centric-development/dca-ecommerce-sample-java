package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.resumecheckout;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CartDataPort;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Use case for resuming the open checkout of the customer's cart.
 *
 * <p>A checkout session belongs to the cart it was started from. When the cart changed hands — an
 * account took a guest cart over at login — the session is found through the customer's active
 * cart, which Cart answers, and is taken over by that customer. It never starts a session.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link ResumeCheckoutInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
public class ResumeCheckoutUseCase implements ResumeCheckoutInputPort {

  private final CartDataPort cartDataPort;
  private final CheckoutSessionRepository checkoutSessionRepository;
  private final DomainEventPublisher eventPublisher;
  private final TransactionBoundary transactionBoundary;

  public ResumeCheckoutUseCase(
      final CartDataPort cartDataPort,
      final CheckoutSessionRepository checkoutSessionRepository,
      final DomainEventPublisher eventPublisher,
      final TransactionBoundary transactionBoundary) {
    this.cartDataPort = cartDataPort;
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.eventPublisher = eventPublisher;
    this.transactionBoundary = transactionBoundary;
  }

  @Override
  public ResumeCheckoutResult execute(final ResumeCheckoutCommand command) {
    final CustomerId customer = CustomerId.of(command.customerId());

    // Which cart is the customer's comes from Cart (remote-capable) - outside the transaction
    final Optional<CartId> cartId = cartDataPort.findActiveCartIdOf(customer);
    if (cartId.isEmpty()) {
      return ResumeCheckoutResult.none();
    }

    return transactionBoundary.inTransaction(
        () -> {
          final Optional<CheckoutSession> open =
              checkoutSessionRepository.findActiveByCartId(cartId.get());
          if (open.isEmpty()) {
            return ResumeCheckoutResult.none();
          }
          final CheckoutSession session = open.get();
          if (!session.customerId().equals(customer)) {
            session.takeOverBy(customer);
            checkoutSessionRepository.save(session);
            eventPublisher.publishAndClearEvents(session);
          }
          return ResumeCheckoutResult.from(session);
        });
  }
}
