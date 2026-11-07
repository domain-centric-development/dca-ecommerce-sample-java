# ADR-047: Expected Refusals Are Cases of a Closed Use-Case Result

**Date**: 2026-11-07 · **Status**: Accepted · **Supersedes**: ADR-023 · **Amends**: ADR-044, ADR-034

## Context

ADR-044 gave every failure a type and left one channel for all of them: a broken rule of the model was a
`DomainException`, a request the application could not serve a `UseCaseException`, and the incoming adapter mapped
type to answer in an `@ExceptionHandler` or a page's `catch`. ADR-023 had already returned "not found" as an empty
`Optional`, and Account answered some outcomes with an enum. Three channels, chosen per use case.

What that cost:

- **The seam was a convention, not a type.** An adapter test stubbed the input port to throw a refusal; nothing but
  a second test held that the real use case threw the same type. A renamed or new refusal compiled everywhere and
  surfaced as a `500`.
- **The port did not say how it refuses.** `CreateProductInputPort.execute(command)` returned the success; which
  refusals the caller had to answer was in Javadoc or nowhere.
- **Expected outcomes took the exceptional path.** A stock keeping unit already in use is an ordinary answer of
  the use case, not an exceptional state.

## Decision

**A use case returns every refusal its caller answers as a case of a closed result.** Scope: expected refusals
only — not found, not the caller's, a precondition, a uniqueness, a rule of the model a caller answers. Everything
else stays an exception. The style is chosen per bounded context and is uniform within it; this shop chose it for
every context.

- **Shape.** The input port returns a sealed `<Op>Result`: one record for what happened (`Created`, `Added`,
  `Found`, `Confirmed`), one record per refusal, named like the failure without `Exception` (`DuplicateSku`,
  `CartNotFound`, `CheckoutStepOutOfOrder`). A refusal case carries the ids and figures the refusal compared, never
  words, and implements the building blocks' `Refusal` marker. A use case without a refusal keeps a plain record.
- **The domain keeps throwing.** An aggregate's behaviour method and a value object's constructor cannot return an
  outcome without polluting every call site, so `DomainException` stays. The use case catches each domain refusal
  a caller answers by its own name, once, and returns its case. A domain exception no use case returns stays
  thrown and is translated as before (`PaymentDiffersFromReviewException`, `AccountClosedException`).
- **What stays an exception.** Argument guards (`IllegalArgumentException`), defects, and failures of the
  system's own infrastructure — its database, its broker. A foreign system behind an output port (the payment
  provider, the mail provider — the external systems of the context map) failing or being unreachable is expected
  behaviour of the integration and a case (amendment below).
- **The failure hierarchy names the port's direction.** `PortException` › `InputPortException` › `UseCaseException`,
  and `PortException` › `OutputPortException`. A failure an output port declares is an `OutputPortException` beside
  the port; the adapter raises it, the use case catches it and returns its case. `ActiveCartAlreadyExistsException`,
  `PaymentProviderUnavailableException`, `PaymentInitiationFailedException` and `MailProviderUnavailableException`
  are the ones in this shop. No incoming adapter catches one of the three abstract bases.
- **A returned refusal rolls back like a thrown one.** `dca-spring` rolls back a `TransactionBoundary` and a
  `@Transactional` method whose work returns a `Refusal` (`dca.spring.refusal-rollback.enabled`, on by default).
  The sample defines no boundary of its own. `CreateCartUseCase` therefore catches the store's refusal inside the
  boundary and returns the case from there.
- **Decide, then write.** A use case checks every refusal before its first write; the roll-back is the net, not
  the design. `StartCheckoutUseCase` superseded and saved the previous session before the new one could refuse an
  empty checkout; it now starts the new session first. No other use case refuses after a write.
- **A remote effect keeps its compensation.** `ConfirmCheckoutUseCase` reserves stock in Inventory and has the
  payment authorized before its own transaction (ADR-041). It gives the reservation back for every refusal returned
  after the reservation, as it did for every exception — which it still does. An authorized payment is still not
  voided when the local confirmation refuses, as before.
- **The adapter answers, exhaustively.** Every incoming adapter switches over the result without a `default`, so a
  new case breaks every caller that does not answer it. The words of one context live in one place —
  `CartProblems`, `ProductProblems`, `PricingProblems`, `AccountProblems`, the checkout's `CheckoutStepPage`.
  `@ExceptionHandler`s and page `catch`es keep only what is still thrown.
- **Nobody to answer.** An event consumer, an open host service or the start-up data turn a refusal case back into
  an `IllegalStateException`; an open host service keeps its public signature.

Per context:

| Context | Closed results | Plain results kept |
|---|---|---|
| Cart | AddItemToCart, RemoveItemFromCart, RemoveCartItem, GetCartById, CreateCart, CompleteCart, MergeCarts, RecoverCart | GetActiveCart, GetOrCreateActiveCart, GetAllCarts, GetCartMergeOptions |
| Product | CreateProduct, GetProductById | GetAllProducts, GetProductSelection |
| Pricing | ChangePrice | GetPricesForProducts, ProvisionInitialPrice |
| Inventory | ReserveStock | ReduceStock (a report per line: the sale has happened, it refuses nothing), ReleaseStock, ProvisionInitialStock, both queries |
| Account | AuthenticateAccount, RegisterAccount, ChangePassword, ChangeProfile, GetProfile, GetAccountOverview | IsAccountRegistered |
| Checkout | StartCheckout, SubmitBuyerInfo, SubmitDelivery, SubmitPayment, ConfirmCheckout, GetCheckoutReview, GetCheckoutSession, SendOrderConfirmation | GetActiveCheckoutSession, GetConfirmedCheckoutSession, ResumeCheckout (none is a normal answer, amendment below), GetPaymentProviders, GetShippingOptions, SyncCheckoutWithCart |
| Backoffice | — | both use cases: replaying an unknown publication resubmits nothing and answers like a replay |

