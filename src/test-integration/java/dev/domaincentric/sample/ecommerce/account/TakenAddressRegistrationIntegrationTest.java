package dev.domaincentric.sample.ecommerce.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import java.util.LinkedHashMap;
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
 * A visitor without cookies registers an address another account already has, on the page the wired
 * application renders.
 */
// The scenario registers Anna, so it gets a store of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:taken_address_registration_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Registration of a taken address")
class TakenAddressRegistrationIntegrationTest {

  private static final Pattern CSRF_FIELD = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Taken address in other capitals is refused")
  void anAddressAnotherAccountHasIsRefusedWhateverItsCapitalsAndSurroundingSpaces()
      throws Exception {
    final MockHttpServletResponse earlier =
        register(new LinkedHashMap<>(), "anna.schmidt@example.com");
    assertThat(earlier.getRedirectedUrl()).as("the earlier registration").isEqualTo("/");

    final MockHttpServletResponse refused =
        register(new LinkedHashMap<>(), " Anna.Schmidt@Example.COM ");

    assertThat(refused.getStatus()).isEqualTo(200);
    assertThat(error(refused.getContentAsString()))
        .isEqualTo("This email address is already registered");
  }

  /**
   * Opens the registration page with the browser's cookies and submits it with Anna's details under
   * the given address; keeps the cookies the shop sets.
   */
  private MockHttpServletResponse register(final Map<String, Cookie> cookies, final String email)
      throws Exception {
    final var open = get("/register");
    if (!cookies.isEmpty()) {
      open.cookie(cookies.values().toArray(Cookie[]::new));
    }
    final MockHttpServletResponse form =
        mockMvc.perform(open).andExpect(status().isOk()).andReturn().getResponse();
    keep(cookies, form);
    final Matcher csrf = CSRF_FIELD.matcher(form.getContentAsString());
    assertThat(csrf.find()).as("the form's csrf field").isTrue();
    final MockHttpServletResponse submitted =
        mockMvc
            .perform(
                post("/register")
                    .cookie(cookies.values().toArray(Cookie[]::new))
                    .param("_csrf", csrf.group(1))
                    .param("firstName", "Anna")
                    .param("lastName", "Schmidt")
                    .param("dateOfBirth", "1990-05-17")
                    .param("email", email)
                    .param("password", "Secret123")
                    .param("confirmPassword", "Secret123"))
            .andReturn()
            .getResponse();
    keep(cookies, submitted);
    return submitted;
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

  private static String error(final String page) {
    final Matcher matcher =
        Pattern.compile("<\\w+[^>]*\\bdata-test=\"register-error-message\"[^>]*>([^<]*)")
            .matcher(page);
    assertThat(matcher.find()).as("the refusal on the registration page").isTrue();
    return HtmlUtils.htmlUnescape(matcher.group(1)).strip();
  }
}
