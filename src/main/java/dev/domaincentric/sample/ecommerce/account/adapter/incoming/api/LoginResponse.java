package dev.domaincentric.sample.ecommerce.account.adapter.incoming.api;

/**
 * API result DTO for a successful login. A refused login answers a problem document instead.
 *
 * @param success always {@code true}
 * @param token the JWT token
 * @param email the user's email
 * @param errorMessage always {@code null}; kept so the answer keeps its shape
 */
public record LoginResponse(boolean success, String token, String email, String errorMessage) {

  public static LoginResponse success(final String token, final String email) {
    return new LoginResponse(true, token, email, null);
  }
}
