package dev.domaincentric.sample.ecommerce.product.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.product.application.getproductselection.GetProductSelectionResult;
import dev.domaincentric.sample.ecommerce.product.domain.model.EnrichedProduct;
import java.util.List;

/**
 * The homepage's "Discover products" slider: one card per drawn product, in draw order.
 *
 * @param cards the slider's cards
 */
public record ProductSliderViewModel(List<Card> cards) {

  public ProductSliderViewModel {
    cards = List.copyOf(cards);
  }

  /**
   * Creates the slider from the product selection.
   *
   * @param result the drawn products
   * @return the slider's view model
   */
  public static ProductSliderViewModel fromResult(final GetProductSelectionResult result) {
    return new ProductSliderViewModel(result.products().stream().map(Card::from).toList());
  }

  /**
   * Whether the slider has a card to show.
   *
   * @return true when at least one product was drawn
   */
  public boolean hasCards() {
    return !cards.isEmpty();
  }

  /**
   * One slider card.
   *
   * @param productId the product the card leads to
   * @param name the product name
   * @param imageUrl the product image URL, empty when the product has none
   * @param price the price as the product page shows it
   */
  public record Card(String productId, String name, String imageUrl, String price) {

    static Card from(final EnrichedProduct product) {
      return new Card(
          product.productId().value().toString(),
          product.name(),
          product.imageUrl() == null ? "" : product.imageUrl(),
          product.currentPrice().amount()
              + " "
              + product.currentPrice().currency().getCurrencyCode());
    }
  }
}
