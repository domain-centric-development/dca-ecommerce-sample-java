package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.application.OutputPortException;
import java.io.Serial;

/**
 * Raised when the mail provider did not accept the mail: it answered otherwise, could not be
 * reached, or did not answer in time.
 *
 * <p>Part of the {@link OrderConfirmationMailer} contract: the provider is a foreign system, and
 * its being unreachable is expected behaviour of the integration. The adapter raises it, the use
 * case catches it and answers with a case of its result. The mail is not lost: trying again later
 * is the sensible answer, with the same key.
 */
public final class MailProviderUnavailableException extends OutputPortException {

  @Serial private static final long serialVersionUID = 1L;

  private final String key;

  public MailProviderUnavailableException(final String key) {
    super("Mail provider is currently unavailable for mail " + key);
    this.key = key;
  }

  public String key() {
    return key;
  }
}
