package dev.domaincentric.sample.ecommerce.sharedkernel.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;
import java.util.Currency;

/**
 * Raised when two amounts in different currencies would be added, subtracted or compared.
 *
 * <p>Money is an amount <em>in</em> a currency; two of them in different currencies have no sum and
 * no order without a conversion rate, which is a decision nobody in this model is allowed to make
 * silently. The rule belongs to the shared kernel because every context that handles money holds
 * it.
 */
public final class CurrencyMismatchException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final Currency left;
  private final Currency right;

  public CurrencyMismatchException(
      final String operation, final Currency left, final Currency right) {
    super(
        "Cannot "
            + operation
            + " money in "
            + left.getCurrencyCode()
            + " and "
            + right.getCurrencyCode());
    this.left = left;
    this.right = right;
  }

  public Currency left() {
    return left;
  }

  public Currency right() {
    return right;
  }
}
