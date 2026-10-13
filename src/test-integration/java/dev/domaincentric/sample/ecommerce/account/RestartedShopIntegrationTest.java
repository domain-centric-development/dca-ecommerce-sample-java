package dev.domaincentric.sample.ecommerce.account;

import static dev.domaincentric.sample.ecommerce.account.Visitor.element;
import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.JwtProperties;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.JwtTokenService;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import jakarta.servlet.http.Cookie;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The shop after a restart, rendered by the wired application: it keeps nothing, so its store holds
 * none of the accounts it had, while a browser still carries the session the shop signed before.
 */
// A database of its own, in which no account was ever stored — the store as a restart leaves it.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:restarted_shop_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Restarted shop")
class RestartedShopIntegrationTest {

  private static final String EMAIL = "anna.schmidt@example.com";
  private static final String PASSWORD = "Secret123";

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtTokenService tokenService;
  @Autowired private JwtProperties jwtProperties;

  @Test
  @DisplayName("Account pages send to the login after a restart")
  void anAccountPageSendsABrowserWithASessionFromBeforeTheRestartToTheLogin() throws Exception {
    final Visitor customer = browserLoggedInBeforeTheRestart();

    assertThat(customer.redirectOf("/account")).isEqualTo("/login?returnUrl=%2Faccount");
  }

  @Test
  @DisplayName("Old credentials are refused after a restart")
  void credentialsOfAnAccountFromBeforeTheRestartAreRefused() throws Exception {
    final Visitor visitor = new Visitor(mockMvc);

    final String loginPage = visitor.refusedLogIn("/login", EMAIL, PASSWORD);

    assertThat(element(loginPage, "login-error-message").text())
        .isEqualTo("Invalid email or password");
  }

  /**
   * A browser that logged in before the restart: it holds the identity and the session the shop
   * signed for the customer then — the signing secret survives a restart, the account does not.
   */
  private Visitor browserLoggedInBeforeTheRestart() {
    final UserId customer = UserId.generateRegistered();
    final Visitor browser = new Visitor(mockMvc);
    browser.holds(
        new Cookie(jwtProperties.cookieName(), tokenService.generateAnonymousToken(customer)));
    browser.holds(
        new Cookie(
            jwtProperties.sessionCookieName(),
            tokenService.generateRegisteredToken(customer, EMAIL, Set.of("CUSTOMER"))));
    return browser;
  }
}
