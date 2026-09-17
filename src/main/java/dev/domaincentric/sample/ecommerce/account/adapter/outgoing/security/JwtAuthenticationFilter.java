package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security;

import dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security.JwtTokenService.TokenValidation;
import dev.domaincentric.sample.ecommerce.account.application.isaccountregistered.IsAccountRegisteredInputPort;
import dev.domaincentric.sample.ecommerce.account.application.isaccountregistered.IsAccountRegisteredQuery;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT Authentication Filter that runs on every request.
 *
 * <p>This filter is responsible for:
 *
 * <ol>
 *   <li>Extracting JWT from the cookie (browser requests) or the Authorization header
 *   <li>If no JWT exists, generating an anonymous token and setting the cookie
 *   <li>Validating the token and extracting the Identity
 *   <li>Setting the SecurityContext with the Identity for downstream access
 * </ol>
 *
 * <p><b>Token-only endpoints:</b> requests to {@code /api/**} and {@code /mcp/**} are authenticated
 * exclusively by {@code Authorization: Bearer}; cookies are neither read nor written there. That is
 * what makes their exemption from CSRF protection sound — a browser cannot authenticate a
 * cross-site request to them with automatically attached cookies.
 *
 * <p><b>Cookie Settings:</b>
 *
 * <ul>
 *   <li>HttpOnly: true (prevents XSS attacks)
 *   <li>Secure: based on request scheme (https = secure)
 *   <li>SameSite: Lax (defence in depth; CSRF protection itself is the token in every form)
 *   <li>Path: / (accessible site-wide)
 *   <li>MaxAge: based on token type (30 days anonymous, 7 days registered)
 * </ul>
 *
 * <p><b>Security Context:</b> The Identity is stored in the SecurityContext as the principal of an
 * UsernamePasswordAuthenticationToken. This allows downstream code to access the identity via
 * SecurityContextHolder or the IdentityProvider.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenService tokenService;
  private final JwtProperties jwtProperties;
  private final IsAccountRegisteredInputPort isAccountRegistered;

  public JwtAuthenticationFilter(
      final JwtTokenService tokenService,
      final JwtProperties jwtProperties,
      final IsAccountRegisteredInputPort isAccountRegistered) {
    this.tokenService = tokenService;
    this.jwtProperties = jwtProperties;
    this.isAccountRegistered = isAccountRegistered;
  }

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain)
      throws ServletException, IOException {

    // Skip filter for static resources
    if (isStaticResource(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    // The identity is resolved first and independently of authentication: it carries the cart, so
    // an expired or missing session must never cost it (ADR-029). It is the same UserId before and
    // after login — authentication adds a session, it does not replace who the browser is.
    if (isTokenOnlyEndpoint(request)) {
      setSecurityContext(resolveBearerIdentity(request));
    } else {
      final UserId identityUserId = resolveIdentity(request, response);
      setSecurityContext(resolveSession(request, identityUserId));
    }

    // Continue filter chain
    filterChain.doFilter(request, response);
  }

  /**
   * Resolves the identity from the identity cookie, minting one only when the browser presents none
   * that can be read.
   *
   * <p>A token in this cookie is used for its {@code UserId} alone. Browsers from before the
   * identity/session split still hold a token carrying registered claims here; honouring those
   * claims would let the identity cookie grant authentication, which is the conflation ADR-030
   * removes.
   */
  private UserId resolveIdentity(
      final HttpServletRequest request, final HttpServletResponse response) {

    final Optional<String> stored = readCookie(request, jwtProperties.cookieName());
    if (stored.isPresent()) {
      final TokenValidation validation = tokenService.validate(stored.get());
      if (validation instanceof TokenValidation.Valid valid) {
        return valid.identity().userId();
      }
      LOG.debug(
          "Identity not usable ({}), issuing a new one", validation.getClass().getSimpleName());
    }

    // A valid session without an identity cookie: adopt the session's UserId rather than inventing
    // a second one that would contradict it.
    final UserId userId =
        sessionToken(request)
            .map(tokenService::validate)
            .flatMap(
                validation ->
                    validation instanceof TokenValidation.Valid valid
                        ? Optional.of(valid.identity().userId())
                        : Optional.<UserId>empty())
            .orElseGet(UserId::generateAnonymous);

    writeCookie(
        response,
        jwtProperties.cookieName(),
        tokenService.generateAnonymousToken(userId),
        jwtProperties.identityCookieMaxAgeSeconds());
    return userId;
  }

  /**
   * Resolves the authenticated session, falling back to an anonymous identity that keeps the
   * browser's existing {@code UserId}.
   *
   * <p>Every fallback below is deliberately silent and non-blocking: the filter enriches the
   * request, it does not gate it (ADR-029). An expired session is the routine end of a session, not
   * an error the person should see.
   */
  private IdentityProvider.Identity resolveSession(
      final HttpServletRequest request, final UserId identityUserId) {

    final Optional<String> token = sessionToken(request);
    if (token.isEmpty()) {
      return JwtIdentity.anonymous(identityUserId);
    }

    if (!(tokenService.validate(token.get()) instanceof TokenValidation.Valid valid)) {
      return JwtIdentity.anonymous(identityUserId);
    }

    final IdentityProvider.Identity identity = valid.identity();
    if (!identity.isRegistered()) {
      return JwtIdentity.anonymous(identityUserId);
    }

    // The token is self-contained, so it outlives the account it names: a deleted account leaves a
    // session that still validates and still carries roles. The question points inward, so it is
    // asked of the Account context through a query use case, not through an output port.
    if (!isRegistered(identity.userId())) {
      LOG.info("Session for {} has no account, continuing anonymously", identity.userId().value());
      return JwtIdentity.anonymous(identityUserId);
    }

    return identity;
  }

  /**
   * Identity for token-only endpoints: whatever the Bearer token says, or a throwaway anonymous
   * identity when there is none. No cookie is consulted or issued.
   */
  private IdentityProvider.Identity resolveBearerIdentity(final HttpServletRequest request) {
    final Optional<String> token = extractTokenFromHeader(request);
    if (token.isEmpty()) {
      return JwtIdentity.anonymous(UserId.generateAnonymous());
    }
    if (!(tokenService.validate(token.get()) instanceof TokenValidation.Valid valid)) {
      return JwtIdentity.anonymous(UserId.generateAnonymous());
    }
    final IdentityProvider.Identity identity = valid.identity();
    if (identity.isRegistered() && !isRegistered(identity.userId())) {
      return JwtIdentity.anonymous(identity.userId());
    }
    return identity;
  }

  private boolean isRegistered(final UserId userId) {
    return isAccountRegistered.execute(new IsAccountRegisteredQuery(userId.value())).registered();
  }

  private static boolean isTokenOnlyEndpoint(final HttpServletRequest request) {
    final String path = request.getRequestURI();
    return path.startsWith("/api/") || path.startsWith("/mcp");
  }

  private Optional<String> sessionToken(final HttpServletRequest request) {
    final Optional<String> fromCookie = readCookie(request, jwtProperties.sessionCookieName());
    return fromCookie.isPresent() ? fromCookie : extractTokenFromHeader(request);
  }

  private Optional<String> readCookie(final HttpServletRequest request, final String name) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    return Arrays.stream(request.getCookies())
        .filter(cookie -> name.equals(cookie.getName()))
        .map(Cookie::getValue)
        .filter(value -> value != null && !value.isBlank())
        .findFirst();
  }

  private void writeCookie(
      final HttpServletResponse response,
      final String name,
      final String value,
      final int maxAgeSeconds) {

    response.addHeader(
        HttpHeaders.SET_COOKIE,
        ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(jwtProperties.secureCookies())
            .sameSite(jwtProperties.sameSite())
            .path("/")
            .maxAge(maxAgeSeconds)
            .build()
            .toString());
  }

  private Optional<String> extractTokenFromHeader(final HttpServletRequest request) {
    final String header = request.getHeader(AUTHORIZATION_HEADER);

    if (header != null && header.startsWith(BEARER_PREFIX)) {
      return Optional.of(header.substring(BEARER_PREFIX.length()));
    }

    return Optional.empty();
  }

  private void setSecurityContext(final IdentityProvider.Identity identity) {
    final var authorities =
        identity.roles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();

    final var authentication =
        new UsernamePasswordAuthenticationToken(
            identity, // principal
            null, // credentials (not needed for JWT)
            authorities);

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private boolean isStaticResource(final HttpServletRequest request) {
    final String path = request.getRequestURI();
    return path.startsWith("/css/")
        || path.startsWith("/js/")
        || path.startsWith("/images/")
        || path.startsWith("/fonts/")
        || path.startsWith("/favicon")
        || path.endsWith(".css")
        || path.endsWith(".js")
        || path.endsWith(".ico")
        || path.endsWith(".png")
        || path.endsWith(".jpg")
        || path.endsWith(".gif")
        || path.endsWith(".svg")
        || path.endsWith(".woff")
        || path.endsWith(".woff2");
  }

  /**
   * Writes the session cookie after a successful authentication.
   *
   * @param response HTTP response to set the cookie on
   * @param token the session JWT
   */
  public void setRegisteredUserCookie(final HttpServletResponse response, final String token) {
    writeCookie(
        response,
        jwtProperties.sessionCookieName(),
        token,
        jwtProperties.sessionCookieMaxAgeSeconds());
  }

  /**
   * Clears the session cookie, leaving the identity untouched.
   *
   * @param response HTTP response to clear the cookie on
   */
  public void clearSessionCookie(final HttpServletResponse response) {
    writeCookie(response, jwtProperties.sessionCookieName(), "", 0);
  }
}
