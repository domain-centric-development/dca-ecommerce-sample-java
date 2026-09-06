package dev.domaincentric.sample.ecommerce.cart.application.mergecarts;

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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for MergeCartsUseCase.
 *
 * <p>Tests the use case orchestration for all 3 merge strategies:
 *
 * <ul>
 *   <li>MERGE_BOTH - combines items from both carts
 *   <li>USE_ACCOUNT_CART - keeps account cart, discards anonymous
 *   <li>USE_ANONYMOUS_CART - replaces account cart with anonymous items
 * </ul>
 */
@DisplayName("MergeCartsUseCase")
class MergeCartsUseCaseTest {

  private static final Currency EUR = Currency.getInstance("EUR");
  private static final String ANONYMOUS_USER_ID = "anonymous-user-123";
  private static final String REGISTERED_USER_ID = "registered-user-456";

  private TestShoppingCartRepository repository;
  private TestDomainEventPublisher eventPublisher;
  private MergeCartsUseCase useCase;

  @BeforeEach
  void setUp() {
    repository = new TestShoppingCartRepository();
    eventPublisher = new TestDomainEventPublisher();
    useCase = new MergeCartsUseCase(repository, eventPublisher);
  }

  @Nested
  @DisplayName("MERGE_BOTH Strategy")
  class MergeBothStrategy {

    @Test
    @DisplayName("combines items from both carts")
    void combinesItemsFromBothCarts() {
      // Arrange
      ProductId product1 = ProductId.generate();
      ProductId product2 = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      ShoppingCart anonymousCart = createCart(ANONYMOUS_USER_ID);
      anonymousCart.addItem(product1, Quantity.of(2), price);
      repository.save(anonymousCart);

      ShoppingCart accountCart = createCart(REGISTERED_USER_ID);
      accountCart.addItem(product2, Quantity.of(3), price);
      repository.save(accountCart);

      MergeCartsCommand command =
          new MergeCartsCommand(
              ANONYMOUS_USER_ID, REGISTERED_USER_ID, CartMergeStrategy.MERGE_BOTH);

      // Act
      MergeCartsResult response = useCase.execute(command);

      // Assert
      assertEquals(CartMergeStrategy.MERGE_BOTH, response.strategyApplied());
      assertEquals(2, response.items().size());
      assertEquals(1, response.itemsFromAnonymous());
      assertEquals(1, response.itemsFromAccount());
      assertTrue(response.anonymousCartDeleted());

      // Verify anonymous cart was deleted
      assertFalse(
          repository.findActiveCartByCustomerId(CustomerId.of(ANONYMOUS_USER_ID)).isPresent());
    }

    @Test
    @DisplayName("combines quantities for same product")
    void combinesQuantitiesForSameProduct() {
      // Arrange
      ProductId sharedProduct = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      ShoppingCart anonymousCart = createCart(ANONYMOUS_USER_ID);
      anonymousCart.addItem(sharedProduct, Quantity.of(2), price);
      repository.save(anonymousCart);

      ShoppingCart accountCart = createCart(REGISTERED_USER_ID);
      accountCart.addItem(sharedProduct, Quantity.of(3), price);
      repository.save(accountCart);

      MergeCartsCommand command =
          new MergeCartsCommand(
              ANONYMOUS_USER_ID, REGISTERED_USER_ID, CartMergeStrategy.MERGE_BOTH);

      // Act
      MergeCartsResult response = useCase.execute(command);

      // Assert
      assertEquals(1, response.items().size());
      assertEquals(5, response.items().get(0).quantity()); // 2 + 3
      // Total should be 5 * 10 = 50
      assertEquals(0, BigDecimal.valueOf(50).compareTo(response.totalAmount()));
    }

    @Test
    @DisplayName("handles empty anonymous cart")
    void handlesEmptyAnonymousCart() {
      // Arrange - no anonymous cart exists
      ProductId product = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      ShoppingCart accountCart = createCart(REGISTERED_USER_ID);
      accountCart.addItem(product, Quantity.of(2), price);
      repository.save(accountCart);

      MergeCartsCommand command =
          new MergeCartsCommand(
              ANONYMOUS_USER_ID, REGISTERED_USER_ID, CartMergeStrategy.MERGE_BOTH);

      // Act
      MergeCartsResult response = useCase.execute(command);

      // Assert
      assertEquals(1, response.items().size());
      assertEquals(0, response.itemsFromAnonymous());
      assertEquals(1, response.itemsFromAccount());
    }
  }

