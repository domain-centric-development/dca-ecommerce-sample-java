package dev.domaincentric.sample.ecommerce.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ActiveCartAlreadyExistsException;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The store the running shop actually uses refuses a second open cart.
 *
 * <p>The unit tests hold the in-memory and the JDBC adapter to the claim; this one holds the
 * adapter the application context picks — the JPA one — because that is the answer a deployment
 * gets. All three report the same failure, which is what lets {@code GetOrCreateActiveCartUseCase}
 * handle a lost race without knowing its store (ADR-045).
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:cart_active_claim;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
class ActiveCartClaimIntegrationTest {

  @Autowired private ShoppingCartRepository carts;

  @Test
  @DisplayName("A second active cart for the same customer is refused by the wired store")
  void aSecondActiveCartIsRefused() {
    final CustomerId customer = CustomerId.of("claim-customer-" + System.nanoTime());
    carts.save(new ShoppingCart(CartId.generate(), customer));

    assertThatThrownBy(() -> carts.save(new ShoppingCart(CartId.generate(), customer)))
        .isInstanceOf(ActiveCartAlreadyExistsException.class);
  }

  @Test
  @DisplayName("The customer is free again once their cart is no longer active")
  void theCustomerIsFreeAgainOnceTheCartIsDone() {
    final CustomerId customer = CustomerId.of("claim-customer-" + System.nanoTime());
    final ShoppingCart first = new ShoppingCart(CartId.generate(), customer);
    carts.save(first);

    first.complete();
    carts.save(first);

    assertThatCode(() -> carts.save(new ShoppingCart(CartId.generate(), customer)))
        .doesNotThrowAnyException();
    assertThat(carts.findActiveCartByCustomerId(customer)).isPresent();
  }
}
