package dev.domaincentric.sample.ecommerce.e2e.pages;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Page object for the backoffice event publication log page.
 *
 * <p>Provides methods to inspect published domain events and their completion status.
 */
public class BackofficeEventsPage extends BasePage {

  private static final String URL_PATTERN = "/backoffice/events**";
  private static final String EVENT_LOG = "event-log";
  private static final String EVENT_LOG_SUMMARY = "event-log-summary";
  private static final String EVENT_LOG_TOTAL = "event-log-total";
  private static final String EVENT_LOG_COMPLETED = "event-log-completed";
  private static final String EVENT_LOG_INCOMPLETE = "event-log-incomplete";
  private static final String EVENT_LOG_ITEM = "event-log-item";
  private static final String EVENT_TYPE = "event-type";
  private static final String EVENT_STATUS = "event-status";
  private static final String EVENT_LISTENER = "event-listener";
  private static final String EVENT_COMPLETION_DATE = "event-completion-date";
  private static final String EVENT_PAYLOAD = "event-payload";

  /**
   * Creates a new BackofficeEventsPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public BackofficeEventsPage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Navigates directly to the backoffice events page and creates a page object.
   *
   * @param page the Playwright page instance
   * @return a new BackofficeEventsPage
   */
  public static BackofficeEventsPage navigateTo(Page page) {
    page.navigate(BASE_URL + "/backoffice/events");
    return new BackofficeEventsPage(page);
  }

  /**
   * Checks if the event log container is present on the page.
   *
   * @return true if the event log is displayed
   */
  public boolean isOnPage() {
    return exists(EVENT_LOG);
  }

  /**
   * Gets the path the browser shows for this page.
   *
   * @return the current path, without the base URL
   */
  public String shownPath() {
    return getCurrentPath();
  }

  /**
   * Gets the page heading.
   *
   * @return the text of the page's first-level heading
   */
  public String heading() {
    return page.locator("h1").textContent().strip();
  }

  /**
   * Gets the labels of the summary's counters, in the order the page shows them.
   *
   * @return one label per counter
   */
  public List<String> counterLabels() {
    waitFor(EVENT_LOG_SUMMARY);
    return page
        .locator("[data-test='" + EVENT_LOG_SUMMARY + "'] .event-log__stat-label")
        .allTextContents()
        .stream()
        .map(String::strip)
        .toList();
  }

  /**
   * Checks if the summary section is present.
   *
   * @return true if the summary section is displayed
   */
  public boolean hasSummary() {
    return exists(EVENT_LOG_SUMMARY);
  }

  /**
   * Gets the total number of events as shown in the summary.
   *
   * @return the total event count
   */
  public int getTotalEvents() {
    waitFor(EVENT_LOG_TOTAL);
    return Integer.parseInt(
        page.locator("[data-test='" + EVENT_LOG_TOTAL + "']").textContent().trim());
  }

  /**
   * Gets the number of completed events as shown in the summary.
   *
   * @return the completed event count
   */
  public int getCompletedCount() {
    waitFor(EVENT_LOG_COMPLETED);
    return Integer.parseInt(
        page.locator("[data-test='" + EVENT_LOG_COMPLETED + "']").textContent().trim());
  }

  /**
   * Gets the number of incomplete events as shown in the summary.
   *
   * @return the incomplete event count
   */
  public int getIncompleteCount() {
    waitFor(EVENT_LOG_INCOMPLETE);
    return Integer.parseInt(
        page.locator("[data-test='" + EVENT_LOG_INCOMPLETE + "']").textContent().trim());
  }

  /**
   * Gets the number of event items rendered in the list.
   *
   * @return the count of event-log-item elements
   */
  public int getEventCount() {
    return page.locator("[data-test='" + EVENT_LOG_ITEM + "']").count();
  }

