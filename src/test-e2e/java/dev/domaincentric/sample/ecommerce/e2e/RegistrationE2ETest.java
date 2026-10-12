package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.SiteHeader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A visitor creates an account on the registration page and is logged in right away. */
@DisplayName("Registration E2E Tests")
class RegistrationE2ETest extends BaseE2ETest {

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
}
