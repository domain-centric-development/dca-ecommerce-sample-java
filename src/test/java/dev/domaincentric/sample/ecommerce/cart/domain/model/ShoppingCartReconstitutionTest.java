package dev.domaincentric.sample.ecommerce.cart.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart.StoredItem;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ShoppingCart#reconstitute}.
 *
 * <p>The persistence adapters have no tests of their own, so this pins the contract they depend on:
 * a stored cart comes back with its status and lines, carries no pending events, and behaves like a
 * cart afterwards — the last part is what proves the lines landed in the aggregate's own list
 * rather than in a copy.
 */
@DisplayName("ShoppingCart reconstitution")
class ShoppingCartReconstitutionTest {

  private static final CartId CART_ID = CartId.of("cart-1");
  private static final CustomerId CUSTOMER_ID = CustomerId.of("customer-1");
  private static final ProductId PRODUCT_ID = ProductId.of("product-1");
  private static final Price PRICE = Price.of(Money.of(new BigDecimal("19.99"), euro()));

  private static Currency euro() {
    return Currency.getInstance("EUR");
  }

  private static StoredItem storedItem(
      final String itemId, final ProductId productId, final int quantity) {
    return new StoredItem(CartItemId.of(itemId), productId, Quantity.of(quantity), PRICE);
  }

  private static ShoppingCart reconstituted(final CartStatus status, final List<StoredItem> items) {
    return ShoppingCart.reconstitute(CART_ID, CUSTOMER_ID, status, items);
  }

  @Test
  @DisplayName("restores identity, customer and status")
  void restoresIdentityAndStatus() {
    final ShoppingCart cart = reconstituted(CartStatus.COMPLETED, List.of());

    assertEquals(CART_ID, cart.id());
    assertEquals(CUSTOMER_ID, cart.customerId());
    assertEquals(
        CartStatus.COMPLETED,
        cart.status(),
        "a stored status must survive, not fall back to ACTIVE");
  }

  @Test
  @DisplayName("restores the stored lines in order")
  void restoresStoredLines() {
    final ShoppingCart cart =
        reconstituted(
            CartStatus.ACTIVE,
            List.of(
                storedItem("item-1", PRODUCT_ID, 2),
                storedItem("item-2", ProductId.of("product-2"), 5)));

    assertEquals(
        List.of(CartItemId.of("item-1"), CartItemId.of("item-2")),
        cart.items().stream().map(CartItem::id).toList());
    assertEquals(2, cart.items().getFirst().quantity().value());
    assertEquals(PRICE, cart.items().getFirst().priceAtAddition());
  }

  @Test
  @DisplayName("raises no domain event, because restoring is not a decision")
  void raisesNoEvents() {
    final ShoppingCart cart =
        reconstituted(CartStatus.ACTIVE, List.of(storedItem("item-1", PRODUCT_ID, 2)));

    assertTrue(
        cart.domainEvents().isEmpty(), "a loaded cart must not look like it just had items added");
  }

  @Test
  @DisplayName("a restored line is a real line: adding the same product merges into it")
  void restoredLineParticipatesInAggregateRules() {
    final ShoppingCart cart =
        reconstituted(CartStatus.ACTIVE, List.of(storedItem("item-1", PRODUCT_ID, 2)));

    cart.addItem(PRODUCT_ID, Quantity.of(3), PRICE);

    assertEquals(
        1,
        cart.items().size(),
        "the product was already in the cart, so no second line may appear");
    assertEquals(5, cart.items().getFirst().quantity().value());
  }

  @Test
  @DisplayName("a restored completed cart still refuses changes")
  void restoredCompletedCartRefusesChanges() {
    final ShoppingCart cart = reconstituted(CartStatus.COMPLETED, List.of());

    assertThrows(
        CartNotModifiableException.class, () -> cart.addItem(PRODUCT_ID, Quantity.of(1), PRICE));
  }

  @Test
  @DisplayName("the returned line list stays unmodifiable")
  void restoredItemsAreUnmodifiable() {
    final ShoppingCart cart =
        reconstituted(CartStatus.ACTIVE, List.of(storedItem("item-1", PRODUCT_ID, 2)));

    assertThrows(UnsupportedOperationException.class, () -> cart.items().clear());
  }
}
