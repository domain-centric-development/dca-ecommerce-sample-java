package dev.domaincentric.sample.ecommerce.account;

import static dev.domaincentric.sample.ecommerce.account.Visitor.element;
import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * A login form sent from a browser that is logged in already, rendered by the wired application:
 * the page it answers with, and the two accounts' carts afterwards.
 */
// MockMvc requests commit, and the shared H2 database outlives the Spring context — so this class
// gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties = "test.database=login_while_logged_in_test")
@AutoConfigureMockMvc
@DisplayName("Login while logged in")
class LoginWhileLoggedInIntegrationTest {

  private static final String ANNA = "anna.schmidt@example.com";
  private static final String BEN = "ben.weber@example.com";
  private static final String PASSWORD = "Secret123";

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("A login sent while logged in changes nothing")
  void aLoginSentWhileLoggedInShowsTheNoticeAndMovesNoCart() throws Exception {
    final Visitor annasBrowser = new Visitor(mockMvc);
    annasBrowser.register(ANNA, PASSWORD);
    annasBrowser.addToCart("Clean Architecture");
    new Visitor(mockMvc).register(BEN, PASSWORD);

    final String page =
        annasBrowser.refusedSubmit("/login", Map.of("email", BEN, "password", PASSWORD));

    assertThat(element(page, "login-logged-in-message").text())
        .isEqualTo("You are logged in as " + ANNA + ". Please log out first.");
    assertThat(element(page, "user-greeting").text()).isEqualTo("Welcome, " + ANNA);
    assertThat(annasBrowser.cartLines()).containsExactly("Clean Architecture x1");
    final Visitor bensBrowser = new Visitor(mockMvc);
    bensBrowser.logIn("/login", BEN, PASSWORD);
    assertThat(bensBrowser.cartLines()).isEmpty();
  }
}
