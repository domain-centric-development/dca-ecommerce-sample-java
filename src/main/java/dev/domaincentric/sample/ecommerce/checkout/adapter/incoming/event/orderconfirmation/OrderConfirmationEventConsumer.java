package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.event.orderconfirmation;

import dev.domaincentric.sample.ecommerce.checkout.application.orderconfirmation.sendorderconfirmation.SendOrderConfirmationCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.orderconfirmation.sendorderconfirmation.SendOrderConfirmationInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.orderconfirmation.sendorderconfirmation.SendOrderConfirmationResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.CheckoutConfirmed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Sends the order confirmation once a checkout was confirmed — a reaction to this context's own
 * domain event, after the order's transaction committed, from the event publication registry.
 *
 * <p>The result maps onto the registry: a sent confirmation completes the publication; a checkout
 * that holds no placed order is parked — logged and completed, since trying again changes nothing;
 * an unavailable mail provider throws {@code MailProviderUnavailableException} out of this method,
 * which leaves the publication failed for the registry's bounded retry. The order itself is never
 * undone by it.
 */
@Component
public class OrderConfirmationEventConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(OrderConfirmationEventConsumer.class);

  private final SendOrderConfirmationInputPort sendOrderConfirmation;

  public OrderConfirmationEventConsumer(
      final SendOrderConfirmationInputPort sendOrderConfirmation) {
    this.sendOrderConfirmation = sendOrderConfirmation;
  }

  /**
   * Sends the order confirmation of the confirmed checkout.
   *
   * @param event the confirmed checkout
   */
  @ApplicationModuleListener
  void on(final CheckoutConfirmed event) {
    final SendOrderConfirmationResult result =
        sendOrderConfirmation.execute(new SendOrderConfirmationCommand(event.sessionId().value()));
    if (result.outcome() == SendOrderConfirmationResult.Outcome.NO_PLACED_ORDER) {
      LOG.warn(
          "No order confirmation for checkout session {}: it holds no placed order",
          result.sessionId());
    }
  }
}
