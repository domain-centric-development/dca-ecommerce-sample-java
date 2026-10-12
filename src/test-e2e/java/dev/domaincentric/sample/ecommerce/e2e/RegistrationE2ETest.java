package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
}
