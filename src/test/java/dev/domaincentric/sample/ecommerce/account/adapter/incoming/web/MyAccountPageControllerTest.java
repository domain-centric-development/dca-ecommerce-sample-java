package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import static dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.queryParameter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.TestGetAccountOverview;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.TestIdentity;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.TestIdentityProvider;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewQuery;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewResult;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewResult.AccountOverview;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

/**
 * Unit tests for {@link MyAccountPageController}.
 *
 * <p>Covers: a registered identity renders the overview view and exposes the email the greeting is
 * built from; an anonymous identity redirects to login with a returnUrl resolving to {@code
 * /account}; a registered identity without an accessible account redirects instead of throwing.
 *
 * <p>The HTTP status codes are the servlet-level effect of the returned view name: {@code
 * "account/overview"} renders 200, {@code "redirect:..."} renders 302. The status codes themselves
 * are not asserted — there is no MockMvc or E2E test for {@code /account} yet.
 */
@DisplayName("MyAccountPageController")
class MyAccountPageControllerTest {

  private static final String USER_ID = "user-4711";
  private static final String EMAIL = "jane.doe@example.com";
  private static final Instant LAST_LOGIN = Instant.parse("2026-07-31T08:15:30Z");

  private TestGetAccountOverview getAccountOverview;
  private TestIdentityProvider identityProvider;
  private MyAccountPageController controller;
  private Model model;

  @BeforeEach
  void setUp() {
    getAccountOverview = new TestGetAccountOverview();
    identityProvider = new TestIdentityProvider();
    controller = new MyAccountPageController(getAccountOverview, identityProvider);
    model = new ExtendedModelMap();
  }

  @Test
  @DisplayName("registered identity renders the account/overview view")
  void registeredIdentityRendersOverview() {
    identityProvider.setIdentity(TestIdentity.registered(UserId.of(USER_ID), EMAIL));
    getAccountOverview.setResult(
        GetAccountOverviewResult.found(new AccountOverview(EMAIL, LAST_LOGIN)));

    final String viewName = controller.showMyAccountPage(model);

    assertEquals(MyAccountPageController.VIEW_NAME, viewName);
    assertEquals(
        List.of(new GetAccountOverviewQuery(USER_ID)),
        getAccountOverview.receivedQueries(),
        "the controller queries the overview for the current identity's userId");
  }

  @Test
  @DisplayName("exposes a ViewModel carrying the authenticated email")
  void exposesViewModelWithEmail() {
    identityProvider.setIdentity(TestIdentity.registered(UserId.of(USER_ID), EMAIL));
    getAccountOverview.setResult(
        GetAccountOverviewResult.found(new AccountOverview(EMAIL, LAST_LOGIN)));

    controller.showMyAccountPage(model);

    final Object attribute = model.getAttribute(MyAccountPageController.MODEL_ATTRIBUTE);
    assertNotNull(attribute, "the overview page must expose its ViewModel to the template");
    final MyAccountPageViewModel viewModel =
        assertInstanceOf(MyAccountPageViewModel.class, attribute);
    assertEquals(EMAIL, viewModel.email(), "the ViewModel must expose the authenticated email");
  }

  @Test
  @DisplayName("anonymous identity redirects to login with returnUrl /account")
  void anonymousIdentityRedirectsToLogin() {
    identityProvider.setIdentity(TestIdentity.anonymous(UserId.of(USER_ID)));

    final String viewName = controller.showMyAccountPage(model);

    assertRedirectsToLoginWithAccountReturnUrl(viewName);
    assertTrue(
        getAccountOverview.receivedQueries().isEmpty(),
        "no account lookup is needed for an anonymous identity");
  }

  @Test
  @DisplayName("registered identity without an account redirects instead of throwing")
  void registeredIdentityWithoutAccountRedirectsToLogin() {
    identityProvider.setIdentity(TestIdentity.registered(UserId.of(USER_ID), EMAIL));
    getAccountOverview.setResult(GetAccountOverviewResult.notFound());

    final String viewName = controller.showMyAccountPage(model);

    assertRedirectsToLoginWithAccountReturnUrl(viewName);
  }

  private static void assertRedirectsToLoginWithAccountReturnUrl(final String viewName) {
    assertNotEquals(
        MyAccountPageController.VIEW_NAME, viewName, "the overview must not be rendered");
    assertTrue(
        viewName.startsWith("redirect:/login"),
        "expected a redirect to the login page, was: " + viewName);
    assertEquals(
        "/account",
        queryParameter(viewName, "returnUrl"),
        "the login redirect must carry a returnUrl resolving to /account");
  }
}
