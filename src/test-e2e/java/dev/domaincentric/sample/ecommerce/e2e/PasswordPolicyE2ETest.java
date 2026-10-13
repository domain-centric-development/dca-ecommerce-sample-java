package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.SiteHeader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A visitor registers with a password the policy refuses, and the registration page names the rule
 * it breaks.
 *
 * <p>The shop the suite shares may already hold the address from another test, and a taken address
 * is refused before the password is looked at — so this test starts a shop of its own.
 */
@DisplayName("Password policy E2E Tests")
class PasswordPolicyE2ETest extends BaseE2ETest {

  private ConfigurableApplicationContext shop;

  @AfterEach
  void stopTheShop() {
    if (shop != null) {
      shop.close();
    }
  }

  @Test
  @DisplayName("Password without uppercase is refused")
  void aPasswordWithoutUppercaseLetterIsRefusedAndTheVisitorStaysLoggedOut() {
    shop = ShopUnderTest.startedWith("password_policy_e2e");

    String refusal =
        RegisterPage.navigateTo(page, ShopUnderTest.baseUrlOf(shop))
            .fillOwner("Anna", "Schmidt", "1990-05-17")
            .fillEmail("anna.schmidt@example.com")
            .fillPassword("secret123")
            .fillConfirmPassword("secret123")
            .submitRefused();

    assertEquals("Password must contain an uppercase letter.", refusal);
    assertFalse(new SiteHeader(page).greets(), "the visitor is not greeted as logged in");
  }
}
