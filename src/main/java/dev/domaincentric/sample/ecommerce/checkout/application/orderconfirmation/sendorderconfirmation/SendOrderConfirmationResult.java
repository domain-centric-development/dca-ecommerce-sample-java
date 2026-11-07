package dev.domaincentric.sample.ecommerce.checkout.application.orderconfirmation.sendorderconfirmation;

import dev.domaincentric.dca.buildingblocks.application.Refusal;

/**
 * What became of the order confirmation: sent, refused because the checkout holds no placed order,
 * or refused because the mail provider — a foreign system — did not accept the mail. Each refusal
 * is a case of the result.
 */
public sealed interface SendOrderConfirmationResult
    permits SendOrderConfirmationResult.Sent,
        SendOrderConfirmationResult.NoPlacedOrder,
        SendOrderConfirmationResult.MailProviderUnavailable {

  /**
   * The mail provider accepted the order confirmation.
   *
   * @param sessionId the checkout session the confirmation was about
   */
  record Sent(String sessionId) implements SendOrderConfirmationResult {}

  /**
   * No order was placed with that session; nothing can be sent, now or later.
   *
   * @param sessionId the checkout session the confirmation was about
   */
  record NoPlacedOrder(String sessionId) implements SendOrderConfirmationResult, Refusal {}

  /**
   * The mail provider did not accept the mail; trying again later is the sensible answer, with the
   * same key.
   *
   * @param key the mail's idempotency key, the same for every attempt for this order
   */
  record MailProviderUnavailable(String key) implements SendOrderConfirmationResult, Refusal {}
}
