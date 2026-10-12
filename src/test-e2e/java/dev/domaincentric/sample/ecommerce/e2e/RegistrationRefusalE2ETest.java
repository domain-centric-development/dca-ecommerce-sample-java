package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.BrowserContext;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.SiteHeader;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A visitor tries to register an address another account already has, and the shop refuses it. */
@DisplayName("Registration refusal E2E Tests")
class RegistrationRefusalE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Taken address is refused")
  void anAddressAnotherAccountHasIsRefusedAndTheVisitorStaysLoggedOut() {
    // The shop is shared by the whole run, so the earlier account takes an address of its own.
    String email = "anna.schmidt-" + UUID.randomUUID() + "@example.com";
    try (BrowserContext earlier = browser.newContext()) {
      RegisterPage.navigateTo(earlier.newPage())
          .fillOwner("Anna", "Schmidt", "1990-05-17")
          .fillEmail(email)
          .fillPassword("Secret123")
          .fillConfirmPassword("Secret123")
          .submit();
    }

    String refusal =
        RegisterPage.navigateTo(page)
            .fillOwner("Anna", "Schmidt", "1990-05-17")
            .fillEmail(email)
            .fillPassword("Secret123")
            .fillConfirmPassword("Secret123")
            .submitRefused();

    assertEquals("This email address is already registered", refusal);
    SiteHeader header = new SiteHeader(page);
    assertFalse(header.greets(), "the visitor is not greeted as logged in");
    assertTrue(header.offersRegister(), "the header still offers to register");
  }
}
