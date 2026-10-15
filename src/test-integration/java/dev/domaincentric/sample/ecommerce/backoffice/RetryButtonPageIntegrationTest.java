package dev.domaincentric.sample.ecommerce.backoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.EventPublicationSummary;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsInputPort;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsQuery;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsResult;
import dev.domaincentric.sample.ecommerce.backoffice.application.replayfailedpublication.ReplayFailedPublicationCommand;
import dev.domaincentric.sample.ecommerce.backoffice.application.replayfailedpublication.ReplayFailedPublicationInputPort;
import dev.domaincentric.sample.ecommerce.backoffice.application.replayfailedpublication.ReplayFailedPublicationResult;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.HtmlUtils;

/**
 * The retry button of the event publication log, as the operator gets the page, over a log the test
 * decides: the event log's input port answers one delivery that used up its five attempts, and the
 * retry's input port records the retry and completes nothing.
 */
// A context of its own: the replaced event log answer must not reach any other test class.
@SpringBootTest(classes = EcommerceSampleApplication.class)
@AutoConfigureMockMvc
@Import(RetryButtonPageIntegrationTest.LogChosenByTheTest.class)
@DisplayName("Retry button on the event log page")
class RetryButtonPageIntegrationTest {

  private static final String LOGIN_PAGE = "/backoffice/login";
  private static final String EVENT_LOG = "/backoffice/events";

  private static final Pattern CSRF_FIELD = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");
  private static final Pattern SUBMIT_BUTTON =
      Pattern.compile("<button[^>]*\\btype=\"submit\"[^>]*>([^<]*)</button>");
  private static final Pattern RETRY_ACTION =
      Pattern.compile("<form[^>]*\\baction=\"(/backoffice/events/[^\"]+/replay)\"");

  @Autowired private MockMvc mockMvc;
  @Autowired private RecordedRetries retries;

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
  @DisplayName("Failed entry offers a retry")
  void anEntryThatUsedUpItsAttemptsShowsTheRetryButton() throws Exception {
    final List<String> entries = entries(page(EVENT_LOG));
    assertThat(entries).as("the entries the log lists").hasSize(1);

    final List<String> buttons = submitButtons(entries.get(0));

    assertThat(buttons).contains("Retry failed listener");
  }

  @Test
  @DisplayName("Failing retry leaves the entry failed")
  void aRetryOpensTheEventLogAgain() throws Exception {
    final String entry = entries(page(EVENT_LOG)).get(0);
    final Matcher action = RETRY_ACTION.matcher(entry);
    assertThat(action.find()).as("the entry's retry form").isTrue();
    final Matcher csrf = CSRF_FIELD.matcher(entry);
    assertThat(csrf.find()).as("the retry form's CSRF field").isTrue();

    mockMvc
        .perform(post(action.group(1)).session(operator).param("_csrf", csrf.group(1)))
        .andExpect(redirectedUrl(EVENT_LOG));

    assertThat(retries.publicationIds()).containsExactly(OneExhaustedDelivery.PUBLICATION_ID);
  }

  /** The markup of every entry of the log, one string per entry. */
  private static List<String> entries(final String page) {
    final String[] parts = page.split("data-test=\"event-log-item\"");
    return Arrays.asList(parts).subList(1, parts.length);
  }

  /** The text of every submit button in the markup. */
  private static List<String> submitButtons(final String markup) {
    return SUBMIT_BUTTON
        .matcher(markup)
        .results()
        .map(button -> HtmlUtils.htmlUnescape(button.group(1)).strip())
        .toList();
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

  @TestConfiguration
  static class LogChosenByTheTest {

    @Bean
    @Primary
    GetEventPublicationsInputPort oneExhaustedDelivery() {
      return new OneExhaustedDelivery();
    }

    @Bean
    @Primary
    RecordedRetries recordedRetries() {
      return new RecordedRetries();
    }
  }

  /** A retry answers as one whose listener still fails: nothing completes, the call is recorded. */
  static final class RecordedRetries implements ReplayFailedPublicationInputPort {

    private final List<UUID> publicationIds = new CopyOnWriteArrayList<>();

    @Override
    public ReplayFailedPublicationResult execute(final ReplayFailedPublicationCommand command) {
      publicationIds.add(command.publicationId());
      return new ReplayFailedPublicationResult();
    }

    List<UUID> publicationIds() {
      return List.copyOf(publicationIds);
    }
  }

  /** The event log holds one delivery whose listener used up its five attempts. */
  static final class OneExhaustedDelivery implements GetEventPublicationsInputPort {

    static final UUID PUBLICATION_ID = UUID.randomUUID();

    @Override
    public GetEventPublicationsResult execute(final GetEventPublicationsQuery query) {
      final EventPublicationSummary exhausted =
          new EventPublicationSummary(
              PUBLICATION_ID,
              "dev.domaincentric.sample.ecommerce.product.events.ProductCreatedEvent",
              "Status: FAILED, attempts: 5\n{\"eventId\":\"" + UUID.randomUUID() + "\"}",
              "dev.domaincentric.sample.ecommerce.inventory.Listener.on(ProductCreatedEvent)",
              Instant.now(),
              null,
              true);
      return new GetEventPublicationsResult(List.of(exhausted), 1, 0, 1);
    }
  }
}
