package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;
import java.util.OptionalInt;
import org.jspecify.annotations.Nullable;

/**
 * What the review of a checkout shows: its lines at today's prices, the buyer, the delivery
 * address, the shipping method with its price, the payment method and the totals. "Place Order"
 * orders exactly this, and only while it is still the review the customer was shown.
 *
 * @param sessionId the checkout reviewed
 * @param lineItems the lines at today's prices, in the checkout's order
 * @param totals the totals of those lines and the shipping
 * @param buyerInfo the buyer, if entered
 * @param deliveryAddress the delivery address, if entered
 * @param shippingOption the shipping method with its price, if chosen
 * @param paymentSelection the payment method, if chosen
 */
public record CheckoutReview(
    CheckoutSessionId sessionId,
    List<CheckoutLineItem> lineItems,
    CheckoutTotals totals,
    @Nullable BuyerInfo buyerInfo,
    @Nullable DeliveryAddress deliveryAddress,
    @Nullable ShippingOption shippingOption,
    @Nullable PaymentSelection paymentSelection)
    implements Value {

  /** Separates the fields of the content a digest is taken of. */
  private static final String FIELD = "\u001F";

  public CheckoutReview {
    if (sessionId == null) {
      throw new IllegalArgumentException("Session ID cannot be null");
    }
    if (lineItems == null) {
      throw new IllegalArgumentException("Line items cannot be null");
    }
    if (totals == null) {
      throw new IllegalArgumentException("Totals cannot be null");
    }
    lineItems = List.copyOf(lineItems);
  }

  /**
   * The fingerprint of everything this review shows.
   *
   * @return the fingerprint
   */
  public ReviewFingerprint fingerprint() {
    return ReviewFingerprint.digestOf(
        lineItems.stream().map(CheckoutReview::lineContent).toList(),
        String.join(
            FIELD,
            buyerContent(),
            addressContent(),
            shippingContent(),
            paymentSelection == null ? "" : paymentSelection.providerId().value(),
            amountOf(totals.total())));
  }

  /**
   * Refuses unless this review is the one the customer was shown.
   *
   * @param shown the fingerprint of the review the customer was shown
   * @throws CheckoutChangedSinceReviewException naming the product of the first line whose price
   *     changed, or naming none for any other change
   */
  public void refuseUnlessShown(final ReviewFingerprint shown) {
    final ReviewFingerprint today = fingerprint();
    if (today.equals(shown)) {
      return;
    }
    final OptionalInt changedLine = today.firstChangedLine(shown);
    throw changedLine.isPresent()
        ? CheckoutChangedSinceReviewException.priceChanged(
            sessionId, lineItems.get(changedLine.getAsInt()).productName())
        : CheckoutChangedSinceReviewException.orderChanged(sessionId);
  }

  /** What is ordered on a line — its product and quantity — apart from its unit price. */
  private static ReviewFingerprint.Line lineContent(final CheckoutLineItem line) {
    return new ReviewFingerprint.Line(
        String.join(FIELD, line.productId().value().toString(), String.valueOf(line.quantity())),
        amountOf(line.unitPrice()));
  }

  private String buyerContent() {
    return buyerInfo == null
        ? ""
        : String.join(
            FIELD,
            buyerInfo.email(),
            buyerInfo.firstName(),
            buyerInfo.lastName(),
            buyerInfo.phone());
  }

  private String addressContent() {
    return deliveryAddress == null
        ? ""
        : String.join(
            FIELD,
            deliveryAddress.street(),
            orEmpty(deliveryAddress.streetLine2()),
            deliveryAddress.city(),
            deliveryAddress.postalCode(),
            deliveryAddress.country(),
            orEmpty(deliveryAddress.state()));
  }

  private String shippingContent() {
    return shippingOption == null
        ? ""
        : String.join(
            FIELD, shippingOption.id(), shippingOption.name(), amountOf(shippingOption.cost()));
  }

  /** An amount as the fingerprint reads it: the same for 49.99 and 49.990. */
  private static String amountOf(final Money money) {
    return money.amount().stripTrailingZeros().toPlainString()
        + " "
        + money.currency().getCurrencyCode();
  }

  private static String orEmpty(final @Nullable String text) {
    return text == null ? "" : text;
  }
}
