---
id: only-a-failed-delivery-offers-a-retry-accept-1
story: only-a-failed-delivery-offers-a-retry
stage: document
kind: acceptance
asked: 2026-10-15T14:02:59Z
digest: c6057456d64b843808e4ee6247e7add1558f6473eb05d743ffa472a8f059496d
---

# Accept only-a-failed-delivery-offers-a-retry?

## Question
Every gate passed. Look at what the story delivers before it counts as delivered:

- a-delivery-under-way-offers-no-retry: Given the operator is logged in and an event's delivery to a listener is still under way and has not failed; When they open `/backoffice/events`; Then that entry shows the badge "Incomplete"; And it shows no button "Retry failed listener" — `dev.domaincentric.sample.ecommerce.e2e.OnlyAFailedDeliveryOffersARetryE2ETest#aDeliveryStillUnderWayOffersNoRetry`

Start the application with `./gradlew bootRun`.

## Options
- accepted: the story is delivered.
- a correction: what should be different, written into the story (criteria and an `answered:` line naming this record); the story runs again from plan.

## Answer
answer: accepted
by: Christoph Bloemer
at: 2026-10-15T15:03:00Z
rationale: Accepted by the person (2026-10-15), recorded by the managing session.
