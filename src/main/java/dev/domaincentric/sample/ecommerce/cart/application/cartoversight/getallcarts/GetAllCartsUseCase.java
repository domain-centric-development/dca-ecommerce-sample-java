package dev.domaincentric.sample.ecommerce.cart.application.cartoversight.getallcarts;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ArticleDataPort;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartArticle;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartItem;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartFactory;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving all shopping carts.
 *
 * <p>This is a query use case that retrieves all carts without modifying state. Each cart is
 * totalled at today's prices: the article data of every product in any cart is fetched in one
 * batch, and each cart is enriched with it, as the answer about one cart is.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetAllCartsInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
public class GetAllCartsUseCase implements GetAllCartsInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final ArticleDataPort articleDataPort;
  private final EnrichedCartFactory enrichedCartFactory;

  public GetAllCartsUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final ArticleDataPort articleDataPort,
      final EnrichedCartFactory enrichedCartFactory) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.articleDataPort = articleDataPort;
    this.enrichedCartFactory = enrichedCartFactory;
  }

  @Override
  public GetAllCartsResult execute(final GetAllCartsQuery input) {
    final List<ShoppingCart> carts = shoppingCartRepository.findAll();

    final Set<ProductId> productIds =
        carts.stream()
            .flatMap(cart -> cart.items().stream())
            .map(CartItem::productId)
            .collect(Collectors.toSet());

    final Map<ProductId, CartArticle> articleData = articleDataPort.getArticleData(productIds);

    final List<EnrichedCart> enrichedCarts =
        carts.stream().map(cart -> enrichedCartFactory.create(cart, articleData)).toList();

    return GetAllCartsResult.from(enrichedCarts);
  }
}
