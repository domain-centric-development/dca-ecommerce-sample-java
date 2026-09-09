package dev.domaincentric.sample.ecommerce.cart.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ShoppingCart resolver-based methods.
 *
 * <p>Tests the domain logic for calculating totals and validating carts using the
 * ArticlePriceResolver for fresh pricing data.
 */
@DisplayName("ShoppingCart Resolver Methods")
class ShoppingCartResolverTest {

  private static final Currency EUR = Currency.getInstance("EUR");
  private ShoppingCart cart;
  private TestArticlePriceResolver priceResolver;

  private java.util.Map<
          dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId, ArticlePrice>
      facts() {
    return cart.items().stream()
        .collect(
            java.util.stream.Collectors.toMap(
                CartItem::productId, item -> priceResolver.resolve(item.productId())));
  }

  @BeforeEach
  void setUp() {
    cart = new ShoppingCart(CartId.generate(), CustomerId.of("test-customer"));
    priceResolver = new TestArticlePriceResolver();
  }

  @Nested
  @DisplayName("calculateTotal(ArticlePriceResolver)")
  class CalculateTotalWithResolver {

    @Test
    @DisplayName("returns zero for empty cart")
    void returnsZeroForEmptyCart() {
      Money total = cart.calculateTotal(facts());

      assertEquals(Money.euro(0.0), total);
    }

    @Test
    @DisplayName("calculates total using resolver prices")
    void calculatesTotalUsingResolverPrices() {
      ProductId product1 = ProductId.generate();
      ProductId product2 = ProductId.generate();
      Price addedPrice = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      // Add items with one price
      cart.addItem(product1, Quantity.of(2), addedPrice);
      cart.addItem(product2, Quantity.of(3), addedPrice);

      // Configure resolver with different prices
      priceResolver.setPrice(product1, Money.of(BigDecimal.valueOf(15.00), EUR), true, 100);
      priceResolver.setPrice(product2, Money.of(BigDecimal.valueOf(25.00), EUR), true, 100);

      Money total = cart.calculateTotal(facts());

      // Expected: 2 * 15 + 3 * 25 = 30 + 75 = 105
      assertEquals(Money.of(BigDecimal.valueOf(105.00), EUR), total);
    }

    @Test
    @DisplayName("throws exception when resolver is null")
    void throwsExceptionWhenResolverIsNull() {
      assertThrows(IllegalArgumentException.class, () -> cart.calculateTotal(null));
    }

    @Test
    @DisplayName("uses current prices, not prices at addition time")
    void usesCurrentPricesNotPricesAtAdditionTime() {
      ProductId productId = ProductId.generate();
      Price originalPrice = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      cart.addItem(productId, Quantity.of(2), originalPrice);

      // Resolver returns different price
      priceResolver.setPrice(productId, Money.of(BigDecimal.valueOf(50.00), EUR), true, 100);

      Money total = cart.calculateTotal(facts());

      // Should use resolver price (50), not original price (10)
      assertEquals(Money.of(BigDecimal.valueOf(100.00), EUR), total);
    }
  }

  @Nested
  @DisplayName("validateForCheckout(ArticlePriceResolver)")
  class ValidateForCheckout {

    @Test
    @DisplayName("returns valid for empty cart")
    void returnsValidForEmptyCart() {
      CartValidationResult outcome = cart.validateForCheckout(facts());

      assertTrue(outcome.isValid());
      assertTrue(outcome.errors().isEmpty());
    }

    @Test
    @DisplayName("returns valid when all products available with sufficient stock")
    void returnsValidWhenAllProductsAvailableWithSufficientStock() {
      ProductId product1 = ProductId.generate();
      ProductId product2 = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      cart.addItem(product1, Quantity.of(2), price);
      cart.addItem(product2, Quantity.of(3), price);

      priceResolver.setPrice(product1, Money.of(BigDecimal.valueOf(10.00), EUR), true, 10);
      priceResolver.setPrice(product2, Money.of(BigDecimal.valueOf(10.00), EUR), true, 10);

      CartValidationResult outcome = cart.validateForCheckout(facts());

      assertTrue(outcome.isValid());
    }

