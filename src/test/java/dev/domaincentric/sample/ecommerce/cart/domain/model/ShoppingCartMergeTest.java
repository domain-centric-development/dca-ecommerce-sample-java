package dev.domaincentric.sample.ecommerce.cart.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ShoppingCart.merge() method.
 *
 * <p>Tests the domain logic for merging carts, covering:
 *
 * <ul>
 *   <li>Same product quantity combination
 *   <li>Different products addition
 *   <li>Empty cart handling
 * </ul>
 */
@DisplayName("ShoppingCart.merge()")
class ShoppingCartMergeTest {

  private static final Currency EUR = Currency.getInstance("EUR");
  private ShoppingCart targetCart;
  private ShoppingCart sourceCart;

  @BeforeEach
  void setUp() {
    targetCart = new ShoppingCart(CartId.generate(), CustomerId.of("target-customer"));
    sourceCart = new ShoppingCart(CartId.generate(), CustomerId.of("source-customer"));
  }

  @Nested
  @DisplayName("Same Product Quantity Combination")
  class SameProductQuantityCombination {

    @Test
    @DisplayName("merges same product by combining quantities")
    void mergesSameProductByCombiningQuantities() {
      ProductId productId = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      targetCart.addItem(productId, Quantity.of(2), price);
      sourceCart.addItem(productId, Quantity.of(3), price);

      int mergedCount = targetCart.merge(sourceCart);

      assertEquals(1, mergedCount);
      assertEquals(1, targetCart.itemCount());
      assertEquals(5, targetCart.totalQuantity());
    }

    @Test
    @DisplayName("keeps original price when merging same product")
    void keepsOriginalPriceWhenMergingSameProduct() {
      ProductId productId = ProductId.generate();
      Price originalPrice = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));
      Price sourcePrice = Price.of(Money.of(BigDecimal.valueOf(15.00), EUR));

      targetCart.addItem(productId, Quantity.of(1), originalPrice);
      sourceCart.addItem(productId, Quantity.of(1), sourcePrice);

      targetCart.merge(sourceCart);

