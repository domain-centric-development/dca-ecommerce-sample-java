package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The notices the checkout hands the cart page when it sends the customer back there. They are part
 * of the cart page's contract, like its path: the flash attributes and the codes are the ones the
 * cart page reads, and the cart page words the sentences.
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

  /** The flash attribute the cart page reads a notice's code from. */
  public static final String KEY = "notice.cart";

  /** The flash attribute the cart page reads the number a notice names from. */
  public static final String COUNT_KEY = "notice.count";

  private final String code;

  CartNotice(final String code) {
    this.code = code;
  }

  /**
   * Hands this notice to the cart page the redirect leads to.
   *
   * @param attributes the redirect's attributes
   */
  public void addTo(final RedirectAttributes attributes) {
    attributes.addFlashAttribute(KEY, code);
  }

  /**
   * Hands this notice and the number it names to the cart page the redirect leads to.
   *
   * @param attributes the redirect's attributes
   * @param count the number the sentence names
   */
  public void addTo(final RedirectAttributes attributes, final int count) {
    addTo(attributes);
    attributes.addFlashAttribute(COUNT_KEY, count);
  }

  public String code() {
    return code;
  }
}
