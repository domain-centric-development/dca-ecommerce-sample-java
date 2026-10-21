package dev.domaincentric.sample.ecommerce.cart.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ShoppingCart: what the cart holds of a product")
class ShoppingCartQuantityOfTest {

  private static final Price PRICE =
      Price.of(Money.of(new BigDecimal("9.99"), Currency.getInstance("EUR")));

  @Test
  @DisplayName("is the quantity of the product's line")
  void isTheQuantityOfTheProductsLine() {
    final ShoppingCart cart = ShoppingCart.create(CartId.generate(), CustomerId.of("guest-1"));
    final ProductId product = ProductId.generate();
    cart.addItem(product, Quantity.of(5), PRICE);
    cart.addItem(ProductId.generate(), Quantity.of(2), PRICE);

    assertEquals(5, cart.quantityOf(product));
  }

  @Test
  @DisplayName("is 0 for a product the cart holds no line for")
  void isZeroForAProductTheCartHoldsNoLineFor() {
    final ShoppingCart cart = ShoppingCart.create(CartId.generate(), CustomerId.of("guest-1"));
    cart.addItem(ProductId.generate(), Quantity.of(2), PRICE);

    assertEquals(0, cart.quantityOf(ProductId.generate()));
  }
}
