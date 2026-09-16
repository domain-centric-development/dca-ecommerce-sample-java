package dev.domaincentric.sample.ecommerce.account.application.isaccountregistered;

import dev.domaincentric.sample.ecommerce.account.application.shared.AccountRepository;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case answering whether an account exists for an identity.
 *
 * <p>Read-only: looks the account up by its linked user ID. A token that names an account nobody
 * can find is stale — whether the account was deleted or the store never kept it.
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
    return new IsAccountRegisteredResult(
        accountRepository.findByLinkedUserId(UserId.of(query.userId())).isPresent());
  }
}
