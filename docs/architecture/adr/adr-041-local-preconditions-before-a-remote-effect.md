# ADR-041: Local Preconditions Come Before a Remote Effect, and an Unusable Effect Is Released

**Date**: 2026-09-17 · **Status**: Accepted

## Context

`SubmitPayment` read the session total, called `initiatePayment(...)` on the payment provider, and only then
opened the short transaction in which `session.submitPayment(...)` may still refuse the session — because it is
not active, because the buyer or delivery step is missing, because the session has moved past payment. The order
of those steps meant a payment intent could exist at the provider for a checkout this shop then rejected: the
shopper sees an error, and a reservation sits with the provider that nobody will ever use or release.

ADR-034 already keeps the remote call out of the transaction; it says nothing about what has to be true before
the call is made at all. The .NET sample carried the identical ordering.

## Decision

**Everything the aggregate can refuse is asked of the aggregate before the remote call.** `CheckoutSession`
gained `assertReadyForPayment()` (`AssertReadyForPayment()` in .NET) — the same preconditions `submitPayment`
enforces, plus a total that is not zero, and it changes nothing. `SubmitPayment` loads the session once, asks it,
and only then reaches the provider. The amount to charge comes from that same snapshot.

**An intent the session cannot accept afterwards is released.** The window does not close entirely: between the
check and the write the session can be confirmed, superseded or expire. When the transaction fails for any
reason, the use case calls `cancelPayment(...)` for the reference it just created and re-throws the original
failure. A provider that refuses the cancellation is logged at warn; the caller is told why their payment was
rejected, not that the clean-up of it failed as well.

**Not an idempotency key.** The alternative was to derive one from the session id so a retry reuses the intent
instead of creating a second. It needs a parameter on the port, and the redirect flow that is still to be built
(WP-16) is where that contract is decided. Compensation uses the port as it stands and makes `cancelPayment`
load-bearing, which it was not: two of the port's three operations had no caller.

## Consequences

- Positive: no payment intent exists for a checkout that cannot accept it, in either sample.
- Positive: `cancelPayment` has a caller, so the port's contract is exercised rather than merely declared.
- Positive: `SubmitPaymentUseCaseTest` in both samples proves the order and the compensation with a provider that
  records what it was asked to do; both tests fail when either half is removed.
- Negative: the session is loaded twice, once for the check and once inside the transaction. That is the price of
  keeping the remote call outside the transaction (ADR-034).
- Negative: compensation is best effort, and three things decide how much of it survives.
  - **It must catch every way out of the transaction.** A compensation attached to the ordinary failure type
    only — `RuntimeException` in Java, a narrowed exception filter in .NET — leaves the intent behind for every
    other failure. Both samples catch as widely as their runtime allows (`Throwable` / `Exception`); the clean-up
    swallows its own failures, so attempting it in a dying process costs nothing.
  - **It must not hang on what killed the operation.** Where the write is cancellable, the token that aborted it
    must not be the token the release call runs under, or the clean-up is cancelled before it reaches the
    provider — exactly the case it exists for. The .NET side gives the release its own deadline; the Java side
    has no ambient cancellation to inherit.
  - **If the process dies between initiation and cancellation**, the intent is orphaned at the provider and
    nothing in the shop will retry. A real system needs a reconciliation job; this one says so here rather than
    pretending otherwise.
- Neutral: the redirect flow of WP-16 will revisit this path. The check stays valid there; the compensation may
  be replaced by the authorisation round trip.

## Harness questions

- **Catalog:** yes — the pitfall "remote effect before local preconditions" belongs beside the existing
  "remote call inside a transaction". Written in the same change.
- **Rule:** none. Whether `assertReadyForPayment()` is called before a port call is an ordering property inside
  one method; a static rule that tried to check it would be guessing.
- **Marker:** none.
