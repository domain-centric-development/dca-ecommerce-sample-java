package dev.domaincentric.sample.ecommerce.account.application.registeraccount;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.io.Serial;

/**
 * Raised when the signed-in user already has an account and would register a second one.
 *
 * <p>One account per user, checked where the identity of the caller is known — in the use case,
 * which is the only layer that sees both the request and the store.
 */
public final class AccountAlreadyExistsException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final UserId userId;

  public AccountAlreadyExistsException(final UserId userId) {
    super("User already has an account");
    this.userId = userId;
  }

  public UserId userId() {
    return userId;
  }
}
