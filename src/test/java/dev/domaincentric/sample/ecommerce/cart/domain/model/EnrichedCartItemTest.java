package dev.domaincentric.sample.ecommerce.cart.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

  @Test
  @DisplayName("a drop from 54.99 to 49.99 differs by 5.00")
  void theDifferenceOfADropIsTheAmountItFellBy() {
    final ProductId productId = ProductId.generate();
    final EnrichedCartItem item =
        new EnrichedCartItem(
            CartItemId.generate(),
            productId,
            Quantity.of(1),
            Price.of(Money.euro(54.99)),
            CartArticle.of(productId, "Article", Money.euro(49.99), 5, true, ""));

    assertEquals(Money.euro(5.00), item.priceDifference());
  }

  @Test
  @DisplayName("an item without its id, product, quantity, price at addition or article is refused")
  void anItemMissingARequiredPartIsRefused() {
    final ProductId productId = ProductId.generate();
    final CartItemId id = CartItemId.generate();
    final Quantity one = Quantity.of(1);
    final Price price = Price.of(Money.euro(10.0));
    final CartArticle article = CartArticle.of(productId, "Article", Money.euro(10.0), 5, true, "");

    assertThrows(
        IllegalArgumentException.class,
        () -> new EnrichedCartItem(null, productId, one, price, article, "snapshot"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new EnrichedCartItem(id, null, one, price, article, "snapshot"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new EnrichedCartItem(id, productId, null, price, article, "snapshot"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new EnrichedCartItem(id, productId, one, null, article, "snapshot"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new EnrichedCartItem(id, productId, one, price, null, "snapshot"));
  }

  @Test
  @DisplayName("an article of another product is refused")
  void anArticleOfAnotherProductIsRefused() {
    final CartArticle another =
        CartArticle.of(ProductId.generate(), "Article", Money.euro(10.0), 5, true, "");

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new EnrichedCartItem(
                CartItemId.generate(),
                ProductId.generate(),
                Quantity.of(1),
                Price.of(Money.euro(10.0)),
                another,
                "snapshot"));
  }
}
