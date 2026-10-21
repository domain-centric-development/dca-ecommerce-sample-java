package dev.domaincentric.sample.ecommerce.checkout;

import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview.GetCheckoutReviewInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview.GetCheckoutReviewQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview.GetCheckoutReviewResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.BuyerInfo;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItemId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.DeliveryAddress;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentSelection;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * A review at today's prices that the test arranges: a checkout of "Domain-Driven Design" once, at
 * the price and with the shipping method the test says are today's. It records what it was asked
 * and answers a fingerprint of the two, so a review that changed carries another one.
 */
final class ArrangedReview implements GetCheckoutReviewInputPort {

  static final ShippingOption STANDARD_SHIPPING =
      ShippingOption.of("standard", "Standard Shipping", "5-7 business days", Money.euro(4.99));

  private final List<GetCheckoutReviewQuery> queries = new CopyOnWriteArrayList<>();
  private volatile Money price = Money.euro(54.99);
  private volatile ShippingOption shipping = STANDARD_SHIPPING;

  @Override
  public GetCheckoutReviewResult execute(final GetCheckoutReviewQuery query) {
    queries.add(query);
    final CheckoutSession session =
        CheckoutSession.start(
            CartId.generate(),
            CustomerId.of(query.customerId()),
            List.of(
                CheckoutLineItem.of(
                    CheckoutLineItemId.generate(),
                    ProductId.generate(),
                    "Domain-Driven Design",
                    price,
                    1,
                    null)),
            price);
    session.submitBuyerInfo(BuyerInfo.of("jane@example.com", "Jane", "Doe", "+49 30 1234567"));
    session.submitDelivery(
        DeliveryAddress.of("Hauptstrasse 1", "Berlin", "10115", "Germany"), shipping);
    session.submitPayment(PaymentSelection.of(PaymentProviderId.of("mock")));
    return new GetCheckoutReviewResult(CheckoutCartSnapshot.from(session), fingerprint());
  }

  /** The fingerprint the review answers today. */
  String fingerprint() {
    return "arranged-" + price.amount().toPlainString() + "-" + shipping.id();
  }

  void priceToday(final Money today) {
    price = today;
  }

  void shippingToday(final ShippingOption today) {
    shipping = today;
  }

  List<GetCheckoutReviewQuery> queries() {
    return List.copyOf(queries);
  }

  void reset() {
    queries.clear();
    price = Money.euro(54.99);
    shipping = STANDARD_SHIPPING;
  }

  /** Puts the arranged review in place of the review use case. */
  @TestConfiguration
  static class Configuration {

    @Bean
    @Primary
    ArrangedReview arrangedReview() {
      return new ArrangedReview();
    }
  }
}
