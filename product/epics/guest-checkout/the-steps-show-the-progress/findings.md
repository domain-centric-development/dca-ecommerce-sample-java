---
story: the-steps-show-the-progress
epic: guest-checkout
classes: backfilled 2026-11-17 by request 20261117T213907070Z-classify-findings (1 rows, 0 without a fitting value)
---

# Findings — the-steps-show-the-progress

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | Origin | Outcome | In plain words | Defect | Fix | Status | Target | Type | Qualifier |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | test | src/test-integration/java/dev/domaincentric/sample/ecommerce/checkout/StepsShowTheProgressIntegrationTest.java:178 | minor | introduced |  |  | Both clauses of delivery-sent-before-the-buyer-step would still hold with the domain guards at `CheckoutSession.java:224-225` removed: the access rule (`CheckoutCartSnapshot.java:255`) sends `/checkout/payment` and `/checkout/review` back to the buyer step either way. Only the refusal's translation is proved, not that the delivery was left unstored. | Sharpen the scenario so it names an outcome you can observe, e.g. after the buyer step the delivery form has no Street Address filled in, then assert that outcome | open | story | traceability | incorrect |
