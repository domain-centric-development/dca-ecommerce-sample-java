package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.JwtIdentitySession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * MVC Controller for user logout.
 *
 * <p>Handles the logout action by ending the session through {@link JwtIdentitySession} and
 * redirecting the user to the login page.
 *
 * <p><b>Security:</b> Uses POST method to prevent CSRF attacks via link prefetching.
 *
 * <p>No use case is involved: logging out is a session operation of the adapter — cookies, not
 * domain state — so the controller collaborates with the security adapter directly.
 */
@Controller
@RequestMapping("/logout")
public class LogoutPageController {

  private final JwtIdentitySession identitySession;

  public LogoutPageController(final JwtIdentitySession identitySession) {
    this.identitySession = identitySession;
  }

  /**
   * Handles logout by clearing the identity cookie and redirecting to the login page.
   *
   * <p>Redirects to {@code /login?logout=true} so the login page can display a success message.
   *
   * @return redirect to login page with logout flag
   */
  @PostMapping
  public String handleLogout() {
    identitySession.logOut();
    return "redirect:/login?logout=true";
  }
}
