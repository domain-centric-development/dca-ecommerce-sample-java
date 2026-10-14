package dev.domaincentric.sample.ecommerce.cart;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getactivecart.GetActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getactivecart.GetActiveCartQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Reading the active cart, through its input port in the wired application: a read never opens one.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@DisplayName("GetActiveCart")
class GetActiveCartIntegrationTest {

  @Autowired private GetActiveCartInputPort getActiveCart;
  @Autowired private GetOrCreateActiveCartInputPort getOrCreateActiveCart;
  @Autowired private ShoppingCartRepository carts;

  @Test
  @DisplayName("a customer without an active cart gets none, and none is opened")
  void aCustomerWithoutACartGetsNoneAndNoneIsOpened() {
    final String customer = "read-customer-" + System.nanoTime();

    assertThat(getActiveCart.execute(new GetActiveCartQuery(customer)).cartId()).isEmpty();
    assertThat(carts.findActiveCartByCustomerId(CustomerId.of(customer))).isEmpty();
  }

  @Test
  @DisplayName("a customer with an active cart gets that cart")
  void aCustomerWithAnActiveCartGetsIt() {
    final String customer = "read-customer-" + System.nanoTime();
    final String opened =
        getOrCreateActiveCart.execute(new GetOrCreateActiveCartCommand(customer)).cartId();

    assertThat(getActiveCart.execute(new GetActiveCartQuery(customer)).cartId()).contains(opened);
  }
}
