package dev.domaincentric.sample.ecommerce.account.application.getaccountoverview;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * What reading the account overview came to: the overview, or no accessible account for the
 * identity — a case of the result, not an exception.
 */
public sealed interface GetAccountOverviewResult
    permits GetAccountOverviewResult.Found, GetAccountOverviewResult.AccountNotAccessible {

  /**
   * The account overview projection.
   *
   * @param account the fields the account overview page renders
   */
  record Found(AccountOverview account) implements GetAccountOverviewResult {}

  /** No account for the given UserId, or the account cannot log in. */
  record AccountNotAccessible() implements GetAccountOverviewResult, Refusal {}

  /**
   * Projection of the fields the account overview page renders.
   *
   * @param email the account's email address
   * @param lastLoginAt when the user last logged in, {@code null} if never
   */
  record AccountOverview(String email, @Nullable Instant lastLoginAt) {
    public AccountOverview {
      if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("Email is required for an account overview");
      }
    }
  }
}
