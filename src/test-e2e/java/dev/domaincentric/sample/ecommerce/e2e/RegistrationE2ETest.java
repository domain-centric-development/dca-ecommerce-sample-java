package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.HomePage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.SiteHeader;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/** A visitor creates an account on the registration page and is logged in right away. */
@DisplayName("Registration E2E Tests")
class RegistrationE2ETest extends BaseE2ETest {

  private ConfigurableApplicationContext shop;

  @AfterEach
  void stopTheShop() {
    if (shop != null) {
      shop.close();
    }
  }

  @Test
  @DisplayName("Registration logs the visitor in")
  void registrationLogsTheVisitorIn() {
    RegisterPage register = RegisterPage.navigateTo(page);

    register
        .fillOwner("Anna", "Schmidt", "1990-05-17")
        .fillEmail("anna.schmidt@example.com")
        .fillPassword("Secret123")
        .fillConfirmPassword("Secret123")
        .submit();

    assertEquals("/", getCurrentPath());
    SiteHeader header = new SiteHeader(page);
    assertEquals("Welcome, anna.schmidt@example.com", header.greeting());
    assertFalse(header.offersRegister(), "a logged-in visitor is not offered to register");
  }

  @Test
  @DisplayName("Registration says the account was created")
  void theHomePageARegistrationLeadsToSaysTheAccountWasCreated() {
    // The shared shop may already hold the address from the test above, and a taken address is
    // refused — so this test starts a shop of its own.
    shop = ShopUnderTest.startedWith("registration_says_created_e2e");
    final String shopUrl = ShopUnderTest.baseUrlOf(shop);

    RegisterPage.navigateTo(page, shopUrl)
        .fillOwner("Anna", "Schmidt", "1990-05-17")
        .fillEmail("anna.schmidt@example.com")
        .fillPassword("Secret123")
        .fillConfirmPassword("Secret123")
        .submit();

    final HomePage home = new HomePage(page, shopUrl);
    assertEquals(shopUrl + "/", page.url());
    assertEquals(List.of("Account created successfully! Welcome!"), home.successMessages());
  }

  @Test
  @DisplayName("An outside return page leads home after registering")
  void aRegistrationLeadsToTheHomePageInsteadOfAnotherSitesReturnPage() {
    // The shared shop may already hold the address, and a taken address is refused.
    shop = ShopUnderTest.startedWith("registration_outside_return_page_e2e");
    final String shopUrl = ShopUnderTest.baseUrlOf(shop);

    RegisterPage.navigateTo(page, shopUrl, "https://elsewhere.example/")
        .fillOwner("Anna", "Schmidt", "1990-05-17")
        .fillEmail("anna.schmidt@example.com")
        .fillPassword("Secret123")
        .fillConfirmPassword("Secret123")
        .submit();

    assertEquals(shopUrl + "/", page.url());
    final HomePage home = new HomePage(page, shopUrl);
    assertEquals(List.of("Account created successfully! Welcome!"), home.successMessages());
  }

  @Test
  @DisplayName("Differing passwords are refused")
  void differingPasswordsAreRefusedAndTheFormKeepsTheRest() {
    RegisterPage register = RegisterPage.navigateTo(page);

    register
        .fillOwner("Anna", "Schmidt", "1990-05-17")
        .fillEmail("anna.schmidt@example.com")
        .fillPassword("Secret123")
        .fillConfirmPassword("Secret124")
        .submitExpectingRefusal();

    assertEquals("Passwords do not match", register.errorMessage());
    assertEquals("Anna", register.fieldValue("firstName"));
    assertEquals("Schmidt", register.fieldValue("lastName"));
    assertEquals("1990-05-17", register.fieldValue("dateOfBirth"));
    assertEquals("anna.schmidt@example.com", register.fieldValue("email"));
    assertEquals("", register.fieldValue("password"));
    assertEquals("", register.fieldValue("confirmPassword"));
    assertTrue(
        new SiteHeader(page).offersRegister(), "a refused visitor is still offered to register");
  }

  @Test
  @DisplayName("Todays date of birth is refused")
  void aDateOfBirthOfTodayIsRefusedWithTheCheckYourEntriesSentence() {
    RegisterPage register = RegisterPage.navigateTo(page);

    register
        .fillOwner("Anna", "Schmidt", LocalDate.now().toString())
        .fillEmail("born.today@example.com")
        .fillPassword("Secret123")
        .fillConfirmPassword("Secret123")
        .submitExpectingRefusal();

    assertEquals(
        "Please check the email address, your name and your date of birth.",
        register.errorMessage());
  }
}
