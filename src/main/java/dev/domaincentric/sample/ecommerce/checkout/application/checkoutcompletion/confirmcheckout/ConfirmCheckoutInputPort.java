package dev.domaincentric.sample.ecommerce.checkout.application.confirmcheckout;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for confirming a checkout session.
 *
 * <p>This port defines the contract for completing the checkout review step and confirming the
 * order. Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see ConfirmCheckoutUseCase
 */
public interface ConfirmCheckoutInputPort
    extends UseCase<ConfirmCheckoutCommand, ConfirmCheckoutResult> {

  /**
   * Confirms the checkout session after customer review.
   *
   * <p>This operation:
   *
   * <ul>
   *   <li>Validates the session exists and is active
   *   <li>Validates all required steps are completed (buyer info, delivery, payment)
   *   <li>Validates the session is at the review step
   *   <li>Transitions the session to CONFIRMED status
   *   <li>Publishes a CheckoutConfirmed integration event
   * </ul>
   *
   * @param command the command containing the session ID
   * @return response containing the confirmed session state
   * @throws IllegalArgumentException if session is not found
   * @throws IllegalStateException if session is not modifiable, steps are incomplete, or not at
   *     review step
   */
  @Override
  ConfirmCheckoutResult execute(ConfirmCheckoutCommand command);
}
