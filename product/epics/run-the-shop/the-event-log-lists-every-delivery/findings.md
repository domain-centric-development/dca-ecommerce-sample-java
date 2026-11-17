---
story: the-event-log-lists-every-delivery
epic: run-the-shop
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (1 rows, 0 without a fitting value)
---

# Findings — the-event-log-lists-every-delivery

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | test | src/test-integration/java/dev/domaincentric/sample/ecommerce/backoffice/EventLogPageIntegrationTest.java:81 | minor | introduced |  |  | The "Completed" date clause is checked only inside `if (badge.equals("Completed"))`; nothing asserts that at least one entry is Completed, so the clause would pass vacuously on a log without one | Assert that the entries contain at least one "Completed" badge before the loop | done (cac3f4f) | test | checking | missing |
