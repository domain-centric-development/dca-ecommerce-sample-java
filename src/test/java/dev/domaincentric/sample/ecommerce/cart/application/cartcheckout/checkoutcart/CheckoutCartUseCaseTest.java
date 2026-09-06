package dev.domaincentric.sample.ecommerce.cart.application.checkoutcart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.AggregateRoot;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ArticleDataPort;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartArticle;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartValidationResult;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartFactory;
import dev.domaincentric.sample.ecommerce.cart.domain.model.Quantity;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Why a checkout is refused — and that the refusal says which of the two reasons applies. */
@DisplayName("CheckoutCartUseCase")
class CheckoutCartUseCaseTest {

  private static final CustomerId CUSTOMER = CustomerId.of("customer-1");
  private static final Price TEN = Price.of(Money.euro(10.0));

  private final TestShoppingCartRepository repository = new TestShoppingCartRepository();
  private final TestArticleDataPort articleDataPort = new TestArticleDataPort();
  private final CheckoutCartUseCase useCase =
      new CheckoutCartUseCase(
          repository,
          articleDataPort,
          new EnrichedCartFactory(),
          new ClearingDomainEventPublisher(),
          new ImmediateTransactionBoundary());

  @Test
  @DisplayName("an empty cart is refused by the aggregate, in its own words")
  void anEmptyCartIsRefusedByTheAggregate() {
    final ShoppingCart cart = repository.save(new ShoppingCart(CartId.generate(), CUSTOMER));

    final IllegalStateException failure =
        assertThrows(IllegalStateException.class, () -> execute(cart));

    assertEquals("Cannot checkout an empty cart", failure.getMessage());
  }

  @Test
  @DisplayName("a cart that is already checked out says so")
  void aCartThatIsAlreadyCheckedOutSaysSo() {
    final ShoppingCart cart = new ShoppingCart(CartId.generate(), CUSTOMER);
    final ProductId productId = ProductId.generate();
    cart.addItem(productId, Quantity.of(1), TEN);
    cart.checkout();
    repository.save(cart);
    articleDataPort.available(productId, 5);

    final IllegalStateException failure =
        assertThrows(IllegalStateException.class, () -> execute(cart));

    assertEquals("Cart is already checked out", failure.getMessage());
  }

  @Test
  @DisplayName("an article that is gone is reported as a validation error")
  void anArticleThatIsGoneIsReportedAsAValidationError() {
    final ShoppingCart cart = new ShoppingCart(CartId.generate(), CUSTOMER);
    final ProductId productId = ProductId.generate();
    cart.addItem(productId, Quantity.of(2), TEN);
    repository.save(cart);
    articleDataPort.unavailable(productId);

    final CheckoutCartUseCase.CartValidationException failure =
        assertThrows(CheckoutCartUseCase.CartValidationException.class, () -> execute(cart));

    final CartValidationResult validationResult = failure.getValidationResult();
    assertEquals(1, validationResult.errors().size());
    assertEquals(
        CartValidationResult.ErrorType.PRODUCT_UNAVAILABLE,
        validationResult.errors().get(0).type());
  }

  @Test
  @DisplayName("stock that does not cover the quantity is reported as a validation error")
  void stockThatDoesNotCoverTheQuantityIsReported() {
    final ShoppingCart cart = new ShoppingCart(CartId.generate(), CUSTOMER);
    final ProductId productId = ProductId.generate();
    cart.addItem(productId, Quantity.of(5), TEN);
    repository.save(cart);
    articleDataPort.available(productId, 3);

    final CheckoutCartUseCase.CartValidationException failure =
        assertThrows(CheckoutCartUseCase.CartValidationException.class, () -> execute(cart));

    assertEquals(
        CartValidationResult.ErrorType.INSUFFICIENT_STOCK,
        failure.getValidationResult().errors().get(0).type());
  }

  @Test
  @DisplayName("a cart with available articles is checked out")
  void aCartWithAvailableArticlesIsCheckedOut() {
    final ShoppingCart cart = new ShoppingCart(CartId.generate(), CUSTOMER);
    final ProductId productId = ProductId.generate();
    cart.addItem(productId, Quantity.of(2), TEN);
    repository.save(cart);
    articleDataPort.available(productId, 10);

    final CheckoutCartResult result = execute(cart);

    assertEquals(cart.id().value(), result.cartId());
    assertSame(CartStatus.CHECKED_OUT, cart.status());
  }

  private CheckoutCartResult execute(final ShoppingCart cart) {
    return useCase.execute(new CheckoutCartCommand(cart.id().value(), CUSTOMER.value()));
  }

  /** Publishing is not what these tests are about; clearing keeps the aggregate honest. */
  private static final class ClearingDomainEventPublisher implements DomainEventPublisher {
    @Override
    public void publish(final DomainEvent event) {
      // nothing to deliver to
    }

    @Override
    public void publishAndClearEvents(final AggregateRoot<?, ?> aggregate) {
      aggregate.clearDomainEvents();
    }
  }

  /** Runs the work right here — a test needs the boundary, not a transaction. */
  private static final class ImmediateTransactionBoundary implements TransactionBoundary {
    @Override
    public <T> T inTransaction(final Supplier<T> work) {
      return work.get();
    }
  }

  private static final class TestShoppingCartRepository implements ShoppingCartRepository {
    private final Map<CartId, ShoppingCart> carts = new HashMap<>();

    @Override
    public Optional<ShoppingCart> findById(final CartId cartId) {
      return Optional.ofNullable(carts.get(cartId));
    }

    @Override
    public ShoppingCart save(final ShoppingCart cart) {
      carts.put(cart.id(), cart);
      return cart;
    }

    @Override
    public void deleteById(final CartId cartId) {
      carts.remove(cartId);
    }

    @Override
    public List<ShoppingCart> findByCustomerId(final CustomerId customerId) {
      return carts.values().stream().filter(cart -> cart.customerId().equals(customerId)).toList();
    }

    @Override
    public Optional<ShoppingCart> findByIdForCustomer(
        final CartId cartId, final CustomerId customerId) {
      return findById(cartId).filter(cart -> cart.customerId().equals(customerId));
    }

    @Override
    public Optional<ShoppingCart> findActiveCartByCustomerId(final CustomerId customerId) {
      return findByCustomerId(customerId).stream().filter(ShoppingCart::isActive).findFirst();
    }

    @Override
    public List<ShoppingCart> findAll() {
      return new ArrayList<>(carts.values());
    }
  }

  private static final class TestArticleDataPort implements ArticleDataPort {
    private final Map<ProductId, CartArticle> articles = new HashMap<>();

    void available(final ProductId productId, final int stock) {
      articles.put(productId, article(productId, stock, true));
    }

    void unavailable(final ProductId productId) {
      articles.put(productId, article(productId, 0, false));
    }

    @Override
    public Map<ProductId, CartArticle> getArticleData(final Collection<ProductId> productIds) {
      final Map<ProductId, CartArticle> requested = new HashMap<>();
      productIds.forEach(
          productId -> {
            final CartArticle article = articles.get(productId);
            if (article != null) {
              requested.put(productId, article);
            }
          });
      return requested;
    }

    @Override
    public Optional<CartArticle> getArticleData(final ProductId productId) {
      return Optional.ofNullable(articles.get(productId));
    }

    private static CartArticle article(
        final ProductId productId, final int stock, final boolean isAvailable) {
      return CartArticle.of(productId, "Article", Money.euro(10.0), stock, isAvailable, "");
    }
  }
}
