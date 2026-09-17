package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.account.application.isaccountregistered.IsAccountRegisteredInputPort;
import dev.domaincentric.sample.ecommerce.account.application.isaccountregistered.IsAccountRegisteredQuery;
import dev.domaincentric.sample.ecommerce.account.application.isaccountregistered.IsAccountRegisteredResult;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import jakarta.servlet.http.Cookie;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Unit tests for {@link JwtAuthenticationFilter}.
 *
 * <p>Pins the four states the identity/session split creates (ADR-030) and the rule that a session
 * ending must never cost the visitor identity (ADR-029) — the cart is keyed on that identity, so
 * minting a fresh one silently orphans it.
 */
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

  private static final String SECRET =
      "test-only-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256";
  private static final String IDENTITY_COOKIE = "shop-identity";
  private static final String SESSION_COOKIE = "shop-session";
  private static final String EMAIL = "jane.doe@example.com";

  private JwtProperties properties;
  private JwtTokenService tokenService;
  private TestIsAccountRegistered accounts;
  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    properties =
        new JwtProperties(
            SECRET, 30, 7, "test-issuer", IDENTITY_COOKIE, SESSION_COOKIE, false, "Lax", false);
    tokenService = new JwtTokenService(properties);
    accounts = new TestIsAccountRegistered();
    filter = new JwtAuthenticationFilter(tokenService, properties, accounts);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private IdentityProvider.Identity runFilter(final Cookie... cookies) throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/products");
    request.setCookies(cookies);
    lastResponse = new MockHttpServletResponse();

    filter.doFilter(request, lastResponse, new MockFilterChain());

    final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return assertInstanceOf(IdentityProvider.Identity.class, principal);
  }

  private MockHttpServletResponse lastResponse;

  private Optional<String> setCookieHeader(final String name) {
    return lastResponse.getHeaders("Set-Cookie").stream()
        .filter(header -> header.startsWith(name + "="))
        .findFirst();
  }

  private Cookie identityCookieFor(final UserId userId) {
    return new Cookie(IDENTITY_COOKIE, tokenService.generateAnonymousToken(userId));
  }

  private Cookie sessionCookieFor(final UserId userId) {
    accounts.register(userId);
    return new Cookie(
        SESSION_COOKIE, tokenService.generateRegisteredToken(userId, EMAIL, Set.of("CUSTOMER")));
  }

  // ------------------------------------------------- the four states

  @Test
  @DisplayName("no cookies at all: mints a visitor identity and sets the identity cookie")
  void firstVisitMintsIdentity() throws Exception {
    final IdentityProvider.Identity identity = runFilter();

    assertTrue(identity.isAnonymous());
    assertTrue(
        setCookieHeader(IDENTITY_COOKIE).isPresent(), "the visitor must be given an identity");
    assertTrue(setCookieHeader(SESSION_COOKIE).isEmpty(), "no session may be invented");
  }

  @Test
  @DisplayName("identity present, no session: anonymous under the existing UserId")
  void expiredSessionKeepsTheVisitorIdentity() throws Exception {
    final UserId visitor = UserId.generateAnonymous();

    final IdentityProvider.Identity identity = runFilter(identityCookieFor(visitor));

    assertTrue(identity.isAnonymous());
    assertEquals(
        visitor,
        identity.userId(),
        "a session that is gone must not cost the identity the cart is keyed on");
  }

  @Test
  @DisplayName("identity present, session valid: registered under that identity")
  void validSessionIsRegistered() throws Exception {
    final UserId visitor = UserId.generateAnonymous();

    final IdentityProvider.Identity identity =
        runFilter(identityCookieFor(visitor), sessionCookieFor(visitor));

    assertTrue(identity.isRegistered());
    assertEquals(visitor, identity.userId());
    assertEquals(EMAIL, identity.email().orElseThrow());
  }

  @Test
  @DisplayName("session valid but no identity cookie: adopts the session's UserId")
  void sessionWithoutIdentityAdoptsItsUserId() throws Exception {
    final UserId visitor = UserId.generateAnonymous();

    final IdentityProvider.Identity identity = runFilter(sessionCookieFor(visitor));

    assertEquals(
        visitor,
        identity.userId(),
        "inventing a second UserId would contradict the session that is present");
    assertTrue(
        setCookieHeader(IDENTITY_COOKIE).isPresent(), "the adopted identity is written back");
  }

  // ------------------------------------------------- degraded tokens

  @Test
  @DisplayName("an unreadable identity cookie is replaced, not trusted")
  void unreadableIdentityIsReplaced() throws Exception {
    final IdentityProvider.Identity identity = runFilter(new Cookie(IDENTITY_COOKIE, "not-a-jwt"));

    assertTrue(identity.isAnonymous());
    assertTrue(setCookieHeader(IDENTITY_COOKIE).isPresent());
  }

  @Test
  @DisplayName("an unreadable session leaves the visitor identity intact")
  void unreadableSessionKeepsIdentity() throws Exception {
    final UserId visitor = UserId.generateAnonymous();

    final IdentityProvider.Identity identity =
        runFilter(identityCookieFor(visitor), new Cookie(SESSION_COOKIE, "not-a-jwt"));

    assertTrue(identity.isAnonymous());
    assertEquals(visitor, identity.userId());
  }

  @Test
  @DisplayName("a session whose account no longer exists falls back to the visitor identity")
  void sessionWithoutAccountKeepsIdentity() throws Exception {
    final UserId visitor = UserId.generateAnonymous();
    final Cookie session = sessionCookieFor(visitor);
    accounts.forget(visitor);

    final IdentityProvider.Identity identity = runFilter(identityCookieFor(visitor), session);

    assertTrue(identity.isAnonymous(), "no account means no authenticated session");
    assertEquals(visitor, identity.userId(), "but the cart identity survives");
  }

  @Test
  @DisplayName("a legacy all-in-one cookie is used for its UserId only, never for its claims")
  void legacyCookieGrantsNoAuthentication() throws Exception {
    final UserId visitor = UserId.generateAnonymous();
    accounts.register(visitor);
    final Cookie legacy =
        new Cookie(
            IDENTITY_COOKIE, tokenService.generateRegisteredToken(visitor, EMAIL, Set.of("ADMIN")));

    final IdentityProvider.Identity identity = runFilter(legacy);

    assertFalse(
        identity.isRegistered(),
        "a cookie named for the visitor identity must not grant authentication");
    assertEquals(visitor, identity.userId(), "its UserId is still the visitor's");
  }

  // ------------------------------------------------- cookie hardening

  @Test
  @DisplayName("every cookie it writes is HttpOnly, SameSite=Lax and honours the Secure setting")
  void cookiesAreHardened() throws Exception {
    runFilter();

    final String cookie = setCookieHeader(IDENTITY_COOKIE).orElseThrow();
    assertTrue(cookie.contains("HttpOnly"), cookie);
    assertTrue(cookie.contains("SameSite=Lax"), cookie);
    assertFalse(cookie.contains("Secure"), "this test configures secure-cookies=false: " + cookie);
  }

  @Test
  @DisplayName("the Secure flag follows configuration rather than being hardcoded")
  void secureFlagIsConfigurable() throws Exception {
    properties =
        new JwtProperties(
            SECRET, 30, 7, "test-issuer", IDENTITY_COOKIE, SESSION_COOKIE, true, "Lax", false);
    filter = new JwtAuthenticationFilter(new JwtTokenService(properties), properties, accounts);

    runFilter();

    assertTrue(setCookieHeader(IDENTITY_COOKIE).orElseThrow().contains("Secure"));
  }

  @Test
  @DisplayName("a request that already has an identity gets no new cookie")
  void existingIdentityIsNotRewritten() throws Exception {
    runFilter(identityCookieFor(UserId.generateAnonymous()));

    assertTrue(
        setCookieHeader(IDENTITY_COOKIE).isEmpty(),
        "rewriting the cookie on every request would extend its lifetime silently");
  }

  // ------------------------------------------------- challenged, not forbidden

  @Test
  @DisplayName("a visitor is anonymous in the security context, a session is authenticated")
  void aVisitorIsNotAuthenticated() throws Exception {
    runFilter();

    assertInstanceOf(
        AnonymousAuthenticationToken.class,
        SecurityContextHolder.getContext().getAuthentication(),
        "a visitor has not authenticated, so a gate must challenge them rather than forbid them");

    final UserId registered = UserId.generateAnonymous();
    runFilter(identityCookieFor(registered), sessionCookieFor(registered));

    assertTrue(
        SecurityContextHolder.getContext().getAuthentication().isAuthenticated(),
        "a registered session is an authentication");
    assertFalse(
        SecurityContextHolder.getContext().getAuthentication()
            instanceof AnonymousAuthenticationToken);
  }

  @Test
  @DisplayName("static resources bypass the filter entirely")
  void staticResourcesAreSkipped() throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/css/app.css");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    assertTrue(response.getHeaders("Set-Cookie").isEmpty());
  }

  // ------------------------------------------------- rotation on logout

  @Test
  @DisplayName("logout gives the browser a different visitor identity")
  void logOutRotatesTheIdentity() {
    final UserId before = UserId.generateAnonymous();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final JwtIdentitySession session = new JwtIdentitySession(properties, tokenService, response);

    session.logOut();

    assertNotEquals(
        before,
        userIdInCookie(response, IDENTITY_COOKIE),
        "the next person on this device must not inherit the cart");
  }

  @Test
  @DisplayName("logging in re-points the identity cookie at the account's UserId")
  void loginAlignsTheIdentityWithTheAccount() {
    // The browser arrived as one UserId; the account it authenticates as is another. The anonymous
    // cart is merged into the account's and then deleted, so an identity cookie left on the
    // superseded UserId would drop the next expired session onto a cart that no longer exists.
    final UserId arrivedAs = UserId.generateAnonymous();
    final UserId accountUserId = UserId.generateAnonymous();
    final MockHttpServletResponse response = new MockHttpServletResponse();

    new JwtIdentitySession(properties, tokenService, response)
        .setRegisteredIdentity(
            tokenService.generateRegisteredToken(accountUserId, EMAIL, Set.of("CUSTOMER")));

    final UserId storedIdentity = userIdInCookie(response, IDENTITY_COOKIE);
    assertEquals(accountUserId, storedIdentity, "the identity must follow the account");
    assertNotEquals(arrivedAs, storedIdentity);
  }

  private UserId userIdInCookie(final MockHttpServletResponse response, final String name) {
    final String header =
        response.getHeaders("Set-Cookie").stream()
            .filter(value -> value.startsWith(name + "="))
            .findFirst()
            .orElseThrow();
    final String token = header.substring(header.indexOf('=') + 1, header.indexOf(';'));
    return tokenService.validateAndParse(token).orElseThrow().userId();
  }

  @Test
  @DisplayName("logout clears the session cookie")
  void logOutClearsTheSession() {
    final MockHttpServletResponse response = new MockHttpServletResponse();

    new JwtIdentitySession(properties, tokenService, response).logOut();

    final String cleared =
        response.getHeaders("Set-Cookie").stream()
            .filter(header -> header.startsWith(SESSION_COOKIE + "="))
            .findFirst()
            .orElseThrow();
    assertTrue(cleared.contains("Max-Age=0"), cleared);
  }

  /**
   * Test double for the account-registered query, so no repository is pulled into a filter test.
   */
  private static final class TestIsAccountRegistered implements IsAccountRegisteredInputPort {

    private final Set<String> known = new HashSet<>();

    void register(final UserId userId) {
      known.add(userId.value());
    }

    void forget(final UserId userId) {
      known.remove(userId.value());
    }

    @Override
    public IsAccountRegisteredResult execute(final IsAccountRegisteredQuery query) {
      return new IsAccountRegisteredResult(known.contains(query.userId()));
    }
  }
}
