package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * A caller who authenticated but lacks the role the route demands: {@code 403}, and for the API as
 * the same kind of document its {@code 401} is. Pages keep the framework's bare refusal.
 */
@Component
public class ShopAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final AccessDeniedException accessDeniedException)
      throws IOException {

    if (TokenOnlyPaths.isTokenOnlyEndpoint(request)) {
      ApiRefusal.write(response, HttpStatus.FORBIDDEN);
      return;
    }

    response.sendError(HttpStatus.FORBIDDEN.value());
  }
}
