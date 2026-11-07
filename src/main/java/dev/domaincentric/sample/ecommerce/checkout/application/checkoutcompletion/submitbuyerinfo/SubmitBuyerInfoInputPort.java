package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for submitting buyer contact information during checkout.
 *
 * <p>This port defines the contract for the buyer info step of checkout. Primary adapters (REST
 * controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see SubmitBuyerInfoUseCase
 */
public interface SubmitBuyerInfoInputPort
    extends UseCase<SubmitBuyerInfoCommand, SubmitBuyerInfoResult> {

  /**
   * Submits buyer contact information for the checkout session.
   *
   * <p>This operation:
   *
   * <ul>
   *   <li>Validates the session exists and is active
   *   <li>Validates the session is at or before the buyer info step
   *   <li>Updates the session with buyer contact information
   *   <li>Advances to the delivery step if at buyer info step
   * </ul>
   *
   * @param command the command containing session ID and buyer info
   * @return the updated session state, or the refusal as a case: no such checkout, a session that
   *     takes no changes, a step out of order
   * @throws IllegalArgumentException if a value of the command is malformed
   */
  @Override
  SubmitBuyerInfoResult execute(SubmitBuyerInfoCommand command);
}
