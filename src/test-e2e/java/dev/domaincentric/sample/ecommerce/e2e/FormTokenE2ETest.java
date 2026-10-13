package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import dev.domaincentric.sample.ecommerce.e2e.pages.LoginPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.SiteHeader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A form post the shop's own page did not send: the browser holds the shop's cookies, but the post
 * lacks the form token the page carries, so the shop refuses it.
 */
@DisplayName("Form token E2E Tests")
class FormTokenE2ETest extends BaseE2ETest {

  private static final String PASSWORD = "Secret123";

  @Test
  @DisplayName("A login post without the form token is refused")
  void aLoginPostWithoutTheFormTokenLeavesTheVisitorLoggedOut() {
    // The suite's shop keeps its accounts across tests, so the account gets an address of its own.
    String email = "ada." + System.currentTimeMillis() + "@example.com";
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(email, PASSWORD);
    }
    LoginPage.navigateTo(page);

    // Posted from the same browser, with its cookies, but not from the login form.
    APIResponse response =
        page.request()
            .post(
                BASE_URL + "/login",
                RequestOptions.create()
                    .setForm(FormData.create().set("email", email).set("password", PASSWORD))
                    .setMaxRedirects(0));

    assertEquals(403, response.status());
    navigateTo("/products");
    SiteHeader header = new SiteHeader(page);
    assertFalse(header.greets(), "the visitor is not logged in");
    assertTrue(header.offersLogin(), "the header still offers to log in");
    assertTrue(header.offersRegister(), "the header still offers to register");
  }
}
