package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.LineItemSnapshot;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * ViewModel for the payment method page.
 *
 * <p>Contains data needed to render the payment form, order summary with shipping costs, and
 * previous step summaries.
 */
public record PaymentPageViewModel(
    String sessionId,
    List<LineItemViewModel> lineItems,
    TotalsViewModel totals,
    BuyerInfoSummaryViewModel buyerInfo,
    DeliverySummaryViewModel delivery,
    @Nullable String existingPaymentProviderId) {

  /** Creates a PaymentPageViewModel from a CheckoutCartSnapshot. */
  public static PaymentPageViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
    return new PaymentPageViewModel(
        snapshot.sessionId().value(),
        snapshot.lineItems().stream().map(LineItemViewModel::fromSnapshot).toList(),
        TotalsViewModel.fromSnapshot(snapshot),
        BuyerInfoSummaryViewModel.fromSnapshot(snapshot),
        DeliverySummaryViewModel.fromSnapshot(snapshot),
        snapshot.paymentSelection() != null
            ? snapshot.paymentSelection().providerId().value()
            : null);
  }

  /** Line item for cart summary display. */
  public record LineItemViewModel(
      String productName,
      int quantity,
      BigDecimal lineTotal,
      String currencyCode,
      String imageUrl) {
    static LineItemViewModel fromSnapshot(final LineItemSnapshot item) {
      return new LineItemViewModel(
          item.name(),
          item.quantity(),
          item.lineTotal().amount(),
          item.lineTotal().currency().getCurrencyCode(),
          item.imageUrl());
    }
  }

  /** Order totals including shipping. */
  public record TotalsViewModel(
      BigDecimal subtotal, BigDecimal shipping, BigDecimal total, String currencyCode) {
    static TotalsViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      if (snapshot.totals() != null) {
        final var totals = snapshot.totals();
        return new TotalsViewModel(
            totals.subtotal().amount(),
            totals.shipping().amount(),
            totals.total().amount(),
            totals.total().currency().getCurrencyCode());
      }
      return new TotalsViewModel(
          snapshot.subtotal().amount(),
          BigDecimal.ZERO,
          snapshot.subtotal().amount(),
          snapshot.subtotal().currency().getCurrencyCode());
    }
  }

  /** Buyer info summary for display. */
  public record BuyerInfoSummaryViewModel(String email, String fullName) {
    static BuyerInfoSummaryViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      final var info = snapshot.buyerInfo();
      if (info == null) {
        return new BuyerInfoSummaryViewModel("", "");
      }
      return new BuyerInfoSummaryViewModel(info.email(), info.firstName() + " " + info.lastName());
    }
  }

  /** Delivery summary for display. */
  public record DeliverySummaryViewModel(
      String shippingAddress, String shippingMethod, BigDecimal shippingCost, String currencyCode) {
    static DeliverySummaryViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      final var addr = snapshot.deliveryAddress();
      final var shipping = snapshot.shippingOption();
      if (addr == null || shipping == null) {
        return new DeliverySummaryViewModel("", "", BigDecimal.ZERO, "EUR");
      }
      final String address = addr.street() + ", " + addr.city() + " " + addr.postalCode();
      return new DeliverySummaryViewModel(
          address,
          shipping.name(),
          shipping.cost().amount(),
          shipping.cost().currency().getCurrencyCode());
    }
  }
}
