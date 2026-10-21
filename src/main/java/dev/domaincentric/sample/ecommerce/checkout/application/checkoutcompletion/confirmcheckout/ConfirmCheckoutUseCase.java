package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutArticleDataPort;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionNotFoundException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentInitiationFailedException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProvider;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderNotFoundException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderRegistry;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderUnavailableException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.StockReservationPort;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutArticle;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutArticlePriceResolver;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult.ErrorType;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult.ValidationError;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.CheckoutPricing;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Use case for confirming a checkout session.
 *
 * <p>This use case handles the confirmation step by:
 *
 * <ul>
 *   <li>Loading and validating the checkout session
 *   <li>Fetching fresh article data (pricing, availability) via CheckoutArticleDataPort
 *   <li>Asking the session whether it may be confirmed with the checkout pricing's verdict
 *   <li>Reserving the stock of every line in Inventory via StockReservationPort — refused when the
 *       stock no longer covers a line, so two orders placed at once never count on the same units
 *   <li>Having the chosen payment provider authorize the session's total via PaymentProvider —
 *       refused or unanswered, the order is not placed
 *   <li>Calling the domain methods to record the authorized payment and to confirm with validation
 *   <li>Persisting the updated session
 *   <li>Publishing domain events — triggers cart completion and stock reduction in separate
 *       transactions via Interface Inversion pattern; the reduction consumes the reservation
 * </ul>
 *
 * <p>A confirmation that fails after the reservation — the payment refused or unanswered included —
 * gives the reservation back (ADR-041). A process that dies between the two leaves the reservation
 * behind; only a reconciliation would find it.
 *
 * <p><b>One Aggregate Per Transaction:</b> This use case only modifies the {@code CheckoutSession}
 * aggregate. The reservation happens before, in Inventory's own transaction; cart completion and
 * stock reduction happen after, in separate transactions via event listeners (Interface Inversion
 * pattern), respecting the DDD rule of one aggregate per transaction.
 */
@Service
public class ConfirmCheckoutUseCase implements ConfirmCheckoutInputPort {

  private static final Logger LOG = LoggerFactory.getLogger(ConfirmCheckoutUseCase.class);

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final CheckoutArticleDataPort checkoutArticleDataPort;
  private final CheckoutPricing checkoutPricing;
  private final DomainEventPublisher domainEventPublisher;
  private final TransactionBoundary transactionBoundary;
  private final StockReservationPort stockReservationPort;
  private final PaymentProviderRegistry paymentProviderRegistry;

