package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getshippingoptions;

import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getshippingoptions.GetShippingOptionsResult.ShippingOptionData;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.ShippingOptionRegistry;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving available shipping options.
 *
 * <p>This use case returns the shipping options the shop offers, as the {@link
 * ShippingOptionRegistry} holds them.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetShippingOptionsInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
public class GetShippingOptionsUseCase implements GetShippingOptionsInputPort {

  private final ShippingOptionRegistry shippingOptionRegistry;

  public GetShippingOptionsUseCase(final ShippingOptionRegistry shippingOptionRegistry) {
    this.shippingOptionRegistry = shippingOptionRegistry;
  }

  @Override
  public GetShippingOptionsResult execute(final GetShippingOptionsQuery query) {
    final List<ShippingOptionData> options =
        shippingOptionRegistry.findAll().stream().map(this::mapToData).toList();
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
