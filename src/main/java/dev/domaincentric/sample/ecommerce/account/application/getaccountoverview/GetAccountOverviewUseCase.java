package dev.domaincentric.sample.ecommerce.account.application.getaccountoverview;

import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewResult.AccountOverview;
import dev.domaincentric.sample.ecommerce.account.application.shared.AccountRepository;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for reading the account overview of a registered user.
 *
 * <p>Read-only: loads the account linked to the given UserId and projects the fields rendered by
 * the account overview page. Performs no write.
 *
 * <p>An account that cannot log in ({@code SUSPENDED}, {@code CLOSED}) is not accessible and is
 * reported as absent — the same rule {@code AuthenticateAccountUseCase} enforces on the login path.
 * Without it, a suspended account holding a still-valid token would keep rendering its account
 * data.
 *
 * <p>A UserId with no account at all is the stale-token case that {@code RegisteredUserValidator}
 * normally downgrades to anonymous in the authentication filter; this branch covers the race with
 * that filter, not a business case.
 */
@Service
public class GetAccountOverviewUseCase implements GetAccountOverviewInputPort {

  private final AccountRepository accountRepository;

  public GetAccountOverviewUseCase(final AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public GetAccountOverviewResult execute(final GetAccountOverviewQuery query) {
    return accountRepository
        .findByLinkedUserId(UserId.of(query.userId()))
        .filter(account -> account.status().canLogin())
        .map(account -> new AccountOverview(account.email().value(), account.lastLoginAt()))
        .map(GetAccountOverviewResult::found)
        .orElseGet(GetAccountOverviewResult::notFound);
  }
}
