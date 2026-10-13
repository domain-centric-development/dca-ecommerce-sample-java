---
story: failed-deliveries-are-retried-on-their
epic: run-the-shop
---

# Findings — failed-deliveries-are-retried-on-their

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Defect | Fix | Status |
|---|---|---|---|---|---|---|
| 1 | test | src/test-integration/java/dev/domaincentric/sample/ecommerce/backoffice/EventLogPageIntegrationTest.java:81 | minor | The page translation of an incomplete entry (badge "Incomplete", no `event-completion-date`) is asserted by no test with such an entry; exhausted-delivery-stays-incomplete proves it at the port only (`isCompleted` false, `completionDate` null) | Add an `else` branch asserting an "Incomplete" entry shows no `event-completion-date`, with an incomplete entry arranged | open |
