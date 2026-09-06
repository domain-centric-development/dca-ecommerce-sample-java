package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.LineItemSnapshot;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * ViewModel for the checkout review page.
 *
 * <p>Contains all data needed to render the order review page where the customer reviews their
 * complete order before confirmation.
 */
public record ReviewPageViewModel(
    String sessionId,
    String currentStep,
    List<LineItemViewModel> lineItems,
    TotalsViewModel totals,
    @Nullable BuyerInfoViewModel buyerInfo,
    @Nullable DeliveryViewModel delivery,
    @Nullable PaymentViewModel payment) {

  /** Creates a ReviewPageViewModel from a CheckoutCartSnapshot. */
  public static ReviewPageViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
    return new ReviewPageViewModel(
        snapshot.sessionId().value(),
        snapshot.step().name(),
        snapshot.lineItems().stream().map(LineItemViewModel::fromSnapshot).toList(),
        TotalsViewModel.fromSnapshot(snapshot),
        BuyerInfoViewModel.fromSnapshot(snapshot),
        DeliveryViewModel.fromSnapshot(snapshot),
        PaymentViewModel.fromSnapshot(snapshot));
  }

  /** Line item for display. */
  public record LineItemViewModel(
      String productId,
      String productName,
      int quantity,
      BigDecimal unitPrice,
      BigDecimal lineTotal,
      String currencyCode,
      String imageUrl) {
    static LineItemViewModel fromSnapshot(final LineItemSnapshot item) {
      return new LineItemViewModel(
          item.productId().value(),
          item.name(),
          item.quantity(),
          item.price().amount(),
          item.lineTotal().amount(),
          item.price().currency().getCurrencyCode(),
          item.imageUrl());
    }
  }

  /** Order totals for display. */
  public record TotalsViewModel(
      BigDecimal subtotal,
      BigDecimal shipping,
      BigDecimal tax,
      BigDecimal total,
      String currencyCode) {
    static TotalsViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      if (snapshot.totals() == null) {
        return new TotalsViewModel(
            snapshot.subtotal().amount(),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            snapshot.subtotal().amount(),
            snapshot.subtotal().currency().getCurrencyCode());
      }
      final var totals = snapshot.totals();
      return new TotalsViewModel(
          totals.subtotal().amount(),
          totals.shipping().amount(),
          totals.tax().amount(),
          totals.total().amount(),
          totals.total().currency().getCurrencyCode());
    }
  }

  /** Buyer information for display. */
  public record BuyerInfoViewModel(String email, String firstName, String lastName, String phone) {
    static BuyerInfoViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      if (snapshot.buyerInfo() == null) {
        return null;
      }
      final var info = snapshot.buyerInfo();
      return new BuyerInfoViewModel(info.email(), info.firstName(), info.lastName(), info.phone());
    }
  }

  /** Delivery information for display. */
  public record DeliveryViewModel(
      AddressViewModel address, ShippingOptionViewModel shippingOption) {
    static DeliveryViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      if (snapshot.deliveryAddress() == null || snapshot.shippingOption() == null) {
        return null;
      }
      return new DeliveryViewModel(
          AddressViewModel.fromSnapshot(snapshot), ShippingOptionViewModel.fromSnapshot(snapshot));
    }
  }

  /** Delivery address for display. */
  public record AddressViewModel(
      String street,
      @Nullable String streetLine2,
      String city,
      String postalCode,
      String country,
      @Nullable String state) {
    static AddressViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      final var addr = snapshot.deliveryAddress();
      if (addr == null) {
        return null;
      }
      return new AddressViewModel(
          addr.street(),
          addr.streetLine2(),
          addr.city(),
          addr.postalCode(),
          addr.country(),
          addr.state());
    }
  }

  /** Shipping option for display. */
  public record ShippingOptionViewModel(
      String id, String name, String estimatedDelivery, BigDecimal cost, String currencyCode) {
    static ShippingOptionViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      final var option = snapshot.shippingOption();
      if (option == null) {
        return null;
      }
      return new ShippingOptionViewModel(
          option.id(),
          option.name(),
          option.estimatedDelivery(),
          option.cost().amount(),
          option.cost().currency().getCurrencyCode());
    }
  }

  /** Payment selection for display. */
  public record PaymentViewModel(String providerId, @Nullable String providerReference) {
    static PaymentViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      final var payment = snapshot.paymentSelection();
      if (payment == null) {
        return null;
      }
      return new PaymentViewModel(payment.providerId().value(), payment.providerReference());
    }
  }
}
