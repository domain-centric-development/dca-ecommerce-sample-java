package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ActiveCartAlreadyExistsException;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.infrastructure.database.PostgresTestDatabase;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The claim on a customer's open cart, held by the database rather than by a check before the
 * write.
 *
 * <p>"At most one active cart per customer" is uniqueness over a subset of the rows, which the
 * production schema expresses as a partial unique index (ADR-045). The in-memory adapter emulates
 * the same refusal; this is where the relational one is held to it, so a use case written against
 * either reads the same.
 */
@DisplayName("JdbcShoppingCartRepository — the active cart is claimed in the store")
class JdbcActiveCartUniquenessTest {

  private static final DataSource DATA_SOURCE = PostgresTestDatabase.migrated("cart-active-claim");

  @BeforeEach
  void emptyTables() {
    new JdbcTemplate(DATA_SOURCE).execute("TRUNCATE cart_items, carts");
  }

  @Test
  @DisplayName("A second active cart for the same customer is refused")
  void aSecondActiveCartIsRefused() {
    final JdbcShoppingCartRepository carts = repository();
    final CustomerId customer = CustomerId.of("customer-" + System.nanoTime());
    carts.save(ShoppingCart.create(CartId.generate(), customer));

    assertThrows(
        ActiveCartAlreadyExistsException.class,
        () -> carts.save(ShoppingCart.create(CartId.generate(), customer)));
  }

  @Test
  @DisplayName("Saving the same cart again is not a second cart")
  void savingTheSameCartAgainIsNotASecondCart() {
    final JdbcShoppingCartRepository carts = repository();
    final CustomerId customer = CustomerId.of("customer-" + System.nanoTime());
    final ShoppingCart cart = ShoppingCart.create(CartId.generate(), customer);
    carts.save(cart);

    assertDoesNotThrow(() -> carts.save(cart));
  }

  @Test
  @DisplayName("The customer is free again once their cart is no longer active")
  void theCustomerIsFreeAgainOnceTheCartIsDone() {
    final JdbcShoppingCartRepository carts = repository();
    final CustomerId customer = CustomerId.of("customer-" + System.nanoTime());
    final ShoppingCart first = ShoppingCart.create(CartId.generate(), customer);
    carts.save(first);

    carts.save(ShoppingCart.reconstitute(first.id(), customer, CartStatus.COMPLETED, List.of()));

    assertDoesNotThrow(() -> carts.save(ShoppingCart.create(CartId.generate(), customer)));
  }

  @Test
  @DisplayName("Eight simultaneous requests leave exactly one open cart")
  void simultaneousRequestsLeaveExactlyOneOpenCart() throws Exception {
    final JdbcShoppingCartRepository carts = repository();

    for (int round = 0; round < 25; round++) {
      final CustomerId customer = CustomerId.of("customer-" + System.nanoTime());
      final AtomicInteger accepted = new AtomicInteger();

      try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
        final var attempts =
            IntStream.range(0, 8)
                .mapToObj(
                    i ->
                        pool.submit(
                            () -> {
                              try {
                                carts.save(ShoppingCart.create(CartId.generate(), customer));
                                accepted.incrementAndGet();
                              } catch (final ActiveCartAlreadyExistsException expected) {
                                // the request that lost the race takes the cart that won
                              }
                              return null;
                            }))
                .toList();
        for (final Future<?> attempt : attempts) {
          attempt.get();
        }
      }

      assertEquals(1, accepted.get(), "exactly one request may create the cart");
      assertTrue(carts.findActiveCartByCustomerId(customer).isPresent());
    }
  }

  private JdbcShoppingCartRepository repository() {
    return new JdbcShoppingCartRepository(DATA_SOURCE, null);
  }
}
