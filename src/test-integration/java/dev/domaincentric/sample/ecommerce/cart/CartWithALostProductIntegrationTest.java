package dev.domaincentric.sample.ecommerce.cart;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.cart.application.cartoversight.getallcarts.GetAllCartsInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.cartoversight.getallcarts.GetAllCartsQuery;
import dev.domaincentric.sample.ecommerce.cart.application.cartoversight.getallcarts.GetAllCartsResult;
import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The staff's list of every cart, read through its input port in the wired application, while one
 * cart holds a product the catalogue no longer knows. The database goes back to the seeded
 * catalogue after the test, so the product it takes out is missing nowhere else.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@DisplayName("A cart with a product the catalogue lost")
class CartWithALostProductIntegrationTest {

  @Autowired private GetOrCreateActiveCartInputPort getOrCreateActiveCart;
  @Autowired private AddItemToCartInputPort addItemToCart;
  @Autowired private GetAllCartsInputPort getAllCarts;
  @Autowired private GetAllProductsInputPort getAllProducts;
  @Autowired private ProductRepository productRepository;

  @Test
  @DisplayName("The staff list answers with a product the catalogue lost")
  void theStaffListShowsALineWhoseProductTheCatalogueLostAsUnavailableWithoutPrice() {
    final String customer = "guest-" + System.nanoTime();
    final String cartId =
        getOrCreateActiveCart.execute(new GetOrCreateActiveCartCommand(customer)).cartId();
    final String cleanArchitecture = productIdOf("Clean Architecture");
    final String domainDrivenDesign = productIdOf("Domain-Driven Design");
    addItemToCart.execute(new AddItemToCartCommand(cartId, customer, cleanArchitecture, 1));
    addItemToCart.execute(new AddItemToCartCommand(cartId, customer, domainDrivenDesign, 1));
    productRepository.deleteById(ProductId.of(domainDrivenDesign));

    final GetAllCartsResult result = getAllCarts.execute(new GetAllCartsQuery());

    final GetAllCartsResult.CartSummary listed =
        result.carts().stream()
            .filter(cart -> cartId.equals(cart.cartId()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("the cart is not in the staff list"));
    final CartItemSummary lostLine =
        listed.items().stream()
            .filter(item -> domainDrivenDesign.equals(item.productId()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("the list shows no line of the lost product"));
    assertThat(lostLine.available()).as("available").isFalse();
    assertThat(lostLine.unitPrice()).as("price").isNull();
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
