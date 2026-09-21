package dev.domaincentric.sample.ecommerce.account.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when a closed account would be changed.
 *
 * <p>Closing is final: the account keeps its history but takes no new password, address or status.
 * Every change refuses with this one rule, whichever change it was.
 */
public final class AccountClosedException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final AccountId accountId;

  public AccountClosedException(final AccountId accountId) {
    super("Account " + accountId.value() + " is closed");
    this.accountId = accountId;
  }

  public AccountId accountId() {
    return accountId;
  }
}
