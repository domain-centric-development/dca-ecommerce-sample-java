package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import static dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.queryParameter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.TestGetAccountOverview;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.TestIdentity;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.TestIdentityProvider;
import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordCommand;
import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordInputPort;
import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordResult;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewQuery;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewResult;
import dev.domaincentric.sample.ecommerce.account.application.getaccountoverview.GetAccountOverviewResult.AccountOverview;
import dev.domaincentric.sample.ecommerce.account.application.shared.IdentitySession;
import dev.domaincentric.sample.ecommerce.account.application.shared.TokenService;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

/**
 * Unit tests for {@link ChangePasswordPageController}.
 *
 * <p>Covers: GET access control and the model it exposes; the controller never puts a submitted
 * password into the model; a confirmation mismatch short-circuits before the port and wins over a
 * wrong current password; error re-render; success redirect carrying a flash message and the GET
 * handler folding it into the ViewModel; an inaccessible account redirecting; and that no token is
 * issued — the controller depends on neither {@code TokenService} nor {@code IdentitySession}.
 *
 * <p>The HTTP status codes are the servlet-level effect of the returned view name and are not
 * asserted here; there is no MockMvc or E2E test for the account area yet.
 */
@DisplayName("ChangePasswordPageController")
class ChangePasswordPageControllerTest {

  private static final String USER_ID = "user-4711";
  private static final String EMAIL = "jane.doe@example.com";
  private static final String LOGIN_REDIRECT =
      "redirect:/login?returnUrl=%2Faccount%2Fchange-password";
  private static final String CURRENT = "OldPassw0rd";
  private static final String NEW = "NewPassw0rd";

  private TestChangePassword changePassword;
  private TestGetAccountOverview getAccountOverview;
  private TestIdentityProvider identityProvider;
  private ChangePasswordPageController controller;
  private Model model;
  private RedirectAttributes redirectAttributes;

  @BeforeEach
  void setUp() {
    changePassword = new TestChangePassword();
    getAccountOverview = new TestGetAccountOverview();
    identityProvider = new TestIdentityProvider();
    controller =
        new ChangePasswordPageController(changePassword, getAccountOverview, identityProvider);
    model = new ExtendedModelMap();
    redirectAttributes = new RedirectAttributesModelMap();
  }

  private void givenRegisteredIdentityWithAccessibleAccount() {
    identityProvider.setIdentity(TestIdentity.registered(UserId.of(USER_ID), EMAIL));
    getAccountOverview.setResult(GetAccountOverviewResult.found(new AccountOverview(EMAIL, null)));
  }

  // ---------------------------------------------------------------- GET

  @Test
  @DisplayName("anonymous GET redirects to login with returnUrl /account/change-password")
  void anonymousGetRedirectsToLogin() {
    identityProvider.setIdentity(TestIdentity.anonymous(UserId.of(USER_ID)));

    final String viewName = controller.showChangePasswordPage(model);

    assertNotEquals(
        ChangePasswordPageController.VIEW_NAME,
        viewName,
        "the change password page must not be rendered for an anonymous identity");
    assertEquals(LOGIN_REDIRECT, viewName);
  }

  @Test
  @DisplayName("anonymous GET does not invoke the change password use case")
  void anonymousGetDoesNotInvokePort() {
    identityProvider.setIdentity(TestIdentity.anonymous(UserId.of(USER_ID)));

    controller.showChangePasswordPage(model);

    assertTrue(
        changePassword.receivedCommands().isEmpty(),
        "rendering the form must never invoke the change password use case");
  }

  @Test
  @DisplayName("GET without an accessible account redirects instead of throwing")
  void getWithoutAccessibleAccountRedirects() {
    identityProvider.setIdentity(TestIdentity.registered(UserId.of(USER_ID), EMAIL));
    getAccountOverview.setResult(GetAccountOverviewResult.notFound());

    assertEquals(LOGIN_REDIRECT, controller.showChangePasswordPage(model));
  }

  @Test
  @DisplayName("GET with an accessible account renders account/change-password")
  void getWithAccessibleAccountRendersView() {
    givenRegisteredIdentityWithAccessibleAccount();

    assertEquals("account/change-password", controller.showChangePasswordPage(model));
    assertEquals(
        List.of(new GetAccountOverviewQuery(USER_ID)),
        getAccountOverview.receivedQueries(),
        "the accessibility gate queries the overview for the current identity's userId");
  }

