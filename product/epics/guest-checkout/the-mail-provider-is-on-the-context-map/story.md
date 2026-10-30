---
id: the-mail-provider-is-on-the-context-map
epic: guest-checkout
kind: task
status: delivered
delivered: 2026-10-30T03:52:18Z
delivered_by: gate
created: 2026-10-28T20:44:04Z
context: checkout
title: The mail provider is on the context map
closes: []
depends_on: [the-buyer-gets-an-order-confirmation]
---

# The mail provider is on the context map

The checkout context declares the mail provider as an external upstream — outbound, REST, translated by an
anti-corruption layer — as the .NET shop does, and the generated and the designed context map show it.

A change no actor sees: no acceptance criterion, no test stage. It runs plan, build, tidy, judge and document, and
the existing suite, run whole, is its evidence; a test that existed before it stays as it is.
