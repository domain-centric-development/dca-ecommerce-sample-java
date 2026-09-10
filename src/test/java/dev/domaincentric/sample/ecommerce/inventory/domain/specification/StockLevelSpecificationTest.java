package dev.domaincentric.sample.ecommerce.inventory.domain.specification;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockQuantity;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The rule behind the operator's overview: stock is short only while the quantity held stays under
 * the threshold. The boundary itself is not short, and reservations do not enter the comparison.
 */
@DisplayName("AvailableQuantityBelow")
class StockLevelSpecificationTest {

  @Test
  @DisplayName("Holds while less is held than the threshold")
  void holdsWhileLessIsHeldThanTheThreshold() {
    assertTrue(new AvailableQuantityBelow(StockQuantity.of(5)).isSatisfiedBy(stockLevelOf(4)));
  }

  @Test
  @DisplayName("Does not hold once the threshold is reached")
  void doesNotHoldOnceTheThresholdIsReached() {
    final StockLevel stockLevel = stockLevelOf(4);

    assertFalse(new AvailableQuantityBelow(StockQuantity.of(4)).isSatisfiedBy(stockLevel));
    assertFalse(new AvailableQuantityBelow(StockQuantity.of(3)).isSatisfiedBy(stockLevel));
  }

  @Test
  @DisplayName("Compares the quantity held, regardless of reservations")
  void comparesTheQuantityHeldRegardlessOfReservations() {
    final StockLevel stockLevel = stockLevelOf(4);
    stockLevel.reserve(4);

    assertTrue(new AvailableQuantityBelow(StockQuantity.of(5)).isSatisfiedBy(stockLevel));
    assertFalse(new AvailableQuantityBelow(StockQuantity.of(4)).isSatisfiedBy(stockLevel));
  }

  private static StockLevel stockLevelOf(final int availableQuantity) {
    return StockLevel.create(ProductId.generate(), availableQuantity);
  }
}
