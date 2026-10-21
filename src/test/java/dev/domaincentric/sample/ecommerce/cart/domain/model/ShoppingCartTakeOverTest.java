package dev.domaincentric.sample.ecommerce.cart.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ShoppingCart: an account takes the guest cart over")
class ShoppingCartTakeOverTest {

  private static final Currency EUR = Currency.getInstance("EUR");
  private static final CustomerId GUEST = CustomerId.of("guest-1");
  private static final CustomerId ACCOUNT = CustomerId.of("account-1");

  @Test
  @DisplayName("keeps the cart's id and its positions")
  void keepsTheCartsIdAndItsPositions() {
    final ShoppingCart cart = aGuestCart();
    final CartId id = cart.id();
    final List<CartItem> positions = List.copyOf(cart.items());

    cart.takeOverBy(ACCOUNT);

    assertEquals(id, cart.id());
    assertEquals(positions, cart.items());
    assertEquals(3, cart.totalQuantity());
  }

  @Test
  @DisplayName("the cart belongs to the account afterwards")
  void theCartBelongsToTheAccountAfterwards() {
    final ShoppingCart cart = aGuestCart();

    cart.takeOverBy(ACCOUNT);

    assertEquals(ACCOUNT, cart.customerId());
  }

  @Test
  @DisplayName("refuses a cart that is no longer active")
  void refusesACartThatIsNoLongerActive() {
    final ShoppingCart cart = aGuestCart();
    cart.abandon();

    assertThrows(CartNotModifiableException.class, () -> cart.takeOverBy(ACCOUNT));
    assertEquals(GUEST, cart.customerId());
  }

  @Test
  @DisplayName("raises no event")
  void raisesNoEvent() {
    final ShoppingCart cart = aGuestCart();

    cart.takeOverBy(ACCOUNT);

    assertTrue(cart.domainEvents().isEmpty(), "nothing in the cart changed");
  }

  /** A guest's active cart with two positions, its events already published. */
  private static ShoppingCart aGuestCart() {
    final ShoppingCart cart = ShoppingCart.create(CartId.generate(), GUEST);
    final Price price = Price.of(Money.of(new BigDecimal("9.99"), EUR));
    cart.addItem(ProductId.generate(), Quantity.of(2), price);
    cart.addItem(ProductId.generate(), Quantity.of(1), price);
    cart.clearDomainEvents();
    return cart;
  }
}
