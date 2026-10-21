package dev.domaincentric.sample.ecommerce.cart.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EnrichedCart price changes")
class EnrichedCartPriceChangeTest {

  private static EnrichedCartItem itemAddedAtTenNowAt(final double currentPrice) {
    final ProductId productId = ProductId.generate();
    return new EnrichedCartItem(
        CartItemId.generate(),
        productId,
        Quantity.of(1),
        Price.of(Money.euro(10.0)),
        CartArticle.of(productId, "Article", Money.euro(currentPrice), 5, true, ""));
  }

  private static EnrichedCart cartOf(final EnrichedCartItem... items) {
    return EnrichedCart.of(
        CartId.generate(), CustomerId.of("customer-1"), List.of(items), CartStatus.ACTIVE);
  }

  @Test
  @DisplayName("one changed line among unchanged ones is a price change of the cart")
  void oneChangedLineIsAPriceChangeOfTheCart() {
    assertTrue(cartOf(itemAddedAtTenNowAt(10.0), itemAddedAtTenNowAt(8.0)).hasAnyPriceChanges());
  }

  @Test
  @DisplayName("a cart whose lines all keep their price has no price change")
  void noChangedLineIsNoPriceChangeOfTheCart() {
    assertFalse(cartOf(itemAddedAtTenNowAt(10.0), itemAddedAtTenNowAt(10.0)).hasAnyPriceChanges());
  }
}
