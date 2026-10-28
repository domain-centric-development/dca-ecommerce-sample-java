package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.mail;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.OrderConfirmationMailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * The mail provider's stand-in where no provider address is configured: it accepts every mail and
 * sends nothing.
 */
@Component
@ConditionalOnExpression("'${checkout.mail-provider.base-url:}'.isBlank()")
public class StandInMailProvider implements OrderConfirmationMailer {

  private static final Logger LOG = LoggerFactory.getLogger(StandInMailProvider.class);

  @Override
  public Delivery send(final OrderConfirmationMail mail) {
    LOG.info("No mail provider configured; order confirmation {} is not sent", mail.key());
    return Delivery.ACCEPTED;
  }
}
