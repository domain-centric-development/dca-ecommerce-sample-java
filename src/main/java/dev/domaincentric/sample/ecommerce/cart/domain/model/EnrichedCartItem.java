package dev.domaincentric.sample.ecommerce.cart.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Optional;

/**
 * Value Object that combines CartItem data with current CartArticle data.
 *
 * <p>Enables domain logic like determining if the price has changed since the item was added to the
 * cart and whether there is sufficient stock for the requested quantity.
 *
 * <p>The article is absent for a product the catalogue no longer knows: the line stays, with its
 * id, product and quantity.
 *
 * <p>Note: This value object extracts data from CartItem (an Entity) to maintain DDD principle that
 * value objects should not contain entities.
 */
public record EnrichedCartItem(
    CartItemId cartItemId,
    ProductId productId,
    Quantity quantity,
    Price priceAtAddition,
    Optional<CartArticle> article,
    String positionSnapshot)
    implements Value {

  public EnrichedCartItem(
      CartItemId id, ProductId productId, Quantity quantity, Price price, CartArticle article) {
    this(
        id,
        productId,
        quantity,
        price,
        article == null ? null : Optional.of(article),
        id.value() + ":" + PositionUnits.initial(quantity.value()).serialize());
  }

  public EnrichedCartItem {
    if (cartItemId == null) {
      throw new IllegalArgumentException("Cart item ID cannot be null");
    }
    if (productId == null) {
      throw new IllegalArgumentException("Product ID cannot be null");
    }
    if (quantity == null) {
      throw new IllegalArgumentException("Quantity cannot be null");
    }
    if (priceAtAddition == null) {
      throw new IllegalArgumentException("Price at addition cannot be null");
    }
    if (article == null) {
      throw new IllegalArgumentException("Article cannot be null");
    }
    if (article.isPresent() && !productId.equals(article.get().productId())) {
      throw new IllegalArgumentException(
          "Product ID must match between cart item and current article");
    }
  }

  /**
   * The current article of a line whose product the catalogue knows.
   *
   * @return the article
   * @throws java.util.NoSuchElementException for a line whose product the catalogue no longer knows
   */
  public CartArticle currentArticle() {
    return article.orElseThrow();
  }

  /**
   * Whether the line can be sold today.
   *
   * @return false for a line whose product the catalogue no longer knows
   */
  public boolean isAvailable() {
    return article.map(CartArticle::isAvailable).orElse(false);
  }

  /**
   * Today's unit price of the line's article.
   *
   * @return empty for a line whose product the catalogue no longer knows
   */
  public Optional<Money> currentPrice() {
    return article.map(CartArticle::currentPrice);
  }

  /**
   * Creates a new EnrichedCartItem from a CartItem entity and its current article.
   *
   * @param cartItem the cart item entity
   * @param article the current article data, empty when the catalogue no longer knows the product
   * @return a new EnrichedCartItem instance
   */
  public static EnrichedCartItem of(final CartItem cartItem, final Optional<CartArticle> article) {
    return new EnrichedCartItem(
        cartItem.id(),
        cartItem.productId(),
        cartItem.quantity(),
        cartItem.priceAtAddition(),
        article,
        cartItem.positionSnapshot());
  }

  /**
   * Calculates the line total using the current article price.
   *
   * @return the total calculated as currentPrice * quantity
   */
  public Money currentLineTotal() {
    return currentArticle().currentPrice().multiply(quantity.value());
  }

  /**
   * Calculates the original line total using the price at addition.
   *
   * @return the total calculated as priceAtAddition * quantity
   */
  public Money originalLineTotal() {
    return priceAtAddition.multiply(quantity.value());
  }

  /**
   * Checks if the price has changed since the item was added to the cart.
   *
   * @return true if the current price differs from the price at addition; false for a line without
   *     a current price
   */
  public boolean hasPriceChanged() {
    return currentPrice().map(current -> !current.equals(priceAtAddition.value())).orElse(false);
  }

  /**
   * Checks whether the current price is above the price at addition.
   *
   * @return true if the price went up since the item was added
   */
  public boolean priceIncreased() {
    return currentArticle().currentPrice().isGreaterThan(priceAtAddition.value());
  }

  /**
   * Returns the absolute price difference between current and original unit price.
   *
   * @return the absolute difference between current price and price at addition
   */
  public Money priceDifference() {
    var currentPrice = currentArticle().currentPrice();
    var originalPrice = priceAtAddition.value();

    if (currentPrice.isGreaterThan(originalPrice)) {
      return currentPrice.subtract(originalPrice);
    } else {
      return originalPrice.subtract(currentPrice);
    }
  }

  /**
   * Checks if there is sufficient stock for the requested quantity.
   *
   * @return true if the current article has enough stock for the cart item quantity
   */
  public boolean hasSufficientStock() {
    return currentArticle().hasStockFor(quantity.value());
  }

  /**
   * Checks if this cart item is valid for checkout.
   *
   * <p>An item is valid for checkout if the product is available AND there is sufficient stock for
   * the requested quantity; a line whose product the catalogue no longer knows never is.
   *
   * @return true if the item can proceed to checkout
   */
  public boolean isValidForCheckout() {
    return isAvailable() && hasSufficientStock();
  }
}
