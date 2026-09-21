package dev.domaincentric.sample.ecommerce.cart.application.shared;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import java.io.Serial;

/**
 * Raised when a second active cart would be stored for a customer who already has one.
 *
 * <p>Part of the {@link ShoppingCartRepository} contract rather than of the aggregate: only the
 * store sees every cart of a customer at once, so only the store can refuse the second one. Every
 * implementation reports it with this type, whatever its own mechanism is, so a caller can react to
 * a lost race without knowing which store it is talking to.
 */
public final class ActiveCartAlreadyExistsException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final CustomerId customerId;

  public ActiveCartAlreadyExistsException(final CustomerId customerId) {
    super("Customer " + customerId.value() + " already has an active cart");
    this.customerId = customerId;
  }

  public CustomerId customerId() {
    return customerId;
  }
}
