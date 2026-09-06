package dev.domaincentric.sample.ecommerce.checkout.application.getshippingoptions;

import dev.domaincentric.sample.ecommerce.checkout.application.getshippingoptions.GetShippingOptionsResult.ShippingOptionData;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving available shipping options.
 *
 * <p>This use case returns a hardcoded list of shipping options. In a real application, this would
 * query a shipping provider or configuration.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetShippingOptionsInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetShippingOptionsUseCase implements GetShippingOptionsInputPort {

  private static final Currency EUR = Currency.getInstance("EUR");

  /** Hardcoded shipping options. */
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
  public GetShippingOptionsResult execute(final GetShippingOptionsQuery query) {
    final List<ShippingOptionData> options =
        SHIPPING_OPTIONS.stream().map(this::mapToData).toList();
    return new GetShippingOptionsResult(options);
  }

  private ShippingOptionData mapToData(final ShippingOption option) {
    return new ShippingOptionData(
        option.id(),
        option.name(),
        option.estimatedDelivery(),
        option.cost().amount(),
        option.cost().currency().getCurrencyCode());
  }
}
