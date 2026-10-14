package dev.domaincentric.sample.ecommerce.backoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.backoffice.RetryAFailedDeliveryIntegrationTest.LoneFailure;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.util.HtmlUtils;

/**
 * The event publication log as the logged-in operator gets it from the wired application: one entry
 * per event and listener with its state and dates, the summary, the breadcrumb and the refresh
 * link. An event whose listener fails until it is fixed gives the log an incomplete entry beside
 * the completed ones of the start-up.
 */
@SpringBootTest(
    classes = {
      EcommerceSampleApplication.class,
      RetryAFailedDeliveryIntegrationTest.Listeners.class
    })
@AutoConfigureMockMvc
@DisplayName("Backoffice event log page")
class EventLogPageIntegrationTest {

  private static final String LOGIN_PAGE = "/backoffice/login";
  private static final String EVENT_LOG = "/backoffice/events";

  private static final Pattern DATE_TIME =
      Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
  private static final Pattern CSRF_FIELD = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");
  private static final Pattern DETAIL_LABEL =
      Pattern.compile("<span class=\"event-log__detail-label\">([^<]*)</span>");
  private static final Pattern BREADCRUMB =
      Pattern.compile("data-test=\"breadcrumb\">(.*?)</div>", Pattern.DOTALL);
  private static final Pattern ELEMENT_TEXT = Pattern.compile(">([^<]+)<");
  private static final Pattern REFRESH_LINK =
      Pattern.compile(
          "<a[^>]*\\bhref=\"([^\"]*)\"[^>]*data-test=\"event-log-refresh\"[^>]*>([^<]*)<");

  /** How long a published event may take to appear in the log. */
  private static final long LISTING_TIMEOUT_MILLIS = 10_000;

  @Autowired private MockMvc mockMvc;
  @Autowired private ApplicationEventPublisher publisher;
  @Autowired private PlatformTransactionManager transactions;

  private MockHttpSession operator;

  @BeforeEach
  void logInAsTheOperator() throws Exception {
    operator = new MockHttpSession();
    final String form = page(LOGIN_PAGE);
    mockMvc
        .perform(
            post(LOGIN_PAGE)
                .session(operator)
                .param("_csrf", text(CSRF_FIELD, form))
                .param("username", "admin")
                .param("password", "admin"))
        .andExpect(redirectedUrl(EVENT_LOG));
  }

  @Test
  @DisplayName("Entry shows listener state and dates")
  void everyEntryShowsItsBadgeListenerAndDates() throws Exception {
    final List<String> entries = entries(pageListing(publishAnEventItsListenerFails()));

    assertThat(entries.stream().map(entry -> text(dataTest("event-status"), entry)))
        .as("the log holds a completed and an incomplete entry")
        .contains("Completed", "Incomplete");
    for (final String entry : entries) {
      final String badge = text(dataTest("event-status"), entry);
      assertThat(badge).isIn("Completed", "Incomplete");
      assertThat(text(dataTest("event-listener"), entry)).isNotBlank();
      assertThat(text(dataTest("event-publication-date"), entry)).matches(DATE_TIME);
      if (badge.equals("Completed")) {
        assertThat(detailLabels(entry)).containsExactly("Listener", "Published", "Completed");
        assertThat(text(dataTest("event-completion-date"), entry)).matches(DATE_TIME);
      } else {
        assertThat(detailLabels(entry)).containsExactly("Listener", "Published");
        assertThat(entry).doesNotContain("data-test=\"event-completion-date\"");
      }
    }
  }

  @Test
  @DisplayName("Counts add up")
  void theSummaryCountsTheEntriesByTheirBadge() throws Exception {
    final String page = page(EVENT_LOG);
    final List<String> badges =
        entries(page).stream().map(entry -> text(dataTest("event-status"), entry)).toList();

    final int total = number(page, "event-log-total");
    final int completed = number(page, "event-log-completed");
    final int incomplete = number(page, "event-log-incomplete");

    assertThat(total).isEqualTo(badges.size());
    assertThat(completed).isEqualTo(badges.stream().filter("Completed"::equals).count());
    assertThat(incomplete).isEqualTo(badges.stream().filter("Incomplete"::equals).count());
    assertThat(completed + incomplete).isEqualTo(total);
  }

