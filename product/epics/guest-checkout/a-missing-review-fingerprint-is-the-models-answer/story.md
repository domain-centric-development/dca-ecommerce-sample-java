---
id: a-missing-review-fingerprint-is-the-models-answer
epic: guest-checkout
kind: task
status: draft
created: 2026-11-01T12:58:58Z
context: checkout
title: A missing review fingerprint is the model's answer, not the page's
---

# A missing review fingerprint is the model's answer, not the page's

"Place Order" carries the fingerprint of the review it was sent from; one that matches no review places nothing
and leads back to the review. A form without a fingerprint answers the same today, but the page decides it: the
page adapter turns the missing field into an empty fingerprint, and the review fingerprint value refuses a missing
one as a programming error. After: the page adapter passes the missing fingerprint on as missing, and the review
fingerprint value says itself that a missing fingerprint matches no review. Same in both shops.

A change no actor sees: the review page always sends its fingerprint, and the answer to a form without one stays
what it is — no acceptance criterion, no test stage. The unit test of the review fingerprint names the missing one.

## Notes

- WP-170 decision 2 (the person, 2026-11-01): a value from input refuses or reads its absence itself; the adapter
  passes absence on, no default that turns it into a value. Today the Java page sets a default of an empty text,
  the .NET page takes an optional text and hands it on.
