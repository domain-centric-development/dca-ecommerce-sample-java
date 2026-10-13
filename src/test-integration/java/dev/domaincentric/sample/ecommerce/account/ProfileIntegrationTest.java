package dev.domaincentric.sample.ecommerce.account;

import static dev.domaincentric.sample.ecommerce.account.Visitor.element;
import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.account.Visitor.Element;
import dev.domaincentric.sample.ecommerce.account.Visitor.Saved;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * "My Profile" rendered by the wired application: the fixed name and the details it shows, a save
 * of the customer's own address, and the login it asks a visitor for.
 */
// MockMvc requests commit, and the shared H2 database outlives the Spring context — so this class
// gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:profile_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Profile")
class ProfileIntegrationTest {

  private static final String EMAIL = "anna.schmidt@example.com";
  private static final String PASSWORD = "Secret123";
  private static final String PROFILE = "/account/profile";

  /** The class shares one database, so the account is registered once for all its tests. */
  private static boolean accountRegistered;

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Profile shows name and details")
  void theProfileShowsTheFixedNameAndTheDetailsThatCanChange() throws Exception {
    final Visitor customer = loggedInCustomer();
    final String myProfile =
        element(customer.page("/account"), "account-nav-profile").attribute("href");

    final String profile = customer.page(myProfile);

    assertThat(myProfile).isEqualTo("/account/profile");
    assertThat(element(profile, "profile-title").text()).isEqualTo("My Profile");
    assertThat(cardTitle(profile)).isEqualTo("Basic Information");
    assertThat(element(profile, "account-nav-profile").attribute("aria-current")).isEqualTo("page");
    assertThat(element(profile, "profile-owner-name").text()).isEqualTo("Anna Schmidt");
    assertThat(element(profile, "profile-name-note").text())
        .isEqualTo("Your name was set when you created your account and cannot be changed.");
    assertThat(
            Pattern.compile("<input[^>]*name=\"(firstName|lastName|name)\"")
                .matcher(profile)
                .find())
        .as("an input for a name")
        .isFalse();
    final Element email = element(profile, "profile-email-input");
    assertThat(email.attribute("value")).isEqualTo(EMAIL);
    assertThat(hintAfter(profile, "profile-email-input"))
        .isEqualTo("This address is also your login");
    assertThat(element(profile, "profile-date-of-birth-input").attribute("value"))
        .isEqualTo("1990-05-17");
    assertThat(element(profile, "profile-submit-button").text()).isEqualTo("Save Changes");
  }

  @Test
  @DisplayName("Own address in other capitals is accepted")
  void theOwnAddressInOtherCapitalsIsSavedAsTheAddressItIs() throws Exception {
    final Visitor customer = loggedInCustomer();

    final Saved saved = customer.saveProfile("Anna.Schmidt@Example.com", "1990-05-17");

    assertThat(saved.location()).isEqualTo("/account/profile");
    assertThat(element(saved.page(), "profile-success-message").text())
        .isEqualTo("Your profile has been updated.");
    assertThat(element(saved.page(), "profile-email-input").attribute("value")).isEqualTo(EMAIL);
  }

  @Test
  @DisplayName("Malformed address is refused in the profile")
  void aMalformedAddressIsRefusedWithThePagesSentence() throws Exception {
    final Visitor customer = loggedInCustomer();

    final String refused =
        customer.refusedSubmit(PROFILE, profileFields("anna.meyer@example", "1990-05-17"));

    assertThat(element(refused, "profile-error-message").text())
        .isEqualTo("Please enter a valid email address.");
  }

  @Test
  @DisplayName("Missing date is refused in the profile")
  void aMissingDateOfBirthIsRefusedWithTheExpectedFormat() throws Exception {
    final Visitor customer = loggedInCustomer();

    final String refused = customer.refusedSubmit(PROFILE, profileFields(EMAIL, ""));

    assertThat(element(refused, "profile-error-message").text())
        .isEqualTo("Please enter a date like 1990-05-17");
  }

  @Test
  @DisplayName("Future date is refused in the profile")
  void aDateOfBirthInTheFutureIsRefused() throws Exception {
    final Visitor customer = loggedInCustomer();

    final String refused = customer.refusedSubmit(PROFILE, profileFields(EMAIL, "2999-01-01"));

    assertThat(element(refused, "profile-error-message").text())
        .isEqualTo("Please enter a date of birth that is not in the future.");
  }

  @Test
  @DisplayName("Unreadable date is refused in the profile")
  void anUnreadableDateIsRefusedAndStaysInTheField() throws Exception {
    final Visitor customer = loggedInCustomer();

    final String refused = customer.refusedSubmit(PROFILE, profileFields(EMAIL, "17.05.1990"));

    assertThat(element(refused, "profile-error-message").text())
        .isEqualTo("Please enter a date like 1990-05-17");
    assertThat(element(refused, "profile-date-of-birth-input").attribute("value"))
        .isEqualTo("17.05.1990");
  }

  @Test
  @DisplayName("Refused change saves nothing")
  void aRefusedChangeSavesNeitherTheValidAddressNorTheDate() throws Exception {
    final Visitor customer = loggedInCustomer();
    customer.refusedSubmit(PROFILE, profileFields("anna.meyer@example.com", "2999-01-01"));

    final String profile = customer.page(PROFILE);

    assertThat(element(profile, "profile-email-input").attribute("value")).isEqualTo(EMAIL);
    assertThat(element(profile, "profile-date-of-birth-input").attribute("value"))
        .isEqualTo("1990-05-17");
  }

  @Test
  @DisplayName("Profile sends a visitor to the login")
  void theProfileSendsAVisitorWithoutCookiesToTheLogin() throws Exception {
    final Visitor visitor = new Visitor(mockMvc);

    assertThat(visitor.redirectOf("/account/profile"))
        .isEqualTo("/login?returnUrl=%2Faccount%2Fprofile");
  }

  /** A browser of its own in which the scenario's customer has logged in. */
  private Visitor loggedInCustomer() throws Exception {
    registerTheAccount();
    final Visitor customer = new Visitor(mockMvc);
    customer.logIn("/login", EMAIL, PASSWORD);
    return customer;
  }

  /** Registers the scenario's account as Anna Schmidt, born 1990-05-17, once for this database. */
  private void registerTheAccount() throws Exception {
    if (accountRegistered) {
      return;
    }
    new Visitor(mockMvc).register(EMAIL, PASSWORD);
    accountRegistered = true;
  }

  /** The profile form's two fields, as the browser submits them. */
  private static Map<String, String> profileFields(final String email, final String dateOfBirth) {
    final Map<String, String> fields = new LinkedHashMap<>();
    fields.put("email", email);
    fields.put("dateOfBirth", dateOfBirth);
    return fields;
  }

  /** The title of the page's card; it carries no data-test. */
  private static String cardTitle(final String page) {
    return Visitor.text(Pattern.compile("class=\"card__header-title\">([^<]*)<"), page);
  }

  /** The hint written right after the input with the given data-test; it carries no data-test. */
  private static String hintAfter(final String page, final String inputDataTest) {
    return Visitor.text(
        Pattern.compile(
            "data-test=\""
                + Pattern.quote(inputDataTest)
                + "\"[^>]*>\\s*<p class=\"form__hint\">([^<]*)<"),
        page);
  }
}