    @Test
    @DisplayName("returns error for unavailable product")
    void returnsErrorForUnavailableProduct() {
      ProductId productId = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      cart.addItem(productId, Quantity.of(1), price);
      priceResolver.setPrice(productId, Money.of(BigDecimal.valueOf(10.00), EUR), false, 0);

      CartValidationResult outcome = cart.validateForCheckout(facts());

      assertFalse(outcome.isValid());
      assertEquals(1, outcome.errors().size());
      assertEquals(
          CartValidationResult.ErrorType.PRODUCT_UNAVAILABLE, outcome.errors().get(0).type());
      assertEquals(productId, outcome.errors().get(0).productId());
    }

    @Test
    @DisplayName("returns error for insufficient stock")
    void returnsErrorForInsufficientStock() {
      ProductId productId = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      cart.addItem(productId, Quantity.of(5), price);
      priceResolver.setPrice(productId, Money.of(BigDecimal.valueOf(10.00), EUR), true, 3);

      CartValidationResult outcome = cart.validateForCheckout(facts());

      assertFalse(outcome.isValid());
      assertEquals(1, outcome.errors().size());
      assertEquals(
          CartValidationResult.ErrorType.INSUFFICIENT_STOCK, outcome.errors().get(0).type());
      assertEquals(productId, outcome.errors().get(0).productId());
    }

    @Test
    @DisplayName("returns multiple errors for multiple invalid products")
    void returnsMultipleErrorsForMultipleInvalidProducts() {
      ProductId unavailableProduct = ProductId.generate();
      ProductId lowStockProduct = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      cart.addItem(unavailableProduct, Quantity.of(1), price);
      cart.addItem(lowStockProduct, Quantity.of(10), price);

      priceResolver.setPrice(
          unavailableProduct, Money.of(BigDecimal.valueOf(10.00), EUR), false, 0);
      priceResolver.setPrice(lowStockProduct, Money.of(BigDecimal.valueOf(10.00), EUR), true, 5);

      CartValidationResult outcome = cart.validateForCheckout(facts());

      assertFalse(outcome.isValid());
      assertEquals(2, outcome.errors().size());
    }

    @Test
    @DisplayName("throws exception when resolver is null")
    void throwsExceptionWhenResolverIsNull() {
      assertThrows(IllegalArgumentException.class, () -> cart.validateForCheckout(null));
    }

    @Test
    @DisplayName("valid when stock exactly matches requested quantity")
    void validWhenStockExactlyMatchesRequestedQuantity() {
      ProductId productId = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      cart.addItem(productId, Quantity.of(5), price);
      priceResolver.setPrice(productId, Money.of(BigDecimal.valueOf(10.00), EUR), true, 5);

      CartValidationResult outcome = cart.validateForCheckout(facts());

      assertTrue(outcome.isValid());
    }
  }

  @Nested
  @DisplayName("CartValidationResult")
  class CartValidationResultTests {

    @Test
    @DisplayName("valid() creates outcome with no errors")
    void validCreatesOutcomeWithNoErrors() {
      CartValidationResult outcome = CartValidationResult.valid();

      assertTrue(outcome.isValid());
      assertTrue(outcome.errors().isEmpty());
    }

    @Test
    @DisplayName("errors list is immutable")
    void errorsListIsImmutable() {
      ProductId productId = ProductId.generate();
      CartValidationResult.ValidationError error =
          CartValidationResult.ValidationError.productUnavailable(productId);
      CartValidationResult outcome = CartValidationResult.withErrors(java.util.List.of(error));

      assertThrows(UnsupportedOperationException.class, () -> outcome.errors().clear());
    }
  }

  /** Test implementation of ArticlePriceResolver for unit testing. */
  private static class TestArticlePriceResolver implements ArticlePriceResolver {
    private final Map<ProductId, ArticlePrice> prices = new HashMap<>();

    void setPrice(ProductId productId, Money price, boolean available, int stock) {
      prices.put(productId, new ArticlePrice(price, available, stock));
    }

    @Override
    public ArticlePrice resolve(ProductId productId) {
      return prices.getOrDefault(productId, new ArticlePrice(Money.euro(0.0), true, 100));
    }
  }
}
