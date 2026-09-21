package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.AggregateRoot;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.dca.spring.InMemoryTransactionBoundary;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ActiveCartAlreadyExistsException;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartUseCase;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A customer has at most one active cart, and the store is what holds that.
 *
 * <p>The rule spans carts, so no single aggregate can enforce it, and "look, then create" cannot
 * either — two requests that both find nothing both create one. The store claims the customer the
 * way a unique index does, which is also what makes this adapter teach the same contract as a
 * relational one.
 */
class ActiveCartUniquenessTest {

  private static final int CONCURRENT_REQUESTS = 8;
  private static final int ROUNDS = 25;

  @Test
  @DisplayName("A second active cart for the same customer is refused")
  void aSecondActiveCartIsRefused() {
    final InMemoryShoppingCartRepository carts = new InMemoryShoppingCartRepository();
    final CustomerId customer = CustomerId.of("customer-" + System.nanoTime());
    carts.save(new ShoppingCart(CartId.generate(), customer));

    assertThrows(
        ActiveCartAlreadyExistsException.class,
        () -> carts.save(new ShoppingCart(CartId.generate(), customer)));
  }

  @Test
  @DisplayName("The customer is free again once their cart is no longer active")
  void theCustomerIsFreeAgainOnceTheCartIsDone() {
    final InMemoryShoppingCartRepository carts = new InMemoryShoppingCartRepository();
    final CustomerId customer = CustomerId.of("customer-" + System.nanoTime());
    final ShoppingCart first = new ShoppingCart(CartId.generate(), customer);
    carts.save(first);

    first.complete();
    carts.save(first);

    final ShoppingCart second = new ShoppingCart(CartId.generate(), customer);
    carts.save(second);
    assertEquals(second.id(), carts.findActiveCartByCustomerId(customer).orElseThrow().id());
  }

  @Test
  @DisplayName("Requests that arrive together end up on one cart, not several")
  void concurrentRequestsShareOneCart() throws Exception {
    for (int round = 0; round < ROUNDS; round++) {
      final InMemoryShoppingCartRepository carts = new InMemoryShoppingCartRepository();
      final GetOrCreateActiveCartUseCase useCase =
          new GetOrCreateActiveCartUseCase(
              carts, new SilentEventPublisher(), new InMemoryTransactionBoundary());
      final String customer = "customer-" + System.nanoTime();

      final Set<String> answered =
          inParallel(() -> useCase.execute(new GetOrCreateActiveCartCommand(customer)).cartId());

      assertEquals(1, answered.size(), "every request must be answered with the same cart");
      assertEquals(
          1,
          carts.findByCustomerId(CustomerId.of(customer)).size(),
          "and only one cart may exist for that customer");
      assertTrue(
          carts.findActiveCartByCustomerId(CustomerId.of(customer)).isPresent(),
          "which is the active one");
    }
  }

  /** Runs the call on several threads released at the same moment, and collects the answers. */
  private static Set<String> inParallel(final Callable<String> call) throws Exception {
    final CyclicBarrier startLine = new CyclicBarrier(CONCURRENT_REQUESTS);
    try (ExecutorService threads = Executors.newFixedThreadPool(CONCURRENT_REQUESTS)) {
      final List<Future<String>> answers =
          IntStream.range(0, CONCURRENT_REQUESTS)
              .mapToObj(
                  i ->
                      threads.submit(
                          () -> {
                            startLine.await();
                            return call.call();
                          }))
              .toList();

      return answers.stream()
          .map(
              future -> {
                try {
                  return future.get();
                } catch (final Exception e) {
                  throw new IllegalStateException("a request failed outright", e);
                }
              })
          .collect(Collectors.toUnmodifiableSet());
    }
  }

  private static final class SilentEventPublisher implements DomainEventPublisher {

    @Override
    public void publish(final DomainEvent event) {
      // the events are not what is under test
    }

    @Override
    public void publishAndClearEvents(final AggregateRoot<?, ?> aggregate) {
      aggregate.clearDomainEvents();
    }
  }
}
