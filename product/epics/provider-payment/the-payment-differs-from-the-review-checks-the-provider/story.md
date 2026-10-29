---
id: the-payment-differs-from-the-review-checks-the-provider
epic: provider-payment
kind: task
status: draft
created: 2026-10-29T15:12:47Z
context: checkout
title: The payment differs from the review and checks the provider
closes: []
depends_on: []
---

# The payment differs from the review and checks the provider

`AuthorizedAmountDiffersFromReviewException` becomes `PaymentDiffersFromReviewException`, and the check behind it
compares the provider too: an order is confirmed only when its payment was authorized by the provider the review
showed and for exactly the total it showed — as the .NET shop does. No actor can reach the case from outside: the
payment is requested from the selected provider. The checkout glossary's entry is headed
`### PaymentDiffersFromReview (PaymentDiffersFromReviewException)`, in the place it has now, and the entry
`Authorized amount` names it.

A change no actor sees: no acceptance criterion, no test stage. It runs plan, build, tidy, judge and document, and
the existing suite, run whole, is its evidence; a test that existed before it stays as it is.
