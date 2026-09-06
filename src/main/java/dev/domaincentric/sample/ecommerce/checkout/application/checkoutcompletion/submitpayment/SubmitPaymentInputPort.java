package dev.domaincentric.sample.ecommerce.checkout.application.submitpayment;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for submitting payment information during checkout.
 *
 * <p>This port defines the contract for the payment step of checkout. Primary adapters (REST
 * controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see SubmitPaymentUseCase
 */
public interface SubmitPaymentInputPort extends UseCase<SubmitPaymentCommand, SubmitPaymentResult> {

  /**
   * Submits payment information for the checkout session.
   *
   * <p>This operation:
   *
   * <ul>
   *   <li>Validates the session exists and is active
   *   <li>Validates the buyer info and delivery steps are completed
   *   <li>Validates the session is at or before the payment step
   *   <li>Validates the payment provider exists and is available
   *   <li>Updates the session with payment selection
   *   <li>Advances to the review step if at payment step
   * </ul>
   *
   * @param command the command containing session ID and payment provider selection
   * @return response containing the updated session state
   * @throws IllegalArgumentException if session is not found or provider does not exist
   * @throws IllegalStateException if session is not modifiable or step validation fails
   */
  @Override
  SubmitPaymentResult execute(SubmitPaymentCommand command);
}
