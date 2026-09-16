package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileCommand;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileInputPort;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileResult;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileInputPort;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileQuery;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileResult;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileResult.Profile;
import dev.domaincentric.sample.ecommerce.account.application.shared.IdentitySession;
import dev.domaincentric.sample.ecommerce.account.application.shared.TokenService;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider.Identity;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for the "My Profile" page of the account area.
 *
 * <p>Lets a logged-in user change the basic information of their own account. Anonymous visitors,
 * and identities whose account is missing or cannot log in, are redirected to the login page with a
 * {@code returnUrl} pointing back to this page.
 *
 * <p>The email is also the login credential and a token claim, so a successful change re-issues the
 * identity token; the session stays valid and the user is not logged out.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/account/profile.pug}
 */
@Controller
@RequestMapping(ProfilePageController.PROFILE_PATH)
public class ProfilePageController {

  public static final String MODEL_ATTRIBUTE = "profilePage";

  public static final String VIEW_NAME = "account/profile";

  /** Path of the profile page, used as {@code returnUrl} of the login redirect. */
  public static final String PROFILE_PATH = "/account/profile";

  static final String TITLE = "My Profile";

  static final String PROFILE_UPDATED = "Your profile has been updated.";

  /** Name of the flash attribute carrying the success message across the redirect. */
  private static final String FLASH_MESSAGE = "message";

  private static final String LOGIN_REDIRECT =
      AccountLoginRedirect.toLoginWithReturnUrl(PROFILE_PATH);

  private final GetProfileInputPort getProfileUseCase;
  private final ChangeProfileInputPort changeProfileUseCase;
  private final IdentityProvider identityProvider;
  private final TokenService tokenService;
  private final IdentitySession identitySession;

  public ProfilePageController(
      final GetProfileInputPort getProfileUseCase,
      final ChangeProfileInputPort changeProfileUseCase,
      final IdentityProvider identityProvider,
      final TokenService tokenService,
      final IdentitySession identitySession) {
    this.getProfileUseCase = getProfileUseCase;
    this.changeProfileUseCase = changeProfileUseCase;
    this.identityProvider = identityProvider;
    this.tokenService = tokenService;
    this.identitySession = identitySession;
  }

  /**
   * Displays the profile page.
   *
   * @param model Spring MVC model (carries the flash attribute {@code message} after a redirect)
   * @return view name {@value #VIEW_NAME}, or a redirect to the login page
   */
  @GetMapping
  public String showProfilePage(final Model model) {
    final Identity identity = identityProvider.getCurrentIdentity();
    if (identity.isAnonymous()) {
      return LOGIN_REDIRECT;
    }

    final GetProfileResult result =
        getProfileUseCase.execute(new GetProfileQuery(identity.userId().value()));

    return result.profile().map(profile -> renderProfile(model, profile)).orElse(LOGIN_REDIRECT);
  }

  private static String renderProfile(final Model model, final Profile profile) {
    final Object flashMessage = model.getAttribute(FLASH_MESSAGE);
    return render(
        model,
        flashMessage == null
            ? ProfilePageViewModel.of(fullName(profile), profile.email(), profile.dateOfBirth())
            : ProfilePageViewModel.withSuccess(
                fullName(profile),
                profile.email(),
                profile.dateOfBirth(),
                String.valueOf(flashMessage)));
  }

  private static String fullName(final Profile profile) {
    return profile.firstName() + " " + profile.lastName();
  }

  /**
   * Handles the profile form submission.
   *
   * @param email the submitted email address
   * @param dateOfBirth the submitted date of birth of the account's owner, as the browser sent it
   * @param model Spring MVC model, used on the error re-render path
   * @param redirectAttributes flash attributes, used on the success redirect path
   * @return view name {@value #VIEW_NAME} on error, a redirect otherwise
   */
  @PostMapping
  public String handleProfileUpdate(
      @RequestParam final String email,
      @RequestParam final String dateOfBirth,
      final Model model,
      final RedirectAttributes redirectAttributes) {
    final Identity identity = identityProvider.getCurrentIdentity();
    if (identity.isAnonymous()) {
      return LOGIN_REDIRECT;
    }

    final Optional<LocalDate> submittedDate = SubmittedDate.parse(dateOfBirth);
    if (submittedDate.isEmpty()) {
      return renderRejection(model, identity, email, dateOfBirth, SubmittedDate.NOT_A_DATE);
    }

    final ChangeProfileResult result =
        changeProfileUseCase.execute(
            new ChangeProfileCommand(identity.userId().value(), email, submittedDate.get()));

    return switch (result.outcome()) {
      case CHANGED -> {
        reIssueToken(identity, result.profile().orElseThrow().email());
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE, PROFILE_UPDATED);
        yield "redirect:" + PROFILE_PATH;
      }
      case ACCOUNT_NOT_ACCESSIBLE -> LOGIN_REDIRECT;
      case EMAIL_ALREADY_IN_USE, INPUT_REJECTED ->
          renderRejection(model, identity, email, dateOfBirth, result.errorMessage().orElseThrow());
    };
  }

  /**
   * Re-renders the form with the submitted values and the stored name.
   *
   * <p>The name is not part of the submission — it cannot be changed and therefore has no input —
   * so it is read back from the stored profile to render the page whole.
   */
  private String renderRejection(
      final Model model,
      final Identity identity,
      final String submittedEmail,
      final String submittedDateOfBirth,
      final String message) {
    return getProfileUseCase
        .execute(new GetProfileQuery(identity.userId().value()))
        .profile()
        .map(
            stored ->
                render(
                    model,
                    ProfilePageViewModel.withError(
                        fullName(stored), submittedEmail, submittedDateOfBirth, message)))
        .orElse(LOGIN_REDIRECT);
  }

  /**
   * Replaces the identity token so that its email claim matches the stored address again.
   *
   * <p>Without this the session would keep claiming the superseded address until it expires.
   *
   * @param identity the current identity, supplying the unchanged userId and roles
   * @param storedEmail the email address as the account stores it after the change
   */
  private void reIssueToken(final Identity identity, final String storedEmail) {
    identitySession.setRegisteredIdentity(
        tokenService.generateRegisteredToken(identity.userId(), storedEmail, identity.roles()));
  }

  private static String render(final Model model, final ProfilePageViewModel viewModel) {
    model.addAttribute("title", TITLE);
    model.addAttribute(MODEL_ATTRIBUTE, viewModel);
    return VIEW_NAME;
  }
}
