package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.infrastructure.AsyncInitialize;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of ShoppingCartRepository.
 *
 * <p>This secondary adapter provides a thread-safe in-memory storage for shopping carts using
 * ConcurrentHashMap. In a production system, this would be replaced with a database implementation.
 *
 * <p><b>Async Initialization:</b> This repository uses {@link AsyncInitialize} to perform
 * non-blocking cache warmup. The {@code asyncInitialize()} method is invoked asynchronously after
 * bean initialization, allowing the application to start without waiting for initialization tasks.
 *
 * @see AsyncInitialize
 */
@org.springframework.context.annotation.Profile("inmemory")
@Repository
@AsyncInitialize(priority = 100, description = "Initialize shopping cart metrics")
public class InMemoryShoppingCartRepository implements ShoppingCartRepository {

  private static final Logger logger =
      LoggerFactory.getLogger(InMemoryShoppingCartRepository.class);

  private final ConcurrentHashMap<CartId, ShoppingCart> carts = new ConcurrentHashMap<>();

  /**
   * The one active cart per customer, held as an index rather than derived by scanning.
   *
   * <p>"At most one active cart per customer" is an invariant no single aggregate can hold, so the
   * store holds it: a claim on this map is what a unique index gives a relational adapter for free.
   * Without it, two requests that both find no active cart both create one.
   */
  private final ConcurrentHashMap<CustomerId, CartId> activeCartByCustomer =
      new ConcurrentHashMap<>();

  @Override
  public Optional<ShoppingCart> findById(final CartId id) {
    return Optional.ofNullable(carts.get(id));
  }

  @Override
  public Optional<ShoppingCart> findByIdForCustomer(
      final CartId cartId, final CustomerId customerId) {
    return findById(cartId).filter(cart -> cart.customerId().equals(customerId));
  }

  @Override
  public List<ShoppingCart> findByCustomerId(final CustomerId customerId) {
    return carts.values().stream().filter(cart -> cart.customerId().equals(customerId)).toList();
  }

  @Override
  public Optional<ShoppingCart> findActiveCartByCustomerId(final CustomerId customerId) {
    return Optional.ofNullable(activeCartByCustomer.get(customerId)).map(carts::get);
  }

  @Override
  public List<ShoppingCart> findAll() {
    return List.copyOf(carts.values());
  }

  /**
   * @throws IllegalStateException if the cart is active and the customer already has a different
   *     active cart — the answer a unique index gives, so a caller written against this adapter
   *     works unchanged against a relational one
   */
  @Override
  public ShoppingCart save(final ShoppingCart cart) {
    if (cart.status() == CartStatus.ACTIVE) {
      final CartId claimed = activeCartByCustomer.putIfAbsent(cart.customerId(), cart.id());
      if (claimed != null && !claimed.equals(cart.id())) {
        throw new IllegalStateException(
            "Customer " + cart.customerId().value() + " already has an active cart");
      }
    } else {
      activeCartByCustomer.remove(cart.customerId(), cart.id());
    }

    carts.put(cart.id(), cart);
    return cart;
  }

  @Override
  public void deleteById(final CartId id) {
    final ShoppingCart removed = carts.remove(id);
    if (removed != null) {
      activeCartByCustomer.remove(removed.customerId(), id);
    }
  }

  /**
   * Asynchronous initialization method triggered by {@link AsyncInitialize}.
   *
   * <p>This method is invoked after bean construction to initialize shopping cart metrics and
   * perform background setup tasks. The {@code @Async} annotation ensures non-blocking execution,
   * allowing the application to start immediately.
   *
   * <p><b>Pattern:</b> This demonstrates the {@code @AsyncInitialize} pattern with lower priority
   * (100) than ProductRepository (50), ensuring products are initialized first.
   *
   * @see AsyncInitialize
   * @see dev.domaincentric.sample.ecommerce.infrastructure.config.AsyncConfiguration
   * @see dev.domaincentric.sample.ecommerce.infrastructure.support.AsyncInitializationProcessor
   */
  @Async
  public void asyncInitialize() {
    logger.info("Starting async initialization of ShoppingCartRepository...");

    try {
      // Simulate initialization delay
      Thread.sleep(1500);

      // In a real application, this would:
      // - Initialize metrics collectors
      // - Preload abandoned cart data
      // - Set up monitoring dashboards
      int cartCount = carts.size();

      logger.info(
          "ShoppingCartRepository initialization completed. Current cart count: {}", cartCount);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("ShoppingCartRepository async initialization interrupted", e);
    } catch (Exception e) {
      logger.error("Error during ShoppingCartRepository async initialization", e);
    }
  }
}
