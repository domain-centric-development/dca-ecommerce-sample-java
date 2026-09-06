package dev.domaincentric.sample.ecommerce.cart.application.getcartbyid;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ArticleDataPort;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartArticle;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartFactory;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving a shopping cart by its ID.
 *
 * <p>This is a query use case that retrieves cart details without modifying state. Returns a {@link
 * GetCartByIdResult} containing an {@link EnrichedCart} read model that combines cart state with
 * current article data (pricing, availability).
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetCartByIdInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
public class GetCartByIdUseCase implements GetCartByIdInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final ArticleDataPort articleDataPort;
  private final EnrichedCartFactory enrichedCartFactory;

  public GetCartByIdUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final ArticleDataPort articleDataPort,
      final EnrichedCartFactory enrichedCartFactory) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.articleDataPort = articleDataPort;
    this.enrichedCartFactory = enrichedCartFactory;
  }

  @Override
  public GetCartByIdResult execute(final GetCartByIdQuery input) {
    final CartId cartId = CartId.of(input.cartId());

    // Scoped to the caller: a cart that is not theirs is indistinguishable from one that does not
    // exist.
    final Optional<ShoppingCart> cartOpt =
        shoppingCartRepository.findByIdForCustomer(cartId, CustomerId.of(input.customerId()));

    if (cartOpt.isEmpty()) {
      return GetCartByIdResult.notFound();
    }

    final ShoppingCart cart = cartOpt.get();

    // Collect product IDs and fetch article data in batch
    final Set<ProductId> productIds =
        cart.items().stream().map(item -> item.productId()).collect(Collectors.toSet());

    final Map<ProductId, CartArticle> articleData = articleDataPort.getArticleData(productIds);

    // The factory assembles the enriched read model from cart state and current article data
    final EnrichedCart enrichedCart = enrichedCartFactory.create(cart, articleData);

    return GetCartByIdResult.found(enrichedCart);
  }
}
