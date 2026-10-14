package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web;

import java.util.Arrays;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * A sentence a cart page — the cart or the merge page — shows after a redirect led the customer
 * there.
 *
 * <p>The notice travels with the redirect as a flash attribute, which the shop keeps in a sealed
 * cookie addressed to the page the redirect leads to: only its code ({@value #KEY}), never the
 * text, and the number a sentence names beside it ({@value #COUNT_KEY}); the page words the
 * sentence. A code the page does not know, or a number it needs and does not get, shows nothing.
 *
 * <p>The checkout sends the customer back to the cart with some of these codes; it knows them, and
 * the two attributes, as the cart page's contract, the sentences stay here.
 */
public enum CartPageNotice {
  PRODUCT_ADDED("product-added", Kind.SUCCESS, "Product added to cart!"),
  NOT_ENOUGH_STOCK(
      "not-enough-stock", Kind.ERROR, "There is not enough stock to add %d of this product."),
  PRODUCT_UNAVAILABLE("product-unavailable", Kind.ERROR, "This product is no longer available."),
  CART_NOT_FOUND("cart-not-found", Kind.ERROR, "Your cart could not be found."),
  CART_NOT_MODIFIABLE("cart-not-modifiable", Kind.ERROR, "This cart can no longer be changed."),
  INVALID_QUANTITY("invalid-quantity", Kind.ERROR, "Please enter a valid quantity."),
  PRODUCT_NOT_ADDED(
      "product-not-added",
      Kind.ERROR,
      "The product could not be added to the cart. Please try again."),
  WELCOME_BACK("welcome-back", Kind.SUCCESS, "Welcome back!"),
  CARTS_MERGED("carts-merged", Kind.SUCCESS, "Carts merged successfully!"),
  ACCOUNT_CART_USED("account-cart-used", Kind.SUCCESS, "Using your account cart."),
  RECENT_CART_USED("recent-cart-used", Kind.SUCCESS, "Using your recent cart."),
  INVALID_MERGE_OPTION("invalid-merge-option", Kind.ERROR, "Invalid merge option selected"),
  NO_ACTIVE_CHECKOUT("no-active-checkout", Kind.ERROR, "No active checkout session found"),
  CHECKOUT_NOT_FOUND("checkout-not-found", Kind.ERROR, "Checkout session not found"),
  NO_CONFIRMED_ORDER("no-confirmed-order", Kind.ERROR, "No confirmed order found"),
  CART_NOT_CHECKABLE("cart-not-checkable", Kind.ERROR, "This cart can no longer be checked out."),
  EMPTY_CART("empty-cart", Kind.ERROR, "Cannot check out an empty cart."),
  CART_PRODUCT_UNAVAILABLE(
      "cart-product-unavailable", Kind.ERROR, "A product in your cart is no longer available."),
  ITEMS_UNAVAILABLE(
      "items-unavailable",
      Kind.ERROR,
      "Cannot start checkout, %d item(s) unavailable or out of stock."),
  CHECKOUT_NOT_STARTED(
      "checkout-not-started", Kind.ERROR, "The checkout could not be started. Please try again.");

  /** The flash attribute that carries a cart page notice's code. */
  public static final String KEY = "notice.cart";

  /** The flash attribute that carries the number a notice names. */
  public static final String COUNT_KEY = "notice.count";

  /** Whether the page shows a notice as a success or as an error. */
  public enum Kind {
    SUCCESS,
    ERROR
  }

  private final String code;
  private final Kind kind;
  private final String sentence;

  CartPageNotice(final String code, final Kind kind, final String sentence) {
    this.code = code;
    this.kind = kind;
    this.sentence = sentence;
  }

  /**
   * The notice with that code.
   *
   * @param code the code a redirect carried, absent when it carried none
   * @return the notice, or empty for an absent or unknown code
   */
  public static Optional<CartPageNotice> fromCode(final @Nullable String code) {
    return Arrays.stream(values()).filter(notice -> notice.code.equals(code)).findFirst();
  }

  /**
   * Puts the sentence of the notice a redirect handed the page on the page's model: a success as
   * {@code message}, an error as {@code error}. An unknown code, or a missing number, puts nothing.
   *
   * @param model the page's model, where Spring puts the flash attributes addressed to the page
   */
  public static void show(final Model model) {
    fromCode(model.getAttribute(KEY) instanceof String code ? code : null)
        .ifPresent(
            notice ->
                notice
                    .sentence(model.getAttribute(COUNT_KEY) instanceof Integer count ? count : null)
                    .ifPresent(
                        text ->
                            model.addAttribute(
                                notice.kind == Kind.SUCCESS ? "message" : "error", text)));
  }

  /**
   * The sentence the page shows.
   *
   * @param count the number the redirect carried, if any
   * @return the sentence, or empty for a notice that names a number the redirect did not carry
   */
  public Optional<String> sentence(final @Nullable Integer count) {
    if (!namesANumber()) {
      return Optional.of(sentence);
    }
    return Optional.ofNullable(count).map(number -> sentence.formatted(number));
  }

  /**
   * The Spring MVC redirect to a cart page, handing it this notice.
   *
   * @param path the page's path, with the query it needs
   * @param attributes the redirect's attributes, which take the notice
   * @return {@code redirect:} followed by the path
   */
  public String redirectTo(final String path, final RedirectAttributes attributes) {
    attributes.addFlashAttribute(KEY, code);
    return "redirect:" + path;
  }

  /**
   * The Spring MVC redirect to a cart page, handing it this notice and the number it names.
   *
   * @param path the page's path, with the query it needs
   * @param count the number the sentence names
   * @param attributes the redirect's attributes, which take the notice
   * @return {@code redirect:} followed by the path
   */
  public String redirectTo(
      final String path, final int count, final RedirectAttributes attributes) {
    attributes.addFlashAttribute(COUNT_KEY, count);
    return redirectTo(path, attributes);
  }

  public String code() {
    return code;
  }

  public Kind kind() {
    return kind;
  }

  private boolean namesANumber() {
    return sentence.contains("%d");
  }
}
