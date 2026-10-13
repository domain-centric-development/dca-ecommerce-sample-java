package dev.domaincentric.sample.ecommerce.backoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.HtmlUtils;

/**
 * The payload toggle of every entry of the event publication log, as the operator gets the page.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@AutoConfigureMockMvc
@DisplayName("Backoffice event payload on the log page")
class EventPayloadPageIntegrationTest {

  private static final String LOGIN_PAGE = "/backoffice/login";
  private static final String EVENT_LOG = "/backoffice/events";

  private static final Pattern CSRF_FIELD = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");
  private static final Pattern PAYLOAD =
      Pattern.compile(
          "<details([^>]*\\bdata-test=\"event-payload\"[^>]*)>\\s*<summary[^>]*>([^<]*)</summary>");
  private static final Pattern OPEN = Pattern.compile("\\bopen\\b");

  @Autowired private MockMvc mockMvc;

  private MockHttpSession operator;

  @BeforeEach
  void logInAsTheOperator() throws Exception {
    operator = new MockHttpSession();
    final Matcher csrf = CSRF_FIELD.matcher(page(LOGIN_PAGE));
    assertThat(csrf.find()).as("the login form's CSRF field").isTrue();
    mockMvc
        .perform(
            post(LOGIN_PAGE)
                .session(operator)
                .param("_csrf", csrf.group(1))
                .param("username", "admin")
                .param("password", "admin"))
        .andExpect(redirectedUrl(EVENT_LOG));
  }

  @Test
  @DisplayName("Payload is collapsed at first")
  void everyEntryOffersItsPayloadCollapsed() throws Exception {
    final List<String> entries = entries(page(EVENT_LOG));
    assertThat(entries).isNotEmpty();

    for (final String entry : entries) {
      final Matcher payload = PAYLOAD.matcher(entry);
      assertThat(payload.find()).as("the entry's payload toggle").isTrue();
      assertThat(HtmlUtils.htmlUnescape(payload.group(2)).strip()).isEqualTo("Event Payload");
      assertThat(OPEN.matcher(payload.group(1)).find()).as("the payload is open").isFalse();
    }
  }

  /** The markup of every entry of the log, one string per entry. */
  private static List<String> entries(final String page) {
    final String[] parts = page.split("data-test=\"event-log-item\"");
    return Arrays.asList(parts).subList(1, parts.length);
  }

  /** A page as the browser gets it within the operator's session. */
  private String page(final String path) throws Exception {
    return mockMvc
        .perform(get(path).session(operator))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }
}
