---
story: the-order-changed-since-the-review-has-the-shared-name
epic: guest-checkout
---

# Findings — the-order-changed-since-the-review-has-the-shared-name

The judge's confirmed defects that did not block the story, kept for a later story or a person's look. The pipeline appends; a person sets `Status` to `done` or `wont-fix`.

| # | Perspective | File:line | Severity | In plain words | Defect | Fix | Status |
|---|---|---|---|---|---|---|---|
| 1 | ddd | src/main/java/dev/domaincentric/sample/ecommerce/checkout/domain/model/OrderChangedSinceReviewException.java:26 | minor | The log line still says "checkout" where the failure is now called "order". | deferred by the plan: `domain-term:` the log message "Checkout … has changed since its review" predates the story and keeps the old word (plan `## Open assumptions`); the buyer never reads it | In a later story, word the message "Order in checkout {id} has changed since its review" | open |
