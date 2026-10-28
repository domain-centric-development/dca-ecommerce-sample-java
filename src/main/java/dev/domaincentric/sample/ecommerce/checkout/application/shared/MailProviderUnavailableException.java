package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import java.io.Serial;

/**
 * Raised when the mail provider cannot take a mail right now.
 *
 * <p>The mail is not lost: trying again later is the sensible answer, with the same key.
 */
public final class MailProviderUnavailableException extends UseCaseException {

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