  /**
   * Gets the text of all event-type labels in the list.
   *
   * @return list of event type strings, one per event item
   */
  public List<String> getEventTypes() {
    return page.locator("[data-test='" + EVENT_TYPE + "']").allTextContents();
  }

  /**
   * Gets the event type text of the first event in the list.
   *
   * @return the first event type string
   */
  public String getFirstEventType() {
    waitFor(EVENT_TYPE);
    return page.locator("[data-test='" + EVENT_TYPE + "']").first().textContent().trim();
  }

  /** Clicks the payload details element for the first event to expand its content. */
  public void expandFirstEventPayload() {
    waitFor(EVENT_PAYLOAD);
    page.locator("[data-test='" + EVENT_PAYLOAD + "']").first().click();
  }

  /**
   * Gets the text content of the payload area of the first event.
   *
   * @return the payload text
   */
  public String getFirstEventPayloadText() {
    waitFor(EVENT_PAYLOAD);
    return page.locator("[data-test='" + EVENT_PAYLOAD + "']").first().textContent().trim();
  }

  /**
   * Checks if the content of the first event's payload is shown, not only its toggle.
   *
   * @return true if the payload content is visible
   */
  public boolean isFirstEventPayloadContentVisible() {
    return firstEventPayloadContent().isVisible();
  }

  /**
   * Gets the content of the first event's payload, without its toggle.
   *
   * @return the payload content text
   */
  public String getFirstEventPayloadContent() {
    return firstEventPayloadContent().textContent().trim();
  }

  /**
   * Opens the page again until the entry carrying the given text shows the badge "Completed", or
   * the time is up.
   *
   * @param text a text the entry's payload carries, such as the event's id
   * @param within how long the entry may take to complete
   * @return the page as last opened
   */
  public BackofficeEventsPage reloadUntilEntryCompleted(String text, Duration within) {
    Instant deadline = Instant.now().plus(within);
    while (!"Completed".equals(statusOfEntryCarrying(text)) && Instant.now().isBefore(deadline)) {
      page.waitForTimeout(250);
      page.reload();
      waitFor(EVENT_LOG);
    }
    return this;
  }

  /**
   * Opens the page again until the payload of the entry carrying the given text starts with the
   * given line, or the time is up.
   *
   * @param text a text the entry's payload carries, such as the event's id
   * @param firstLine the payload's first line to wait for
   * @param within how long the entry may take to get there
   * @return the page as last opened
   */
  public BackofficeEventsPage reloadUntilPayloadFirstLine(
      String text, String firstLine, Duration within) {
    Instant deadline = Instant.now().plus(within);
    while (!firstLine.equals(payloadFirstLineOrEmpty(text)) && Instant.now().isBefore(deadline)) {
      page.waitForTimeout(250);
      page.reload();
      waitFor(EVENT_LOG);
    }
    return this;
  }

