package dev.domaincentric.sample.ecommerce.account.application.isaccountregistered;

/**
 * Query asking whether an account is registered for a user id.
 *
 * @param userId the linked user id a session token names
 */
public record IsAccountRegisteredQuery(String userId) {

  public IsAccountRegisteredQuery {
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("UserId is required");
    }
  }
}
