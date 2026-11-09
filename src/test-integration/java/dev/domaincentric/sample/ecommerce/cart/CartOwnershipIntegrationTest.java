package dev.domaincentric.sample.ecommerce.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
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
@SpringBootTest(classes = EcommerceSampleApplication.class)
class CartOwnershipIntegrationTest {

  @Autowired private GetOrCreateActiveCartInputPort getOrCreateActiveCart;
  @Autowired private GetCartByIdInputPort getCartById;
  @Autowired private AddItemToCartInputPort addItemToCart;
  @Autowired private StartCheckoutInputPort startCheckout;
  @Autowired private GetAllProductsInputPort getAllProducts;

  @Test
  @DisplayName(
      "Reading a cart that is not the caller's is indistinguishable from a cart that does not exist")
  void readingAStrangersCartFindsNothing() {
    final String owner = "owner-" + System.nanoTime();
    final String stranger = "stranger-" + System.nanoTime();
    final String cartId = activeCartOf(owner);

    assertThat(getCartById.execute(new GetCartByIdQuery(cartId, owner)))
        .isInstanceOf(GetCartByIdResult.Found.class);
    assertThat(getCartById.execute(new GetCartByIdQuery(cartId, stranger)))
        .isInstanceOf(GetCartByIdResult.CartNotFound.class);
  }

  @Test
  @DisplayName("Writing to a cart that is not the caller's is refused")
  void writingToAStrangersCartIsRefused() {
    final String owner = "owner-" + System.nanoTime();
    final String stranger = "stranger-" + System.nanoTime();
    final String cartId = activeCartOf(owner);
    final String productId = anyProductId();

    assertThat(addItemToCart.execute(new AddItemToCartCommand(cartId, stranger, productId, 1)))
        .asInstanceOf(type(AddItemToCartResult.CartNotFound.class))
        .extracting(refusal -> refusal.cartId().value())
        .isEqualTo(cartId);

    // The owner is unaffected
    addItemToCart.execute(new AddItemToCartCommand(cartId, owner, productId, 1));
    assertThat(
            ((GetCartByIdResult.Found) getCartById.execute(new GetCartByIdQuery(cartId, owner)))
                .cart()
                .items())
        .hasSize(1);
  }

  @Test
  @DisplayName("A stranger cannot start a checkout on somebody else's cart")
  void startingACheckoutOnAStrangersCartIsRefused() {
    final String owner = "owner-" + System.nanoTime();
    final String stranger = "stranger-" + System.nanoTime();
    final String cartId = activeCartOf(owner);
    addItemToCart.execute(new AddItemToCartCommand(cartId, owner, anyProductId(), 1));

    assertThat(startCheckout.execute(new StartCheckoutCommand(cartId, stranger)))
        .isEqualTo(new StartCheckoutResult.CartNotAvailable(CartId.of(cartId)));

    // and the owner can still start theirs
    assertThat(startCheckout.execute(new StartCheckoutCommand(cartId, owner)))
        .isInstanceOf(StartCheckoutResult.Started.class);
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
