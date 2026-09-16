package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.IdentitySession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * MVC Controller for user logout.
 *
 * <p>Handles the logout action by ending the session via {@link IdentitySession} and redirecting
 * the user to the home page.
 *
 * <p><b>Security:</b> Uses POST method to prevent CSRF attacks via link prefetching.
 *
 * <p><b>Clean Architecture:</b> This controller depends only on {@link IdentitySession}, adapter
 * mechanics of the account bounded context, not a port. No use case is needed because logout is a
 * session operation, not a domain operation.
 */
@Controller
@RequestMapping("/logout")
public class LogoutPageController {

  private final IdentitySession identitySession;

  public LogoutPageController(final IdentitySession identitySession) {
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
