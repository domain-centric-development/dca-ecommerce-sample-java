# ADR-044: A Failure Carries Its Own Type, and Only the Adapter Turns It Into an Answer

**Date**: 2026-09-21 · **Status**: Accepted

## Context

Until now this shop raised the platform's own exceptions for everything. A cart that refused a change, a stock
keeping unit the catalog already held, a session that belonged to somebody else and a null argument all arrived at
the boundary as `IllegalArgumentException` or `IllegalStateException`. Measured before this change: 258 argument
throws and 34 state throws, 54 of them inside domain packages.

That cost us three things.

- **The adapter could not tell the cases apart.** `ShoppingCartResource` decided between `404` and `400` by
  reading the exception message (`msg.startsWith("Product not found")`), and `ProductResource` answered `400` with
  an empty body whether the stock keeping unit was malformed or already taken. A message is not a contract: a
  reworded sentence changes the status code.
- **A defect looked like a caller's mistake.** `IllegalArgumentException` is also what the runtime raises when
  this application passes something wrong internally. Catching it at the boundary and answering `400` reports our
  own bug as the customer's error, and hides it from anything watching for server faults.
- **The domain lost words it has.** "The reservation is already confirmed" and "only three left" are sentences a
  domain expert says. As argument exceptions they were strings.

`ChangePasswordUseCase` shows the seam at its clearest: it had to catch `IllegalArgumentException` around a
strength check and carried a four-line comment explaining that the catch must not accidentally swallow a failure
from the hashing adapter, because both arrive as the same type.

## Decision

**Three layers of failure, each with a base type from `dca-building-blocks`, and one translation site per
context.**

- A broken rule of the model is a `DomainException` in the domain package: `InsufficientStockException`,
  `CartNotModifiableException`, `CheckoutStepNotCompletedException`, `PasswordTooWeakException`,
  `CurrencyMismatchException` — 20 types across the four contexts that have rules and the shared kernel.
- A request the application cannot serve is a `UseCaseException` beside the use case: `CartNotFoundException`,
  `DuplicateSkuException`, `CheckoutSessionNotFoundException`, `PaymentProviderUnavailableException` and the rest —
  16 types.
- **An argument guard stays the platform's own exception.** A null check or a range check in a value-object
  constructor states what a caller must never pass; it is not a business rule and no rule of the catalog selects
  it. The cut is the name: if a domain expert has a word for the failure, it is a domain exception with that word
  in it.
- **The incoming adapter translates, and it is the only layer that does.** `ProductApiExceptionHandler`,
  `CartApiExceptionHandler` and `AccountApiExceptionHandler` are `@RestControllerAdvice`es scoped to their own
  context's `adapter.incoming.api` package, each producing a `ProblemDetail` (RFC 9457). The page controllers of
  Checkout and Account catch the two base types and render the message into the form they came from.
- **The status follows the failure, not the base type.** `CartItemNotFoundException` is a rule of the model and
  still answers `404`, because what the caller has to do about it is ask for something that exists. Deciding that
  is the adapter's job: the same use case serves the REST exposure and a page with different answers.
- **Neither base type carries a code or a status field.** That would be the adapter's decision taken in the inner
  layer.

**The result channel stays where it is.** Account and Inventory already answer some outcomes with a result variant
(`ChangePasswordResult.Outcome`, `ReduceStockResult.failure`, ADR-023) rather than an exception, and that does not
change. What changed is that the catch which converts a domain rule into such a variant now names the rule's own
type: `ChangePasswordUseCase` catches `PasswordTooWeakException`, `ReduceStockUseCase` catches
`InsufficientStockException`, and a malformed call from an adapter no longer arrives in the same clause.

## Consequences

- The build needs `-PwithDcaJava` until `dca-building-blocks` ships the two base types. Until then this work lives
  on `wp-22-java-sample`; CI on `main` keeps building against the published coordinates.
- `DCA-ERR-001` … `DCA-ERR-005` hold the shape from now on: a new exception in a domain or application package
  must extend the base type of its layer, live there, carry no framework annotation and name no transport concept.
- `DCA-ERR-006` lists the incoming adapter packages that name no failure type. Nine remain: four event-consumer
  packages, where a failed reaction belongs to the delivery machinery's retry rather than to an answer; the
  bootstrap initializer, which runs before any customer exists; the product page and tool packages, whose queries
  report "not found" as a result flag and raise nothing; the cart recovery page, which only parses a merge
  strategy; and the backoffice page. The last two are the ones worth revisiting when those flows grow a refusal
  of their own.
- `RegisterResponse` lost its failure factory: a refused registration is a problem document now, so the success
  body no longer carries an error field that was always null.
- The remaining imprecision is deliberate and marked in each handler: `IllegalArgumentException` is still mapped
  to `400`, because a form field reaches a value object unvalidated. Every field moved into request validation is
  one reason less for that mapping to exist.
