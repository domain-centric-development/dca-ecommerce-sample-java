---
story: retry-a-failed-delivery
epic: run-the-shop
---

# Findings — retry-a-failed-delivery

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Defect | Fix | Status |
|---|---|---|---|---|---|---|
| 1 | test | src/test-integration/java/dev/domaincentric/sample/ecommerce/backoffice/RetryAFailedDeliveryIntegrationTest.java:99 | minor | The break for retry-leaves-other-listeners-alone empties the replay, so the test fails at the await (line 93) and never reaches the completionDate assertion. A retry that published the event again would make new rows for HandlingListener and leave this one unchanged, so the test would still pass. | Also assert that HandlingListener got no new entry and no second delivery after the retry. Write a break that re-delivers to it. | open |
