package dev.domaincentric.sample.ecommerce.cart.application.shared;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import java.io.Serial;

/**
 * Raised when a merge or a take-over names a guest other than the one the visitor was before their
 * login: only that guest's cart may follow into the account (ADR-036 — ownership is the use case's,
 * whichever adapter calls it).
 */
public final class GuestCartNotOwnedException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final CustomerId guest;

  public GuestCartNotOwnedException(final CustomerId guest) {
    super("Not the visitor's own guest: " + guest.value());
    this.guest = guest;
  }

  /** The guest the request named. */
  public CustomerId guest() {
    return guest;
  }
}
