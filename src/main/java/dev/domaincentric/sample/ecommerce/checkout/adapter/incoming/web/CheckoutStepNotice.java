package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import java.util.Arrays;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * A sentence a checkout step page shows after a redirect led the customer there.
 *
 * <p>The notice travels with the redirect as a flash attribute, which the shop keeps in a sealed
 * cookie addressed to the page the redirect leads to: only its code ({@value #KEY}), never the
 * text, and the number or the product name a sentence names beside it ({@value #COUNT_KEY}, {@value
 * #NAME_KEY}); the page words the sentence. A code the page does not know, or a number or a name it
 * needs and does not get, shows nothing.
 */
public enum CheckoutStepNotice {
  EARLIER_STEPS_FIRST(
      Kind.REFUSAL, "earlier-steps-first", "Please complete the previous checkout steps first."),
  SESSION_NOT_FOUND(Kind.REFUSAL, "session-not-found", "Checkout session not found"),
  CHECKOUT_NOT_MODIFIABLE(
      Kind.REFUSAL, "checkout-not-modifiable", "This checkout can no longer be changed."),
  INVALID_BUYER_DETAILS(
      Kind.REFUSAL, "invalid-buyer-details", "Please check the details you entered."),
  BUYER_DETAILS_NOT_SAVED(
      Kind.REFUSAL,
      "buyer-details-not-saved",
      "Your details could not be saved. Please try again."),
  INVALID_DELIVERY_ADDRESS(
      Kind.REFUSAL, "invalid-delivery-address", "Please check the delivery address you entered."),
  SHIPPING_METHOD_NOT_OFFERED(
      Kind.REFUSAL,
      "shipping-method-not-offered",
      "This shipping method is not offered. Please choose another one."),
  DELIVERY_NOT_SAVED(
      Kind.REFUSAL,
      "delivery-not-saved",
      "Your delivery details could not be saved. Please try again."),
  PAYMENT_REFUSED(
      Kind.REFUSAL,
      "payment-refused",
      "The payment was refused. Please choose another way to pay."),
  PAYMENT_PROVIDER_UNAVAILABLE(
      Kind.REFUSAL,
      "payment-provider-unavailable",
      "The payment provider is not available right now. Please try again later."),
  PAYMENT_METHOD_NOT_OFFERED(
      Kind.REFUSAL,
      "payment-method-not-offered",
      "This way to pay is not offered. Please choose another one."),
  NOTHING_TO_PAY(Kind.REFUSAL, "nothing-to-pay", "There is nothing to pay for this order."),
  PAYMENT_METHOD_MISSING(Kind.REFUSAL, "payment-method-missing", "Please choose a way to pay."),
  PAYMENT_NOT_SAVED(
      Kind.REFUSAL,
      "payment-not-saved",
      "Your payment selection could not be saved. Please try again."),
  ITEMS_UNAVAILABLE(
      Kind.REFUSAL,
      "items-unavailable",
      "Cannot place the order, %d item(s) unavailable or out of stock."),
  STOCK_RAN_SHORT(
      Kind.REFUSAL, "stock-ran-short", "Not enough stock left for '%s'. Please adjust your order."),
  REVIEW_FIRST(Kind.REFUSAL, "review-first", "Please review your order before placing it."),
  ORDER_NOT_PLACED(
      Kind.REFUSAL, "order-not-placed", "Your order could not be placed. Please try again."),
  ORDER_NOT_CONFIRMED(Kind.REFUSAL, "order-not-confirmed", "Order has not been confirmed yet");

  /** What a notice tells the customer, and so the page attribute it is shown under. */
  public enum Kind {
    /** Why something was refused, shown as the page's error. */
    REFUSAL("error"),
    /** What happened, shown as the page's notice. */
    INFORMATION("notice");

    private final String pageAttribute;

    Kind(final String pageAttribute) {
      this.pageAttribute = pageAttribute;
    }
  }

  /** The flash attribute that carries a checkout step notice's code. */
  public static final String KEY = "notice.checkout-step";

  /** The flash attribute that carries the number a notice names. */
  public static final String COUNT_KEY = "notice.count";

  /** The flash attribute that carries the product name a notice names. */
  public static final String NAME_KEY = "notice.name";

  private final Kind kind;
  private final String code;
  private final String sentence;

  CheckoutStepNotice(final Kind kind, final String code, final String sentence) {
    this.kind = kind;
    this.code = code;
    this.sentence = sentence;
  }

  /**
   * The notice with that code.
   *
   * @param code the code a redirect carried, absent when it carried none
   * @return the notice, or empty for an absent or unknown code
   */
  public static Optional<CheckoutStepNotice> fromCode(final @Nullable String code) {
    return Arrays.stream(values()).filter(notice -> notice.code.equals(code)).findFirst();
  }

  /**
   * Shows the notice a redirect brought to this page: reads it from the page's model, where Spring
   * puts the flash attributes addressed to the page, and puts its sentence there under the
   * attribute its kind names — a refusal as the page's error, any other as its notice. Nothing is
   * shown when no notice came, its code is unknown or its number or name missing.
   *
   * @param model the page's model
   */
  public static void showIn(final Model model) {
    fromCode(model.getAttribute(KEY) instanceof String code ? code : null)
        .ifPresent(
            notice ->
                notice
                    .sentence(
                        model.getAttribute(COUNT_KEY) instanceof Integer count ? count : null,
                        model.getAttribute(NAME_KEY) instanceof String name ? name : null)
                    .ifPresent(text -> model.addAttribute(notice.kind.pageAttribute, text)));
  }

  /**
   * Hands this notice to the page the redirect leads to.
   *
   * @param attributes the redirect's attributes
   */
  public void addTo(final RedirectAttributes attributes) {
    attributes.addFlashAttribute(KEY, code);
  }

  /**
   * Hands this notice and the number it names to the page the redirect leads to.
   *
   * @param attributes the redirect's attributes
   * @param count the number the sentence names
   */
  public void addTo(final RedirectAttributes attributes, final int count) {
    addTo(attributes);
    attributes.addFlashAttribute(COUNT_KEY, count);
  }

  /**
   * Hands this notice and the product name it names to the page the redirect leads to.
   *
   * @param attributes the redirect's attributes
   * @param name the product name the sentence names
   */
  public void addTo(final RedirectAttributes attributes, final String name) {
    addTo(attributes);
    attributes.addFlashAttribute(NAME_KEY, name);
  }

  public String code() {
    return code;
  }

  /**
   * The sentence the page shows.
   *
   * @param count the number the redirect carried, if any
   * @return the sentence, or empty for a notice that names a number the redirect did not carry
   */
  public Optional<String> sentence(final @Nullable Integer count) {
    return sentence(count, null);
  }

  /**
   * The sentence the page shows.
   *
   * @param count the number the redirect carried, if any
   * @param name the product name the redirect carried, if any
   * @return the sentence, or empty for a notice that names a number or a name the redirect did not
   *     carry
   */
  public Optional<String> sentence(final @Nullable Integer count, final @Nullable String name) {
    if (sentence.contains("%s")) {
      return Optional.ofNullable(name).map(product -> sentence.formatted(product));
    }
    if (sentence.contains("%d")) {
      return Optional.ofNullable(count).map(number -> sentence.formatted(number));
    }
    return Optional.of(sentence);
  }
}
