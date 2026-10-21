package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.IdentitySession;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountCommand;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountInputPort;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountResult;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountNotAccessibleException;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * MVC Controller for login page.
 *
 * <p>This controller handles server-side rendered login pages using Pug templates.
 *
 * <p><b>Clean Architecture:</b> This controller only depends on its own bounded context (account).
 * After successful login, it redirects to the cart merge page, allowing the cart context to decide
 * if a merge is actually needed. This avoids cross-context dependencies.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/account/login.pug}
 */
@Controller
@RequestMapping("/login")
public class LoginPageController {

  private final AuthenticateAccountInputPort authenticateAccountUseCase;
  private final TokenService tokenService;
  private final IdentityProvider identityProvider;
  private final IdentitySession identitySession;

  public LoginPageController(
      final AuthenticateAccountInputPort authenticateAccountUseCase,
      final TokenService tokenService,
      final IdentityProvider identityProvider,
      final IdentitySession identitySession) {
    this.authenticateAccountUseCase = authenticateAccountUseCase;
    this.tokenService = tokenService;
    this.identityProvider = identityProvider;
    this.identitySession = identitySession;
  }

  /**
   * Displays the login page.
   *
   * @param model Spring MVC model
   * @param returnUrl optional URL to redirect to after login
   * @param logout optional flag indicating the user just logged out
   * @return view name "account/login"
   */
  @GetMapping
  public String showLoginPage(
      final Model model,
      @RequestParam(required = false) final String returnUrl,
      @RequestParam(required = false) final String logout) {

    model.addAttribute("title", "Login");
    addReturnPage(model, returnUrl);
    if (logout != null) {
      model.addAttribute("logoutSuccess", true);
    }
    return "account/login";
  }

  /**
   * Handles login form submission.
   *
   * <p>After successful login, always redirects to the cart merge page; the session token names the
   * guest the browser was before. The cart context will determine if a merge is actually required
   * and redirect appropriately if not.
   *
   * @param email the user's email
   * @param password the user's password
   * @param returnUrl optional URL to redirect to after login
   * @param model Spring MVC model
   * @return redirect to cart merge page on success, login page on failure
   */
  @PostMapping
  public String handleLogin(
      @RequestParam final String email,
      @RequestParam final String password,
      @RequestParam(required = false) final String returnUrl,
      final Model model) {

    // Capture anonymous user ID before authentication changes the identity
    final String anonymousUserId = identityProvider.getCurrentIdentity().userId().value();

    try {
      final AuthenticateAccountCommand command = new AuthenticateAccountCommand(email, password);
      final AuthenticateAccountResult result = authenticateAccountUseCase.execute(command);

      if (!result.success()) {
        model.addAttribute("title", "Login");
        model.addAttribute("error", result.errorMessage());
        model.addAttribute("email", email);
        addReturnPage(model, returnUrl);
        return "account/login";
      }

      final UserId registeredUserId = UserId.of(result.userId());

      // The session names the guest this browser was, so that the cart context brings only that
      // guest's cart along — never one a request names.
      final String token =
          anonymousUserId.equals(registeredUserId.value())
              ? tokenService.generateRegisteredToken(
                  registeredUserId, result.email(), result.roles())
              : tokenService.generateRegisteredToken(
                  registeredUserId, result.email(), result.roles(), UserId.of(anonymousUserId));

      identitySession.setRegisteredIdentity(token);

      // Always redirect to merge page - let cart context decide if merge is needed
      // This avoids cross-context dependency (account → cart)
      String mergeUrl = "/cart/merge";
      if (returnUrl != null && !returnUrl.isBlank()) {
        mergeUrl += "?returnUrl=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);
      }
      return "redirect:" + mergeUrl;

    } catch (final UseCaseException | DomainException | IllegalArgumentException e) {
      // The context's own refusals, plus the argument exception a form field still reaches a value
      // object with.
      model.addAttribute("title", "Login");
      model.addAttribute("error", messageFor(e));
      model.addAttribute("email", email);
      addReturnPage(model, returnUrl);
      return "account/login";
    }
  }

  /**
   * Puts the return page on the login page: the form hands it to the login as it came, and the
   * "Register here" link carries it on only when it is a page of the shop.
   */
  private static void addReturnPage(final Model model, final String returnUrl) {
    model.addAttribute("returnUrl", returnUrl);
    model.addAttribute("registerUrl", ReturnPage.carriedOnTo("/register", returnUrl));
  }

  /**
   * The page's own sentence for a refusal: chosen by the failure's type, filled with its values.
   * The failure's message is for the log, never for the person at the form.
   */
  private static String messageFor(final RuntimeException failure) {
    return switch (failure) {
      case AccountNotAccessibleException e -> "This account cannot sign in.";
      case IllegalArgumentException e -> "Please enter your email and your password.";
      default -> "Signing in failed. Please try again.";
    };
  }
}
