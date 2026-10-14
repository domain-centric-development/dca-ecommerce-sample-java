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
 * text, and the number a sentence names beside it ({@value #COUNT_KEY}); the page words the
 * sentence. A code the page does not know, or a number it needs and does not get, shows nothing.
 */
public enum CheckoutStepNotice {
  EARLIER_STEPS_FIRST("earlier-steps-first", "Please complete the previous checkout steps first."),
  SESSION_NOT_FOUND("session-not-found", "Checkout session not found"),
  CHECKOUT_NOT_MODIFIABLE("checkout-not-modifiable", "This checkout can no longer be changed."),
  INVALID_BUYER_DETAILS("invalid-buyer-details", "Please check the details you entered."),
  BUYER_DETAILS_NOT_SAVED(
      "buyer-details-not-saved", "Your details could not be saved. Please try again."),
  INVALID_DELIVERY_ADDRESS(
      "invalid-delivery-address", "Please check the delivery address you entered."),
  DELIVERY_NOT_SAVED(
      "delivery-not-saved", "Your delivery details could not be saved. Please try again."),
  PAYMENT_REFUSED("payment-refused", "The payment was refused. Please choose another way to pay."),
  PAYMENT_PROVIDER_UNAVAILABLE(
      "payment-provider-unavailable",
      "The payment provider is not available right now. Please try again later."),
  PAYMENT_METHOD_NOT_OFFERED(
      "payment-method-not-offered", "This way to pay is not offered. Please choose another one."),
  NOTHING_TO_PAY("nothing-to-pay", "There is nothing to pay for this order."),
  PAYMENT_METHOD_MISSING("payment-method-missing", "Please choose a way to pay."),
  PAYMENT_NOT_SAVED(
      "payment-not-saved", "Your payment selection could not be saved. Please try again."),
  ITEMS_UNAVAILABLE(
      "items-unavailable", "Cannot place the order, %d item(s) unavailable or out of stock."),
  REVIEW_FIRST("review-first", "Please review your order before placing it."),
  ORDER_NOT_PLACED("order-not-placed", "Your order could not be placed. Please try again."),
  ORDER_NOT_CONFIRMED("order-not-confirmed", "Order has not been confirmed yet");

  /** The flash attribute that carries a checkout step notice's code. */
  public static final String KEY = "notice.checkout-step";

  /** The flash attribute that carries the number a notice names. */
  public static final String COUNT_KEY = "notice.count";

  private final String code;
  private final String sentence;

  CheckoutStepNotice(final String code, final String sentence) {
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
   * The sentence of the notice a redirect brought to this page, read from the page's model, where
   * Spring puts the flash attributes addressed to the page.
   *
   * @param model the page's model
   * @return the sentence, or empty when no notice came, its code is unknown or its number missing
   */
  public static Optional<String> sentenceIn(final Model model) {
    return fromCode(model.getAttribute(KEY) instanceof String code ? code : null)
        .flatMap(
            notice ->
                notice.sentence(
                    model.getAttribute(COUNT_KEY) instanceof Integer count ? count : null));
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
    if (!sentence.contains("%d")) {
      return Optional.of(sentence);
    }
    return Optional.ofNullable(count).map(number -> sentence.formatted(number));
  }
}
