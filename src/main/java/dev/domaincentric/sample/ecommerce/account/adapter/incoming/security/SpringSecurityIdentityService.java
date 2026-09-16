package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security;

import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Spring Security implementation of IdentityProvider.
 *
 * <p>This component extracts the current user's Identity from the Spring Security context. The
 * Identity is placed in the security context by the {@link JwtAuthenticationFilter}.
 *
 * <p><b>Usage:</b>
 *
 * <pre>{@code
 * @Autowired
 * private IdentityProvider identityProvider;
 *
 * public void someMethod() {
 *     IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
 *     UserId userId = identity.userId();
 *     // ...
 * }
 * }</pre>
 *
 * <p><b>Thread Safety:</b> This implementation is thread-safe because Spring Security uses
 * thread-local storage for the security context.
 */
@Component
public class SpringSecurityIdentityProvider implements IdentityProvider {

  @Override
  public IdentityProvider.Identity getCurrentIdentity() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      throw new IllegalStateException(
          "No authentication found in security context. "
              + "This usually means the request did not pass through the JWT filter.");
    }

    final Object principal = authentication.getPrincipal();

    if (principal instanceof IdentityProvider.Identity identity) {
      return identity;
    }

    throw new IllegalStateException(
        "Expected Identity in security context principal, but found: "
            + (principal != null ? principal.getClass().getName() : "null"));
  }
}
