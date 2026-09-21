package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ActiveCartAlreadyExistsException;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

/**
 * The claim on a customer's open cart, held by the database rather than by a check before the
 * write.
 *
 * <p>"At most one active cart per customer" is uniqueness over a subset of the rows, which the
 * production schema expresses as a unique index over a generated column (ADR-045). The in-memory
 * adapter emulates the same refusal; this is where the relational one is held to it, so a use case
 * written against either reads the same.
 */
@DisplayName("JdbcShoppingCartRepository — the active cart is claimed in the store")
class JdbcActiveCartUniquenessTest {

  private static final String URL =
      "jdbc:h2:mem:cart-active-claim;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

  private DataSource dataSource;

  @BeforeEach
  void prepareSchema() {
    dataSource = new DriverManagerDataSource(URL, "sa", "");
    try (var connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"));
    } catch (final SQLException e) {
      throw new IllegalStateException("Could not prepare the cart schema", e);
    }
    final JdbcTemplate setup = new JdbcTemplate(dataSource);
    setup.execute("DELETE FROM cart_items");
    setup.execute("DELETE FROM carts");
  }

  @Test
  @DisplayName("A second active cart for the same customer is refused")
  void aSecondActiveCartIsRefused() {
    final JdbcShoppingCartRepository carts = repository();
    final CustomerId customer = CustomerId.of("customer-" + System.nanoTime());
    carts.save(new ShoppingCart(CartId.generate(), customer));

    assertThrows(
        ActiveCartAlreadyExistsException.class,
        () -> carts.save(new ShoppingCart(CartId.generate(), customer)));
  }

  @Test
  @DisplayName("Saving the same cart again is not a second cart")
  void savingTheSameCartAgainIsNotASecondCart() {
    final JdbcShoppingCartRepository carts = repository();
    final CustomerId customer = CustomerId.of("customer-" + System.nanoTime());
    final ShoppingCart cart = new ShoppingCart(CartId.generate(), customer);
    carts.save(cart);

    assertDoesNotThrow(() -> carts.save(cart));
  }

  @Test
  @DisplayName("The customer is free again once their cart is no longer active")
  void theCustomerIsFreeAgainOnceTheCartIsDone() {
    final JdbcShoppingCartRepository carts = repository();
    final CustomerId customer = CustomerId.of("customer-" + System.nanoTime());
    final ShoppingCart first = new ShoppingCart(CartId.generate(), customer);
    carts.save(first);

    first.complete();
    carts.save(first);

    assertDoesNotThrow(() -> carts.save(new ShoppingCart(CartId.generate(), customer)));
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
                                carts.save(new ShoppingCart(CartId.generate(), customer));
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
    return new JdbcShoppingCartRepository(dataSource, null);
  }
}
