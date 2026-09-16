package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.account.application.registeraccount.RegisterAccountCommand;
import dev.domaincentric.sample.ecommerce.account.application.registeraccount.RegisterAccountInputPort;
import dev.domaincentric.sample.ecommerce.account.application.registeraccount.RegisterAccountResult;
import dev.domaincentric.sample.ecommerce.account.application.shared.IdentitySession;
import dev.domaincentric.sample.ecommerce.account.application.shared.TokenService;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.time.LocalDate;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for registration page.
 *
 * <p>This controller handles server-side rendered registration pages using Pug templates.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/account/register.pug}
 */
@Controller
@RequestMapping("/register")
public class RegisterPageController {

  private final RegisterAccountInputPort registerAccountUseCase;
  private final TokenService tokenService;
  private final IdentityProvider identityProvider;
  private final IdentitySession identitySession;

  public RegisterPageController(
      final RegisterAccountInputPort registerAccountUseCase,
      final TokenService tokenService,
      final IdentityProvider identityProvider,
      final IdentitySession identitySession) {
    this.registerAccountUseCase = registerAccountUseCase;
    this.tokenService = tokenService;
    this.identityProvider = identityProvider;
    this.identitySession = identitySession;
  }

  /**
   * Displays the registration page.
   *
   * @param model Spring MVC model
   * @param returnUrl optional URL to redirect to after registration
   * @return view name "account/register"
   */
  @GetMapping
  public String showRegisterPage(
      final Model model, @RequestParam(required = false) final String returnUrl) {

    model.addAttribute("title", "Register");
    model.addAttribute("returnUrl", returnUrl);
    return "account/register";
  }

  /**
   * Handles registration form submission.
   *
   * @param email the user's email
   * @param password the user's password
   * @param confirmPassword password confirmation
   * @param firstName the owner's first name
   * @param lastName the owner's last name
   * @param dateOfBirth the owner's date of birth, as the browser sent it
   * @param returnUrl optional URL to redirect to after registration
   * @param redirectAttributes for passing flash messages
   * @param model Spring MVC model
   * @return redirect to home or returnUrl on success, register page on failure
   */
  @PostMapping
  public String handleRegistration(
      @RequestParam final String email,
      @RequestParam final String password,
      @RequestParam final String confirmPassword,
      @RequestParam final String firstName,
      @RequestParam final String lastName,
      @RequestParam final String dateOfBirth,
      @RequestParam(required = false) final String returnUrl,
      final RedirectAttributes redirectAttributes,
      final Model model) {

    final Submission submission =
        new Submission(email, firstName, lastName, dateOfBirth, returnUrl);

    if (!password.equals(confirmPassword)) {
      return renderError(model, submission, "Passwords do not match");
    }

    if (password.length() < 8) {
      return renderError(model, submission, "Password must be at least 8 characters");
    }

    final Optional<LocalDate> submittedDate = SubmittedDate.parse(dateOfBirth);
    if (submittedDate.isEmpty()) {
      return renderError(model, submission, SubmittedDate.NOT_A_DATE);
    }

    try {
      final String currentUserId = identityProvider.getCurrentIdentity().userId().value();

      final RegisterAccountCommand command =
          new RegisterAccountCommand(
              email, password, currentUserId, firstName, lastName, submittedDate.get());

      final RegisterAccountResult result = registerAccountUseCase.execute(command);

      final String token =
          tokenService.generateRegisteredToken(
              UserId.of(result.userId()), result.email(), result.roles());

      identitySession.setRegisteredIdentity(token);

      redirectAttributes.addFlashAttribute("message", "Account created successfully! Welcome!");

      if (returnUrl != null && !returnUrl.isBlank()) {
        return "redirect:" + returnUrl;
      }
      return "redirect:/";

    } catch (final IllegalArgumentException e) {
      return renderError(model, submission, e.getMessage());
    }
  }

  /**
   * Re-renders the form with the submitted values so the user does not retype them.
   *
   * <p>The password is deliberately not carried over.
   */
  private static String renderError(
      final Model model, final Submission submission, final String error) {
    model.addAttribute("title", "Register");
    model.addAttribute("error", error);
    model.addAttribute("email", submission.email());
    model.addAttribute("firstName", submission.firstName());
    model.addAttribute("lastName", submission.lastName());
    model.addAttribute("dateOfBirth", submission.dateOfBirth());
    model.addAttribute("returnUrl", submission.returnUrl());
    return "account/register";
  }

  /** The re-fillable part of a registration submission. */
  private record Submission(
      String email,
      String firstName,
      String lastName,
      String dateOfBirth,
      @Nullable String returnUrl) {}
}
