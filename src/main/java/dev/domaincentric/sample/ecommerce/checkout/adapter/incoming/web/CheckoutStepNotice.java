package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import java.util.Arrays;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * A sentence a checkout step page shows after a redirect led the customer there.
 *
 * <p>The shop runs as several instances without sticky sessions, so the notice travels with the
 * redirect as the query parameter {@value #PARAMETER}: only its code, never the text, so a URL
 * cannot make a page say anything else. A notice that names a number carries it beside the code
 * ({@value #COUNT_PARAMETER}); the page words the sentence. A code the page does not know, or a
 * number it needs and does not get, shows nothing.
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

  /** The query parameter that carries a notice's code. */
  public static final String PARAMETER = "notice";

  /** The query parameter that carries the number a notice names. */
  public static final String COUNT_PARAMETER = "count";

  private final String code;
  private final String sentence;

  CheckoutStepNotice(final String code, final String sentence) {
    this.code = code;
    this.sentence = sentence;
  }

  /**
   * The notice with that code.
   *
   * @param code the parameter's value, absent when the request carries none
   * @return the notice, or empty for an absent or unknown code
   */
  public static Optional<CheckoutStepNotice> fromCode(final @Nullable String code) {
    return Arrays.stream(values()).filter(notice -> notice.code.equals(code)).findFirst();
  }

  public String code() {
    return code;
  }

  /**
   * The sentence the page shows.
   *
   * @param count the number the address carried, if any
   * @return the sentence, or empty for a notice that names a number the address did not carry
   */
  public Optional<String> sentence(final @Nullable Integer count) {
    if (!sentence.contains("%d")) {
      return Optional.of(sentence);
    }
    return Optional.ofNullable(count).map(number -> sentence.formatted(number));
  }
}
