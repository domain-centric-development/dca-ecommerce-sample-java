# ADR-036: A Guard Goes Where Its Inputs Are — and `authenticated()` Is Not One

**Date**: 2026-08-30 · **Status**: Accepted · **Amended**: 2026-09-17 (how a refusal is phrased)

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
- Negative: the coarse role gate was a line of code in each handler rather than a policy in the filter chain.
  The amendment below moved it into the framework, which is what the ADR already named as the production answer.
- Negative: `findByIdForCustomer` sits next to `findById`, and a use case that reaches for the wrong one is back
  where it started. The scoped one is the default and the unscoped one is documented as the system path, but
  nothing enforces the choice.
- Negative: the staff role has no provisioning path. Granting it means minting a token out of band, which is what
  the test does. Honest for a sample, not acceptable in a real shop.
- Neutral: `anyRequest().authenticated()` stayed at first, because it still did the one thing it could — keep a
  request without any identity at all out. The amendment below removes it: with the anonymous visitor no longer
  counting as authenticated, it would have started refusing the visitors the shop is for.
- Neutral: six of Cart's use cases already took the caller as input (`CreateCart`, `GetOrCreateActiveCart`,
  `GetActiveCart`, `MergeCarts`, `RecoverCartOnLogin`, `GetCartMergeOptions`). The five that did not are exactly
  the five that needed a guard. Read that way this is less a decision about authorization than a correction of
  five under-specified commands: "check out cart X" without saying on whose behalf is an incomplete instruction.

## Amendment (2026-09-17): a stranger is challenged, a customer is forbidden

**Context.** The role gate read `identityProvider.getCurrentIdentity().hasRole(ROLE_STAFF)` in the handler and
answered `403` to everybody it refused — an anonymous caller included. `403` says "you are known and this is not
for you", which tells a caller with no token that a token would not help either. The .NET twin already answered
`401` with `WWW-Authenticate: Bearer` to a stranger and `403` to a customer without the role (its ADR-008), so the
two samples contradicted each other on the one surface a reader copies first.

**Decision.**

- The gate is the framework's: `@PreAuthorize("hasRole('STAFF')")` on the two routes that demand the role
  (`POST /api/products`, `GET /api/carts`), with method security enabled in `SecurityConfiguration`. The
  discriminator of the original decision is untouched — claims-only checks stay at the exposure, ownership stays
  in the use case.
- The JWT filter still gives every request an identity, and the identity is still the principal, so nothing
  changes for the use cases (the enrich-not-gate rule of ADR-029 stands). What changes is the *kind* of token: a
  registered session becomes an authenticated token, a visitor an `AnonymousAuthenticationToken`. Only the
  security context stops pretending that a visitor authenticated.
- `anyRequest().authenticated()` becomes `permitAll()`. It guarded nothing while every request was authenticated,
  and it would now lock the visitor out of the shop it is written for.
- A refusal is rendered by the entry point and the access-denied handler: on `/api/**` and `/mcp` a problem
  document, `401` carrying `WWW-Authenticate: Bearer` for the stranger and `403` for the caller who lacks the
  role; in the browser a redirect to `/login?returnUrl=…`. `TokenOnlyPaths` names that surface once, so the CSRF
  exemption of ADR-035 and the refusal style cannot drift apart.

**Consequences.**

- Positive: both shops now answer a stranger and a customer alike; `ApiAuthorizationIntegrationTest` holds the
  status codes and the challenge header, and a filter unit test holds that a visitor is anonymous in the context.
- Positive: a reader copying the resource copies an annotation, not a hand-written status code.
- Negative: authorization is now spread over the annotation and the two handlers, and `permitAll()` on
  `anyRequest()` reads more open than it is. What protects a route is the annotation on it or the scoped question
  in its use case, and nothing points at that from the filter chain but a comment.
- Neutral: the staff role still has no provisioning path.

**Harness questions.** Catalog: done — the authorization decision node gained a "challenged or forbidden"
paragraph and the pitfall "authenticated is not authorized" gained the two paragraphs that follow from it (mark
the visitor anonymous; declare the claims-only gate). No node of its own: the split is a consequence of that
pitfall, not a second one. Rule: a candidate exists — a claims-only gate is an annotation, never a role branch
inside a handler — but the selection needs a framework-neutral phrasing in both libraries before it can be
written, and it is not written yet. Marker: none.
