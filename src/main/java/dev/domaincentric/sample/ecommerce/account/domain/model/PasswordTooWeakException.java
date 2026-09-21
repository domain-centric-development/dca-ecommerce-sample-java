package dev.domaincentric.sample.ecommerce.account.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when a plaintext password does not meet the account's strength rules.
 *
 * <p>A rule of the model, not an argument contract: the caller passed a perfectly well-formed
 * string, and the model refuses it for what it is made of. Its own type is what lets a use case
 * show the reason to the person choosing the password, while a malformed call from the hashing
 * adapter keeps travelling as the defect it is.
 */
public final class PasswordTooWeakException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  public PasswordTooWeakException(final String reason) {
    super(reason);
  }
}
