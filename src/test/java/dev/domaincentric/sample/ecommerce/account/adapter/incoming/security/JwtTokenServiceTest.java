package dev.domaincentric.sample.ecommerce.account.adapter.incoming.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Role;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for the guest a registered session token names. */
@DisplayName("JwtTokenService")
class JwtTokenServiceTest {

  private static final String SECRET =
      "test-only-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256";
  private static final String EMAIL = "jane.doe@example.com";

  private final JwtTokenService tokenService =
      new JwtTokenService(
          new JwtProperties(
              SECRET, 30, 7, "test-issuer", "shop-identity", "shop-session", false, "Lax", false));

  @Test
  @DisplayName("a registered token names the guest the browser was before the login")
  void aRegisteredTokenNamesTheGuestBeforeLogin() {
    final UserId guest = UserId.generateAnonymous();

    final String token =
        tokenService.generateRegisteredToken(
            UserId.generateAnonymous(), EMAIL, Set.of(Role.CUSTOMER), guest);

    assertEquals(Optional.of(guest), identityOf(token).guestBeforeLogin());
  }

  @Test
  @DisplayName("a registered token without a guest names none")
  void aRegisteredTokenWithoutAGuestNamesNone() {
    final String token =
        tokenService.generateRegisteredToken(
            UserId.generateAnonymous(), EMAIL, Set.of(Role.CUSTOMER));

    assertEquals(Optional.empty(), identityOf(token).guestBeforeLogin());
  }

  private IdentityProvider.Identity identityOf(final String token) {
    return assertInstanceOf(
            JwtTokenService.TokenValidation.Valid.class, tokenService.validate(token))
        .identity();
  }
}