  @Nested
  @DisplayName("USE_ACCOUNT_CART Strategy")
  class UseAccountCartStrategy {

    @Test
    @DisplayName("keeps account cart items only")
    void keepsAccountCartItemsOnly() {
      // Arrange
      ProductId anonymousProduct = ProductId.generate();
      ProductId accountProduct = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      ShoppingCart anonymousCart = createCart(ANONYMOUS_USER_ID);
      anonymousCart.addItem(anonymousProduct, Quantity.of(5), price);
      repository.save(anonymousCart);

      ShoppingCart accountCart = createCart(REGISTERED_USER_ID);
      accountCart.addItem(accountProduct, Quantity.of(2), price);
      repository.save(accountCart);

      MergeCartsCommand command =
          new MergeCartsCommand(
              ANONYMOUS_USER_ID, REGISTERED_USER_ID, CartMergeStrategy.USE_ACCOUNT_CART);

      // Act
      MergeCartsResult response = useCase.execute(command);

      // Assert
      assertEquals(CartMergeStrategy.USE_ACCOUNT_CART, response.strategyApplied());
      assertEquals(1, response.items().size());
      assertEquals(0, response.itemsFromAnonymous());
      assertEquals(1, response.itemsFromAccount());
      assertEquals(2, response.items().get(0).quantity());
      assertTrue(response.anonymousCartDeleted());
    }

    @Test
    @DisplayName("deletes anonymous cart")
    void deletesAnonymousCart() {
      // Arrange
      ProductId product = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      ShoppingCart anonymousCart = createCart(ANONYMOUS_USER_ID);
      anonymousCart.addItem(product, Quantity.of(3), price);
      CartId anonymousCartId = anonymousCart.id();
      repository.save(anonymousCart);

      ShoppingCart accountCart = createCart(REGISTERED_USER_ID);
      accountCart.addItem(product, Quantity.of(1), price);
      repository.save(accountCart);

      MergeCartsCommand command =
          new MergeCartsCommand(
              ANONYMOUS_USER_ID, REGISTERED_USER_ID, CartMergeStrategy.USE_ACCOUNT_CART);

      // Act
      useCase.execute(command);

      // Assert
      assertFalse(repository.findById(anonymousCartId).isPresent());
    }

    @Test
    @DisplayName("handles missing anonymous cart gracefully")
    void handlesMissingAnonymousCartGracefully() {
      // Arrange - no anonymous cart
      ProductId product = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      ShoppingCart accountCart = createCart(REGISTERED_USER_ID);
      accountCart.addItem(product, Quantity.of(2), price);
      repository.save(accountCart);

      MergeCartsCommand command =
          new MergeCartsCommand(
              ANONYMOUS_USER_ID, REGISTERED_USER_ID, CartMergeStrategy.USE_ACCOUNT_CART);

      // Act
      MergeCartsResult response = useCase.execute(command);

      // Assert
      assertEquals(1, response.items().size());
      // itemsFromAccount is the item count (not quantity), so 1 item
      assertEquals(1, response.itemsFromAccount());
    }
  }

  @Nested
  @DisplayName("USE_ANONYMOUS_CART Strategy")
  class UseAnonymousCartStrategy {

    @Test
    @DisplayName("replaces account cart with anonymous cart items")
    void replacesAccountCartWithAnonymousCartItems() {
      // Arrange
      ProductId anonymousProduct = ProductId.generate();
      ProductId accountProduct = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      ShoppingCart anonymousCart = createCart(ANONYMOUS_USER_ID);
      anonymousCart.addItem(anonymousProduct, Quantity.of(5), price);
      repository.save(anonymousCart);

      ShoppingCart accountCart = createCart(REGISTERED_USER_ID);
      accountCart.addItem(accountProduct, Quantity.of(2), price);
      repository.save(accountCart);

      MergeCartsCommand command =
          new MergeCartsCommand(
              ANONYMOUS_USER_ID, REGISTERED_USER_ID, CartMergeStrategy.USE_ANONYMOUS_CART);

      // Act
      MergeCartsResult response = useCase.execute(command);

      // Assert
      assertEquals(CartMergeStrategy.USE_ANONYMOUS_CART, response.strategyApplied());
      assertEquals(1, response.items().size());
      // itemsFromAnonymous is item count (not quantity), so 1 item
      assertEquals(1, response.itemsFromAnonymous());
      assertEquals(0, response.itemsFromAccount());
      assertEquals(5, response.items().get(0).quantity());
      assertTrue(response.anonymousCartDeleted());
    }

