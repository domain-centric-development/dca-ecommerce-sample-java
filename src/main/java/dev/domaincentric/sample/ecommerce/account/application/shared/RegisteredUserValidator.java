package dev.domaincentric.sample.ecommerce.account.application.shared;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.OutputPort;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;

/**
 * Validator for checking if a registered user's account exists.
 *
 * <p>A session token is self-contained and outlives the account it names: an account that was
 * deleted — or never survived the restart of a store that does not persist — leaves a token that
 * still validates and still carries roles. The security infrastructure asks this validator whether
 * the account behind a token is still there before honouring it.
 *
 * <p>This interface belongs to the account bounded context because only the account context knows
 * about account existence.
 */
public interface RegisteredUserValidator extends OutputPort {

  /**
   * Checks if a registered user account exists for the given user ID.
   *
   * @param userId the user ID to check
   * @return true if an account exists for this user ID, false otherwise
   */
  boolean existsForUserId(UserId userId);
}
