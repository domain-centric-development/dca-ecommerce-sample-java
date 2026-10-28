package dev.domaincentric.sample.ecommerce.checkout.application.orderconfirmation.sendorderconfirmation;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for sending the order confirmation: the buyer of a confirmed checkout gets a mail
 * through the mail provider, under one key per order however often it is sent.
 *
 * @see SendOrderConfirmationUseCase
 */
public interface SendOrderConfirmationInputPort
    extends UseCase<SendOrderConfirmationCommand, SendOrderConfirmationResult> {

  @Override
  SendOrderConfirmationResult execute(SendOrderConfirmationCommand command);
}
