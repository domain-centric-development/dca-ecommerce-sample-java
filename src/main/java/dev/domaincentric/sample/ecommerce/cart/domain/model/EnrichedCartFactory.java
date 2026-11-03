package dev.domaincentric.sample.ecommerce.cart.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Factory;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Factory for assembling EnrichedCart instances from ShoppingCart and article data.
 *
 * <p>Encapsulates the complex assembly of enriched cart items by combining cart items with their
 * corresponding article data to create a fully hydrated EnrichedCart.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ul>
 *   <li>Creates EnrichedCartItem instances by pairing cart items with articles; a cart item whose
 *       product has no article data — the catalogue no longer knows it — is kept without one
 *   <li>Assembles the final EnrichedCart with all enriched items
 * </ul>
 */
public final class EnrichedCartFactory implements Factory {

  /**
   * Creates an EnrichedCart from a ShoppingCart and article data.
   *
   * @param cart the shopping cart to enrich
   * @param articleData map of product IDs to their corresponding article data; a product missing
   *     from it is enriched without an article
   * @return a new EnrichedCart with enriched items
   */
  public EnrichedCart create(
      final ShoppingCart cart, final Map<ProductId, CartArticle> articleData) {

    final List<EnrichedCartItem> enrichedItems = createEnrichedItems(cart.items(), articleData);

    return EnrichedCart.of(cart.id(), cart.customerId(), enrichedItems, cart.status());
  }

  private List<EnrichedCartItem> createEnrichedItems(
      final List<CartItem> items, final Map<ProductId, CartArticle> articleData) {

    final List<EnrichedCartItem> enrichedItems = new ArrayList<>();

    for (final CartItem item : items) {
      final Optional<CartArticle> article = Optional.ofNullable(articleData.get(item.productId()));
      enrichedItems.add(EnrichedCartItem.of(item, article));
    }

    return enrichedItems;
  }
}
