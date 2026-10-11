package dev.domaincentric.sample.ecommerce.account.application.changeprofile;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileResult.Profile;
import dev.domaincentric.sample.ecommerce.account.application.shared.AccountRepository;
import dev.domaincentric.sample.ecommerce.account.domain.model.Account;
import dev.domaincentric.sample.ecommerce.account.domain.model.Email;
import dev.domaincentric.sample.ecommerce.account.domain.specification.UsableDateOfBirth;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for changing the basic profile information of the currently authenticated account.
 *
 * <p>All submitted values are validated before the aggregate is mutated, so a rejected value leaves
 * the whole profile untouched. The email uniqueness check skips the account's own current address,
 * so re-submitting it unchanged is never reported as already in use.
 *
 * <p>The name of the account's owner cannot be changed here — the aggregate offers no operation for
 * it and this command carries no name.
 */
@Service
public class ChangeProfileUseCase implements ChangeProfileInputPort {

  private static final Logger LOG = LoggerFactory.getLogger(ChangeProfileUseCase.class);

  private final AccountRepository accountRepository;
  private final DomainEventPublisher eventPublisher;

  public ChangeProfileUseCase(
      final AccountRepository accountRepository, final DomainEventPublisher eventPublisher) {
    this.accountRepository = accountRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public ChangeProfileResult execute(final ChangeProfileCommand command) {
    final Optional<Account> accessibleAccount =
        accountRepository
            .findByLinkedUserId(UserId.of(command.userId()))
            .filter(account -> account.status().canLogin());

    if (accessibleAccount.isEmpty()) {
      LOG.warn("Profile change attempt without an accessible account: {}", command.userId());
      return ChangeProfileResult.accountNotAccessible();
    }

    final Account account = accessibleAccount.get();

    // Both values are checked before either is applied, so a rejection leaves the profile whole.
    final Email newEmail;
    try {
      newEmail = Email.of(command.email());
    } catch (final IllegalArgumentException e) {
      LOG.debug("Profile change rejected for {}: {}", command.userId(), e.getMessage());
      return ChangeProfileResult.emailInvalid();
    }
    if (!UsableDateOfBirth.RULE.isSatisfiedBy(command.dateOfBirth())) {
      LOG.debug("Profile change rejected for {}: date of birth not usable", command.userId());
      return ChangeProfileResult.dateOfBirthInvalid();
    }

    if (!newEmail.equals(account.email()) && accountRepository.existsByEmail(newEmail)) {
      LOG.debug("Profile change rejected for {}: email already in use", command.userId());
      return ChangeProfileResult.emailAlreadyInUse();
    }

    account.changeEmail(newEmail);
    account.changeOwnerDateOfBirth(command.dateOfBirth());

    accountRepository.save(account);

    eventPublisher.publishAndClearEvents(account);

    LOG.info("Profile changed for user: {}", command.userId());

    return ChangeProfileResult.changed(
        new Profile(account.email().value(), account.owner().dateOfBirth()));
  }
}
