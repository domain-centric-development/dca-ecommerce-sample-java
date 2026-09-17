package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * How the shop refuses a caller who has not authenticated.
 *
 * <p>An API caller gets {@code 401} and is told which scheme to use; a browser is sent to the login
 * form and back to where it wanted to go. A registered caller who merely lacks a role is not
 * challenged but forbidden — that is {@link ShopAccessDeniedHandler} (ADR-036).
 */
@Component
public class ShopAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final String loginPath;

  public ShopAuthenticationEntryPoint() {
    this("/login");
  }

  ShopAuthenticationEntryPoint(final String loginPath) {
    this.loginPath = loginPath;
  }

  @Override
  public void commence(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final AuthenticationException authenticationException)
      throws IOException {

    if (TokenOnlyPaths.isTokenOnlyEndpoint(request)) {
      response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
      ApiRefusal.write(response, HttpStatus.UNAUTHORIZED);
      return;
    }

    response.sendRedirect(loginPath + "?returnUrl=" + encodedTarget(request));
  }

  private static String encodedTarget(final HttpServletRequest request) {
    final String query = request.getQueryString();
    final String target = request.getRequestURI() + (query == null ? "" : "?" + query);
    return URLEncoder.encode(target, StandardCharsets.UTF_8);
  }
}
