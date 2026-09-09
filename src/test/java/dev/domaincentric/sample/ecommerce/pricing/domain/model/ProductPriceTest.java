package dev.domaincentric.sample.ecommerce.pricing.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.sample.ecommerce.pricing.domain.event.PriceChanged;
import dev.domaincentric.sample.ecommerce.pricing.domain.event.PriceCreated;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ProductPrice aggregate.
 *
 * <p>Tests the domain logic for product pricing, covering:
 *
 * <ul>
 *   <li>Creation with valid price
 *   <li>Price update behavior
 *   <li>Validation rules
 *   <li>Domain events
 * </ul>
 */
@DisplayName("ProductPrice")
class ProductPriceTest {

  private static final Currency EUR = Currency.getInstance("EUR");

  @Nested
  @DisplayName("create()")
  class CreateTests {

    @Test
    @DisplayName("creates ProductPrice with valid price")
    void createsProductPriceWithValidPrice() {
      ProductId productId = ProductId.generate();
      Money price = Money.of(BigDecimal.valueOf(19.99), EUR);

      ProductPrice productPrice =
          ProductPrice.create(
              productId,
              dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(price));

      assertNotNull(productPrice.id());
      assertEquals(productId, productPrice.productId());
      assertEquals(price, productPrice.currentPrice().value());
      assertNotNull(productPrice.effectiveFrom());
    }

    @Test
    @DisplayName("throws exception when price is zero")
    void throwsExceptionWhenPriceIsZero() {
      ProductId productId = ProductId.generate();
      Money zeroPrice = Money.of(BigDecimal.ZERO, EUR);

      assertThrows(
          IllegalArgumentException.class,
          () ->
              ProductPrice.create(
                  productId,
                  dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(
                      zeroPrice)));
    }

    @Test
    @DisplayName("throws exception when price is negative")
    void throwsExceptionWhenPriceIsNegative() {
      ProductId productId = ProductId.generate();

      // Money itself rejects negative values
      assertThrows(IllegalArgumentException.class, () -> Money.of(BigDecimal.valueOf(-10.00), EUR));
    }

    @Test
    @DisplayName("raises PriceCreated event on creation")
    void raisesPriceCreatedEventOnCreation() {
      ProductId productId = ProductId.generate();
      Money price = Money.of(BigDecimal.valueOf(19.99), EUR);

      ProductPrice productPrice =
          ProductPrice.create(
              productId,
              dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(price));

      assertEquals(1, productPrice.domainEvents().size());
      assertInstanceOf(PriceCreated.class, productPrice.domainEvents().get(0));

      PriceCreated event = (PriceCreated) productPrice.domainEvents().get(0);
      assertEquals(productPrice.id(), event.priceId());
      assertEquals(productId, event.productId());
      assertEquals(price, event.price().value());
      assertEquals(productPrice.effectiveFrom(), event.effectiveFrom());
    }
  }

  @Nested
  @DisplayName("updatePrice()")
  class UpdatePriceTests {

    @Test
    @DisplayName("updates price to new value")
    void updatesPriceToNewValue() {
      ProductId productId = ProductId.generate();
      Money initialPrice = Money.of(BigDecimal.valueOf(10.00), EUR);
      Money newPrice = Money.of(BigDecimal.valueOf(15.00), EUR);

      ProductPrice productPrice =
          ProductPrice.create(
              productId,
              dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(initialPrice));
      productPrice.updatePrice(
          dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(newPrice));

      assertEquals(newPrice, productPrice.currentPrice().value());
    }

    @Test
    @DisplayName("updates effectiveFrom timestamp on price change")
    void updatesEffectiveFromTimestampOnPriceChange() throws InterruptedException {
      ProductId productId = ProductId.generate();
      Money initialPrice = Money.of(BigDecimal.valueOf(10.00), EUR);
      Money newPrice = Money.of(BigDecimal.valueOf(15.00), EUR);

      ProductPrice productPrice =
          ProductPrice.create(
              productId,
              dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(initialPrice));
      var originalEffectiveFrom = productPrice.effectiveFrom();

      Thread.sleep(10); // Small delay to ensure timestamp difference
      productPrice.updatePrice(
          dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(newPrice));

      assertTrue(productPrice.effectiveFrom().isAfter(originalEffectiveFrom));
    }

    @Test
    @DisplayName("throws exception when updating to zero price")
    void throwsExceptionWhenUpdatingToZeroPrice() {
      ProductId productId = ProductId.generate();
      Money initialPrice = Money.of(BigDecimal.valueOf(10.00), EUR);
      Money zeroPrice = Money.of(BigDecimal.ZERO, EUR);

      ProductPrice productPrice =
          ProductPrice.create(
              productId,
              dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(initialPrice));

      assertThrows(
          IllegalArgumentException.class,
          () ->
              productPrice.updatePrice(
                  dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(
                      zeroPrice)));
    }

    @Test
    @DisplayName("raises PriceChanged event on update")
    void raisesPriceChangedEventOnUpdate() {
      ProductId productId = ProductId.generate();
      Money initialPrice = Money.of(BigDecimal.valueOf(10.00), EUR);
      Money newPrice = Money.of(BigDecimal.valueOf(15.00), EUR);

      ProductPrice productPrice =
          ProductPrice.create(
              productId,
              dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(initialPrice));
      productPrice.clearDomainEvents();

      productPrice.updatePrice(
          dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(newPrice));

      assertEquals(1, productPrice.domainEvents().size());
      assertInstanceOf(PriceChanged.class, productPrice.domainEvents().get(0));

      PriceChanged event = (PriceChanged) productPrice.domainEvents().get(0);
      assertEquals(productPrice.id(), event.priceId());
      assertEquals(productId, event.productId());
      assertEquals(initialPrice, event.oldPrice().value());
      assertEquals(newPrice, event.newPrice().value());
    }
  }
}
