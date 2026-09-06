package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.LineItemSnapshot;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * ViewModel for the order confirmation (thank you) page.
 *
 * <p>Contains complete order details for display after successful checkout.
 */
public record ConfirmationPageViewModel(
    String sessionId,
    String status,
    @Nullable String orderReference,
    List<LineItemViewModel> lineItems,
    TotalsViewModel totals,
    BuyerInfoViewModel buyerInfo,
    @Nullable DeliveryViewModel delivery,
    PaymentViewModel payment) {

  /** Creates a ConfirmationPageViewModel from a CheckoutCartSnapshot. */
  public static ConfirmationPageViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
    return new ConfirmationPageViewModel(
        snapshot.sessionId().value(),
        snapshot.status().name(),
        snapshot.orderReference(),
        snapshot.lineItems().stream().map(LineItemViewModel::fromSnapshot).toList(),
        TotalsViewModel.fromSnapshot(snapshot),
        BuyerInfoViewModel.fromSnapshot(snapshot),
        DeliveryViewModel.fromSnapshot(snapshot),
        PaymentViewModel.fromSnapshot(snapshot));
  }

  /** Line item for order display. */
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

  /** Order totals. */
  public record TotalsViewModel(
      BigDecimal subtotal,
      BigDecimal shipping,
      BigDecimal tax,
      BigDecimal total,
      String currencyCode) {
    static TotalsViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      if (snapshot.totals() != null) {
        final var totals = snapshot.totals();
        return new TotalsViewModel(
            totals.subtotal().amount(),
            totals.shipping().amount(),
            totals.tax().amount(),
            totals.total().amount(),
            totals.total().currency().getCurrencyCode());
      }
      return new TotalsViewModel(
          snapshot.subtotal().amount(),
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          snapshot.subtotal().amount(),
          snapshot.subtotal().currency().getCurrencyCode());
    }
  }

  /** Buyer information. */
  public record BuyerInfoViewModel(String email, String firstName, String lastName, String phone) {
    static BuyerInfoViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      final var info = snapshot.buyerInfo();
      if (info == null) {
        return new BuyerInfoViewModel("", "", "", "");
      }
      return new BuyerInfoViewModel(info.email(), info.firstName(), info.lastName(), info.phone());
    }
  }

  /** Delivery information. */
  public record DeliveryViewModel(
      AddressViewModel address, ShippingOptionViewModel shippingOption) {
    static DeliveryViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      final var addr = snapshot.deliveryAddress();
      final var shipping = snapshot.shippingOption();
      if (addr == null || shipping == null) {
        return null;
      }
      return new DeliveryViewModel(
          AddressViewModel.fromAddress(addr), ShippingOptionViewModel.fromOption(shipping));
    }
  }

  /** Delivery address. */
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

  /** Shipping option. */
  public record ShippingOptionViewModel(
      String name, String estimatedDelivery, BigDecimal cost, String currencyCode) {
    static ShippingOptionViewModel fromOption(
        final dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption option) {
      return new ShippingOptionViewModel(
          option.name(),
          option.estimatedDelivery(),
          option.cost().amount(),
          option.cost().currency().getCurrencyCode());
    }
  }

  /** Payment information. */
  public record PaymentViewModel(String providerId, @Nullable String providerReference) {
    static PaymentViewModel fromSnapshot(final CheckoutCartSnapshot snapshot) {
      final var payment = snapshot.paymentSelection();
      if (payment == null) {
        return new PaymentViewModel("", null);
      }
      return new PaymentViewModel(payment.providerId().value(), payment.providerReference());
    }
  }
}
