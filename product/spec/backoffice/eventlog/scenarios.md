# backoffice — eventlog

### Rule: The shop's start-up already shows in the log

#### log-lists-the-events-published-at-start-up
- Given the shop has started and seeded the sample catalogue, and the operator is logged in
- When they open `/backoffice/events`
- Then the log lists at least one entry
- And every entry shows a type label that names the event
Test: dev.domaincentric.sample.ecommerce.e2e.EventLogE2ETest#theLogListsTheEventsThatStartUpPublished

### Rule: One entry per event and listener, with its state and dates

#### entry-shows-listener-state-and-dates
- Given the operator is logged in and the log lists the start-up events
- When they open `/backoffice/events`
- Then every entry shows a badge "Completed" or "Incomplete", a "Listener" and a "Published" date in the form yyyy-MM-dd HH:mm:ss
- And every entry with the badge "Completed" also shows a "Completed" date in the same form
Test: dev.domaincentric.sample.ecommerce.backoffice.EventLogPageIntegrationTest#everyEntryShowsItsBadgeListenerAndDates

#### newest-entry-comes-first
- Given the operator is logged in and the log lists the start-up events
- When they open `/backoffice/events`
- Then the entries are ordered by their "Published" date, the newest first
Test: dev.domaincentric.sample.ecommerce.backoffice.EventPublicationOrderIntegrationTest#entriesAreListedNewestPublicationFirst

### Rule: The summary counts the entries

#### counts-add-up
- Given the operator is logged in and the log lists the start-up events
- When they open `/backoffice/events`
- Then "Total Events" equals the number of entries listed
- And "Completed" is the number of entries with the badge "Completed", "Incomplete" the number with the badge "Incomplete", and both add up to "Total Events"
Test: dev.domaincentric.sample.ecommerce.backoffice.EventLogPageIntegrationTest#theSummaryCountsTheEntriesByTheirBadge

### Rule: The page is recognisable and can be reloaded

#### log-page-breadcrumb
- Given the operator is logged in
- When they open `/backoffice/events`
- Then the breadcrumb reads "Home / Event Publication Log" and "Home" links to `/`
Test: dev.domaincentric.sample.ecommerce.backoffice.EventLogPageIntegrationTest#theBreadcrumbLeadsHomeFromTheEventLog

#### refresh-reloads-the-log
- Given the operator is logged in on `/backoffice/events`
- When they press "Refresh"
- Then `/backoffice/events` is loaded again with its summary and entries
Test: dev.domaincentric.sample.ecommerce.backoffice.EventLogPageIntegrationTest#refreshLoadsTheEventLogAgain

### Rule: Every entry has a collapsible payload

#### payload-is-collapsed-at-first
- Given the operator is logged in and the log lists at least one entry
- When they open `/backoffice/events`
- Then every entry offers "Event Payload", collapsed
Test: dev.domaincentric.sample.ecommerce.backoffice.EventPayloadPageIntegrationTest#everyEntryOffersItsPayloadCollapsed

#### payload-can-be-expanded
- Given the operator is logged in and the log lists at least one entry
- When they expand "Event Payload" of the first entry
- Then the payload is shown and is not empty
Test: dev.domaincentric.sample.ecommerce.e2e.EventPayloadE2ETest#theFirstEntrysPayloadShowsWhatTheEventCarried

### Rule: The payload starts with a line "Status: <status>, attempts: <number>", followed by the event's content

#### payload-starts-with-status-and-attempts
- Given the operator is logged in and the log lists a "Completed" entry for a product created at start-up, delivered at the first attempt
- When they expand that entry's "Event Payload"
- Then its first line reads "Status: COMPLETED, attempts: 1"
- And the lines after it hold the event's content: eventId, occurredOn, productId, amount, currency and initialStock
Test: dev.domaincentric.sample.ecommerce.backoffice.EventPayloadIntegrationTest#aDeliveredProductCreationShowsItsStatusAttemptsAndContent

### Rule: An entry's attempts are the tries of its own listener, the successful one included

#### each-listener-counts-its-own-attempts
- Given the operator is logged in and an event with two listeners, the first of which failed twice and the second
  once before each handled it
- When they expand the "Event Payload" of the event's two entries
- Then the first listener's entry reads "Status: COMPLETED, attempts: 3" on its first line
- And the second listener's entry reads "Status: COMPLETED, attempts: 2"
Test: dev.domaincentric.sample.ecommerce.e2e.AttemptsPerListenerE2ETest#theEntriesOfOneEventCountTheTriesOfTheirOwnListener

#### a-failing-listener-leaves-the-others-count-alone
- Given the operator is logged in and an event with two listeners, the first of which fails on every delivery and
  the second handled it at the first try
- When they expand the "Event Payload" of the event's two entries after the automatic retries have stopped
- Then the first listener's entry reads "Status: FAILED, attempts: 5" on its first line
- And the second listener's entry reads "Status: COMPLETED, attempts: 1"
Test: dev.domaincentric.sample.ecommerce.backoffice.AttemptsPerListenerIntegrationTest#aListenerFailingEveryTimeLeavesTheCountOfTheOneThatHandledTheEventAtOne
