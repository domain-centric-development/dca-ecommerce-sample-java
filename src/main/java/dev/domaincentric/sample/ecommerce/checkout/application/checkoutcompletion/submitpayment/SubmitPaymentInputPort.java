package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment;

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
   * @return the updated session state, or the refusal as a case: a provider not offered, no such
   *     checkout, a session that takes no changes, a step not completed or out of order, nothing to
   *     pay
   * @throws IllegalArgumentException if a value of the command is malformed
   */
  @Override
  SubmitPaymentResult execute(SubmitPaymentCommand command);
}
