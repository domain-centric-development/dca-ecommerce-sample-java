package dev.domaincentric.sample.ecommerce.account.domain.specification;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Specification;
import java.time.LocalDate;

/**
 * A date of birth is usable when it is known and does not lie in the future.
 *
 * <p>A first-class rule rather than a check buried in a constructor, because two components need
 * it: the {@code Owner} Value Object refuses to exist without a usable date, and the change-profile
 * use case rejects a submitted one before touching the aggregate — a submission that would be
 * refused must not leave a half-applied change behind.
 *
 * <p>Deliberately not a rule about plausible ages. An account does not know how old its owner may
 * be, and an arbitrary upper bound would refuse real people.
 */
public final class UsableDateOfBirth implements Specification<LocalDate> {

  /** The rule carries no parameters, so one instance serves every caller. */
  public static final UsableDateOfBirth RULE = new UsableDateOfBirth();

  private UsableDateOfBirth() {}

  @Override
  public boolean isSatisfiedBy(final LocalDate candidate) {
    return candidate != null && !candidate.isAfter(LocalDate.now());
  }

  /**
   * Enforces the rule, naming which half of it the candidate fails.
   *
   * <p>The message is for the log; a page that shows the refusal words it itself.
   *
   * @param candidate the date to check
   * @throws IllegalArgumentException if the date is missing or lies in the future
   */
  public void requireSatisfiedBy(final LocalDate candidate) {
    if (candidate == null) {
      throw new IllegalArgumentException("Date of birth is required");
    }
    if (candidate.isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Date of birth cannot be in the future");
    }
  }
}
