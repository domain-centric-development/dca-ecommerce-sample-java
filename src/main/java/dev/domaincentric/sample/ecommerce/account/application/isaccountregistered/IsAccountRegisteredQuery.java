package dev.domaincentric.sample.ecommerce.account.application.isaccountregistered;

/**
 * Query for the existence of an account behind an identity.
 *
 * @param userId the linked user ID a session names
 */
public record IsAccountRegisteredQuery(String userId) {
  public IsAccountRegisteredQuery {
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("UserId is required");
    }
  }
}
