package dev.domaincentric.sample.ecommerce.checkout.application.confirmcheckout;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutArticleDataPort;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutArticle;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutArticlePriceResolver;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Use case for confirming a checkout session.
 *
 * <p>This use case handles the confirmation step by:
 *
 * <ul>
 *   <li>Loading and validating the checkout session
 *   <li>Fetching fresh article data (pricing, availability) via CheckoutArticleDataPort
 *   <li>Building a resolver for current pricing validation
 *   <li>Calling the domain method to confirm with validation
 *   <li>Persisting the updated session
 *   <li>Publishing domain events — triggers cart completion and stock reduction in separate
 *       transactions via Interface Inversion pattern
 * </ul>
 *
 * <p><b>One Aggregate Per Transaction:</b> This use case only modifies the {@code CheckoutSession}
 * aggregate. Cart completion and stock reduction happen in separate transactions via event
 * listeners (Interface Inversion pattern), respecting the DDD rule of one aggregate per
 * transaction.
 */
@Service
public class ConfirmCheckoutUseCase implements ConfirmCheckoutInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final CheckoutArticleDataPort checkoutArticleDataPort;
  private final DomainEventPublisher domainEventPublisher;
  private final TransactionBoundary transactionBoundary;

  public ConfirmCheckoutUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final CheckoutArticleDataPort checkoutArticleDataPort,
      final DomainEventPublisher domainEventPublisher,
      final TransactionBoundary transactionBoundary) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.checkoutArticleDataPort = checkoutArticleDataPort;
    this.domainEventPublisher = domainEventPublisher;
    this.transactionBoundary = transactionBoundary;
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
    final CheckoutArticlePriceResolver resolver =
        productId -> {
          final CheckoutArticle article = articleDataMap.get(productId);
          if (article == null) {
            throw new IllegalArgumentException("Article data not found for: " + productId.value());
          }
          return new CheckoutArticlePriceResolver.ArticlePrice(
              article.currentPrice(), article.isAvailable(), article.availableStock());
        };

    // Short transaction: reload, confirm, save, publish
    return transactionBoundary.inTransaction(
        () -> {
          final CheckoutSession session = loadSession(sessionId, command);
          session.confirm(resolver);
          checkoutSessionRepository.save(session);
          domainEventPublisher.publishAndClearEvents(session);
          return mapToResponse(session);
        });
  }

  private CheckoutSession loadSession(
      final CheckoutSessionId sessionId, final ConfirmCheckoutCommand command) {
    return checkoutSessionRepository
        .findById(sessionId)
        .orElseThrow(
            () -> new IllegalArgumentException("Session not found: " + command.sessionId()));
  }

  private ConfirmCheckoutResult mapToResponse(final CheckoutSession session) {
    return new ConfirmCheckoutResult(
        session.id().value().toString(),
        session.currentStep().name(),
        session.status().name(),
        session.cartId().value().toString(),
        session.customerId().value(),
        session.totals().total().amount().toPlainString(),
        session.totals().total().currency().getCurrencyCode(),
        session.orderReference());
  }
}