  public ConfirmCheckoutUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final CheckoutArticleDataPort checkoutArticleDataPort,
      final CheckoutPricing checkoutPricing,
      final DomainEventPublisher domainEventPublisher,
      final TransactionBoundary transactionBoundary,
      final StockReservationPort stockReservationPort,
      final PaymentProviderRegistry paymentProviderRegistry) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.checkoutArticleDataPort = checkoutArticleDataPort;
    this.checkoutPricing = checkoutPricing;
    this.domainEventPublisher = domainEventPublisher;
    this.transactionBoundary = transactionBoundary;
    this.stockReservationPort = stockReservationPort;
    this.paymentProviderRegistry = paymentProviderRegistry;
  }

  @Override
  public ConfirmCheckoutResult execute(final ConfirmCheckoutCommand command) {
    final CheckoutSessionId sessionId = CheckoutSessionId.of(command.sessionId());

    // Article data comes from the Product Catalog (remote-capable) - fetched outside the
    // transaction, keyed by the line items of the session as it is now
    final CheckoutSession current = loadSession(sessionId, command);
    final List<ProductId> productIds =
        current.lineItems().stream().map(item -> item.productId()).toList();
    final Map<ProductId, CheckoutArticle> articleDataMap =
        checkoutArticleDataPort.getArticleData(productIds);
    final Map<ProductId, CheckoutArticlePriceResolver.ArticlePrice> facts =
        articleDataMap.entrySet().stream()
            .collect(
                java.util.stream.Collectors.toUnmodifiableMap(
                    Map.Entry::getKey,
                    e ->
                        new CheckoutArticlePriceResolver.ArticlePrice(
                            e.getValue().currentPrice(),
                            e.getValue().isAvailable(),
                            e.getValue().availableStock())));

    // Serialized against a replacement of the cart's checkout, as the confirmation itself is
    return checkoutSessionRepository.inCartSession(
        current.cartId(),
        () -> {
          // Everything the session itself can refuse is refused here, before Inventory is asked to
          // hold stock: a reservation must not exist for a checkout that cannot be confirmed
          final CheckoutSession ready = loadSession(sessionId, command);
          ready.assertReadyForConfirmation(checkoutPricing.validateItems(ready, facts));
          final List<StockReservationPort.Line> lines = reserveStockOf(ready);

          try {
            // The provider is asked only once the order's checks and its stock hold, for the total
            // the review showed — outside the transaction, the provider being remote
            final String paymentReference = authorizePaymentOf(ready);

            // Short transaction: reload, record the payment, confirm, save, publish
            return transactionBoundary.inTransaction(
                () -> {
                  final CheckoutSession session = loadSession(sessionId, command);
                  session.authorizePayment(paymentReference);
                  session.confirm(
                      checkoutPricing.validateItems(session, facts),
                      checkoutPricing.calculateOrderTotal(session, facts));
                  checkoutSessionRepository.save(session);
                  domainEventPublisher.publishAndClearEvents(session);
                  return ConfirmCheckoutResult.from(session);
                });
          } catch (final Throwable t) {
            // Every way out releases the reservation — a refused or unanswered payment, an Error
            // included: the release swallows its own failures, and stock held for an order never
            // placed is worse than the attempt
            releaseQuietly(lines);
            throw t;
          }
        });
  }

  /**
   * Has the provider the customer chose authorize the session's total.
   *
   * @return the payment reference the provider answered with
   * @throws PaymentProviderNotFoundException if the chosen provider is no longer offered
   * @throws PaymentProviderUnavailableException if the provider is not available or did not answer
   * @throws PaymentInitiationFailedException if the provider refused the payment
   */
  private String authorizePaymentOf(final CheckoutSession session) {
    final PaymentProviderId providerId = session.paymentSelection().providerId();
    final PaymentProvider provider =
        paymentProviderRegistry
            .findById(providerId)
            .orElseThrow(() -> new PaymentProviderNotFoundException(providerId));
    if (!provider.isAvailable()) {
      throw new PaymentProviderUnavailableException(providerId);
    }
    final PaymentProvider.PaymentResult authorization =
        provider.initiatePayment(session.id(), session.totals().total());
    if (authorization.outcome() == PaymentProvider.PaymentResult.Outcome.UNAVAILABLE) {
      throw new PaymentProviderUnavailableException(providerId);
    }
    if (!authorization.success()) {
      throw new PaymentInitiationFailedException(providerId, authorization.errorMessage());
    }
    return authorization.providerReference();
  }

  /**
   * Reserves the stock of every line of the session, or refuses the order with the line whose stock
   * another order took since the check — nothing is reserved then.
   *
   * @return the reserved lines
   */
  private List<StockReservationPort.Line> reserveStockOf(final CheckoutSession session) {
    final List<StockReservationPort.Line> lines =
        session.lineItems().stream()
            .map(item -> new StockReservationPort.Line(item.productId(), item.quantity()))
            .toList();
    stockReservationPort
        .reserve(lines)
        .ifPresent(
            refused -> {
              throw new CheckoutValidationException(
                  CheckoutValidationResult.withError(stockNotReserved(session, refused)));
            });
    return lines;
  }

  /**
   * The refusal for a line whose stock another order took between the check and the reservation.
   */
  private static ValidationError stockNotReserved(
      final CheckoutSession session, final ProductId refused) {
    final CheckoutLineItem line =
        session.lineItems().stream()
            .filter(item -> item.productId().equals(refused))
            .findFirst()
            .orElseThrow();
    return new ValidationError(
        refused,
        line.productName(),
        "Stock could not be reserved for " + line.quantity() + " unit(s)",
        ErrorType.INSUFFICIENT_STOCK);
  }

  /**
   * Gives back a reservation the confirmation could not use. A refused release is logged and
   * changes nothing else: the caller is told why their order was not placed, not that the clean-up
   * failed as well.
   */
  private void releaseQuietly(final List<StockReservationPort.Line> lines) {
    try {
      stockReservationPort.release(lines);
    } catch (final RuntimeException e) {
      LOG.warn("Stock reserved for {} line(s) could not be released", lines.size(), e);
    }
  }

  /** Loads the session as the caller's own: a session that is not theirs is not found. */
  private CheckoutSession loadSession(
      final CheckoutSessionId sessionId, final ConfirmCheckoutCommand command) {
    return checkoutSessionRepository
        .findByIdForCustomer(sessionId, CustomerId.of(command.customerId()))
        .orElseThrow(() -> new CheckoutSessionNotFoundException(sessionId));
  }
}
