package dev.domaincentric.sample.ecommerce.checkout.application.orderconfirmation.sendorderconfirmation;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.MailProviderUnavailableException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.OrderConfirmationMailer;
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
 *
 * <p>A checkout without a placed order is a refusal, returned as a case. So is a mail provider that
 * did not accept the mail: the provider is a foreign system, its port declares the failure, and the
 * use case answers it with its case — the caller decides to try again.
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

  @Override
  public SendOrderConfirmationResult execute(final SendOrderConfirmationCommand command) {
    final Optional<CheckoutSession> placed =
        checkoutSessionRepository
            .findById(CheckoutSessionId.of(command.sessionId()))
            .filter(CheckoutSession::isOrderPlaced);
    if (placed.isEmpty()) {
      return new SendOrderConfirmationResult.NoPlacedOrder(command.sessionId());
    }
    final CheckoutSession session = placed.get();
    final OrderConfirmationMail mail =
        new OrderConfirmationMail(
            session.buyerInfo().email(),
            KEY_PREFIX + session.id().value(),
            session.totals().total());
    try {
      orderConfirmationMailer.send(mail);
    } catch (final MailProviderUnavailableException unavailable) {
      return new SendOrderConfirmationResult.MailProviderUnavailable(unavailable.key());
    }
    return new SendOrderConfirmationResult.Sent(command.sessionId());
  }
}
