# ADR-046: A Step Page Takes the Checkout Over

**Date**: 2026-10-21 · **Status**: Accepted

## Context

A guest who opens the login from the checkout's buyer step had a cart and an open checkout session, both under the
guest's identity. After the login the identity is the account's: the buyer step found no session for it and sent
the person back to the cart with "No active checkout session found", although their order was still open.

A checkout session belongs to the cart it was started from. Every Checkout use case checks ownership through
`CheckoutSession.customerId` (`CheckoutSessionRepository.findByIdForCustomer`), so a session whose cart has
changed hands has to change hands too, or none of them finds it. The step pages so far only read on GET; the
session changes on POST.

## Decision

**The buyer step's GET resumes the open checkout of the customer's cart, and that is the one step-page GET that
changes a session.**

- At login, Cart's `RecoverCart` keeps the guest cart itself — same id, same positions — as the account's when the
  account's cart holds nothing (`ShoppingCart.takeOverBy`); an empty account cart is deleted. No event is raised:
  nothing in the cart changed.
- When the buyer step finds no active session for the customer, `ResumeCheckout` (feature `checkoutcompletion`)
  asks Cart, through `CartDataPort` and Cart's Open Host Service, for the customer's active cart, finds the active
  session of that cart, and lets the customer take it over (`CheckoutSession.takeOverBy`): same id, step and line
  items. Cart never calls Checkout.
- It never starts a session. Without an open session of the customer's cart the page still sends the person to the
  cart with "No active checkout session found".
- The use case sits in `checkoutcompletion`, not in `session`: the step pages are that feature's adapters, and an
  adapter drives the commands of one feature (DCA-USE-018).

## Consequences

- Logging in from the buyer step returns to the buyer step with the guest's order, as registering from it already
  did. A reload changes nothing further: the session is the customer's by then.
- A GET that writes is an exception readers will not expect; this record and the controller's comment name it.
  Taking over is idempotent, so a repeated or prefetched GET is harmless.
- DCA-HEX-014 (a GET handler drives no command) would refuse it. `dca-archunit.properties` ignores exactly this
  violation — `BuyerInfoPageController.showBuyerInfoForm` driving `ResumeCheckoutInputPort` — and nothing else, so
  every other GET handler stays held.
- The merge path — the account's cart holds items too — is not covered: the merge page asks first and the guest's
  session stays the guest's. Following a "guest cart X was merged into cart Y" record from Cart is the next step.
- The in-memory cart store drops the former owner's active-cart claim when a cart changes hands; the relational
  stores key the claim on the row and need nothing.
