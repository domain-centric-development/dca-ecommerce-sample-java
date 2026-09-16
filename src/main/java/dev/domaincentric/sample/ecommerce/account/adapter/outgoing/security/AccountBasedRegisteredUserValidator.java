package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security;

import dev.domaincentric.sample.ecommerce.account.application.shared.AccountRepository;
import dev.domaincentric.sample.ecommerce.account.application.shared.RegisteredUserValidator;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import org.springframework.stereotype.Component;

/**
 * Implementation of RegisteredUserValidator that checks account existence.
 *
 * <p>This secondary adapter answers the existence question by looking the account up. A token that
 * names an account nobody can find is stale — whether the account was deleted or the store never
 * kept it.
 */
@Component
public class AccountBasedRegisteredUserValidator implements RegisteredUserValidator {

  private final AccountRepository accountRepository;

  public AccountBasedRegisteredUserValidator(final AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public boolean existsForUserId(final UserId userId) {
    return accountRepository.findByLinkedUserId(userId).isPresent();
  }
}