    @Test
    @DisplayName("deletes anonymous cart after moving items")
    void deletesAnonymousCartAfterMovingItems() {
      // Arrange
      ProductId product = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      ShoppingCart anonymousCart = createCart(ANONYMOUS_USER_ID);
      anonymousCart.addItem(product, Quantity.of(3), price);
      CartId anonymousCartId = anonymousCart.id();
      repository.save(anonymousCart);

      ShoppingCart accountCart = createCart(REGISTERED_USER_ID);
      accountCart.addItem(product, Quantity.of(1), price);
      repository.save(accountCart);

      MergeCartsCommand command =
          new MergeCartsCommand(
              ANONYMOUS_USER_ID, REGISTERED_USER_ID, CartMergeStrategy.USE_ANONYMOUS_CART);

      // Act
      useCase.execute(command);

      // Assert
      assertFalse(repository.findById(anonymousCartId).isPresent());
    }

    @Test
    @DisplayName("clears existing account cart items before adding anonymous items")
    void clearsExistingAccountCartItemsBeforeAddingAnonymousItems() {
      // Arrange
      ProductId anonymousProduct = ProductId.generate();
      ProductId accountProduct1 = ProductId.generate();
      ProductId accountProduct2 = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      ShoppingCart anonymousCart = createCart(ANONYMOUS_USER_ID);
      anonymousCart.addItem(anonymousProduct, Quantity.of(2), price);
      repository.save(anonymousCart);

      ShoppingCart accountCart = createCart(REGISTERED_USER_ID);
      accountCart.addItem(accountProduct1, Quantity.of(1), price);
      accountCart.addItem(accountProduct2, Quantity.of(1), price);
      repository.save(accountCart);

      MergeCartsCommand command =
          new MergeCartsCommand(
              ANONYMOUS_USER_ID, REGISTERED_USER_ID, CartMergeStrategy.USE_ANONYMOUS_CART);

      // Act
      MergeCartsResult response = useCase.execute(command);

      // Assert
      assertEquals(1, response.items().size());
      assertEquals(anonymousProduct.value(), response.items().get(0).productId());
    }

    @Test
    @DisplayName("handles empty anonymous cart")
    void handlesEmptyAnonymousCart() {
      // Arrange - no anonymous cart
      ProductId product = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      ShoppingCart accountCart = createCart(REGISTERED_USER_ID);
      accountCart.addItem(product, Quantity.of(2), price);
      repository.save(accountCart);

      MergeCartsCommand command =
          new MergeCartsCommand(
              ANONYMOUS_USER_ID, REGISTERED_USER_ID, CartMergeStrategy.USE_ANONYMOUS_CART);

      // Act
      MergeCartsResult response = useCase.execute(command);

      // Assert
      assertEquals(0, response.itemsFromAnonymous());
    }
  }

  @Nested
  @DisplayName("Cart Creation")
  class CartCreation {

    @Test
    @DisplayName("creates account cart if not exists")
    void createsAccountCartIfNotExists() {
      // Arrange - no account cart exists
      ProductId product = ProductId.generate();
      Price price = Price.of(Money.of(BigDecimal.valueOf(10.00), EUR));

      ShoppingCart anonymousCart = createCart(ANONYMOUS_USER_ID);
      anonymousCart.addItem(product, Quantity.of(3), price);
      repository.save(anonymousCart);

      MergeCartsCommand command =
          new MergeCartsCommand(
              ANONYMOUS_USER_ID, REGISTERED_USER_ID, CartMergeStrategy.MERGE_BOTH);

      // Act
      MergeCartsResult response = useCase.execute(command);

      // Assert
      assertNotNull(response.cartId());
      assertEquals(REGISTERED_USER_ID, response.customerId());
      assertEquals(1, response.items().size());
    }
  }

  // Helper methods

  private ShoppingCart createCart(String userId) {
    return new ShoppingCart(CartId.generate(), CustomerId.of(userId));
  }

  // Test doubles

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

    public List<DomainEvent> getPublishedEvents() {
      return publishedEvents;
    }
  }
}
