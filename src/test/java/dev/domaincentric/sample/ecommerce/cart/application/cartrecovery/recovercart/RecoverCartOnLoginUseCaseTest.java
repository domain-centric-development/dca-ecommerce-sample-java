package dev.domaincentric.sample.ecommerce.cart.application.recovercart;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.AggregateRoot;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.Quantity;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The half of the login flow that needs no decision from the user: the account holds no cart of its
 * own, so the one they filled as a guest simply becomes theirs. Without it, logging in would
 * silently empty the cart — see ADR-029.
 */
@DisplayName("RecoverCartOnLoginUseCase")
class RecoverCartOnLoginUseCaseTest {

  private static final Currency EUR = Currency.getInstance("EUR");
  private static final String ANONYMOUS_USER_ID = "anonymous-user-123";
  private static final String REGISTERED_USER_ID = "registered-user-456";

  private TestShoppingCartRepository repository;
  private TestDomainEventPublisher eventPublisher;
  private RecoverCartOnLoginUseCase useCase;

  @BeforeEach
  void setUp() {
    repository = new TestShoppingCartRepository();
    eventPublisher = new TestDomainEventPublisher();
    useCase = new RecoverCartOnLoginUseCase(repository, eventPublisher);
  }

  @Test
  @DisplayName("moves the anonymous cart to an account that has none")
  void movesTheAnonymousCartToAnAccountThatHasNone() {
    final ProductId product = ProductId.generate();
    cartFor(ANONYMOUS_USER_ID, product, 2);

    final RecoverCartOnLoginResult result = recover();

    assertEquals(1, result.itemsMerged());
    assertTrue(result.anonymousCartDeleted());
    final ShoppingCart recovered = activeCartOf(REGISTERED_USER_ID).orElseThrow();
    assertEquals(1, recovered.itemCount());
    assertEquals(product, recovered.items().getFirst().productId());
    assertTrue(activeCartOf(ANONYMOUS_USER_ID).isEmpty());
    assertFalse(eventPublisher.publishedEvents().isEmpty());
  }

  @Test
  @DisplayName("adds the anonymous items to a cart the account already has")
  void addsTheAnonymousItemsToACartTheAccountAlreadyHas() {
    cartFor(ANONYMOUS_USER_ID, ProductId.generate(), 1);
    cartFor(REGISTERED_USER_ID, ProductId.generate(), 3);

    recover();

    assertEquals(2, activeCartOf(REGISTERED_USER_ID).orElseThrow().itemCount());
  }

  @Test
  @DisplayName("does nothing when the identity did not change")
  void doesNothingWhenTheIdentityDidNotChange() {
    cartFor(REGISTERED_USER_ID, ProductId.generate(), 1);

    final RecoverCartOnLoginResult result =
        useCase.execute(new RecoverCartOnLoginCommand(REGISTERED_USER_ID, REGISTERED_USER_ID));

    assertEquals(0, result.itemsMerged());
    assertFalse(result.anonymousCartDeleted());
    assertEquals(1, activeCartOf(REGISTERED_USER_ID).orElseThrow().itemCount());
  }

  @Test
  @DisplayName("does nothing when there is no anonymous cart")
  void doesNothingWhenThereIsNoAnonymousCart() {
    final RecoverCartOnLoginResult result = recover();

    assertEquals(0, result.itemsMerged());
    assertFalse(result.anonymousCartDeleted());
    assertTrue(activeCartOf(REGISTERED_USER_ID).isEmpty());
  }

  @Test
  @DisplayName("does nothing when the anonymous cart is empty")
  void doesNothingWhenTheAnonymousCartIsEmpty() {
    repository.save(new ShoppingCart(CartId.generate(), CustomerId.of(ANONYMOUS_USER_ID)));

    final RecoverCartOnLoginResult result = recover();

    assertEquals(0, result.itemsMerged());
    assertTrue(activeCartOf(REGISTERED_USER_ID).isEmpty());
  }

  private RecoverCartOnLoginResult recover() {
    return useCase.execute(new RecoverCartOnLoginCommand(ANONYMOUS_USER_ID, REGISTERED_USER_ID));
  }

  private void cartFor(final String userId, final ProductId productId, final int quantity) {
    final ShoppingCart cart = new ShoppingCart(CartId.generate(), CustomerId.of(userId));
    cart.addItem(productId, Quantity.of(quantity), Price.of(Money.of(new BigDecimal("9.99"), EUR)));
    cart.clearDomainEvents();
    repository.save(cart);
  }

  private Optional<ShoppingCart> activeCartOf(final String userId) {
    return repository.findActiveCartByCustomerId(CustomerId.of(userId));
  }

  private static class TestShoppingCartRepository implements ShoppingCartRepository {

    private final Map<CartId, ShoppingCart> carts = new ConcurrentHashMap<>();

    @Override
    public Optional<ShoppingCart> findByIdForCustomer(
        final CartId cartId, final CustomerId customerId) {
      return findById(cartId).filter(cart -> cart.customerId().equals(customerId));
    }

    @Override
    public Optional<ShoppingCart> findById(CartId id) {
      return Optional.ofNullable(carts.get(id));
    }

    @Override
    public List<ShoppingCart> findByCustomerId(CustomerId customerId) {
      return carts.values().stream().filter(cart -> cart.customerId().equals(customerId)).toList();
    }

    @Override
    public Optional<ShoppingCart> findActiveCartByCustomerId(CustomerId customerId) {
      return carts.values().stream()
          .filter(cart -> cart.customerId().equals(customerId))
          .filter(ShoppingCart::isActive)
          .findFirst();
    }

    @Override
    public List<ShoppingCart> findAll() {
      return new ArrayList<>(carts.values());
    }

    @Override
    public ShoppingCart save(ShoppingCart cart) {
      carts.put(cart.id(), cart);
      return cart;
    }

    @Override
    public void deleteById(CartId id) {
      carts.remove(id);
    }
  }

  private static class TestDomainEventPublisher implements DomainEventPublisher {

    private final List<DomainEvent> publishedEvents = new ArrayList<>();

    @Override
    public void publish(DomainEvent event) {
      publishedEvents.add(event);
    }

    @Override
    public void publishAndClearEvents(AggregateRoot<?, ?> aggregate) {
      publishedEvents.addAll(aggregate.domainEvents());
      aggregate.clearDomainEvents();
    }

    List<DomainEvent> publishedEvents() {
      return publishedEvents;
    }
  }
}
