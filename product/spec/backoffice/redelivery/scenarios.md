# backoffice — redelivery

### Rule: A failed delivery is retried automatically

#### passing-failure-heals-on-its-own
- Given the operator is logged in and a listener failed once on the delivery of an event and handles it again from then on
- When the operator opens `/backoffice/events` a few seconds later
- Then that event's entry for the listener shows the badge "Completed" and a "Completed" date, without the operator having done anything
- And its payload's first line reads "Status: COMPLETED, attempts: 2"
Test: dev.domaincentric.sample.ecommerce.e2e.AutomaticRedeliveryE2ETest#aDeliveryThatFailedOnceIsCompletedWithoutTheOperator

### Rule: After five attempts the shop gives up and leaves the entry for the operator

#### exhausted-delivery-stays-incomplete
- Given the operator is logged in and a listener fails on every delivery of an event
- When the operator opens `/backoffice/events` after the automatic retries have stopped
- Then that event's entry for the listener shows the badge "Incomplete" and no "Completed" date
- And its payload's first line reads "Status: FAILED, attempts: 5"
Test: dev.domaincentric.sample.ecommerce.backoffice.AutomaticRedeliveryIntegrationTest#aDeliveryFailingEveryTimeStaysFailedAfterFiveAttempts

#### exhausted-delivery-counts-as-incomplete
- Given the operator is logged in and one delivery has used up its five attempts
- When they open `/backoffice/events`
- Then the entry is counted under "Incomplete" and not under "Completed"
Test: dev.domaincentric.sample.ecommerce.backoffice.AutomaticRedeliveryIntegrationTest#anExhaustedDeliveryIsCountedUnderIncomplete

### Rule: A failed entry offers a retry

#### failed-entry-offers-a-retry
- Given the operator is logged in and a delivery has used up its five attempts ("Status: FAILED, attempts: 5")
- When they open `/backoffice/events`
- Then that entry shows the button "Retry failed listener"
Test: dev.domaincentric.sample.ecommerce.backoffice.RetryButtonPageIntegrationTest#anEntryThatUsedUpItsAttemptsShowsTheRetryButton

### Rule: A retry delivers the event once more to that one listener

#### retry-completes-a-failed-delivery
- Given the operator is logged in, a delivery has used up its five attempts and its listener handles the event again
- When the operator presses "Retry failed listener" on that entry
- Then `/backoffice/events` opens again, and once the delivery has run the entry shows the badge "Completed" and a "Completed" date
- And "Completed" has grown by one and "Incomplete" has shrunk by one
Test: dev.domaincentric.sample.ecommerce.e2e.RetryAFailedDeliveryE2ETest#aRetriedDeliveryWhoseCauseIsFixedIsCompleted

#### retry-delivers-the-original-event
- Given the operator retried a failed delivery and it completed
- When they expand that entry's "Event Payload"
- Then the event's content is the same as before the retry, with the same eventId
Test: dev.domaincentric.sample.ecommerce.backoffice.RetryAFailedDeliveryIntegrationTest#aRetriedDeliveryCarriesTheEventItCarriedBefore

#### retry-leaves-other-listeners-alone
- Given an event with two listeners, one delivery "Completed" and the other failed for good, and the operator logged in
- When the operator presses "Retry failed listener" on the failed entry
- Then the other listener's entry keeps its "Completed" date unchanged
Test: dev.domaincentric.sample.ecommerce.backoffice.RetryAFailedDeliveryIntegrationTest#aRetryKeepsTheCompletionOfTheListenerThatHandledTheEvent

#### failing-retry-leaves-the-entry-failed
- Given the operator is logged in and a delivery has used up its five attempts and its listener still fails
- When the operator presses "Retry failed listener" on that entry
- Then `/backoffice/events` opens again and the entry still shows the badge "Incomplete"
Test: dev.domaincentric.sample.ecommerce.backoffice.RetryAFailedDeliveryIntegrationTest#aRetryWhoseListenerStillFailsLeavesTheEntryIncomplete, dev.domaincentric.sample.ecommerce.backoffice.RetryButtonPageIntegrationTest#aRetryOpensTheEventLogAgain
