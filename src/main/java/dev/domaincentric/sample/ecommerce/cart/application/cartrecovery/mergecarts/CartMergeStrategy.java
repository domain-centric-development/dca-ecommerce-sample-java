package dev.domaincentric.sample.ecommerce.cart.application.mergecarts;

/**
 * Enumeration of available cart merge strategies.
 *
 * <p>When a user logs in and both their anonymous cart and account cart have items, they can choose
 * one of these strategies to resolve the conflict.
 */
public enum CartMergeStrategy {

  /**
   * Merge both carts by combining items. Items from the anonymous cart are added to the account
   * cart. If the same product exists in both, quantities are combined. The anonymous cart is
   * deleted after merge.
   */
  MERGE_BOTH,

  /** Keep only the account cart, discard the anonymous cart. The anonymous cart is deleted. */
  USE_ACCOUNT_CART,

  /**
   * Keep only the anonymous cart, discard the account cart. The account cart items are replaced
   * with anonymous cart items. The anonymous cart is deleted and its items moved to account cart.
   */
  USE_ANONYMOUS_CART
}
