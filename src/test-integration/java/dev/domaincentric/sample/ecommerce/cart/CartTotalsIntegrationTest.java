package dev.domaincentric.sample.ecommerce.cart;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.CartTotals;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The amounts the cart reports below its lines, read through the input port in the wired
 * application: the subtotal at today's prices and the VAT contained in it at 19 %.
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:cart_totals;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@DisplayName("Cart totals")
class CartTotalsIntegrationTest {

  private static final Currency EUR = Currency.getInstance("EUR");

  @Autowired private GetOrCreateActiveCartInputPort getOrCreateActiveCart;
  @Autowired private AddItemToCartInputPort addItemToCart;
  @Autowired private GetCartByIdInputPort getCartById;
  @Autowired private GetAllProductsInputPort getAllProducts;

  @Test
  @DisplayName("The cart shows the VAT its subtotal contains")
  void cartShowsTheVatItsSubtotalContains() {
    final String guest = "guest-" + System.nanoTime();
    final String cartId = activeCartOf(guest);
    add(cartId, guest, "Domain-Driven Design");
    add(cartId, guest, "Domain-Driven Design");
    add(cartId, guest, "Clean Architecture");

    final CartTotals totals = totalsOf(cartId, guest);

    assertThat(totals.containedTax()).isEqualTo(euro("23.94"));
  }

  @Test
  @DisplayName("The contained VAT is rounded half up to the cent")
  void containedVatIsRoundedHalfUpToTheCent() {
    final String guest = "guest-" + System.nanoTime();
    final String cartId = activeCartOf(guest);
    add(cartId, guest, "Domain-Driven Design");

    final CartTotals totals = totalsOf(cartId, guest);

    assertThat(totals.currentSubtotal()).isEqualTo(euro("54.99"));
    assertThat(totals.containedTax()).isEqualTo(euro("8.78"));
  }

  private static Money euro(final String amount) {
    return Money.of(new BigDecimal(amount), EUR);
  }

  private String activeCartOf(final String customerId) {
    return getOrCreateActiveCart.execute(new GetOrCreateActiveCartCommand(customerId)).cartId();
  }

  private void add(final String cartId, final String customerId, final String productName) {
    addItemToCart.execute(
        new AddItemToCartCommand(cartId, customerId, productIdOf(productName), 1));
  }

  private CartTotals totalsOf(final String cartId, final String customerId) {
    return getCartById.execute(new GetCartByIdQuery(cartId, customerId)).totals();
  }

  private String productIdOf(final String name) {
    return getAllProducts.execute(new GetAllProductsQuery()).products().stream()
        .filter(product -> name.equals(product.name()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no product named " + name))
        .productId()
        .value()
        .toString();
  }
}