      // Total should reflect original price * combined quantity
      // Original: 1 * 10 = 10, After merge: 2 items
      // The addItem method uses the existing item's price when product exists
      Money expectedTotal = Money.of(BigDecimal.valueOf(20.00), EUR);
      assertEquals(expectedTotal, targetCart.calculateTotal());
    }

    @Test
    @DisplayName("handles multiple same products")
    void handlesMultipleSameProducts() {
      ProductId product1 = ProductId.generate();
      ProductId product2 = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      targetCart.addItem(product1, Quantity.of(1), price);
      targetCart.addItem(product2, Quantity.of(2), price);
      sourceCart.addItem(product1, Quantity.of(2), price);
      sourceCart.addItem(product2, Quantity.of(3), price);

      int mergedCount = targetCart.merge(sourceCart);

      assertEquals(2, mergedCount);
      assertEquals(2, targetCart.itemCount());
      assertEquals(8, targetCart.totalQuantity()); // (1+2) + (2+3) = 8
    }
  }

  @Nested
  @DisplayName("Different Products Addition")
  class DifferentProductsAddition {

    @Test
    @DisplayName("adds new products from source cart")
    void addsNewProductsFromSourceCart() {
      ProductId targetProduct = ProductId.generate();
      ProductId sourceProduct = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      targetCart.addItem(targetProduct, Quantity.of(1), price);
      sourceCart.addItem(sourceProduct, Quantity.of(2), price);

      int mergedCount = targetCart.merge(sourceCart);

      assertEquals(1, mergedCount);
      assertEquals(2, targetCart.itemCount());
      assertTrue(targetCart.containsProduct(targetProduct));
      assertTrue(targetCart.containsProduct(sourceProduct));
    }

    @Test
    @DisplayName("preserves source item price for new products")
    void preservesSourceItemPriceForNewProducts() {
      ProductId targetProduct = ProductId.generate();
      ProductId sourceProduct = ProductId.generate();
      Price targetPrice = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));
      Price sourcePrice = Price.of(Money.of(BigDecimal.valueOf(25.00), EUR));

      targetCart.addItem(targetProduct, Quantity.of(1), targetPrice);
      sourceCart.addItem(sourceProduct, Quantity.of(2), sourcePrice);

      targetCart.merge(sourceCart);

      // Total should be: 1 * 10 + 2 * 25 = 60
      Money expectedTotal = Money.of(BigDecimal.valueOf(60.00), EUR);
      assertEquals(expectedTotal, targetCart.calculateTotal());
    }

    @Test
    @DisplayName("handles mix of same and different products")
    void handlesMixOfSameAndDifferentProducts() {
      ProductId commonProduct = ProductId.generate();
      ProductId targetOnlyProduct = ProductId.generate();
      ProductId sourceOnlyProduct = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      targetCart.addItem(commonProduct, Quantity.of(1), price);
      targetCart.addItem(targetOnlyProduct, Quantity.of(1), price);
      sourceCart.addItem(commonProduct, Quantity.of(2), price);
      sourceCart.addItem(sourceOnlyProduct, Quantity.of(3), price);

      int mergedCount = targetCart.merge(sourceCart);

      assertEquals(2, mergedCount);
      assertEquals(3, targetCart.itemCount());
      assertEquals(7, targetCart.totalQuantity()); // (1+2) + 1 + 3 = 7
    }
  }

  @Nested
  @DisplayName("Empty Cart Handling")
  class EmptyCartHandling {

    @Test
    @DisplayName("merging empty source cart leaves target unchanged")
    void mergingEmptySourceCartLeavesTargetUnchanged() {
      ProductId productId = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      targetCart.addItem(productId, Quantity.of(2), price);
      // sourceCart is empty

      int mergedCount = targetCart.merge(sourceCart);

      assertEquals(0, mergedCount);
      assertEquals(1, targetCart.itemCount());
      assertEquals(2, targetCart.totalQuantity());
    }

    @Test
    @DisplayName("merging into empty target cart adds all source items")
    void mergingIntoEmptyTargetCartAddsAllSourceItems() {
      ProductId product1 = ProductId.generate();
      ProductId product2 = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      // targetCart is empty
      sourceCart.addItem(product1, Quantity.of(1), price);
      sourceCart.addItem(product2, Quantity.of(2), price);

      int mergedCount = targetCart.merge(sourceCart);

      assertEquals(2, mergedCount);
      assertEquals(2, targetCart.itemCount());
      assertEquals(3, targetCart.totalQuantity());
    }

    @Test
    @DisplayName("merging two empty carts results in empty target")
    void mergingTwoEmptyCartsResultsInEmptyTarget() {
      // both carts are empty

      int mergedCount = targetCart.merge(sourceCart);

      assertEquals(0, mergedCount);
      assertTrue(targetCart.isEmpty());
    }
  }

  @Nested
  @DisplayName("Error Handling")
  class ErrorHandling {

    @Test
    @DisplayName("throws exception when target cart is not active")
    void throwsExceptionWhenTargetCartIsNotActive() {
      ProductId productId = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      targetCart.addItem(productId, Quantity.of(1), price);
      targetCart.abandon();

      sourceCart.addItem(productId, Quantity.of(1), price);

      assertThrows(IllegalStateException.class, () -> targetCart.merge(sourceCart));
    }

    @Test
    @DisplayName("source cart is not modified after merge")
    void sourceCartIsNotModifiedAfterMerge() {
      ProductId productId = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      sourceCart.addItem(productId, Quantity.of(3), price);
      int originalSourceCount = sourceCart.itemCount();
      int originalSourceQuantity = sourceCart.totalQuantity();

      targetCart.merge(sourceCart);

      assertEquals(originalSourceCount, sourceCart.itemCount());
      assertEquals(originalSourceQuantity, sourceCart.totalQuantity());
    }
  }

  @Nested
  @DisplayName("Domain Events")
  class DomainEvents {

    @Test
    @DisplayName("raises CartItemAddedToCart events for merged items")
    void raisesEventsForMergedItems() {
      ProductId product1 = ProductId.generate();
      ProductId product2 = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      sourceCart.addItem(product1, Quantity.of(1), price);
      sourceCart.addItem(product2, Quantity.of(2), price);

      // Clear any existing events from setup
      targetCart.clearDomainEvents();

      targetCart.merge(sourceCart);

      // Should have 2 events (one for each merged item)
      assertEquals(2, targetCart.domainEvents().size());
    }
  }
}
