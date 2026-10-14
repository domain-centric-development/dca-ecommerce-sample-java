package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordCommand;
import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordInputPort;
import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordResult;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewInputPort;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewQuery;
import dev.domaincentric.sample.ecommerce.account.domain.model.HashedPassword;
import dev.domaincentric.sample.ecommerce.account.domain.model.PasswordRule;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider.Identity;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * MVC Controller for the "Change Password" page of the account area.
 *
 * <p>Lets a logged-in user replace their own password. Anonymous visitors, and identities whose
 * account is missing or cannot log in, are redirected to the login page with a {@code returnUrl}
 * pointing back to this page — the same rule {@link MyAccountPageController} applies, because "any
 * authenticated request" includes anonymous identities.
 *
 * <p>The confirmation field is a pure presentation concern and is compared here; the current
 * password and the strength of the new one are decided by {@link ChangePasswordInputPort}, whose
 * outcome this page words.
 *
 * <p>Changing the password issues no new token: the token carries the userId, email and roles,
 * never the password, so the session stays valid.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/account/change-password.pug}
 */
@Controller
@RequestMapping(ChangePasswordPageController.CHANGE_PASSWORD_PATH)
public class ChangePasswordPageController {

  public static final String MODEL_ATTRIBUTE = "changePasswordPage";

  public static final String VIEW_NAME = "account/change-password";

  /** Path of the change password page, used as {@code returnUrl} of the login redirect. */
  public static final String CHANGE_PASSWORD_PATH = "/account/change-password";

  static final String TITLE = "Change Password";

  static final String CONFIRMATION_MISMATCH = "New password and confirmation do not match";

  static final String CURRENT_PASSWORD_INVALID = "Current password is not correct";

  private static final String LOGIN_REDIRECT =
      AccountLoginRedirect.toLoginWithReturnUrl(CHANGE_PASSWORD_PATH);

  private final ChangePasswordInputPort changePasswordUseCase;
  private final GetAccountOverviewInputPort getAccountOverviewUseCase;
  private final IdentityProvider identityProvider;

  public ChangePasswordPageController(
      final ChangePasswordInputPort changePasswordUseCase,
      final GetAccountOverviewInputPort getAccountOverviewUseCase,
      final IdentityProvider identityProvider) {
    this.changePasswordUseCase = changePasswordUseCase;
    this.getAccountOverviewUseCase = getAccountOverviewUseCase;
    this.identityProvider = identityProvider;
  }

  /**
   * Displays the change password page.
   *
   * <p>The account overview is queried as an accessibility gate only; its projection is discarded
   * so that no account data reaches this page's model.
   *
   * @param noticeCode the code of the notice a redirect brought along, if any
   * @param model Spring MVC model
   * @return view name {@value #VIEW_NAME}, or a redirect to the login page
   */
  @GetMapping
  public String showChangePasswordPage(
      @RequestParam(name = AccountPageNotice.PARAMETER, required = false)
          final @Nullable String noticeCode,
      final Model model) {
    final Identity identity = identityProvider.getCurrentIdentity();
    if (identity.isAnonymous()) {
      return LOGIN_REDIRECT;
    }

    final boolean accountAccessible =
        getAccountOverviewUseCase
            .execute(new GetAccountOverviewQuery(identity.userId().value()))
            .found();
    if (!accountAccessible) {
      return LOGIN_REDIRECT;
    }

    return render(
        model,
        AccountPageNotice.fromCode(noticeCode)
            .map(notice -> ChangePasswordPageViewModel.withSuccess(notice.sentence()))
            .orElseGet(ChangePasswordPageViewModel::blank));
  }

  /**
   * Handles the change password form submission.
   *
   * @param currentPassword the current plaintext password
   * @param newPassword the new plaintext password
   * @param confirmPassword the confirmation of the new plaintext password
   * @param model Spring MVC model, used on the error re-render path
   * @return view name {@value #VIEW_NAME} on error, a redirect otherwise
   */
  @PostMapping
  public String handleChangePassword(
      @RequestParam final String currentPassword,
      @RequestParam final String newPassword,
      @RequestParam final String confirmPassword,
      final Model model) {
    final Identity identity = identityProvider.getCurrentIdentity();
    if (identity.isAnonymous()) {
      return LOGIN_REDIRECT;
    }

    if (!newPassword.equals(confirmPassword)) {
      return render(model, ChangePasswordPageViewModel.withError(CONFIRMATION_MISMATCH));
    }

    final ChangePasswordResult result =
        changePasswordUseCase.execute(
            new ChangePasswordCommand(identity.userId().value(), currentPassword, newPassword));

    return switch (result.outcome()) {
      case CHANGED -> AccountPageNotice.PASSWORD_CHANGED.redirectTo(CHANGE_PASSWORD_PATH);
      case ACCOUNT_NOT_ACCESSIBLE -> LOGIN_REDIRECT;
      case CURRENT_PASSWORD_INVALID ->
          render(model, ChangePasswordPageViewModel.withError(CURRENT_PASSWORD_INVALID));
      case NEW_PASSWORD_REJECTED ->
          render(
              model,
              ChangePasswordPageViewModel.withError(
                  passwordRuleSentence(result.brokenRule().orElseThrow())));
    };
  }

  private static String render(final Model model, final ChangePasswordPageViewModel viewModel) {
    model.addAttribute("title", TITLE);
    model.addAttribute(MODEL_ATTRIBUTE, viewModel);
    return VIEW_NAME;
  }

  /**
   * The account pages' sentence for a password the policy refuses, worded from the broken rule —
   * the rule, never the attempt. The register page uses it too.
   */
  static String passwordRuleSentence(final PasswordRule rule) {
    return switch (rule) {
      case TOO_SHORT ->
          "Password must be at least " + HashedPassword.MIN_LENGTH + " characters long.";
      case TOO_LONG ->
          "Password must not be longer than " + HashedPassword.MAX_BYTE_LENGTH + " bytes.";
      case NO_UPPERCASE_LETTER -> "Password must contain an uppercase letter.";
      case NO_LOWERCASE_LETTER -> "Password must contain a lowercase letter.";
      case NO_DIGIT -> "Password must contain a digit.";
    };
  }
}
