package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.OutputPort;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.Objects;

/**
 * Output port to the mail provider for the order confirmation: hands it one mail to the buyer.
 *
 * <p>The mail carries a key that is the same for every attempt of the same order's confirmation, so
 * the provider sends it once however often it is asked.
 */
public interface OrderConfirmationMailer extends OutputPort {

  /**
   * Hands the order confirmation to the mail provider.
   *
   * <p>The provider is a foreign system: its not accepting the mail is expected, and this contract
   * declares it as its own failure, which every implementation raises whatever its mechanism is.
   *
   * @param mail the order confirmation
   * @throws MailProviderUnavailableException if the provider did not accept the mail, could not be
   *     reached, or did not answer in time — the mail is to be tried again with the same key
   */
  void send(OrderConfirmationMail mail);

  /**
   * One order confirmation.
   *
   * @param recipient the buyer e-mail of the checkout
   * @param key the idempotency key, the same for every attempt for this order
   * @param orderTotal the total the order was placed at
   */
  record OrderConfirmationMail(String recipient, String key, Money orderTotal) {

    public OrderConfirmationMail {
      Objects.requireNonNull(recipient, "recipient");
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(orderTotal, "orderTotal");
    }
  }
}
