package dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;

/**
 * What adding an item to a cart came to: the item was added, or one of the refusals a caller
 * answers. A caller switches over the cases; the compiler holds that it answers every one.
 */
public sealed interface AddItemToCartResult
    permits AddItemToCartResult.Added,
        AddItemToCartResult.ArticleNotAvailable,
        AddItemToCartResult.CartNotFound,
        AddItemToCartResult.InsufficientArticleStock,
        AddItemToCartResult.CartNotModifiable {

  /**
   * The item was added.
   *
   * @param cartId the cart ID
   * @param customerId the customer ID
   * @param items the updated list of cart items, each at today's price
   * @param total the total cart amount at today's prices
   */
  record Added(String cartId, String customerId, List<CartItemSummary> items, Money total)
      implements AddItemToCartResult {

    /** Builds the case from the cart as it stands after the item was added, at today's prices. */
    public static Added from(final EnrichedCart cart) {
      return new Added(
          cart.cartId().value(),
          cart.customerId().value(),
          cart.items().stream().map(CartItemSummary::from).toList(),
          cart.calculateCurrentSubtotal());
    }
  }

  /**
   * The article facts a cart position needs are not available for the product. The cart does not
   * own the assortment; a product the answer does not carry cannot become a position, and that is a
   * statement about the request, not about the cart.
   *
   * @param productId the product the caller named
   */
  record ArticleNotAvailable(ProductId productId) implements AddItemToCartResult, Refusal {}

  /**
   * The addressed cart is not available to the asking customer — it does not exist or is somebody
   * else's, told apart on purpose by nothing (ADR-036).
   *
   * @param cartId the cart the caller named
   */
  record CartNotFound(CartId cartId) implements AddItemToCartResult, Refusal {}

  /**
   * The article facts do not cover what the cart's line would hold: the quantity the customer wants
   * to add beside the quantity the line holds already. The figure is a snapshot the cart read
   * through its port, not the warehouse's own decision.
   *
   * @param productId the product
   * @param requested the quantity the customer wanted to add
   * @param held the quantity the cart's line held already
   * @param inStock the stock the article facts named
   */
  record InsufficientArticleStock(ProductId productId, int requested, int held, int inStock)
      implements AddItemToCartResult, Refusal {}

  /**
   * The cart is no longer active and takes no item — the aggregate's rule.
   *
   * @param cartId the cart
   * @param status the status that refuses the change
   */
  record CartNotModifiable(CartId cartId, CartStatus status)
      implements AddItemToCartResult, Refusal {}
}
