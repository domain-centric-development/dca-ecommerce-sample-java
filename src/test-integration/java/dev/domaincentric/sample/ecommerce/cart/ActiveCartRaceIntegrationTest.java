package dev.domaincentric.sample.ecommerce.cart;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * What the loser of a race gets.
 *
 * <p>The store claims the customer's open cart, so two requests that both find none can not both
 * create one. ADR-042 says the request that loses takes the cart that won — this is where that
 * recovery is held against the store the application actually wires, rather than against the
 * in-memory one where the claim costs no transaction.
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:cart_active_race;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
class ActiveCartRaceIntegrationTest {

  @Autowired private GetOrCreateActiveCartInputPort getOrCreateActiveCart;

  @Autowired private ShoppingCartRepository carts;

  @Test
  @DisplayName("Simultaneous callers all end up with the one cart that won")
  void simultaneousCallersAllEndUpWithTheOneCartThatWon() throws Exception {
    final CustomerId customer = CustomerId.of("race-customer-" + System.nanoTime());
    final int callers = 8;
    final CountDownLatch startLine = new CountDownLatch(1);
    final List<String> answered = new ArrayList<>();

    try (ExecutorService pool = Executors.newFixedThreadPool(callers)) {
      final List<Future<String>> attempts = new ArrayList<>();
      for (int i = 0; i < callers; i++) {
        attempts.add(
            pool.submit(
                () -> {
                  startLine.await();
                  return getOrCreateActiveCart
                      .execute(new GetOrCreateActiveCartCommand(customer.value()))
                      .cartId();
                }));
      }
      startLine.countDown();
      for (final Future<String> attempt : attempts) {
        answered.add(attempt.get());
      }
    }

    assertThat(answered).hasSize(callers).doesNotContainNull();
    assertThat(answered).containsOnly(answered.get(0));
    assertThat(carts.findActiveCartByCustomerId(customer)).isPresent();
  }
}
