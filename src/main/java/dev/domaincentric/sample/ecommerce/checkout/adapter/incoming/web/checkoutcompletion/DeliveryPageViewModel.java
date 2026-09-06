package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.LineItemSnapshot;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * ViewModel for the delivery/shipping page.
 *
 * <p>Contains data needed to render the delivery form, buyer info summary, and cart summary
 * sidebar.
 */
public record DeliveryPageViewModel(
    String sessionId,
    List<LineItemViewModel> lineItems,
    TotalsViewModel totals,
    BuyerInfoSummaryViewModel buyerInfo,
    @Nullable AddressViewModel existingAddress,
    @Nullable String existingShippingOptionId) {

  /** Creates a DeliveryPageViewModel from a CheckoutCartSnapshot. */
  public static DeliveryPageViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
    return new DeliveryPageViewModel(
        snapshot.sessionId().value(),
        snapshot.lineItems().stream().map(LineItemViewModel::fromSnapshot).toList(),
        TotalsViewModel.fromSnapshot(snapshot),
        BuyerInfoSummaryViewModel.fromSnapshot(snapshot),
        snapshot.deliveryAddress() != null
            ? AddressViewModel.fromAddress(snapshot.deliveryAddress())
            : null,
        snapshot.shippingOption() != null ? snapshot.shippingOption().id() : null);
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

  /** Order totals for cart summary. */
  public record TotalsViewModel(BigDecimal subtotal, String currencyCode) {
    static TotalsViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      return new TotalsViewModel(
          snapshot.subtotal().amount(), snapshot.subtotal().currency().getCurrencyCode());
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

  /** Existing address for pre-filling the form. */
  public record AddressViewModel(
      String street,
      @Nullable String streetLine2,
      String city,
      String postalCode,
      String country,
      @Nullable String state) {
    static AddressViewModel fromAddress(
        final dev.domaincentric.sample.ecommerce.checkout.domain.model.DeliveryAddress addr) {
      return new AddressViewModel(
          addr.street(),
          addr.streetLine2(),
          addr.city(),
          addr.postalCode(),
          addr.country(),
          addr.state());
    }
  }
}
