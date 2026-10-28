package dev.domaincentric.sample.ecommerce.checkout.application.orderconfirmation.sendorderconfirmation;

import dev.domaincentric.sample.ecommerce.checkout.application.orderconfirmation.sendorderconfirmation.SendOrderConfirmationResult.Outcome;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.MailProviderUnavailableException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.OrderConfirmationMailer;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.OrderConfirmationMailer.Delivery;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.OrderConfirmationMailer.OrderConfirmationMail;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Use case for sending the order confirmation of a confirmed checkout to its buyer.
 *
 * <p>Reads the checkout — a system path acting on nobody's behalf — and hands the mail provider one
 * mail to the buyer e-mail with the total the order was placed at. The mail's key is derived from
 * the checkout alone, so every attempt for one order carries the same key and the provider sends it
 * once. No transaction: nothing is changed, and the provider is remote.
 */
@Service
public class SendOrderConfirmationUseCase implements SendOrderConfirmationInputPort {

  private static final String KEY_PREFIX = "order-confirmation-";

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final OrderConfirmationMailer orderConfirmationMailer;

  public SendOrderConfirmationUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final OrderConfirmationMailer orderConfirmationMailer) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.orderConfirmationMailer = orderConfirmationMailer;
  }

  /**
   * {@inheritDoc}
   *
   * @throws MailProviderUnavailableException if the mail provider did not accept the mail — the
   *     confirmation is to be tried again
   */
  @Override
  public SendOrderConfirmationResult execute(final SendOrderConfirmationCommand command) {
    final Optional<CheckoutSession> placed =
        checkoutSessionRepository
            .findById(CheckoutSessionId.of(command.sessionId()))
            .filter(CheckoutSession::isOrderPlaced);
    if (placed.isEmpty()) {
      return new SendOrderConfirmationResult(command.sessionId(), Outcome.NO_PLACED_ORDER);
    }
    final CheckoutSession session = placed.get();
    final OrderConfirmationMail mail =
        new OrderConfirmationMail(
            session.buyerInfo().email(),
            KEY_PREFIX + session.id().value(),
            session.totals().total());
    if (orderConfirmationMailer.send(mail) != Delivery.ACCEPTED) {
      throw new MailProviderUnavailableException(mail.key());
    }
    return new SendOrderConfirmationResult(command.sessionId(), Outcome.SENT);
  }
}
