package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.OutputPort;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import java.util.List;
import java.util.Optional;

/**
 * Output port for looking up the shipping options the shop offers.
 *
 * <p>The delivery step lists the offered options and takes the chosen one, its price included, from
 * here — never from what the buyer sends. Which options exist and what they cost is the adapter's
 * business.
 */
public interface ShippingOptionRegistry extends OutputPort {

  /**
   * Finds an offered shipping option by its identifier.
   *
   * @param shippingOptionId the shipping option's identifier
   * @return the offered option with the shop's price, empty when the shop offers none by that id
   */
  Optional<ShippingOption> findById(String shippingOptionId);

  /**
   * Returns every offered shipping option.
   *
   * @return the offered options in the order the delivery step shows them (never null)
   */
  List<ShippingOption> findAll();
}
