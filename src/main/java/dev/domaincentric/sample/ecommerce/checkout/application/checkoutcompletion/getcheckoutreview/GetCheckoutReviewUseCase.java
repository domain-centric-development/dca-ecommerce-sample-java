package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutArticleDataPort;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionNotFoundException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutArticlePriceResolver;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutReview;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.CheckoutPricing;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Use case for the review of a checkout at today's prices: the session's lines priced from the
 * article data as it is now, and the fingerprint "Place Order" carries of what the review shows.
 * Changes nothing.
 */
@Service
public class GetCheckoutReviewUseCase implements GetCheckoutReviewInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final CheckoutArticleDataPort checkoutArticleDataPort;
  private final CheckoutPricing checkoutPricing;

  public GetCheckoutReviewUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final CheckoutArticleDataPort checkoutArticleDataPort,
      final CheckoutPricing checkoutPricing) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.checkoutArticleDataPort = checkoutArticleDataPort;
    this.checkoutPricing = checkoutPricing;
  }

  /**
   * {@inheritDoc}
   *
   * @throws CheckoutSessionNotFoundException if the caller has no such checkout
   */
  @Override
  public GetCheckoutReviewResult execute(final GetCheckoutReviewQuery query) {
    final CheckoutSessionId sessionId = CheckoutSessionId.of(query.sessionId());
    final CheckoutSession session =
        checkoutSessionRepository
            .findByIdForCustomer(sessionId, CustomerId.of(query.customerId()))
            .orElseThrow(() -> new CheckoutSessionNotFoundException(sessionId));

    // Article data comes from the Product Catalog (remote-capable); nothing here is stored
    final Map<ProductId, CheckoutArticlePriceResolver.ArticlePrice> facts =
        CheckoutArticlePriceResolver.ArticlePrice.allOf(
            checkoutArticleDataPort.getArticleData(
                session.lineItems().stream().map(CheckoutLineItem::productId).toList()));

    final CheckoutReview review = session.reviewAt(checkoutPricing.todaysPrices(session, facts));
    return new GetCheckoutReviewResult(
        CheckoutCartSnapshot.from(session, review), review.fingerprint().value());
  }
}
