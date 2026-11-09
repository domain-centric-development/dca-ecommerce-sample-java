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
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Adding a product to a guest's cart through the input port, in the wired application with the
 * cart's real store: a product holds one line, and adding it again raises that line's quantity.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@DisplayName("Adding a product to the cart")
class AddItemToCartIntegrationTest {

  @Autowired private GetOrCreateActiveCartInputPort getOrCreateActiveCart;
  @Autowired private AddItemToCartInputPort addItemToCart;
  @Autowired private GetCartByIdInputPort getCartById;
  @Autowired private GetAllProductsInputPort getAllProducts;

  @Test
  @DisplayName("Adding a product the cart already holds raises that line's quantity")
  void addingTheSameProductAgainRaisesItsLinesQuantity() {
    final String guest = "guest-" + System.nanoTime();
    final String cartId = activeCartOf(guest);
    add(cartId, guest, "Domain-Driven Design");

    add(cartId, guest, "Domain-Driven Design");

    assertThat(linesOf(cartId, guest)).containsExactly("Domain-Driven Design x2");
  }

  @Test
  @DisplayName("Another product gets a line of its own")
  void anotherProductGetsALineOfItsOwn() {
    final String guest = "guest-" + System.nanoTime();
    final String cartId = activeCartOf(guest);
    add(cartId, guest, "Domain-Driven Design");

    add(cartId, guest, "Clean Architecture");

    assertThat(linesOf(cartId, guest))
        .containsExactlyInAnyOrder("Domain-Driven Design x1", "Clean Architecture x1");
  }

  @Test
  @DisplayName("Adding past the stock through the API is refused")
  void addingMoreThanTheLineCanHoldBesideWhatItHoldsIsRefusedAndTheCartStaysAsItWas() {
    final String ada = "ada-" + System.nanoTime();
    final String cartId = activeCartOf(ada);
    final String deskModel = productIdOf("Wooden Hexagon Desk Model");
    add(cartId, ada, deskModel, 5);

    final AddItemToCartResult.InsufficientArticleStock notEnough =
        assertThat(addItemToCart.execute(new AddItemToCartCommand(cartId, ada, deskModel, 4)))
            .asInstanceOf(type(AddItemToCartResult.InsufficientArticleStock.class))
            .actual();

    assertThat(notEnough.requested()).isEqualTo(4);
    assertThat(notEnough.productId().value().toString()).isEqualTo(deskModel);
    assertThat(linesOf(cartId, ada)).containsExactly("Wooden Hexagon Desk Model x5");
  }

  @Test
  @DisplayName("Adding up to exactly the stock is accepted")
  void addingUpToExactlyTheStockBesideWhatTheLineHoldsIsAccepted() {
    final String ada = "ada-" + System.nanoTime();
    final String cartId = activeCartOf(ada);
    final String deskModel = productIdOf("Wooden Hexagon Desk Model");
    add(cartId, ada, deskModel, 5);

    add(cartId, ada, deskModel, 3);

    assertThat(linesOf(cartId, ada)).containsExactly("Wooden Hexagon Desk Model x8");
  }

  private String activeCartOf(final String customerId) {
    return getOrCreateActiveCart.execute(new GetOrCreateActiveCartCommand(customerId)).cartId();
  }

  private void add(final String cartId, final String customerId, final String productName) {
    add(cartId, customerId, productIdOf(productName), 1);
  }

  private void add(
      final String cartId, final String customerId, final String productId, final int quantity) {
    assertThat(
            addItemToCart.execute(
                new AddItemToCartCommand(cartId, customerId, productId, quantity)))
        .isInstanceOf(AddItemToCartResult.Added.class);
  }

  /** The cart's lines as {@code name xquantity}, the way the cart page lists them. */
  private List<String> linesOf(final String cartId, final String customerId) {
    return ((GetCartByIdResult.Found) getCartById.execute(new GetCartByIdQuery(cartId, customerId)))
        .cart().items().stream()
            .map(item -> item.currentArticle().name() + " x" + item.quantity().value())
            .toList();
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
