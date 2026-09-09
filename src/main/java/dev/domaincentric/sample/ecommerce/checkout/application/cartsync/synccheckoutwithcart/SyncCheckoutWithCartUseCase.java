package dev.domaincentric.sample.ecommerce.checkout.application.cartsync.synccheckoutwithcart;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CartDataPort;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.ProductInfoPort;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.TaxCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Legacy cart-change compatibility contract. Snapshot checkout never synchronizes sessions; only an
 * explicit checkout action creates a new snapshot.
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
    // D06a: only a new explicit checkout action creates a new snapshot.
    return SyncCheckoutWithCartResult.noActiveSession();
  }
}
