package dev.domaincentric.sample.ecommerce.account.application.registeraccount;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.account.domain.model.Email;
import java.io.Serial;

/**
 * Raised when a registration uses an email address another account already holds.
 *
 * <p>Uniqueness across all accounts is nothing a single account can check, so the rule belongs to
 * the use case and its store, not to the model.
 */
public final class EmailAlreadyRegisteredException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final Email email;

  public EmailAlreadyRegisteredException(final Email email) {
    super("Email is already registered: " + email.value());
    this.email = email;
  }

  public Email email() {
    return email;
  }
}