  @Test
  @DisplayName("the accessibility gate's projection does not reach the model")
  void getDiscardsAccountProjection() {
    givenRegisteredIdentityWithAccessibleAccount();

    controller.showChangePasswordPage(model);

    for (final Object attribute : model.asMap().values()) {
      assertFalse(
          String.valueOf(attribute).contains(EMAIL),
          "the overview projection is an accessibility gate only and must not reach the model: "
              + attribute);
    }
  }

  @Test
  @DisplayName("GET sets the model attribute title to 'Change Password'")
  void getSetsTitle() {
    givenRegisteredIdentityWithAccessibleAccount();

    controller.showChangePasswordPage(model);

    assertEquals("Change Password", model.getAttribute("title"));
  }

  @Test
  @DisplayName("GET exposes a ChangePasswordPageViewModel as 'changePasswordPage'")
  void getExposesViewModel() {
    givenRegisteredIdentityWithAccessibleAccount();

    controller.showChangePasswordPage(model);

    assertInstanceOf(
        ChangePasswordPageViewModel.class,
        model.getAttribute(ChangePasswordPageController.MODEL_ATTRIBUTE),
        "the page must expose its ViewModel under 'changePasswordPage'");
  }

  @Test
  @DisplayName("GET folds the flash attribute 'message' into successMessage()")
  void getFoldsFlashMessageIntoViewModel() {
    givenRegisteredIdentityWithAccessibleAccount();
    model.addAttribute("message", "Your password has been changed.");

    controller.showChangePasswordPage(model);

    assertEquals(
        "Your password has been changed.",
        viewModel(model).successMessage(),
        "the flash message of the success redirect must reach the ViewModel");
  }

  @Test
  @DisplayName("GET without a flash message exposes no success message")
  void getWithoutFlashMessageExposesNoSuccessMessage() {
    givenRegisteredIdentityWithAccessibleAccount();

    controller.showChangePasswordPage(model);

    assertEquals(null, viewModel(model).successMessage());
  }

  // ---------------------------------------------------------------- POST

  @Test
  @DisplayName("anonymous POST redirects to login and does not invoke the use case")
  void anonymousPostRedirectsToLogin() {
    identityProvider.setIdentity(TestIdentity.anonymous(UserId.of(USER_ID)));

    final String viewName =
        controller.handleChangePassword(CURRENT, NEW, NEW, model, redirectAttributes);

    assertEquals(LOGIN_REDIRECT, viewName);
    assertTrue(
        changePassword.receivedCommands().isEmpty(),
        "an anonymous identity must never reach the change password use case");
  }

  @Test
  @DisplayName("a confirmation mismatch re-renders the view without invoking the use case")
  void confirmationMismatchReRendersWithoutInvokingPort() {
    givenRegisteredIdentityWithAccessibleAccount();

    final String viewName =
        controller.handleChangePassword(CURRENT, NEW, "Different1", model, redirectAttributes);

    assertEquals("account/change-password", viewName);
    assertTrue(
        changePassword.receivedCommands().isEmpty(),
        "a mismatch is detected before the use case is invoked");
  }

  @Test
  @DisplayName("a confirmation mismatch exposes the mismatch message")
  void confirmationMismatchExposesMessage() {
    givenRegisteredIdentityWithAccessibleAccount();

    controller.handleChangePassword(CURRENT, NEW, "Different1", model, redirectAttributes);

    assertEquals("New password and confirmation do not match", viewModel(model).errorMessage());
  }

  @Test
  @DisplayName("the mismatch message wins over a wrong current password")
  void mismatchWinsOverWrongCurrentPassword() {
    givenRegisteredIdentityWithAccessibleAccount();
    changePassword.setResult(
        ChangePasswordResult.currentPasswordInvalid("Current password is not correct"));

    controller.handleChangePassword("WrongPassw0rd", NEW, "Different1", model, redirectAttributes);

    assertEquals("New password and confirmation do not match", viewModel(model).errorMessage());
    assertTrue(
        changePassword.receivedCommands().isEmpty(),
        "the mismatch short-circuits before the current password is verified");
  }

  @Test
  @DisplayName("a rejected new password re-renders with the use case's message")
  void rejectedNewPasswordReRendersWithMessage() {
    givenRegisteredIdentityWithAccessibleAccount();
    changePassword.setResult(
        ChangePasswordResult.newPasswordRejected("Password must contain at least one digit"));

    final String viewName =
        controller.handleChangePassword(
            CURRENT, "Weakpassword", "Weakpassword", model, redirectAttributes);

    assertEquals("account/change-password", viewName);
    assertEquals("Password must contain at least one digit", viewModel(model).errorMessage());
  }

