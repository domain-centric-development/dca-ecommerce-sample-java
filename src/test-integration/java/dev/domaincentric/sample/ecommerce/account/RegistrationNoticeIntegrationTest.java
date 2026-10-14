package dev.domaincentric.sample.ecommerce.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.HtmlUtils;

/**
 * The sentence the page a registration leads to shows, rendered by the wired application: the
 * registration's redirect carries the notice, the page it leads to words it.
 */
// MockMvc requests commit, and the shared H2 database outlives the Spring context — so this class
// gets a database of its own, where the address it registers is still free.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:registration_notice_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Registration notice")
class RegistrationNoticeIntegrationTest {

  private static final Pattern CSRF_FIELD = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");
  private static final Pattern RETURN_URL_FIELD =
      Pattern.compile("name=\"returnUrl\" value=\"([^\"]*)\"");
  private static final Pattern SUCCESS_MESSAGE =
      Pattern.compile("data-test=\"success-message\"[^>]*>([^<]*)<");

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("The page returned to says the account was created")
  void thePageTheVisitorCameFromSaysTheAccountWasCreated() throws Exception {
    final Map<String, Cookie> cookies = new LinkedHashMap<>();
    final String form = page("/register?returnUrl=/products", cookies);

    final MockHttpServletResponse submitted =
        mockMvc
            .perform(
                post("/register")
                    .cookie(cookies.values().toArray(Cookie[]::new))
                    .param("_csrf", text(CSRF_FIELD, form))
                    .param("returnUrl", text(RETURN_URL_FIELD, form))
                    .param("firstName", "Anna")
                    .param("lastName", "Schmidt")
                    .param("dateOfBirth", "1990-05-17")
                    .param("email", "anna.schmidt@example.com")
                    .param("password", "Secret123")
                    .param("confirmPassword", "Secret123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/products"))
            .andReturn()
            .getResponse();
    keep(cookies, submitted);

    final String catalogue = page("/products", cookies);
    assertThat(all(SUCCESS_MESSAGE, catalogue))
        .containsExactly("Account created successfully! Welcome!");
  }

  /** A page as the browser gets it, with the cookies collected so far; keeps the ones it sets. */
  private String page(final String path, final Map<String, Cookie> cookies) throws Exception {
    final var request = get(path);
    if (!cookies.isEmpty()) {
      request.cookie(cookies.values().toArray(Cookie[]::new));
    }
    final MockHttpServletResponse response =
        mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse();
    keep(cookies, response);
    return response.getContentAsString();
  }

  private static void keep(final Map<String, Cookie> cookies, final MockHttpServletResponse r) {
    for (final Cookie cookie : r.getCookies()) {
      if (cookie.getMaxAge() == 0) {
        cookies.remove(cookie.getName());
      } else {
        cookies.put(cookie.getName(), cookie);
      }
    }
  }

  private static String text(final Pattern pattern, final String html) {
    final Matcher matcher = pattern.matcher(html);
    assertThat(matcher.find()).as("%s in the rendered page", pattern).isTrue();
    return HtmlUtils.htmlUnescape(matcher.group(1)).strip();
  }

  private static List<String> all(final Pattern pattern, final String html) {
    return pattern
        .matcher(html)
        .results()
        .map(m -> HtmlUtils.htmlUnescape(m.group(1)).strip())
        .toList();
  }
}
