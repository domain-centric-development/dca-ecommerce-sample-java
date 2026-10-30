---
id: the-order-confirmation-findings-are-closed
epic: guest-checkout
kind: task
status: delivered
delivered: 2026-10-30T04:19:24Z
delivered_by: gate
created: 2026-10-28T20:44:04Z
context: checkout
title: The order confirmation's open findings are closed
closes: [the-buyer-gets-an-order-confirmation#1]
depends_on: [the-buyer-gets-an-order-confirmation]
---

# The order confirmation's open findings are closed

The confirmation page's model no longer carries the name the glossary gives the mail: the controller and the page call
it by a step-side name, for example `confirmedCheckout`. The row's fix stands in its findings row; the delivering gate
sets it done.

A change no actor sees: no acceptance criterion, no test stage. It runs plan, build, tidy, judge and document, and
the existing suite, run whole, is its evidence; a test that existed before it stays as it is.
