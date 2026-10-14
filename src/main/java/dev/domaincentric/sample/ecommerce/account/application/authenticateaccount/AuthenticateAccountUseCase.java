package dev.domaincentric.sample.ecommerce.account.application.authenticateaccount;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.account.application.shared.AccountRepository;
import dev.domaincentric.sample.ecommerce.account.domain.gateway.PasswordHasher;
import dev.domaincentric.sample.ecommerce.account.domain.model.Account;
import dev.domaincentric.sample.ecommerce.account.domain.model.Email;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for authenticating an account (login).
 *
 * <p>This use case:
 *
 * <ol>
 *   <li>Looks up the account by email
 *   <li>Verifies the password (delegated to domain)
 *   <li>Checks the account is active
 *   <li>Records the login time
 *   <li>Returns information needed to issue a new JWT
 * </ol>
 *
 * <p><b>Security Notes:</b>
 *
 * <ul>
 *   <li>Generic error messages prevent email enumeration
 *   <li>Password comparison is timing-safe (BCrypt)
 *   <li>Failed attempts are logged for security monitoring
 * </ul>
 *
 * <p>After authentication, the controller should:
 *
 * <ol>
 *   <li>Generate a new registered JWT with the returned userId, email, and roles
 *   <li>Set the JWT cookie (replacing any anonymous token)
 *   <li>Optionally handle cart recovery/merge
 * </ol>
 */
@Service
public class AuthenticateAccountUseCase implements AuthenticateAccountInputPort {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticateAccountUseCase.class);

  private static final String GENERIC_ERROR = "Invalid email or password";

  private final AccountRepository accountRepository;
  private final PasswordHasher passwordHasher;
  private final DomainEventPublisher eventPublisher;

  public AuthenticateAccountUseCase(
      final AccountRepository accountRepository,
      final PasswordHasher passwordHasher,
      final DomainEventPublisher eventPublisher) {
    this.accountRepository = accountRepository;
    this.passwordHasher = passwordHasher;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public AuthenticateAccountResult execute(final AuthenticateAccountCommand command) {
    final Email email;
    try {
      email = Email.of(command.email());
    } catch (IllegalArgumentException e) {
      LOG.debug("Invalid email format during login attempt: {}", command.email());
      return AuthenticateAccountResult.failure(GENERIC_ERROR);
    }

    // Find account by email
    final Optional<Account> accountOpt = accountRepository.findByEmail(email);

    if (accountOpt.isEmpty()) {
      LOG.debug("Login attempt for non-existent email: {}", email.value());
      return AuthenticateAccountResult.failure(GENERIC_ERROR);
    }

    final Account account = accountOpt.get();

    // Check password via the aggregate (which delegates to the domain gateway)
    if (!account.checkPassword(command.password(), passwordHasher)) {
      LOG.warn("Failed login attempt for email: {}", email.value());
      return AuthenticateAccountResult.failure(GENERIC_ERROR);
    }

    // Check account status
    if (!account.status().canLogin()) {
      LOG.warn("Login attempt for {} account: {}", account.status(), email.value());
      return AuthenticateAccountResult.failure(
          "Account is " + account.status().name().toLowerCase());
    }

    // Record successful login
    account.recordLogin();
    accountRepository.save(account);

    eventPublisher.publishAndClearEvents(account);

    LOG.info("Successful login for user: {}", account.linkedUserId().value());

    return AuthenticateAccountResult.success(
        account.linkedUserId().value(), account.email().value(), account.roles());
  }
}
