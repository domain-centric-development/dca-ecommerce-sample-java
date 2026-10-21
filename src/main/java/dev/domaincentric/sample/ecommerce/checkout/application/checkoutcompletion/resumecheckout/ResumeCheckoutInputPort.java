package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.resumecheckout;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for resuming the open checkout of the customer's cart.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see ResumeCheckoutUseCase
 */
public interface ResumeCheckoutInputPort
    extends UseCase<ResumeCheckoutCommand, ResumeCheckoutResult> {

  /**
   * Resumes the active checkout session of the customer's active cart.
   *
   * <p>A session started while the cart was someone else's — a guest's, before the account took the
   * cart over at login — becomes the customer's. Never starts a session.
   *
   * @param command the customer the caller is acting as
   * @return the resumed session, or none when the customer's cart has no open checkout
   */
  @Override
  ResumeCheckoutResult execute(ResumeCheckoutCommand command);
}
