package dev.domaincentric.sample.ecommerce.inventory.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What the stock keeping unit refuses, and how it says so. A quantity the warehouse does not have
 * is a business answer with a name; a negative quantity is a malformed call and stays an argument
 * guard, so a caller can tell the two apart without reading a message.
 */
@DisplayName("StockLevel refusals")
class StockLevelRefusalTest {

  private static final ProductId PRODUCT_ID = ProductId.of("product-1");

  @Test
  @DisplayName("Decreasing by more than is held names the quantity that was there")
  void decreasingByMoreThanIsHeldNamesTheQuantityThatWasThere() {
    final StockLevel stockLevel = StockLevel.create(PRODUCT_ID, 4);

    final InsufficientStockException refused =
        assertThrows(InsufficientStockException.class, () -> stockLevel.decreaseStock(5));

    assertEquals(PRODUCT_ID, refused.productId());
    assertEquals(5, refused.requested());
    assertEquals(4, refused.available());
  }

  @Test
  @DisplayName("Reserving beyond the unreserved part names what was still promisable")
  void reservingBeyondTheUnreservedPartNamesWhatWasStillPromisable() {
    final StockLevel stockLevel = StockLevel.create(PRODUCT_ID, 4);
    stockLevel.reserve(3);

    final InsufficientUnreservedStockException refused =
        assertThrows(InsufficientUnreservedStockException.class, () -> stockLevel.reserve(2));

    assertEquals(2, refused.requested());
    assertEquals(1, refused.unreserved());
  }

  @Test
  @DisplayName("Releasing more than was reserved names the reservation")
  void releasingMoreThanWasReservedNamesTheReservation() {
    final StockLevel stockLevel = StockLevel.create(PRODUCT_ID, 4);
    stockLevel.reserve(1);

    final InsufficientReservedStockException refused =
        assertThrows(InsufficientReservedStockException.class, () -> stockLevel.release(2));

    assertEquals(2, refused.requested());
    assertEquals(1, refused.reserved());
  }

  @Test
  @DisplayName("A negative quantity stays a malformed call, not a business answer")
  void aNegativeQuantityStaysAMalformedCall() {
    final StockLevel stockLevel = StockLevel.create(PRODUCT_ID, 4);

    assertThrows(IllegalArgumentException.class, () -> stockLevel.decreaseStock(-1));
    assertThrows(IllegalArgumentException.class, () -> stockLevel.reserve(-1));
    assertThrows(IllegalArgumentException.class, () -> stockLevel.release(-1));
  }
}
