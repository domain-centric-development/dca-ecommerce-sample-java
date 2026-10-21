package dev.domaincentric.sample.ecommerce.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartResult;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ChangePriceCommand;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ChangePriceInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Removing a product from a cart after a price changed, through the input port in the wired
 * application with the cart's real store and the real price list. Its own database, so the changed
 * price reaches no other test.
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:remove_item_from_cart_price;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@DisplayName("Removing a product answers at today's prices")
class RemoveItemFromCartPriceIntegrationTest {

  @Autowired private GetOrCreateActiveCartInputPort getOrCreateActiveCart;
  @Autowired private AddItemToCartInputPort addItemToCart;
  @Autowired private RemoveItemFromCartInputPort removeItemFromCart;
  @Autowired private ChangePriceInputPort changePrice;
  @Autowired private GetAllProductsInputPort getAllProducts;

  @Test
  @DisplayName("Removing answers at todays prices")
  void removingAProductAnswersTheRestOfTheCartAtTodaysPrice() {
    final String ada = "ada-" + System.nanoTime();
    final String cartId =
        getOrCreateActiveCart.execute(new GetOrCreateActiveCartCommand(ada)).cartId();
    final String ddd = productIdOf("Domain-Driven Design");
    final String cleanArchitecture = productIdOf("Clean Architecture");
    addItemToCart.execute(new AddItemToCartCommand(cartId, ada, ddd, 1));
    addItemToCart.execute(new AddItemToCartCommand(cartId, ada, cleanArchitecture, 1));
    changePrice.execute(new ChangePriceCommand(ddd, new BigDecimal("49.99")));

    final RemoveItemFromCartResult cart =
        removeItemFromCart.execute(new RemoveItemFromCartCommand(cartId, ada, cleanArchitecture));

    assertThat(cart.items())
        .extracting(CartItemSummary::productId, CartItemSummary::unitPrice)
        .containsExactly(tuple(ddd, Money.euro(new BigDecimal("49.99"))));
    assertThat(cart.total()).isEqualTo(Money.euro(new BigDecimal("49.99")));
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
