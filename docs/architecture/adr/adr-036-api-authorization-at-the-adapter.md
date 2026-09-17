# ADR-036: A Guard Goes Where Its Inputs Are — and `authenticated()` Is Not One

**Date**: 2026-08-30 · **Status**: Accepted

## Context

`SecurityConfiguration` ends in `anyRequest().authenticated()`, which reads like a guard and is not one. The JWT
filter (ADR-029) gives *every* request an authentication: a first-time visitor gets an anonymous identity, and an
anonymous identity is still an `Authentication` in the security context. So `authenticated()` is satisfied by
everybody, and the REST resources were left open by it:

- `GET /api/carts` returned every cart of every customer.
- `GET /api/carts/{id}` returned any stranger's cart to anyone who knew or guessed the id.
- `/api/products/**` is `permitAll`, so `POST /api/products` let an anonymous caller create a product.

The .NET twin was about to be built from these resources. Porting them as they stood would have shipped an open
API as a reference implementation, twice.

Two smaller flaws sat in the same files: the resources injected the concrete `*UseCase` classes — the only place
in the sample that does — and both carried commented-out endpoint stubs for use cases that do not exist.

## Decision

**The filter chain says who *is* here; what they may do is decided where the inputs for that decision are.** The
discriminator is not "business or technical" — it is whether the check needs the aggregate.

- **Claims only, no resource** → the adapter. `POST /api/products` and `GET /api/carts` require the staff role,
  checked through `IdentityProvider.Identity#hasRole(ROLE_STAFF)` (new in that interface; no registration path
  grants it, so an account only holds it by being given it). This is a property of the *exposure*, not of the
  operation: `GetAllCartsUseCase` is a legitimate thing for an admin console or a batch job with no HTTP identity
  to run, and forcing an identity port into it would make it unusable there.
- **Ownership of a resource** → the use case, always. It can never be exposure-shaped: *no* caller may act on a
  stranger's cart, through *any* adapter. The caller therefore became part of the command —
  `GetCartByIdQuery(cartId, customerId)`, `CheckoutCartCommand(cartId, customerId)`,
  `AddItemToCartCommand(cartId, customerId, …)`, `RemoveItemFromCartCommand(cartId, customerId, …)` — and the use
  cases read through `ShoppingCartRepository#findByIdForCustomer`, which cannot return somebody else's cart. All
  three persistence adapters implement it; the JDBC one as a single `WHERE id = ? AND customer_id = ?`. The
  Cart's Open Host Service demands the customer for the same reason, so the Checkout context inherits the rule
  instead of repeating it.
- Reading the catalog stays public (`GET /api/products`, `GET /api/products/{id}`).
- `POST /api/carts` no longer takes a `customerId` parameter — it creates a cart for the caller.

**The decision belongs to the use case; its rendering belongs to the adapter.** A cart that is not the caller's
answers `404`, not `403`: a `403` confirms the id exists, which is the one fact a stranger must not learn. That
choice is an HTTP concern, so the resource makes it; what it renders is the use case's answer.

**A use case with no caller is not under-specified.** `CompleteCart` runs from an integration event, at least
once, on nobody's behalf — there is no identity to check and it stays unscoped.

Alongside it: the resources and the MCP tool provider now depend on `*InputPort` interfaces, and the dead
commented-out endpoint blocks are gone.

## Consequences

- Positive: no endpoint exposes another customer's cart, and no anonymous caller can create a product. The .NET
  sample was built to the same rules (its ADR-007), so the two samples do not contradict each other.
- Positive: `ApiAuthorizationIntegrationTest` holds the coarse rules — a refusal for each role, and a stranger's
  cart answering `404` — and `CartOwnershipIntegrationTest` holds the ownership rule at the use-case level, where
  every adapter inherits it.
- Positive: the ownership rule reached an adapter this ADR is not about. `POST /checkout/start` took its `cartId`
  from a hidden form field and `StartCheckoutUseCase` never looked at the caller, so a visitor who learned a cart
  id could open a checkout session on somebody else's cart in their name. Putting the caller into
  `StartCheckoutCommand` closed it; no discipline in the REST resource would have reached it.
- Negative: the coarse role gate is a line of code in each handler rather than a policy in the filter chain.
  Method security (`@PreAuthorize`) over a real `GrantedAuthority` set would move it into the framework, and is
  what a production system should do; the sample keeps it visible instead.
- Negative: `findByIdForCustomer` sits next to `findById`, and a use case that reaches for the wrong one is back
  where it started. The scoped one is the default and the unscoped one is documented as the system path, but
  nothing enforces the choice.
- Negative: the staff role has no provisioning path. Granting it means minting a token out of band, which is what
  the test does. Honest for a sample, not acceptable in a real shop.
- Neutral: `anyRequest().authenticated()` stays, because it still does the one thing it can — it keeps a request
  without any identity at all out. It is no longer mistaken for authorization.
- Neutral: six of Cart's use cases already took the caller as input (`CreateCart`, `GetOrCreateActiveCart`,
  `GetActiveCart`, `MergeCarts`, `RecoverCartOnLogin`, `GetCartMergeOptions`). The five that did not are exactly
  the five that needed a guard. Read that way this is less a decision about authorization than a correction of
  five under-specified commands: "check out cart X" without saying on whose behalf is an incomplete instruction.

## Amendment (2026-09-17): the checkout steps carry the caller too

**Context.** The rule above reached `StartCheckoutCommand`, and stopped there. The five commands and the query of
the wizard steps — `SubmitBuyerInfoCommand`, `SubmitDeliveryCommand`, `SubmitPaymentCommand`,
`ConfirmCheckoutCommand`, `GetCheckoutSessionQuery` — carried the session id alone and loaded through
`findById`, so at the use-case boundary any caller who knew a session id could fill in its buyer information, pay
for it or confirm it. Nothing exploited that today: the web adapter resolves the visitor's *active* session and
never takes a session id from the request. That is exactly the shape this ADR calls out — a guard living in one
exposure rather than in the operation — and the .NET twin carried it identically.

**Decision.** The caller is part of those commands and that query, next to the session id, and every step loads
through `CheckoutSessionRepository#findByIdForCustomer` (`FindByIdForCustomerAsync` in .NET), beside the
unscoped `findById` that stays for the system paths. The use case asks the scoped question rather than comparing
after the fact; a session that is not the caller's is not found, and the adapter renders that as it renders a
missing one.

**Consequences.**

- Positive: `CheckoutOwnershipIntegrationTest` (Java) and `CheckoutOwnershipTest` (.NET) hold the rule at the
  use-case level, where every adapter inherits it. Both fail without the scoped lookup — verified by removing the
  customer predicate from the in-memory repository in each sample.
- Positive: the two samples stay identical on this rule; the .NET half was changed in the same work package.
- Negative: the wizard now passes the caller through five call sites in the page controllers. The web flow does
  not need it — it already resolved the session from the caller — so the parameter looks redundant at the adapter
  and is load-bearing only at the boundary.
- Neutral: `GetActiveCheckoutSession` and `GetConfirmedCheckoutSession` were already keyed on the customer and
  are unchanged.

**Harness questions.** Catalog: covered — the pitfall "resource id without an owner" already states this exactly,
and the fix is what it prescribes; no new node. Rule: the same candidate as before — a command naming an owned
aggregate must also carry the caller — still needs a marker to be mechanical, and is parked. Marker: an `Owned`
marker on the aggregate would make the rule checkable, but the identity port is deliberately project-specific,
so it stays an open question rather than a change.