  @Test
  @DisplayName("a wrong current password re-renders with the use case's message")
  void wrongCurrentPasswordReRendersWithMessage() {
    givenRegisteredIdentityWithAccessibleAccount();
    changePassword.setResult(
        ChangePasswordResult.currentPasswordInvalid("Current password is not correct"));

    final String viewName =
        controller.handleChangePassword("WrongPassw0rd", NEW, NEW, model, redirectAttributes);

    assertEquals("account/change-password", viewName);
    assertEquals("Current password is not correct", viewModel(model).errorMessage());
  }

  @Test
  @DisplayName("an error re-render puts no submitted password into the model")
  void errorReRenderLeaksNoPassword() {
    givenRegisteredIdentityWithAccessibleAccount();
    changePassword.setResult(
        ChangePasswordResult.currentPasswordInvalid("Current password is not correct"));

    controller.handleChangePassword("WrongPassw0rd", NEW, NEW, model, redirectAttributes);

    for (final Object attribute : model.asMap().values()) {
      final String rendered = String.valueOf(attribute);
      assertFalse(
          rendered.contains("WrongPassw0rd"), "the submitted current password leaked: " + rendered);
      assertFalse(rendered.contains(NEW), "the submitted new password leaked: " + rendered);
    }
  }

  @Test
  @DisplayName("a successful change redirects with the flash message")
  void successRedirectsWithFlashMessage() {
    givenRegisteredIdentityWithAccessibleAccount();
    changePassword.setResult(ChangePasswordResult.changed());

    final String viewName =
        controller.handleChangePassword(CURRENT, NEW, NEW, model, redirectAttributes);

    assertEquals("redirect:/account/change-password", viewName);
    assertEquals(
        "Your password has been changed.", redirectAttributes.getFlashAttributes().get("message"));
    assertEquals(
        List.of(new ChangePasswordCommand(USER_ID, CURRENT, NEW)),
        changePassword.receivedCommands(),
        "the controller passes the current identity's userId and both passwords to the use case");
  }

  @Test
  @DisplayName("an inaccessible account redirects to login")
  void inaccessibleAccountRedirectsToLogin() {
    identityProvider.setIdentity(TestIdentity.registered(UserId.of(USER_ID), EMAIL));
    changePassword.setResult(ChangePasswordResult.accountNotAccessible());

    final String viewName =
        controller.handleChangePassword(CURRENT, NEW, NEW, model, redirectAttributes);

    assertEquals(LOGIN_REDIRECT, viewName);
    assertEquals(
        "/account/change-password",
        queryParameter(viewName, "returnUrl"),
        "the login redirect must carry a returnUrl resolving to /account/change-password");
  }

  // ---------------------------------------------------------------- structure

  @Test
  @DisplayName("the controller depends on neither TokenService nor IdentitySession")
  void controllerIssuesNoToken() {
    final List<Class<?>> parameterTypes =
        Arrays.stream(ChangePasswordPageController.class.getDeclaredConstructors())
            .flatMap(constructor -> Arrays.stream(constructor.getParameterTypes()))
            .toList();

    assertFalse(
        parameterTypes.contains(TokenService.class),
        "changing a password must not issue a new token");
    assertFalse(
        parameterTypes.contains(IdentitySession.class),
        "changing a password must not touch the identity session");
    assertEquals(
        1,
        ChangePasswordPageController.class.getDeclaredConstructors().length,
        "the controller declares a single constructor");
    final Constructor<?> constructor =
        ChangePasswordPageController.class.getDeclaredConstructors()[0];
    assertTrue(
        Arrays.asList(constructor.getParameterTypes()).contains(ChangePasswordInputPort.class),
        "the controller drives the change password input port");
  }

  // ---------------------------------------------------------------- helpers

  private static ChangePasswordPageViewModel viewModel(final Model model) {
    final Object attribute = model.getAttribute(ChangePasswordPageController.MODEL_ATTRIBUTE);
    assertNotNull(attribute, "the page must expose its ViewModel under 'changePasswordPage'");
    return assertInstanceOf(ChangePasswordPageViewModel.class, attribute);
  }

  /** Test double for the change password input port, recording the commands it receives. */
  private static final class TestChangePassword implements ChangePasswordInputPort {

    private final List<ChangePasswordCommand> receivedCommands = new ArrayList<>();
    private ChangePasswordResult result = ChangePasswordResult.changed();

    @Override
    public ChangePasswordResult execute(final ChangePasswordCommand command) {
      receivedCommands.add(command);
      return result;
    }

    void setResult(final ChangePasswordResult result) {
      this.result = result;
    }

    List<ChangePasswordCommand> receivedCommands() {
      return List.copyOf(receivedCommands);
    }
  }
}
