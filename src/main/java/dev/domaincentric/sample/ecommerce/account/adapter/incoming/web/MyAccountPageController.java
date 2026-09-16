package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewInputPort;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewQuery;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewResult;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewResult.AccountOverview;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider.Identity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * MVC Controller for the "My Account" area.
 *
 * <p>Renders the personalised account overview for registered users. Anonymous visitors, and
 * identities whose account is missing or cannot log in, are redirected to the login page with a
 * {@code returnUrl} pointing back to the account area; authentication then reports the actual
 * reason.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/account/overview.pug}
 */
@Controller
@RequestMapping(MyAccountPageController.ACCOUNT_PATH)
public class MyAccountPageController {

  public static final String MODEL_ATTRIBUTE = "myAccountPage";

  public static final String VIEW_NAME = "account/overview";

  /** Path of the account area, used as {@code returnUrl} of the login redirect. */
  public static final String ACCOUNT_PATH = "/account";

  private static final String LOGIN_REDIRECT =
      AccountLoginRedirect.toLoginWithReturnUrl(ACCOUNT_PATH);

  private final GetAccountOverviewInputPort getAccountOverviewUseCase;
  private final IdentityProvider identityProvider;

  public MyAccountPageController(
      final GetAccountOverviewInputPort getAccountOverviewUseCase,
      final IdentityProvider identityProvider) {
    this.getAccountOverviewUseCase = getAccountOverviewUseCase;
    this.identityProvider = identityProvider;
  }

  /**
   * Displays the account overview page.
   *
   * @param model Spring MVC model
   * @return view name {@value #VIEW_NAME}, or a redirect to the login page for anonymous identities
   *     and identities without an accessible account
   */
  @GetMapping
  public String showMyAccountPage(final Model model) {
    final Identity identity = identityProvider.getCurrentIdentity();
    if (identity.isAnonymous()) {
      return LOGIN_REDIRECT;
    }

    final GetAccountOverviewResult result =
        getAccountOverviewUseCase.execute(new GetAccountOverviewQuery(identity.userId().value()));
    return result.account().map(overview -> renderOverview(model, overview)).orElse(LOGIN_REDIRECT);
  }

  private static String renderOverview(final Model model, final AccountOverview overview) {
    model.addAttribute("title", "My Account");
    model.addAttribute(MODEL_ATTRIBUTE, MyAccountPageViewModel.from(overview));
    return VIEW_NAME;
  }
}
