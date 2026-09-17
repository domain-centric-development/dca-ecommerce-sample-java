package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import dev.domaincentric.sample.ecommerce.account.application.isaccountregistered.IsAccountRegisteredInputPort;
import dev.domaincentric.sample.ecommerce.account.application.isaccountregistered.IsAccountRegisteredQuery;
import dev.domaincentric.sample.ecommerce.account.application.isaccountregistered.IsAccountRegisteredResult;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The two ways a session can be absent — no cookie at all, and a cookie whose token has expired —
 * and the one rule that holds for both (ADR-029): the visitor identity the cart is keyed on
 * survives, and nobody is authenticated.
 *
 * <p>The expired token is minted here with the token service's own secret, issuer and claims, so
 * that its age is the only thing that can make it fall; the token service never issues one.
 */
@DisplayName("JwtAuthenticationFilter: session expiry")
class JwtAuthenticationFilterExpiryTest {

  private static final String SECRET =
      "test-only-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256";
  private static final String ISSUER = "test-issuer";
  private static final String IDENTITY_COOKIE = "shop-identity";
  private static final String SESSION_COOKIE = "shop-session";
  private static final String EMAIL = "jane.doe@example.com";

  private JwtTokenService tokenService;
  private TestIsAccountRegistered accounts;
  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    final JwtProperties properties =
        new JwtProperties(
            SECRET, 30, 7, ISSUER, IDENTITY_COOKIE, SESSION_COOKIE, false, "Lax", false);
    tokenService = new JwtTokenService(properties);
    accounts = new TestIsAccountRegistered();
    filter = new JwtAuthenticationFilter(tokenService, properties, accounts);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("no session cookie: anonymous under the existing visitor identity")
  void missingSessionCookieKeepsTheVisitorIdentity() throws Exception {
    final UserId visitor = UserId.generateAnonymous();

    final IdentityProvider.Identity identity = runFilter(identityCookieFor(visitor));

    assertFalse(identity.isRegistered());
    assertEquals(visitor, identity.userId());
  }

  @Test
  @DisplayName("an expired session token: anonymous under the existing visitor identity")
  void expiredSessionKeepsTheVisitorIdentity() throws Exception {
    final UserId visitor = UserId.generateAnonymous();
    accounts.register(visitor);

    final IdentityProvider.Identity identity =
        runFilter(
            identityCookieFor(visitor),
            new Cookie(SESSION_COOKIE, expiredSessionTokenFor(visitor)));

    assertFalse(identity.isRegistered(), "an expired session must not authenticate");
    assertEquals(visitor, identity.userId(), "but the identity the cart is keyed on survives");
  }

  private IdentityProvider.Identity runFilter(final Cookie... cookies) throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/products");
    request.setCookies(cookies);

    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return assertInstanceOf(IdentityProvider.Identity.class, principal);
  }

  private Cookie identityCookieFor(final UserId userId) {
    return new Cookie(IDENTITY_COOKIE, tokenService.generateAnonymousToken(userId));
  }

  /** A registered session token that expired an hour ago, signed like the token service signs. */
  private static String expiredSessionTokenFor(final UserId userId) {
    final Instant issuedAt = Instant.now().minus(Duration.ofHours(2));
    return Jwts.builder()
        .subject(userId.value())
        .claim("type", "registered")
        .claim("email", EMAIL)
        .claim("roles", List.of("CUSTOMER"))
        .issuer(ISSUER)
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(issuedAt.plus(Duration.ofHours(1))))
        .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  /**
   * Test double for the account-registered query, so no repository is pulled into a filter test.
   */
  private static final class TestIsAccountRegistered implements IsAccountRegisteredInputPort {

    private final Set<String> known = new HashSet<>();

    void register(final UserId userId) {
      known.add(userId.value());
    }

    @Override
    public IsAccountRegisteredResult execute(final IsAccountRegisteredQuery query) {
      return new IsAccountRegisteredResult(known.contains(query.userId()));
    }
  }
}
