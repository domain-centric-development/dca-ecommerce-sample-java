package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

/**
 * The paths authenticated by an {@code Authorization: Bearer} header and nothing else.
 *
 * <p>This list and the CSRF exemption in the security configuration are two halves of one decision:
 * these endpoints may skip the CSRF token <b>only</b> because no browser cookie can authenticate
 * them (ADR-035). The JWT filter and the refusal of an unauthenticated caller both ask this class,
 * so neither keeps a list of its own that could drift.
 */
public final class TokenOnlyPaths {

  /** The prefixes of the Bearer-only surface. */
  public static final List<String> PREFIXES = List.of("/api/", "/mcp");

  private TokenOnlyPaths() {}

  /** Whether a request is authenticated by a Bearer token alone. */
  public static boolean isTokenOnlyEndpoint(final HttpServletRequest request) {
    final String path = request.getRequestURI().toLowerCase(Locale.ROOT);
    return PREFIXES.stream().anyMatch(path::startsWith);
  }
}
