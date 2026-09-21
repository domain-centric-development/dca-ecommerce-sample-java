package dev.domaincentric.sample.ecommerce.account.application.changepassword;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.account.application.shared.AccountRepository;
import dev.domaincentric.sample.ecommerce.account.domain.gateway.PasswordHasher;
import dev.domaincentric.sample.ecommerce.account.domain.model.Account;
import dev.domaincentric.sample.ecommerce.account.domain.model.HashedPassword;
import dev.domaincentric.sample.ecommerce.account.domain.model.PasswordTooWeakException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for changing the password of the currently authenticated account.
 *
 * <p>This use case:
 *
 * <ol>
 *   <li>Loads the account linked to the given UserId, requiring it to be accessible
 *   <li>Verifies the supplied current password (delegated to the aggregate)
 *   <li>Changes the password (validation and hashing delegated to the aggregate)
 *   <li>Persists the account and publishes {@code AccountPasswordChanged}
 * </ol>
 *
 * <p>An account that cannot log in ({@code SUSPENDED}, {@code CLOSED}) is not accessible and is
 * reported as {@link ChangePasswordResult.Outcome#ACCOUNT_NOT_ACCESSIBLE} — the same rule {@code
 * GetAccountOverviewUseCase} and {@code AuthenticateAccountUseCase} enforce. The aggregate itself
 * only blocks the terminal status, so without this check a suspended account holding a still-valid
 * token could change its password.
 *
 * <p>Verifying the <em>current</em> password happens here rather than in the aggregate, following
 * {@code AuthenticateAccountUseCase}: the aggregate owns the credential, the use case owns the
 * decision about which caller is allowed to replace it.
 *
 * <p>Neither a wrong current password nor a rejected new one is an exception crossing the port;
 * both are ordinary outcomes of {@link ChangePasswordResult} (ADR-023).
 *
 * <p>Changing the password does not invalidate the current session: the token carries the userId,
 * email and roles, never the password.
 */
@Service
public class ChangePasswordUseCase implements ChangePasswordInputPort {

  private static final Logger LOG = LoggerFactory.getLogger(ChangePasswordUseCase.class);

  private static final String CURRENT_PASSWORD_INVALID = "Current password is not correct";

  private final AccountRepository accountRepository;
  private final PasswordHasher passwordHasher;
  private final DomainEventPublisher eventPublisher;

  public ChangePasswordUseCase(
      final AccountRepository accountRepository,
      final PasswordHasher passwordHasher,
      final DomainEventPublisher eventPublisher) {
    this.accountRepository = accountRepository;
    this.passwordHasher = passwordHasher;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public ChangePasswordResult execute(final ChangePasswordCommand command) {
    final Optional<Account> accessibleAccount =
        accountRepository
            .findByLinkedUserId(UserId.of(command.userId()))
            .filter(account -> account.status().canLogin());

    if (accessibleAccount.isEmpty()) {
      LOG.warn("Password change attempt without an accessible account: {}", command.userId());
      return ChangePasswordResult.accountNotAccessible();
    }

    final Account account = accessibleAccount.get();

    if (!account.checkPassword(command.currentPassword(), passwordHasher)) {
      LOG.warn("Password change attempt with a wrong current password: {}", command.userId());
      return ChangePasswordResult.currentPasswordInvalid(CURRENT_PASSWORD_INVALID);
    }

    // Only the strength decision may become NEW_PASSWORD_REJECTED. The rule has its own type, so
    // a malformed call inside the hasher or the HashedPassword constructor - which would arrive as
    // the platform's argument exception - is no longer caught here and rendered to the user as if
    // it were a password rule.
    try {
      HashedPassword.validatePasswordStrength(command.newPassword());
    } catch (final PasswordTooWeakException e) {
      LOG.debug("Password change rejected for {}: {}", command.userId(), e.getMessage());
      return ChangePasswordResult.newPasswordRejected(e.getMessage());
    }

    account.changePassword(command.newPassword(), passwordHasher);

    accountRepository.save(account);

    eventPublisher.publishAndClearEvents(account);

    LOG.info("Password changed for user: {}", command.userId());

    return ChangePasswordResult.changed();
  }
}
