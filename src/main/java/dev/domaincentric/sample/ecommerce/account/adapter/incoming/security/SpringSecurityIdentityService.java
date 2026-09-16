package dev.domaincentric.sample.ecommerce.account.adapter.incoming.security;

import dev.domaincentric.sample.ecommerce.account.api.Identity;
import dev.domaincentric.sample.ecommerce.account.api.IdentityService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Spring Security implementation of the Account context's {@link IdentityService}.
 *
 * <p>Reads the {@link Identity} that {@link JwtAuthenticationFilter} placed in the security context
 * for the current request. Thread-safe because Spring Security keeps the context per thread.
 */
@Component
public class SpringSecurityIdentityService implements IdentityService {

  @Override
  public Identity currentIdentity() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      throw new IllegalStateException(
          "No authentication found in security context. "
              + "This usually means the request did not pass through the JWT filter.");
    }

    final Object principal = authentication.getPrincipal();

    if (principal instanceof Identity identity) {
      return identity;
    }

    throw new IllegalStateException(
        "Expected Identity in security context principal, but found: "
            + (principal != null ? principal.getClass().getName() : "null"));
  }
}
