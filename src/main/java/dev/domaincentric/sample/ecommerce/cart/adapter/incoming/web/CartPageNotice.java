package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
 * text, and the numbers a sentence names beside it — the first as {@value #COUNT_KEY}, each further
 * one as {@value #COUNT_KEY} followed by its position ({@code notice.count.2}, {@code
 * notice.count.3}); the page words the sentence. A code the page does not know, or a number it
 * needs and does not get, shows nothing.
 *
 * <p>The checkout sends the customer back to the cart with some of these codes; it knows them, and
 * the code and the first number's attribute, as the cart page's contract, the sentences stay here.
 */
public enum CartPageNotice {
  PRODUCT_ADDED("product-added", Kind.SUCCESS, "Product added to cart!"),
  NOT_ENOUGH_STOCK(
      "not-enough-stock",
      Kind.ERROR,
      "There is not enough stock to add %d of this product — your cart already holds %d, and %d"
          + " are in stock."),
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

  /** The flash attribute that carries the first number a notice names. */
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
                    .sentence(countsOn(model))
                    .ifPresent(
                        text ->
                            model.addAttribute(
                                notice.kind == Kind.SUCCESS ? "message" : "error", text)));
  }

  /**
   * The sentence the page shows.
   *
   * @param counts the numbers the redirect carried, in the order the sentence names them
   * @return the sentence, or empty when the redirect did not carry every number it names
   */
  public Optional<String> sentence(final List<Integer> counts) {
    final int named = numbersNamed();
    if (counts.size() < named) {
      return Optional.empty();
    }
    return Optional.of(sentence.formatted(counts.subList(0, named).toArray()));
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
    return redirectTo(path, attributes, count);
  }

  /**
   * The Spring MVC redirect to a cart page, handing it this notice and the numbers it names.
   *
   * @param path the page's path, with the query it needs
   * @param attributes the redirect's attributes, which take the notice
   * @param counts the numbers the sentence names, in its order
   * @return {@code redirect:} followed by the path
   */
  public String redirectTo(
      final String path, final RedirectAttributes attributes, final int... counts) {
    for (int position = 1; position <= counts.length; position++) {
      attributes.addFlashAttribute(countKey(position), counts[position - 1]);
    }
    return redirectTo(path, attributes);
  }

  public String code() {
    return code;
  }

  public Kind kind() {
    return kind;
  }

  /** The numbers on the model, in order, up to the first position the redirect did not carry. */
  private static List<Integer> countsOn(final Model model) {
    final List<Integer> counts = new ArrayList<>();
    while (model.getAttribute(countKey(counts.size() + 1)) instanceof Integer count) {
      counts.add(count);
    }
    return counts;
  }

  private static String countKey(final int position) {
    return position == 1 ? COUNT_KEY : COUNT_KEY + "." + position;
  }

  private int numbersNamed() {
    return sentence.split("%d", -1).length - 1;
  }
}
