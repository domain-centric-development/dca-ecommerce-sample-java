package dev.domaincentric.sample.ecommerce.cart.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EnrichedCartItem price change")
class EnrichedCartItemTest {

  private static EnrichedCartItem itemAddedAtTenNowAt(final double currentPrice) {
    final ProductId productId = ProductId.generate();
    return new EnrichedCartItem(
        CartItemId.generate(),
        productId,
        Quantity.of(1),
        Price.of(Money.euro(10.0)),
        CartArticle.of(productId, "Article", Money.euro(currentPrice), 5, true, ""));
  }

  @Test
  @DisplayName("a higher current price is an increase with an absolute difference")
  void priceIncrease() {
    final EnrichedCartItem item = itemAddedAtTenNowAt(12.0);

    assertTrue(item.hasPriceChanged());
    assertTrue(item.priceIncreased());
    assertEquals(Money.euro(2.0), item.priceDifference());
  }

  @Test
  @DisplayName("a lower current price is a change but not an increase")
  void priceDrop() {
    final EnrichedCartItem item = itemAddedAtTenNowAt(8.0);

    assertTrue(item.hasPriceChanged());
    assertFalse(item.priceIncreased());
    assertEquals(Money.euro(2.0), item.priceDifference());
  }

  @Test
  @DisplayName("an unchanged price is neither")
  void unchanged() {
    final EnrichedCartItem item = itemAddedAtTenNowAt(10.0);

    assertFalse(item.hasPriceChanged());
    assertFalse(item.priceIncreased());
  }
}
