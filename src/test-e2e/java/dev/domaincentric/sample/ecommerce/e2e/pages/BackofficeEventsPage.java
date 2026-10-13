package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
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
  private static final String EVENT_LOG_LIST = "event-log-list";
  private static final String EVENT_LOG_ITEM = "event-log-item";
  private static final String EVENT_TYPE = "event-type";
  private static final String EVENT_STATUS = "event-status";
  private static final String EVENT_LISTENER = "event-listener";
  private static final String EVENT_PUBLICATION_DATE = "event-publication-date";
  private static final String EVENT_COMPLETION_DATE = "event-completion-date";
  private static final String EVENT_PAYLOAD = "event-payload";
  private static final String EVENT_LOG_REFRESH = "event-log-refresh";
  private static final String EVENT_LOG_LOGOUT = "event-log-logout";

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
   * Checks if the event list container is present.
   *
   * @return true if the event list is displayed
   */
  public boolean hasEvents() {
    return exists(EVENT_LOG_LIST);
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

  /**
   * Gets the status badge text of the first event in the list.
   *
   * @return the first event status string
   */
  public String getFirstEventStatus() {
    waitFor(EVENT_STATUS);
    return page.locator("[data-test='" + EVENT_STATUS + "']").first().textContent().trim();
  }

  /**
   * Clicks the refresh button and returns a fresh page object.
   *
   * @return a new BackofficeEventsPage after the refresh
   */
  public BackofficeEventsPage clickRefresh() {
    click(EVENT_LOG_REFRESH);
    return new BackofficeEventsPage(page);
  }

  /**
   * Clicks the logout button and returns the backoffice login page.
   *
   * @return the BackofficeLoginPage after logout
   */
  public BackofficeLoginPage clickLogout() {
    click(EVENT_LOG_LOGOUT);
    return new BackofficeLoginPage(page);
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
