---
story: only-a-failed-delivery-offers-a-retry
epic: run-the-shop
---

# Findings — only-a-failed-delivery-offers-a-retry

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | hexagonal | src/main/java/dev/domaincentric/sample/ecommerce/backoffice/adapter/outgoing/persistence/JdbcEventPublicationLogStore.java:34 | minor | The rule for when a delivery has used up its attempts is written down twice, so the log and the automatic retry can drift apart. | The store binds `dca.events.retry.max-attempts:5` and decides `FAILED && attempts >= maxAttempts` (:56) beside `IntegrationEventRecovery.java:28,51-52`, which owns the same key, default and the negated rule; the plan placed it there, so no plan deviation. | Give the exhaustion rule and its threshold one home (one properties record or policy both readers use). | open |
| 2 | clean-code | src/main/java/dev/domaincentric/sample/ecommerce/backoffice/application/geteventpublications/EventPublicationSummary.java:21 | minor | The same flag is explained three different ways along its path to the page. | `failed` is described as "used up its attempts — the one an operator can retry" here, "and no automatic retry is left" at `EventPublicationLogStore.java:34`, "and offers a retry" at `EventPublicationPageViewModel.java:79`. | Use one sentence, the glossary's definition of Failed, in all three `@param` tags. | open |
