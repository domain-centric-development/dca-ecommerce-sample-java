---
id: a-guest-cart-not-owned-is-a-named-failure
epic: account
kind: task
status: approved
created: 2026-10-29T15:12:47Z
context: cart
title: A guest cart not owned is a named failure
closes: []
depends_on: []
---

# A guest cart not owned is a named failure

A merge or a take-over that names a guest other than the guest before the login is refused with a use-case failure
`GuestCartNotOwnedException` instead of being passed over in silence — as the .NET shop does. The incoming adapter
answers it as today: the cart page, no notice, no cart changed. The cart glossary gets the entry
`### Guest cart not owned (GuestCartNotOwned)` under `## Failures`, after `CartNotFound`, where the .NET glossary
has it.

A change no actor sees: no acceptance criterion, no test stage. It runs plan, build, tidy, judge and document, and
the existing suite, run whole, is its evidence; a test that existed before it stays as it is.
The new refusal is proven by a unit test of the use case.
