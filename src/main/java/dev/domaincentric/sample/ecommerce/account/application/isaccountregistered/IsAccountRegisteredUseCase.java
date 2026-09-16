package dev.domaincentric.sample.ecommerce.account.application.isaccountregistered;

import dev.domaincentric.sample.ecommerce.account.application.shared.AccountRepository;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case answering whether an account is registered for a user id.
 *
 * <p>Read-only: an account exists or it does not. A session token that names an account nobody can
 * find is stale — whether the account was deleted or the store never kept it — and the
 * authentication filter downgrades such a session to an anonymous visitor. Whether the account may
 * currently log in is a different question, answered where the login happens.
 */
@Service
public class IsAccountRegisteredUseCase implements IsAccountRegisteredInputPort {

  private final AccountRepository accountRepository;

  public IsAccountRegisteredUseCase(final AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public IsAccountRegisteredResult execute(final IsAccountRegisteredQuery query) {
    return accountRepository.findByLinkedUserId(UserId.of(query.userId())).isPresent()
        ? new IsAccountRegisteredResult(true)
        : new IsAccountRegisteredResult(false);
  }
}