  /**
   * Presses "Retry failed listener" on the entry carrying the given text.
   *
   * @param text a text the entry's payload carries, such as the event's id
   * @return the page the shop answers with
   */
  public BackofficeEventsPage retryEntryCarrying(String text) {
    Locator retry =
        entryCarrying(text)
            .first()
            .getByRole(
                AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Retry failed listener"));
    assertThat(retry).isVisible();
    retry.click();
    return new BackofficeEventsPage(page);
  }

  /**
   * Checks if the entry carrying the given text offers "Retry failed listener".
   *
   * @param text a text the entry's payload carries, such as the event's id
   * @return true if the entry shows the retry button
   */
  public boolean entryCarryingOffersRetry(String text) {
    Locator entry = entryCarrying(text);
    assertThat(entry).hasCount(1);
    return entry
            .getByRole(
                AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Retry failed listener"))
            .count()
        > 0;
  }

  /**
   * Gets the status badge of the entry carrying the given text.
   *
   * @param text a text the entry's payload carries, such as the event's id
   * @return the badge text, or empty when the page lists no such entry
   */
  public String statusOfEntryCarrying(String text) {
    Locator entry = entryCarrying(text);
    if (entry.count() == 0) {
      return "";
    }
    return entry.first().locator("[data-test='" + EVENT_STATUS + "']").textContent().trim();
  }

  /**
   * Checks if the entry carrying the given text shows a "Completed" date.
   *
   * @param text a text the entry's payload carries, such as the event's id
   * @return true if the entry shows its completion date
   */
  public boolean entryCarryingShowsCompletionDate(String text) {
    return entryCarrying(text)
            .first()
            .locator("[data-test='" + EVENT_COMPLETION_DATE + "']")
            .count()
        > 0;
  }

  /**
   * Gets the first line of the payload of the entry carrying the given text.
   *
   * @param text a text the entry's payload carries, such as the event's id
   * @return the payload's first line
   */
  public String payloadFirstLineOfEntryCarrying(String text) {
    String content =
        entryCarrying(text).first().locator(".event-log__payload-content").textContent().strip();
    return content.split("\n", 2)[0];
  }

  /**
   * Opens the page again until the entry carrying the given text for the given listener shows the
   * badge "Completed", or the time is up.
   *
   * @param text a text the entry's payload carries, such as the event's id
   * @param listener a text the entry shows under "Listener"
   * @param within how long the entry may take to complete
   * @return the page as last opened
   */
  public BackofficeEventsPage reloadUntilEntryCompleted(
      String text, String listener, Duration within) {
    Instant deadline = Instant.now().plus(within);
    while (!"Completed".equals(statusOfEntry(text, listener)) && Instant.now().isBefore(deadline)) {
      page.waitForTimeout(250);
      page.reload();
      waitFor(EVENT_LOG);
    }
    return this;
  }

  /**
   * Expands "Event Payload" of the entry carrying the given text for the given listener.
   *
   * @param text a text the entry's payload carries, such as the event's id
   * @param listener a text the entry shows under "Listener"
   */
  public void expandPayloadOfEntry(String text, String listener) {
    Locator entry = entryOf(text, listener);
    assertThat(entry).hasCount(1);
    entry.locator("[data-test='" + EVENT_PAYLOAD + "'] summary").click();
    assertThat(entry.locator(".event-log__payload-content")).isVisible();
  }

  /**
   * Gets the first line of the shown payload of the entry carrying the given text for the given
   * listener.
   *
   * @param text a text the entry's payload carries, such as the event's id
   * @param listener a text the entry shows under "Listener"
   * @return the payload's first line
   */
  public String payloadFirstLineOfEntry(String text, String listener) {
    String content =
        entryOf(text, listener).locator(".event-log__payload-content").innerText().strip();
    return content.split("\n", 2)[0];
  }

  private String statusOfEntry(String text, String listener) {
    Locator entry = entryOf(text, listener);
    if (entry.count() == 0) {
      return "";
    }
    return entry.first().locator("[data-test='" + EVENT_STATUS + "']").textContent().trim();
  }

  private Locator entryOf(String text, String listener) {
    return entryCarrying(text)
        .filter(
            new Locator.FilterOptions()
                .setHas(
                    page.locator("[data-test='" + EVENT_LISTENER + "']")
                        .filter(new Locator.FilterOptions().setHasText(listener))));
  }

  private String payloadFirstLineOrEmpty(String text) {
    if (entryCarrying(text).count() == 0) {
      return "";
    }
    return payloadFirstLineOfEntryCarrying(text);
  }

  private Locator entryCarrying(String text) {
    return page.locator("[data-test='" + EVENT_LOG_ITEM + "']")
        .filter(new Locator.FilterOptions().setHasText(text));
  }

  private Locator firstEventPayloadContent() {
    waitFor(EVENT_PAYLOAD);
    return page.locator("[data-test='" + EVENT_PAYLOAD + "']")
        .first()
        .locator(".event-log__payload-content");
  }
}
