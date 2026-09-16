package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.account.application.shared.IdentitySession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * MVC Controller for user logout.
 *
 * <p>Handles the logout action by clearing the JWT identity cookie via the {@link IdentitySession}
 * output port and redirecting the user to the home page.
 *
 * <p><b>Security:</b> Uses POST method to prevent CSRF attacks via link prefetching.
 *
 * <p><b>Clean Architecture:</b> This controller depends only on the {@link IdentitySession} output
 * port from the account bounded context. No use case is needed because logout is a session
 * operation, not a domain operation.
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
