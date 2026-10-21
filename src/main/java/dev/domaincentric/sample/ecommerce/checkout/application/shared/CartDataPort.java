package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.OutputPort;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import java.util.Optional;

/**
 * Output port for accessing cart data from the Cart bounded context.
 *
 * <p>This is part of the Anti-Corruption Layer (ACL) that isolates the Checkout context from direct
 * coupling to the Cart context's domain model. The adapter implementation translates Cart domain
 * objects into Checkout-specific data structures.
 *
 * <p><b>Hexagonal Architecture:</b> This is a secondary/driven port that defines what the Checkout
 * application layer needs from the Cart context.
 */
public interface CartDataPort extends OutputPort {

  /**
   * Finds cart data by cart ID.
   *
   * <p>Empty when the cart does not exist <em>or</em> is not that customer's.
   *
   * @param cartId the cart identifier
   * @param customerId the customer this context is acting for
   * @return the cart data if found, empty otherwise
   */
  Optional<CartData> findById(CartId cartId, CustomerId customerId);

  /**
   * Finds the ID of the cart that belongs to the customer now.
   *
   * @param customerId the customer this context is acting for
   * @return the customer's active cart ID, empty when they have none
   */
  Optional<CartId> findActiveCartIdOf(CustomerId customerId);
}
