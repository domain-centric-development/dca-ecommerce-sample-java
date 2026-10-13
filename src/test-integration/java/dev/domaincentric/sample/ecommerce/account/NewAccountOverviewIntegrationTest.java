package dev.domaincentric.sample.ecommerce.account;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The account overview of an account that has never logged in, rendered by the wired application
 * right after the registration signed the visitor in.
 */
// The account must not have logged in before, so the class gets a database of its own: no other
// test can log the same address in first.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:new_account_overview_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Account overview of a new account")
class NewAccountOverviewIntegrationTest {

  private static final Pattern LAST_LOGIN_ROW =
      Pattern.compile(
          ">\\s*Last Login\\s*</span>\\s*<span[^>]*\\bdata-test=\"account-last-login\"[^>]*>([^<]*)<");

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Overview after registration shows never")
  void overviewOfAnAccountThatNeverLoggedInShowsNeverAsLastLogin() throws Exception {
    final Visitor visitor = new Visitor(mockMvc);
    visitor.register("anna.schmidt@example.com", "Secret123");

    final String overview = visitor.page("/account");

    assertThat(Visitor.text(LAST_LOGIN_ROW, overview)).isEqualTo("Never");
  }
}
