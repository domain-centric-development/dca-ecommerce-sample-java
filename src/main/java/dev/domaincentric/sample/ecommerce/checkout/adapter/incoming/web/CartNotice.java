package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

/**
 * The notices the checkout hands the cart page when it sends the customer back there. They are part
 * of the cart page's address, like its path: the codes are the ones the cart page knows, and the
 * cart page words the sentences.
 */
public enum CartNotice {
  NO_ACTIVE_CHECKOUT("no-active-checkout"),
  CHECKOUT_NOT_FOUND("checkout-not-found"),
  NO_CONFIRMED_ORDER("no-confirmed-order"),
  CART_NOT_FOUND("cart-not-found"),
  CART_NOT_CHECKABLE("cart-not-checkable"),
  EMPTY_CART("empty-cart"),
  CART_PRODUCT_UNAVAILABLE("cart-product-unavailable"),
  ITEMS_UNAVAILABLE("items-unavailable"),
  CHECKOUT_NOT_STARTED("checkout-not-started");

  private final String code;

  CartNotice(final String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }
}