## Consequences

Measured on the cart alone (the prototype): lines at the translation sites 534 → 574 (+7 %), application / domain
exception classes 5 / 4 → 1 / 4, `@ExceptionHandler` methods 9 → 3, switches over results in incoming adapters 4 →
16, try / catch in the application 1 / 1 → 5 / 7, refusals constructed as exceptions in the application 8 → 0; one
new case breaks 6 switches at compile time. The .NET twin measured 459 → 481 (+5 %) for the same context.

Across the whole sample, counted per file the change touched: lines in the touched incoming adapters and open host
services 4,792 → 5,044 (+5 %); application exception classes 20 → 2 (domain 21, unchanged); `@ExceptionHandler`
methods 26 → 13; switches in incoming adapters and open host services 17 → 68; try / catch in the application 7 / 7
→ 18 / 26.

- **What it buys.** A new or renamed refusal is a compile error at every site that must answer it, instead of a
  `500` at run time; the port's signature says how it refuses; the application constructs no exception for an
  expected refusal; the seam an adapter test and a use-case test held together by convention is now the
  compiler's.
- **What it does not buy.** Less code: translation grew by about 5 %, the application gained catch blocks (the
  domain still throws, and the use case catches once), and every adapter keeps a second, exception-based path for
  what is still thrown.
- **An ignored result swallows a refusal.** Java has no must-use check. Test helpers that set up state through a
  use case assert the success case; production callers with nobody to answer throw.
- **A transaction with a step after the commit** — `AddItemToCart`, `RemoveItemFromCart` re-read the cart at
  today's prices — needs a private `Saved | Refused` type and a second switch; its `Refused` is a `Refusal`, so the
  net sees it. `ConfirmCheckout` needs a private `Authorized | NotAuthorized` for the payment, a multi-catch with a
  mapping switch, and an `instanceof Refusal` check for the compensation.
- **Case names repeat** (`CartNotFound` in six results, `CheckoutSessionNotFound` in nine): a sealed interface can
  only permit nested types of one package, so a case cannot be shared; switches use qualified names.
- **Answers that need more than the case** — a step sent too early is answered on the step still open — take a
  `Supplier<StepAccess>` beside the result; a query mapped from an `Optional` needs a type witness; a page that
  reads a value from a lookup declares it before a statement-switch.
- **`DCA-ERR-006`** (a diagnostic) counts a switch over a closed result as translation; **`DCA-ERR-008`**
  (experimental, enforced) holds that every closed result has a `Refusal` case.
- **The refusal wording** names refusal cases instead of exception types; the .NET twin's copy of
  `refusal-wording.md` follows.
- Behaviour is unchanged — statuses, problem bodies, page sentences, redirects, markup — with one exception: a
  checkout session that disappears between the step check and the review query now leads back to the cart with
  "checkout not found" instead of an error page.

## Amendment (2026-11-07): foreign systems, and "which one, if any"

Taken while aligning the checkout with the .NET twin (WP-171); the twin's ADR-017 carries the same amendment.

- **A foreign system's expected failure is a case; the system's own infrastructure throws.** The payment provider
  and the mail provider are external systems on the context map. Their output ports declare what the system may
  answer besides success as `OutputPortException`s — `PaymentProvider.initiatePayment` returns the payment reference
  and raises `PaymentInitiationFailedException` (the provider refused) or `PaymentProviderUnavailableException` (no
  answer in time, no connection, an answer outside the contract); `OrderConfirmationMailer.send` raises
  `MailProviderUnavailableException`. The adapter throws them, the use case catches them and returns
  `PaymentInitiationFailed`, `PaymentProviderUnavailable` or `MailProviderUnavailable`, each a `Refusal`. The
  port's earlier outcome values (`PaymentResult.Outcome.UNAVAILABLE`, `Delivery`) are gone from the initiation and
  the mail; confirming and cancelling a payment keep `PaymentResult`, since no use case answers them. A failure of
  the shop's own database stays an exception.
- **`SendOrderConfirmation` is `Sent | NoPlacedOrder | MailProviderUnavailable`** in both shops. The event consumer
  has nobody to answer: it parks `NoPlacedOrder` (logged, completed — trying again changes nothing) and throws
  `MailProviderUnavailable` back, so the event publication registry retries the delivery.
- **"Which one, if any" queries stay plain.** When "none" is a normal answer — the customer's active checkout, their
  placed order, an open checkout of their cart to resume — absence is an empty value, not a refusal:
  `GetActiveCheckoutSession`, `GetConfirmedCheckoutSession` and `ResumeCheckout` return plain results with a `found`
  / `resumed` flag again. A query that names one session by its id — `GetCheckoutSession`, `GetCheckoutReview` —
  demands that session, and its absence stays the refusal `CheckoutSessionNotFound`.
- **Counts after the amendment.** Application exception classes 2 → 4, all of them `OutputPortException`s; no
  `UseCaseException` subtype is left in the sample.
