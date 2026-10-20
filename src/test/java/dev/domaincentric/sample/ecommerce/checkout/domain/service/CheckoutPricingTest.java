package dev.domaincentric.sample.ecommerce.checkout.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutArticlePriceResolver.ArticlePrice;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItemId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult.ErrorType;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckoutPricing: a refused line names its product")
class CheckoutPricingTest {

  private static final ProductId DESK_MODEL = ProductId.generate();
  private static final ProductId COMPASS = ProductId.generate();

  @Test
  @DisplayName("an unavailable and a short line each carry their product's name")
  void anUnavailableAndAShortLineEachCarryTheirProductsName() {
    final CheckoutSession session =
        CheckoutSession.start(
            CartId.generate(),
            CustomerId.of("customer-1"),
            List.of(
                line(DESK_MODEL, "Wooden Hexagon Desk Model", 5),
                line(COMPASS, "Brass Compass", 2)),
            Money.euro(70));
    final Map<ProductId, ArticlePrice> facts =
        Map.of(
            DESK_MODEL, new ArticlePrice(Money.euro(10), true, 3),
            COMPASS, new ArticlePrice(Money.euro(10), false, 0));

    final CheckoutValidationResult verdict = new CheckoutPricing().validateItems(session, facts);

    assertEquals(2, verdict.errors().size());
    assertEquals(ErrorType.INSUFFICIENT_STOCK, verdict.errors().get(0).type());
    assertEquals("Wooden Hexagon Desk Model", verdict.errors().get(0).productName());
    assertEquals(ErrorType.PRODUCT_UNAVAILABLE, verdict.errors().get(1).type());
    assertEquals("Brass Compass", verdict.errors().get(1).productName());
  }

  private static CheckoutLineItem line(
      final ProductId productId, final String name, final int quantity) {
    return CheckoutLineItem.of(
        CheckoutLineItemId.generate(), productId, name, Money.euro(10), quantity, null);
  }
}
