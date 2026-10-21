package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for the review of a checkout at today's prices. A read: opening the review changes
 * nothing.
 *
 * @see GetCheckoutReviewUseCase
 */
public interface GetCheckoutReviewInputPort
    extends UseCase<GetCheckoutReviewQuery, GetCheckoutReviewResult> {

  /**
   * The review of the caller's checkout at today's prices.
   *
   * @param query the checkout and the customer the caller is acting as
   * @return the review and its fingerprint
   */
  @Override
  GetCheckoutReviewResult execute(GetCheckoutReviewQuery query);
}
