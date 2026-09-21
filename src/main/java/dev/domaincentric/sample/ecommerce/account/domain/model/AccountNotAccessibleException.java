package dev.domaincentric.sample.ecommerce.account.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when an account that may not sign in would record a login.
 *
 * <p>Suspended and closed accounts exist but do not let their owner in; the status says which case
 * it is, and the caller decides how much of that to tell the person at the keyboard.
 */
public final class AccountNotAccessibleException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final AccountId accountId;
  private final AccountStatus status;

  public AccountNotAccessibleException(final AccountId accountId, final AccountStatus status) {
    super("Cannot login with account status: " + status);
    this.accountId = accountId;
    this.status = status;
  }

  public AccountId accountId() {
    return accountId;
  }

  /** The status that refuses the login. */
  public AccountStatus status() {
    return status;
  }
}
