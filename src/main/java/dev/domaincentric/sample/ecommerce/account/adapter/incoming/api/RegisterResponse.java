package dev.domaincentric.sample.ecommerce.account.adapter.incoming.api;

/**
 * API result DTO for the register endpoint — the success case only.
 *
 * <p>A refused registration travels as a problem document built by {@link
 * AccountApiExceptionHandler}, so this record carries no error field: a body that can describe both
 * outcomes invites a client to read the status from the body instead of from the response.
 *
 * @param success whether registration was successful
 * @param token the JWT token
 * @param email the user's email
 */
public record RegisterResponse(boolean success, String token, String email) {

  public static RegisterResponse success(final String token, final String email) {
    return new RegisterResponse(true, token, email);
  }
}
