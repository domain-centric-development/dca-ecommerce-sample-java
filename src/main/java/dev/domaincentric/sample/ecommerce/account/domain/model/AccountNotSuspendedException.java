package dev.domaincentric.sample.ecommerce.account.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when an account that is not suspended would be reactivated.
 *
 * <p>Reactivation undoes a suspension. An active account has nothing to undo, and a closed one is
 * past the point where it could be undone.
 */
public final class AccountNotSuspendedException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final AccountId accountId;
  private final AccountStatus status;

  public AccountNotSuspendedException(final AccountId accountId, final AccountStatus status) {
    super("Can only reactivate suspended accounts, this one is " + status);
    this.accountId = accountId;
    this.status = status;
  }

  public AccountId accountId() {
    return accountId;
  }

  public AccountStatus status() {
    return status;
  }
}
