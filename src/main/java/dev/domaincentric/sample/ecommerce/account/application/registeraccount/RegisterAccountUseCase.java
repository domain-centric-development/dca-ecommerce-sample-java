package dev.domaincentric.sample.ecommerce.account.application.registeraccount;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.account.application.shared.AccountRepository;
import dev.domaincentric.sample.ecommerce.account.domain.gateway.PasswordHasher;
import dev.domaincentric.sample.ecommerce.account.domain.model.Account;
import dev.domaincentric.sample.ecommerce.account.domain.model.Email;
import dev.domaincentric.sample.ecommerce.account.domain.model.Owner;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for registering a new account.
 *
 * <p>This use case:
 *
 * <ol>
 *   <li>Validates the email is not already registered
 *   <li>Validates password strength requirements (delegated to domain)
 *   <li>Hashes the password (delegated to domain)
 *   <li>Builds the Owner from the submitted name and date of birth (validated by the Value Object)
 *   <li>Creates a new Account
 *   <li>Links the Account to the user's current UserId
 *   <li>Publishes domain events (AccountRegistered, AccountLinkedToIdentity)
 *   <li>Returns information needed to issue a new JWT
 * </ol>
 *
 * <p>After registration, the controller should:
 *
 * <ol>
 *   <li>Generate a new registered JWT with the returned userId, email, and roles
 *   <li>Set the JWT cookie to upgrade the user from anonymous to registered
 * </ol>
 */
@Service
public class RegisterAccountUseCase implements RegisterAccountInputPort {

  private final AccountRepository accountRepository;
  private final PasswordHasher passwordHasher;
  private final DomainEventPublisher eventPublisher;

  public RegisterAccountUseCase(
      final AccountRepository accountRepository,
      final PasswordHasher passwordHasher,
      final DomainEventPublisher eventPublisher) {
    this.accountRepository = accountRepository;
    this.passwordHasher = passwordHasher;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public RegisterAccountResult execute(final RegisterAccountCommand command) {
    final Email email = Email.of(command.email());

    // Check if email is already registered
    if (accountRepository.existsByEmail(email)) {
      throw new EmailAlreadyRegisteredException(email);
    }

    final UserId currentUserId = UserId.of(command.currentUserId());

    // Check if this user already has an account
    if (accountRepository.findByLinkedUserId(currentUserId).isPresent()) {
      throw new AccountAlreadyExistsException(currentUserId);
    }

    // Create the account (password validation and hashing done by aggregate via gateway)
    final Owner owner = Owner.of(command.firstName(), command.lastName(), command.dateOfBirth());
    final Account account =
        Account.register(email, owner, command.password(), currentUserId, passwordHasher);

    // Persist the account
    accountRepository.save(account);

    // Publish domain events
    eventPublisher.publishAndClearEvents(account);

    return RegisterAccountResult.of(
        account.id().value(),
        account.linkedUserId().value(),
        account.email().value(),
        account.roles());
  }
}
