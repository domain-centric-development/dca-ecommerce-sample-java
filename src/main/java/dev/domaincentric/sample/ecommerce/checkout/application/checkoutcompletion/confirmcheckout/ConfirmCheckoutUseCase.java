package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult.CheckoutNotModifiable;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult.CheckoutSessionNotFound;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult.CheckoutStepNotCompleted;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult.CheckoutStepOutOfOrder;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult.CheckoutValidation;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult.Confirmed;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult.OrderChangedSinceReview;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult.PaymentInitiationFailed;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult.PaymentProviderNotFound;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult.PaymentProviderUnavailable;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult.PriceChangedSinceReview;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutArticleDataPort;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentInitiationFailedException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProvider;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderRegistry;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderUnavailableException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.StockReservationPort;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutArticlePriceResolver;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutNotModifiableException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStepNotCompletedException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStepOutOfOrderException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult.ErrorType;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult.ValidationError;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.OrderChangedSinceReviewException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentSelection;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ReviewFingerprint;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.CheckoutPricing;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
 *   <li>Asking the session whether it may be confirmed with the checkout pricing's verdict, and
 *       whether its review at today's prices is still the one the customer placed the order from —
 *       the order is placed at exactly what that review showed
 *   <li>Reserving the stock of every line in Inventory via StockReservationPort — refused when the
 *       stock no longer covers a line, so two orders placed at once never count on the same units
 *   <li>Having the chosen payment provider authorize the total the review showed via
 *       PaymentProvider — refused or unanswered, the order is not placed
 *   <li>Calling the domain methods to record the authorized payment and to confirm with validation
 *   <li>Persisting the updated session
 *   <li>Publishing domain events — triggers cart completion and stock reduction in separate
 *       transactions via Interface Inversion pattern; the reduction consumes the reservation
 * </ul>
 *
 * <p>Every expected refusal is returned as a case of {@link ConfirmCheckoutResult}; the domain's
 * refusals are caught once, here. A confirmation that ends after the reservation without the order
 * placed — a refusal returned, the payment refused or unanswered included, or a failure thrown —
 * gives the reservation back (ADR-041). A process that dies between the two leaves the reservation
 * behind; only a reconciliation would find it. A payment the provider authorized is not voided when
 * the local confirmation refuses afterwards.
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
    final ReviewFingerprint shown = ReviewFingerprint.of(command.reviewFingerprint());

    // Article data comes from the Product Catalog (remote-capable) - fetched outside the
    // transaction, keyed by the line items of the session as it is now
    final Optional<CheckoutSession> current = findSession(sessionId, command);
    if (current.isEmpty()) {
      return new CheckoutSessionNotFound(sessionId);
    }
    final List<ProductId> productIds =
        current.get().lineItems().stream().map(item -> item.productId()).toList();
    final Map<ProductId, CheckoutArticlePriceResolver.ArticlePrice> facts =
        CheckoutArticlePriceResolver.ArticlePrice.allOf(
            checkoutArticleDataPort.getArticleData(productIds));

    // Serialized against a replacement of the cart's checkout, as the confirmation itself is
    return checkoutSessionRepository.inCartSession(
        current.get().cartId(), () -> placeOrder(sessionId, command, shown, facts));
  }

  /**
   * Checks, reserves, has the payment authorized and confirms — every refusal decided before the
   * session is saved, the reservation given back when the order is not placed after it.
   */
  private ConfirmCheckoutResult placeOrder(
      final CheckoutSessionId sessionId,
      final ConfirmCheckoutCommand command,
      final ReviewFingerprint shown,
      final Map<ProductId, CheckoutArticlePriceResolver.ArticlePrice> facts) {
    // Everything the session itself can refuse is refused here, before Inventory is asked to hold
    // stock: a reservation must not exist for a checkout that cannot be confirmed
    final Optional<CheckoutSession> found = findSession(sessionId, command);
    if (found.isEmpty()) {
      return new CheckoutSessionNotFound(sessionId);
    }
    final CheckoutSession ready = found.get();
    final Money reviewedTotal;
    try {
      ready.assertReadyForConfirmation(checkoutPricing.validateItems(ready, facts));
      reviewedTotal =
          ready.assertReviewed(checkoutPricing.todaysPrices(ready, facts), shown).totals().total();
    } catch (final CheckoutNotModifiableException
        | CheckoutStepNotCompletedException
        | CheckoutStepOutOfOrderException
        | CheckoutValidationException
        | OrderChangedSinceReviewException refused) {
      return refusalOf(refused);
    }

    final List<StockReservationPort.Line> lines = linesOf(ready);
    final Optional<ProductId> notReserved = stockReservationPort.reserve(lines);
    if (notReserved.isPresent()) {
      return new CheckoutValidation(
          CheckoutValidationResult.withError(stockNotReserved(ready, notReserved.get())));
    }

    final ConfirmCheckoutResult result;
    try {
      result = authorizeAndConfirm(ready, reviewedTotal, sessionId, command, shown, facts);
    } catch (final Throwable t) {
      // A failure thrown after the reservation, an Error included: the release swallows its own
      // failures, and stock held for an order never placed is worse than the attempt
      releaseQuietly(lines);
      throw t;
    }
    if (result instanceof Refusal) {
      // A refusal returned after the reservation - a refused or unanswered payment, or the session
      // refusing inside the transaction: the order is not placed, the stock goes back
      releaseQuietly(lines);
    }
    return result;
  }

  /**
   * Has the payment authorized and, once it is, confirms the session in a short transaction:
   * reload, record the payment, confirm, save, publish.
   */
  private ConfirmCheckoutResult authorizeAndConfirm(
      final CheckoutSession ready,
      final Money reviewedTotal,
      final CheckoutSessionId sessionId,
      final ConfirmCheckoutCommand command,
      final ReviewFingerprint shown,
      final Map<ProductId, CheckoutArticlePriceResolver.ArticlePrice> facts) {
    // The provider is asked only once the order's checks and its stock hold, for the total the
    // review showed — outside the transaction, the provider being remote
    final PaymentSelection payment;
    switch (authorizePaymentOf(ready, reviewedTotal)) {
      case Authorized authorized -> payment = authorized.payment();
      case NotAuthorized notAuthorized -> {
        return notAuthorized.refusal();
      }
    }

    return transactionBoundary.inTransaction(
        () -> {
          final Optional<CheckoutSession> reloaded = findSession(sessionId, command);
          if (reloaded.isEmpty()) {
            return new CheckoutSessionNotFound(sessionId);
          }
          final CheckoutSession session = reloaded.get();
          try {
            session.authorizePayment(payment);
            session.confirm(
                checkoutPricing.validateItems(session, facts),
                checkoutPricing.todaysPrices(session, facts),
                shown);
          } catch (final CheckoutNotModifiableException
              | CheckoutStepNotCompletedException
              | CheckoutStepOutOfOrderException
              | CheckoutValidationException
              | OrderChangedSinceReviewException refused) {
            return refusalOf(refused);
          }
          checkoutSessionRepository.save(session);
          domainEventPublisher.publishAndClearEvents(session);
          return Confirmed.from(session);
        });
  }

  /** The case for a refusal of the session's own rules. */
  private static ConfirmCheckoutResult refusalOf(final DomainException refused) {
    return switch (refused) {
      case CheckoutNotModifiableException e -> new CheckoutNotModifiable(e.sessionId(), e.status());
      case CheckoutStepNotCompletedException e ->
          new CheckoutStepNotCompleted(e.sessionId(), e.step());
      case CheckoutStepOutOfOrderException e ->
          new CheckoutStepOutOfOrder(e.sessionId(), e.requested(), e.current());
      case CheckoutValidationException e -> new CheckoutValidation(e.validation());
      case OrderChangedSinceReviewException e ->
          e.productWithChangedPrice()
              .<ConfirmCheckoutResult>map(
                  product -> new PriceChangedSinceReview(e.sessionId(), product))
              .orElseGet(() -> new OrderChangedSinceReview(e.sessionId()));
      default -> throw refused;
    };
  }

  /**
   * Has the provider the customer chose authorize the total the review showed.
   *
   * @param total the reviewed total — the one the order is confirmed at
   * @return the authorized payment — the provider that answered, its payment reference and the
   *     total — or the refusal: the provider no longer offered, not available, or refusing
   */
  private Authorization authorizePaymentOf(final CheckoutSession session, final Money total) {
    final PaymentProviderId providerId = session.paymentSelection().providerId();
    final Optional<PaymentProvider> offered = paymentProviderRegistry.findById(providerId);
    if (offered.isEmpty()) {
      return new NotAuthorized(new PaymentProviderNotFound(providerId));
    }
    final PaymentProvider provider = offered.get();
    if (!provider.isAvailable()) {
      return new NotAuthorized(new PaymentProviderUnavailable(providerId));
    }
    final String reference;
    try {
      reference = provider.initiatePayment(session.id(), total);
    } catch (final PaymentProviderUnavailableException unavailable) {
      LOG.warn("Payment provider {} is unavailable: {}", providerId, unavailable.getMessage());
      return new NotAuthorized(new PaymentProviderUnavailable(providerId));
    } catch (final PaymentInitiationFailedException refused) {
      return new NotAuthorized(new PaymentInitiationFailed(providerId, refused.getMessage()));
    }
    return new Authorized(PaymentSelection.authorized(provider.providerId(), reference, total));
  }

  /** The order's lines as Inventory reserves them. */
  private static List<StockReservationPort.Line> linesOf(final CheckoutSession session) {
    return session.lineItems().stream()
        .map(item -> new StockReservationPort.Line(item.productId(), item.quantity()))
        .toList();
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

  /** Finds the session as the caller's own: a session that is not theirs is not found. */
  private Optional<CheckoutSession> findSession(
      final CheckoutSessionId sessionId, final ConfirmCheckoutCommand command) {
    return checkoutSessionRepository.findByIdForCustomer(
        sessionId, CustomerId.of(command.customerId()));
  }

  /** What the payment provider made of the order's total. */
  private sealed interface Authorization permits Authorized, NotAuthorized {}

  /** The provider authorized the total. */
  private record Authorized(PaymentSelection payment) implements Authorization {}

  /** The provider was not asked or did not authorize; the refusal says which. */
  private record NotAuthorized(ConfirmCheckoutResult refusal) implements Authorization {}
}
