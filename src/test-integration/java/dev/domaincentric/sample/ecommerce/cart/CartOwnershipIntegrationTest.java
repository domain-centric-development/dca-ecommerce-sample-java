package dev.domaincentric.sample.ecommerce.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.checkoutcart.CheckoutCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.checkoutcart.CheckoutCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A cart ID says which cart, never whose.
 *
 * <p>These use cases take the cart ID from the outside — a path segment, a hidden form field — so
 * the caller could name somebody else's cart. The rule lives in the use case rather than in each
 * adapter: the checkout case below is reached through the web form, where no discipline in the REST
 * resource would have helped.
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:cart_ownership;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
class CartOwnershipIntegrationTest {

  @Autowired private GetOrCreateActiveCartInputPort getOrCreateActiveCart;
  @Autowired private GetCartByIdInputPort getCartById;
  @Autowired private AddItemToCartInputPort addItemToCart;
  @Autowired private CheckoutCartInputPort checkoutCart;
  @Autowired private StartCheckoutInputPort startCheckout;
  @Autowired private GetAllProductsInputPort getAllProducts;

  @Test
  @DisplayName(
      "Reading a cart that is not the caller's is indistinguishable from a cart that does not exist")
  void readingAStrangersCartFindsNothing() {
    final String owner = "owner-" + System.nanoTime();
    final String stranger = "stranger-" + System.nanoTime();
    final String cartId = activeCartOf(owner);

    assertThat(getCartById.execute(new GetCartByIdQuery(cartId, owner)).found()).isTrue();
    assertThat(getCartById.execute(new GetCartByIdQuery(cartId, stranger)).found()).isFalse();
  }

  @Test
  @DisplayName("Writing to a cart that is not the caller's is refused")
  void writingToAStrangersCartIsRefused() {
    final String owner = "owner-" + System.nanoTime();
    final String stranger = "stranger-" + System.nanoTime();
    final String cartId = activeCartOf(owner);
    final String productId = anyProductId();

    assertThatThrownBy(
            () -> addItemToCart.execute(new AddItemToCartCommand(cartId, stranger, productId, 1)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Cart not found");

    assertThatThrownBy(() -> checkoutCart.execute(new CheckoutCartCommand(cartId, stranger)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Cart not found");

    // The owner is unaffected
    addItemToCart.execute(new AddItemToCartCommand(cartId, owner, productId, 1));
    assertThat(
            getCartById.execute(new GetCartByIdQuery(cartId, owner)).cart().orElseThrow().items())
        .hasSize(1);
  }

  @Test
  @DisplayName("A stranger cannot start a checkout on somebody else's cart")
  void startingACheckoutOnAStrangersCartIsRefused() {
    final String owner = "owner-" + System.nanoTime();
    final String stranger = "stranger-" + System.nanoTime();
    final String cartId = activeCartOf(owner);
    addItemToCart.execute(new AddItemToCartCommand(cartId, owner, anyProductId(), 1));

    assertThatThrownBy(() -> startCheckout.execute(new StartCheckoutCommand(cartId, stranger)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Cart not found");

    // and the owner can still start theirs
    assertThat(startCheckout.execute(new StartCheckoutCommand(cartId, owner))).isNotNull();
  }

  private String activeCartOf(final String customerId) {
    return getOrCreateActiveCart.execute(new GetOrCreateActiveCartCommand(customerId)).cartId();
  }

  private String anyProductId() {
    return getAllProducts
        .execute(new GetAllProductsQuery())
        .products()
        .getFirst()
        .productId()
        .value()
        .toString();
  }
}
