package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.HashMap;
import java.util.Map;

/**
 * Functional interface for resolving current pricing and availability information for articles
 * during checkout operations. This resolver provides a domain-level abstraction for obtaining fresh
 * pricing data that may come from external systems.
 */
@FunctionalInterface
public interface CheckoutArticlePriceResolver {

  /**
   * Resolves the current price and availability for an article.
   *
   * @param productId the product identifier to resolve pricing for
   * @return the article price information including availability status
   */
  ArticlePrice resolve(ProductId productId);

  /**
   * Value object containing pricing and availability information for an article.
   *
   * @param price the current price of the article
   * @param isAvailable whether the article is currently available for purchase
   * @param availableStock the quantity currently available in stock
   */
  record ArticlePrice(Money price, boolean isAvailable, int availableStock) implements Value {

    public ArticlePrice {
      if (availableStock < 0) {
        throw new IllegalArgumentException("Available stock cannot be negative");
      }
    }

    /**
     * Today's facts about each article: its price, whether it is available and its stock.
     *
     * @param articles the article data as the catalogue answers it now, per product
     * @return the facts per product
     */
    public static Map<ProductId, ArticlePrice> allOf(
        final Map<ProductId, CheckoutArticle> articles) {
      final Map<ProductId, ArticlePrice> facts = new HashMap<>();
      articles.forEach(
          (product, article) ->
              facts.put(
                  product,
                  new ArticlePrice(
                      article.currentPrice(), article.isAvailable(), article.availableStock())));
      return Map.copyOf(facts);
    }
  }
}
