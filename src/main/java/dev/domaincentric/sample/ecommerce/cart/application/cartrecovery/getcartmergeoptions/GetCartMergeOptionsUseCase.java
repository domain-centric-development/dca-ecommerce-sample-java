package dev.domaincentric.sample.ecommerce.cart.application.getcartmergeoptions;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ArticleDataPort;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartArticle;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartItem;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 * Use case for checking if cart merge options should be presented.
 *
 * <p>When a user logs in, this use case determines if both the anonymous session and the registered
 * account have active carts with items. If so, the user should be presented with options to choose
 * how to handle the cart conflict.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetCartMergeOptionsInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
public class GetCartMergeOptionsUseCase implements GetCartMergeOptionsInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final ArticleDataPort articleDataPort;

  public GetCartMergeOptionsUseCase(
      final ShoppingCartRepository shoppingCartRepository, final ArticleDataPort articleDataPort) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.articleDataPort = articleDataPort;
  }

  @Override
  public GetCartMergeOptionsResult execute(final GetCartMergeOptionsQuery input) {
    final CustomerId anonymousCustomerId = CustomerId.of(input.anonymousUserId());
    final CustomerId registeredCustomerId = CustomerId.of(input.registeredUserId());

    // If same user ID, no merge needed (identity continuity case)
    if (anonymousCustomerId.equals(registeredCustomerId)) {
      return GetCartMergeOptionsResult.noMergeRequired();
    }

    // Find anonymous user's active cart
    final Optional<ShoppingCart> anonymousCart =
        shoppingCartRepository.findActiveCartByCustomerId(anonymousCustomerId);

    // Find registered user's active cart
    final Optional<ShoppingCart> accountCart =
        shoppingCartRepository.findActiveCartByCustomerId(registeredCustomerId);

    // Check if both carts exist and have items
    final boolean anonymousHasItems = anonymousCart.isPresent() && !anonymousCart.get().isEmpty();
    final boolean accountHasItems = accountCart.isPresent() && !accountCart.get().isEmpty();

    if (anonymousHasItems && accountHasItems) {
      // Fetch article data for all products in both carts
      final var allProductIds =
          Stream.concat(
                  anonymousCart.get().items().stream().map(CartItem::productId),
                  accountCart.get().items().stream().map(CartItem::productId))
              .collect(Collectors.toSet());
      final Map<ProductId, CartArticle> articleData = articleDataPort.getArticleData(allProductIds);

      // Both carts have items - user must choose
      return GetCartMergeOptionsResult.mergeRequired(
          toCartSummary(anonymousCart.get(), articleData),
          toCartSummary(accountCart.get(), articleData));
    }

    // No merge required - either one or both carts are empty
    return GetCartMergeOptionsResult.noMergeRequired();
  }

  private GetCartMergeOptionsResult.CartSummary toCartSummary(
      final ShoppingCart cart, final Map<ProductId, CartArticle> articleData) {
    final Money total = cart.calculateTotal();
    return new GetCartMergeOptionsResult.CartSummary(
        cart.id().value(),
        cart.itemCount(),
        cart.totalQuantity(),
        total.amount(),
        total.currency().getCurrencyCode(),
        cart.items().stream().map(item -> toItemSummary(item, articleData)).toList());
  }

  private GetCartMergeOptionsResult.CartItemSummary toItemSummary(
      final CartItem item, final Map<ProductId, CartArticle> articleData) {
    final CartArticle article = articleData.get(item.productId());
    final String name = article != null ? article.name() : item.productId().value();
    final String imageUrl = article != null ? article.imageUrl() : null;
    return new GetCartMergeOptionsResult.CartItemSummary(
        item.productId().value(),
        name,
        imageUrl,
        item.quantity().value(),
        item.priceAtAddition().value().amount(),
        item.priceAtAddition().value().currency().getCurrencyCode());
  }
}