  @Test
  @DisplayName("Log page breadcrumb")
  void theBreadcrumbLeadsHomeFromTheEventLog() throws Exception {
    final String breadcrumb = text(BREADCRUMB, page(EVENT_LOG));

    final List<String> parts = new ArrayList<>();
    final Matcher texts = ELEMENT_TEXT.matcher(breadcrumb);
    while (texts.find()) {
      if (!texts.group(1).isBlank()) {
        parts.add(HtmlUtils.htmlUnescape(texts.group(1)).strip());
      }
    }
    assertThat(String.join(" ", parts)).isEqualTo("Home / Event Publication Log");
    assertThat(breadcrumb).containsPattern("<a[^>]*\\bhref=\"/\"[^>]*>Home</a>");
  }

  @Test
  @DisplayName("Refresh reloads the log")
  void refreshLoadsTheEventLogAgain() throws Exception {
    final Matcher refresh = REFRESH_LINK.matcher(page(EVENT_LOG));
    assertThat(refresh.find()).as("the Refresh link").isTrue();
    assertThat(refresh.group(2).strip()).isEqualTo("Refresh");

    final String reloaded = page(refresh.group(1));

    assertThat(refresh.group(1)).isEqualTo(EVENT_LOG);
    assertThat(reloaded).contains("data-test=\"event-log-summary\"");
    assertThat(entries(reloaded)).isNotEmpty();
  }

  /** Publishes an event whose one listener fails, and answers its id. */
  private UUID publishAnEventItsListenerFails() {
    final LoneFailure event = new LoneFailure(UUID.randomUUID(), Instant.now());
    new TransactionTemplate(transactions)
        .executeWithoutResult(status -> publisher.publishEvent(event));
    return event.eventId();
  }

  /** The log page once it lists an entry of the event, failing when it does not in time. */
  private String pageListing(final UUID eventId) throws Exception {
    final long deadline = System.currentTimeMillis() + LISTING_TIMEOUT_MILLIS;
    String page = page(EVENT_LOG);
    while (!page.contains(eventId.toString()) && System.currentTimeMillis() < deadline) {
      Thread.sleep(100);
      page = page(EVENT_LOG);
    }
    assertThat(page).as("the log lists the published event").contains(eventId.toString());
    return page;
  }

  /** The markup of every entry of the log, one string per entry. */
  private static List<String> entries(final String page) {
    final String[] parts = page.split("data-test=\"event-log-item\"");
    return Arrays.asList(parts).subList(1, parts.length);
  }

  private static List<String> detailLabels(final String entry) {
    final List<String> labels = new ArrayList<>();
    final Matcher matcher = DETAIL_LABEL.matcher(entry);
    while (matcher.find()) {
      labels.add(HtmlUtils.htmlUnescape(matcher.group(1)).strip());
    }
    return labels;
  }

  private static int number(final String page, final String dataTest) {
    return Integer.parseInt(text(dataTest(dataTest), page));
  }

  /** A page as the browser gets it within the operator's session. */
  private String page(final String path) throws Exception {
    return mockMvc
        .perform(get(URI.create(path)).session(operator))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  private static Pattern dataTest(final String dataTest) {
    return Pattern.compile(
        "<\\w+[^>]*\\bdata-test=\"" + Pattern.quote(dataTest) + "\"[^>]*>([^<]*)<");
  }

  private static String text(final Pattern pattern, final String html) {
    final Matcher matcher = pattern.matcher(html);
    assertThat(matcher.find()).as("%s in the rendered page", pattern).isTrue();
    return HtmlUtils.htmlUnescape(matcher.group(1)).strip();
  }
}
