package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.shipping;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.ShippingOptionRegistry;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * In-memory implementation of ShippingOptionRegistry.
 *
 * <p>This secondary adapter holds the shop's fixed list of shipping options. In a real application
 * it would query a shipping provider or configuration.
 */
@Component
public class InMemoryShippingOptionRegistry implements ShippingOptionRegistry {

  private static final Currency EUR = Currency.getInstance("EUR");

  private static final List<ShippingOption> SHIPPING_OPTIONS =
      List.of(
          ShippingOption.of(
              "standard",
              "Standard Shipping",
              "5-7 business days",
              Money.of(BigDecimal.valueOf(4.99), EUR)),
          ShippingOption.of(
              "express",
              "Express Shipping",
              "2-3 business days",
              Money.of(BigDecimal.valueOf(9.99), EUR)),
          ShippingOption.of(
              "overnight",
              "Overnight Shipping",
              "Next business day",
              Money.of(BigDecimal.valueOf(19.99), EUR)),
          ShippingOption.of("free", "Free Shipping", "7-10 business days", Money.zero(EUR)));

  @Override
  public Optional<ShippingOption> findById(final String shippingOptionId) {
    return SHIPPING_OPTIONS.stream()
        .filter(option -> option.id().equals(shippingOptionId))
        .findFirst();
  }

  @Override
  public List<ShippingOption> findAll() {
    return SHIPPING_OPTIONS;
  }
}
