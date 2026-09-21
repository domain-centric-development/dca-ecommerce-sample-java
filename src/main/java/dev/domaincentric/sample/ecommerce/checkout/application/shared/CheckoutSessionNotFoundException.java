package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import java.io.Serial;

/**
 * Raised when the addressed checkout session is not available to the asking customer.
 *
 * <p>One failure for two situations, on purpose: no such session, and somebody else's session. The
 * lookup asks for the session <em>of this customer</em>, so a stranger cannot learn which session
 * identities exist (ADR-036).
 */
public final class CheckoutSessionNotFoundException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final CheckoutSessionId sessionId;

  public CheckoutSessionNotFoundException(final CheckoutSessionId sessionId) {
    super("Session not found: " + sessionId.value());
    this.sessionId = sessionId;
  }

  public CheckoutSessionId sessionId() {
    return sessionId;
  }
}
