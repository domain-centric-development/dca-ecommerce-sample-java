package dev.domaincentric.sample.ecommerce.checkout.application.submitbuyerinfo;

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
   * @return response containing the updated session state
   * @throws IllegalArgumentException if session is not found
   * @throws IllegalStateException if session is not modifiable or step validation fails
   */
  @Override
  SubmitBuyerInfoResult execute(SubmitBuyerInfoCommand command);
}
