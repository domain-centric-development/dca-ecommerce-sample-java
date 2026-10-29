---
id: the-order-changed-since-the-review-has-the-shared-name
epic: guest-checkout
kind: task
status: approved
created: 2026-10-29T15:12:47Z
context: checkout
title: The order changed since the review has the shared name
closes: []
depends_on: []
---

# The order changed since the review has the shared name

`CheckoutChangedSinceReviewException` becomes `OrderChangedSinceReviewException` — the name both shops agreed on
(2026-10-29): "order" is the word the buyer reads ("Your order has changed since you reviewed it."). It stays one type
that names the product when only a price differs. The checkout glossary's entry is headed
`### OrderChangedSinceReview (OrderChangedSinceReviewException)`, in the place it has now; the messages the buyer sees
do not change.

A change no actor sees: no acceptance criterion, no test stage. It runs plan, build, tidy, judge and document, and
the existing suite, run whole, is its evidence; a test that existed before it stays as it is.
