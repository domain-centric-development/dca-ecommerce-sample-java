package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.LineItemSnapshot;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * ViewModel for the buyer information page.
 *
 * <p>Contains data needed to render the buyer info form and cart summary sidebar.
 */
public record BuyerInfoPageViewModel(
    String sessionId,
    List<LineItemViewModel> lineItems,
    TotalsViewModel totals,
    @Nullable BuyerInfoViewModel existingBuyerInfo) {

  /** Creates a BuyerInfoPageViewModel from a CheckoutCartSnapshot. */
  public static BuyerInfoPageViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
    return new BuyerInfoPageViewModel(
        snapshot.sessionId().value(),
        snapshot.lineItems().stream().map(LineItemViewModel::fromSnapshot).toList(),
        TotalsViewModel.fromSnapshot(snapshot),
        snapshot.buyerInfo() != null
            ? BuyerInfoViewModel.fromBuyerInfo(snapshot.buyerInfo())
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

  /** Order totals for cart summary. */
  public record TotalsViewModel(BigDecimal subtotal, String currencyCode) {
    static TotalsViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      return new TotalsViewModel(
          snapshot.subtotal().amount(), snapshot.subtotal().currency().getCurrencyCode());
    }
  }

  /** Existing buyer info for pre-filling the form. */
  public record BuyerInfoViewModel(String email, String firstName, String lastName, String phone) {
    static BuyerInfoViewModel fromBuyerInfo(
        final dev.domaincentric.sample.ecommerce.checkout.domain.model.BuyerInfo info) {
      return new BuyerInfoViewModel(info.email(), info.firstName(), info.lastName(), info.phone());
    }
  }
}
