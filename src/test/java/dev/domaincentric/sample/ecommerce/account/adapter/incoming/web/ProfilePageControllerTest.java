package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import static dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.queryParameter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.TestGetProfile;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.TestIdentity;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.TestIdentityProvider;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.TestIdentitySession;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.TestTokenService;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.web.AccountWebTestFixtures.TestTokenService.IssuedToken;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileCommand;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileInputPort;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileResult;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileQuery;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileResult;
import dev.domaincentric.sample.ecommerce.account.application.getprofile.GetProfileResult.Profile;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

/**
 * Unit tests for {@link ProfilePageController}.
 *
 * <p>Covers: GET access control and the model it exposes; the success redirect carrying a flash
 * message and the GET handler folding it into the ViewModel; the error re-render keeping the
 * submitted values; and the token re-issue that keeps the session valid after the login credential
 * changed.
 *
 * <p>The HTTP status codes are the servlet-level effect of the returned view name and are not
 * asserted here; there is no MockMvc or E2E test for the account area yet.
 */
@DisplayName("ProfilePageController")
class ProfilePageControllerTest {

  private static final String USER_ID = "user-4711";
  private static final String EMAIL = "jane.doe@example.com";
  private static final String NEW_EMAIL = "jane.new@example.com";
  private static final String FIRST_NAME = "Jane";
  private static final String LAST_NAME = "Doe";
  private static final String FULL_NAME = FIRST_NAME + " " + LAST_NAME;
  private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 17);
  private static final LocalDate NEW_DATE_OF_BIRTH = LocalDate.of(1990, 5, 18);

  /** The same date as the browser submits it — the POST handler binds a raw string. */
  private static final String NEW_DATE_OF_BIRTH_INPUT = "1990-05-18";

  private static final String LOGIN_REDIRECT = "redirect:/login?returnUrl=%2Faccount%2Fprofile";

  private TestGetProfile getProfile;
  private TestChangeProfile changeProfile;
  private TestIdentityProvider identityProvider;
  private TestTokenService tokenService;
  private TestIdentitySession identitySession;
  private ProfilePageController controller;
  private Model model;
  private RedirectAttributes redirectAttributes;

  @BeforeEach
  void setUp() {
    getProfile = new TestGetProfile();
    changeProfile = new TestChangeProfile();
    identityProvider = new TestIdentityProvider();
    tokenService = new TestTokenService();
    identitySession = new TestIdentitySession();
    controller =
        new ProfilePageController(
            getProfile, changeProfile, identityProvider, tokenService, identitySession);
    model = new ExtendedModelMap();
    redirectAttributes = new RedirectAttributesModelMap();
  }

  private void givenRegisteredIdentityWithAccessibleAccount() {
    identityProvider.setIdentity(TestIdentity.registered(UserId.of(USER_ID), EMAIL));
    getProfile.setResult(
        GetProfileResult.found(new Profile(EMAIL, FIRST_NAME, LAST_NAME, DATE_OF_BIRTH)));
  }

  // ---------------------------------------------------------------- GET

  @Test
  @DisplayName("anonymous GET redirects to login with returnUrl /account/profile")
  void anonymousGetRedirectsToLogin() {
    identityProvider.setIdentity(TestIdentity.anonymous(UserId.of(USER_ID)));

    final String viewName = controller.showProfilePage(model);

    assertNotEquals(
        ProfilePageController.VIEW_NAME,
        viewName,
        "the profile page must not be rendered for an anonymous identity");
    assertEquals(LOGIN_REDIRECT, viewName);
    assertEquals(
        "/account/profile",
        queryParameter(viewName, "returnUrl"),
        "the login redirect must carry a returnUrl resolving to /account/profile");
  }

  @Test
  @DisplayName("anonymous GET does not read the profile")
  void anonymousGetDoesNotReadProfile() {
    identityProvider.setIdentity(TestIdentity.anonymous(UserId.of(USER_ID)));

    controller.showProfilePage(model);

    assertTrue(
        getProfile.receivedQueries().isEmpty(),
        "an anonymous identity must never reach the get profile use case");
  }

  @Test
  @DisplayName("GET without an accessible account redirects and exposes no profile data")
  void getWithoutAccessibleAccountRedirects() {
    identityProvider.setIdentity(TestIdentity.registered(UserId.of(USER_ID), EMAIL));
    getProfile.setResult(GetProfileResult.notFound());

    assertEquals(LOGIN_REDIRECT, controller.showProfilePage(model));
    assertNull(
        model.getAttribute(ProfilePageController.MODEL_ATTRIBUTE),
        "no profile data may reach the model without an accessible account");
  }

  @Test
  @DisplayName("GET with an accessible account renders account/profile for the current userId")
  void getWithAccessibleAccountRendersView() {
    givenRegisteredIdentityWithAccessibleAccount();

    assertEquals("account/profile", controller.showProfilePage(model));
    assertEquals(
        List.of(new GetProfileQuery(USER_ID)),
        getProfile.receivedQueries(),
        "the page reads the profile of the current identity's userId");
  }

  @Test
  @DisplayName("GET sets the model attribute title to 'My Profile'")
  void getSetsTitle() {
    givenRegisteredIdentityWithAccessibleAccount();

    controller.showProfilePage(model);

    assertEquals("My Profile", model.getAttribute("title"));
  }

  @Test
  @DisplayName("GET pre-fills the ViewModel with the stored email and date of birth")
  void getPreFillsStoredValues() {
    givenRegisteredIdentityWithAccessibleAccount();

    controller.showProfilePage(model);

    assertEquals(EMAIL, viewModel(model).email());
    assertEquals("1990-05-17", viewModel(model).dateOfBirth());
  }

  @Test
  @DisplayName("GET exposes the owner's name for display")
  void getExposesOwnerName() {
    givenRegisteredIdentityWithAccessibleAccount();

    controller.showProfilePage(model);

    assertEquals(FULL_NAME, viewModel(model).fullName());
  }

  @Test
  @DisplayName("GET folds the flash attribute 'message' into successMessage()")
  void getFoldsFlashMessageIntoViewModel() {
    givenRegisteredIdentityWithAccessibleAccount();
    model.addAttribute("message", "Your profile has been updated.");

    controller.showProfilePage(model);

    assertEquals(
        "Your profile has been updated.",
        viewModel(model).successMessage(),
        "the flash message of the success redirect must reach the ViewModel");
  }

  @Test
  @DisplayName("GET without a flash message exposes no success message")
  void getWithoutFlashMessageExposesNoSuccessMessage() {
    givenRegisteredIdentityWithAccessibleAccount();

    controller.showProfilePage(model);

    assertNull(viewModel(model).successMessage());
  }

  // ---------------------------------------------------------------- POST

  @Test
  @DisplayName("anonymous POST redirects to login and does not invoke the use case")
  void anonymousPostRedirectsToLogin() {
    identityProvider.setIdentity(TestIdentity.anonymous(UserId.of(USER_ID)));

    final String viewName =
        controller.handleProfileUpdate(
            NEW_EMAIL, NEW_DATE_OF_BIRTH_INPUT, model, redirectAttributes);

    assertEquals(LOGIN_REDIRECT, viewName);
    assertTrue(
        changeProfile.receivedCommands().isEmpty(),
        "an anonymous identity must never reach the change profile use case");
  }

  @Test
  @DisplayName("a submitted change reaches the use case with the current identity's userId")
  void postPassesSubmittedValuesToUseCase() {
    givenRegisteredIdentityWithAccessibleAccount();
    changeProfile.setResult(
        ChangeProfileResult.changed(profileResult(NEW_EMAIL, NEW_DATE_OF_BIRTH)));

    controller.handleProfileUpdate(NEW_EMAIL, NEW_DATE_OF_BIRTH_INPUT, model, redirectAttributes);

    assertEquals(
        List.of(new ChangeProfileCommand(USER_ID, NEW_EMAIL, NEW_DATE_OF_BIRTH)),
        changeProfile.receivedCommands());
  }

  @Test
  @DisplayName("an inaccessible account redirects to login")
  void inaccessibleAccountRedirectsToLogin() {
    identityProvider.setIdentity(TestIdentity.registered(UserId.of(USER_ID), EMAIL));
    changeProfile.setResult(ChangeProfileResult.accountNotAccessible());

    final String viewName =
        controller.handleProfileUpdate(
            NEW_EMAIL, NEW_DATE_OF_BIRTH_INPUT, model, redirectAttributes);

    assertEquals(LOGIN_REDIRECT, viewName);
    assertEquals("/account/profile", queryParameter(viewName, "returnUrl"));
  }

  @Test
  @DisplayName("a successful change redirects to the profile page with the flash message")
  void successRedirectsWithFlashMessage() {
    givenRegisteredIdentityWithAccessibleAccount();
    changeProfile.setResult(
        ChangeProfileResult.changed(profileResult(NEW_EMAIL, NEW_DATE_OF_BIRTH)));

    final String viewName =
        controller.handleProfileUpdate(
            NEW_EMAIL, NEW_DATE_OF_BIRTH_INPUT, model, redirectAttributes);

    assertEquals("redirect:/account/profile", viewName);
    assertEquals(
        "Your profile has been updated.", redirectAttributes.getFlashAttributes().get("message"));
  }

  @Test
  @DisplayName("a successful change re-issues the token with the new email and sets the cookie")
  void successReIssuesToken() {
    givenRegisteredIdentityWithAccessibleAccount();
    changeProfile.setResult(
        ChangeProfileResult.changed(profileResult(NEW_EMAIL, NEW_DATE_OF_BIRTH)));

    controller.handleProfileUpdate(
        NEW_EMAIL.toUpperCase(Locale.ROOT), NEW_DATE_OF_BIRTH_INPUT, model, redirectAttributes);

    assertEquals(
        List.of(new IssuedToken(UserId.of(USER_ID), NEW_EMAIL, Set.of("CUSTOMER"))),
        tokenService.issuedTokens(),
        "the new token keeps the userId and roles and claims the stored, normalised email");
    assertEquals(
        List.of("token-for-" + NEW_EMAIL),
        identitySession.setTokens(),
        "the re-issued token must be written to the identity cookie");
    assertEquals(0, identitySession.clearCount(), "changing the email must not log the user out");
  }

  @Test
  @DisplayName("an email already in use re-renders with the message and the submitted values")
  void emailAlreadyInUseReRenders() {
    givenRegisteredIdentityWithAccessibleAccount();
    changeProfile.setResult(
        ChangeProfileResult.emailAlreadyInUse("This email address is already registered"));

    final String viewName =
        controller.handleProfileUpdate(
            "taken@example.com", NEW_DATE_OF_BIRTH_INPUT, model, redirectAttributes);

    assertEquals("account/profile", viewName);
    assertEquals("This email address is already registered", viewModel(model).errorMessage());
    assertEquals(
        "taken@example.com",
        viewModel(model).email(),
        "the rejected submission stays in the form instead of being retyped");
    assertEquals("1990-05-18", viewModel(model).dateOfBirth());
    assertEquals(
        FULL_NAME,
        viewModel(model).fullName(),
        "the name is not submitted, so the re-render reads it back from the stored profile");
  }

  @Test
  @DisplayName("rejected input re-renders with the use case's message verbatim")
  void rejectedInputReRendersWithMessage() {
    givenRegisteredIdentityWithAccessibleAccount();
    changeProfile.setResult(
        ChangeProfileResult.inputRejected("Invalid email format: not-an-email"));

    final String viewName =
        controller.handleProfileUpdate(
            "not-an-email", NEW_DATE_OF_BIRTH_INPUT, model, redirectAttributes);

    assertEquals("account/profile", viewName);
    assertEquals("Invalid email format: not-an-email", viewModel(model).errorMessage());
    assertEquals("not-an-email", viewModel(model).email());
  }

  @Test
  @DisplayName("a rejected change issues no token")
  void rejectedChangeIssuesNoToken() {
    givenRegisteredIdentityWithAccessibleAccount();
    changeProfile.setResult(
        ChangeProfileResult.emailAlreadyInUse("This email address is already registered"));

    controller.handleProfileUpdate(
        "taken@example.com", NEW_DATE_OF_BIRTH_INPUT, model, redirectAttributes);

    assertTrue(
        tokenService.issuedTokens().isEmpty(), "a rejected change must not re-issue the token");
    assertTrue(identitySession.setTokens().isEmpty());
  }

  @Test
  @DisplayName("no error re-render exposes a success message")
  void errorReRenderExposesNoSuccessMessage() {
    givenRegisteredIdentityWithAccessibleAccount();
    changeProfile.setResult(ChangeProfileResult.inputRejected("Invalid email format: nope"));

    controller.handleProfileUpdate("nope", NEW_DATE_OF_BIRTH_INPUT, model, redirectAttributes);

    assertNull(viewModel(model).successMessage());
    assertFalse(
        redirectAttributes.getFlashAttributes().containsKey("message"),
        "a rejected change must not announce success");
  }

  @Test
  @DisplayName("an unreadable date re-renders the form instead of failing the request")
  void unreadableDateReRendersForm() {
    givenRegisteredIdentityWithAccessibleAccount();

    final String viewName =
        controller.handleProfileUpdate(NEW_EMAIL, "not-a-date", model, redirectAttributes);

    assertEquals("account/profile", viewName);
    assertFalse(
        viewModel(model).errorMessage().isBlank(), "the rejection must name a reason to the user");
    assertTrue(
        changeProfile.receivedCommands().isEmpty(),
        "a value that is not a date must never reach the use case");
    assertEquals(
        "not-a-date",
        viewModel(model).dateOfBirth(),
        "the rejected value stays in the form instead of being retyped");
  }

  @Test
  @DisplayName("a blank date re-renders the form instead of failing the request")
  void blankDateReRendersForm() {
    givenRegisteredIdentityWithAccessibleAccount();

    assertEquals(
        "account/profile",
        controller.handleProfileUpdate(NEW_EMAIL, "", model, redirectAttributes));
    assertTrue(changeProfile.receivedCommands().isEmpty());
  }

  // ---------------------------------------------------------------- helpers

  private static ChangeProfileResult.Profile profileResult(
      final String email, final LocalDate dateOfBirth) {
    return new ChangeProfileResult.Profile(email, dateOfBirth);
  }

  private static ProfilePageViewModel viewModel(final Model model) {
    final Object attribute = model.getAttribute(ProfilePageController.MODEL_ATTRIBUTE);
    assertNotNull(attribute, "the page must expose its ViewModel under 'profilePage'");
    return assertInstanceOf(ProfilePageViewModel.class, attribute);
  }

  /** Test double for the change profile input port, recording the commands it receives. */
  private static final class TestChangeProfile implements ChangeProfileInputPort {

    private final List<ChangeProfileCommand> receivedCommands = new ArrayList<>();
    private ChangeProfileResult result = ChangeProfileResult.accountNotAccessible();

    @Override
    public ChangeProfileResult execute(final ChangeProfileCommand command) {
      receivedCommands.add(command);
      return result;
    }

    void setResult(final ChangeProfileResult result) {
      this.result = result;
    }

    List<ChangeProfileCommand> receivedCommands() {
      return List.copyOf(receivedCommands);
    }
  }
}
