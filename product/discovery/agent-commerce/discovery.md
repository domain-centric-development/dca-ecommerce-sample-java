# Discovery: Agents browse and buy in the shop

<!-- Started from request 20261103T095428585Z-discover by Christoph Blömer, 2026-11-03; continued the same day
after the catalogue's questions were answered (product/decisions/20261103T095431Z-discover.md, option c). No web
search was available in either run: every statement about UCP or ACP comes from the model's knowledge (to 2026-06)
and is listed under `## Risks and open questions` as something to verify, not as evidence. -->

## Problem

Wished deliverable, in the requester's words: "I want agent to browse and buy in the shop. Maybe be via UCP."
The problem, as Christoph Blömer confirmed it [S11]: a person who hands their shopping to an agent cannot buy
here — the agent can only name products. An AI assistant reads the catalogue through the MCP server [S1][S2], but
nothing an agent can call fills a cart or completes a checkout: the cart is reachable only through the REST API
with a shopper's token [S3], and the checkout only through the five web pages [S4]. How often that happens and
what it costs is not measured; the shop is a demonstration [S5], and its purpose is to make the agent harness
deterministic [S7] — the evidence for this work is the demonstration of agent commerce over DCA ports, not a
shopper's complaint [S11].

## Users and evidence

- The **AI assistant** is a named actor today, limited to reading the catalogue [S5]; the epic that gave it the
  catalogue measures "an assistant's answer names a product with its price and stock" [S2].
- The agent acts **for a shopper with an account**, through the existing token, not for a guest [S11]; the
  shopper checks out as a guest or with an account today [S5], and the token belongs to the REST API [S3][S6].
- What the product already does: the MCP server has the tools `all-products` and `product-by-id`, read-only [S1];
  the REST API runs carts for a signed-in partner application, measured by `CartItemAddedToCart` [S3][S6]; checkout
  has no incoming adapter besides the web pages and the cart event consumer [S4].
- Evidence that agents try to buy here: none, by the requester's answer — no interview, ticket or log; the work
  is a demonstration [S11].

## Options

- Do nothing — an agent keeps answering what the shop sells [S2] and a person still buys through the pages.
- Own MCP tools for cart and checkout, over the existing MCP server [S1]: new incoming adapters onto the use cases
  that exist (add to cart, start checkout, submit buyer info, delivery, payment, confirm) [S4]. Smallest change;
  no standard an outside agent recognises.
- UCP over the existing MCP server — a standard checkout capability over the MCP binding. Reuses the MCP surface
  [S1]; costliest, built twice [S9].
- **UCP over its REST binding with a published profile — chosen** [S11]: a surface of its own beside the JSON API
  [S5], with a profile an agent discovers, and the checkout capability driving the existing checkout use cases [S4].
  Standard without MCP; a second protocol edge beside the REST API and the MCP server. Claims about UCP itself are
  unverified (see risks).
- Agentic Commerce Protocol (ACP) — the comparable standard from the other side of the market. Same trade-off as
  UCP; not chosen [S11]; unverified (see risks).
- Agent prepares, person confirms — the agent fills the cart and hands the person a link to the review page.
  Keeps payment with the person; not chosen [S11].

## Outcome

- `CheckoutConfirmed` — for a checkout an agent completed: the glossary's event for placing the order [S8],
  confirmed without a target [S11]. Telling an agent's order from a person's (a field on the event, or the surface
  it came through) is open and decides how the epic is counted.

## Risks and open questions

- constraint: no real payment and no stored payment data [S5][S11]; an agent protocol's payment token or mandate
  ends at the stand-in or the configured payment provider [S9].
- constraint: the protocol stays in an adapter [S11] — its vocabulary belongs in the incoming adapter, never in
  the domain, a marker or a rule name [S7].
- constraint: the .NET twin follows [S11][S9]; whatever is built is built twice, against one specification.
- open: how an agent's checkout is told apart from a person's, so `CheckoutConfirmed` can be counted per surface.
- open: how the protocol's checkout session maps onto the shop's five checkout steps [S4] — one protocol update
  may carry buyer, delivery and shipping at once, where the pages take them one by one.
- open: identity — the existing token [S11] against the protocol's own identity linking, if it has one.
- assumption (model knowledge, not verified in this run): UCP was published in January 2026 by Google with
  retailers and platforms as an open standard; a business publishes a profile under `/.well-known/ucp` naming its
  capabilities (checkout, identity linking, order) and its transports (REST, MCP, A2A); payment runs through
  pluggable payment handlers, among them the Agent Payments Protocol (AP2). To verify at ucp.dev before the first
  story is planned.
- assumption (model knowledge, not verified in this run): ACP was published in September 2025 by OpenAI and Stripe;
  an agent creates, updates, completes and cancels a checkout session over REST, and payment passes as a delegated,
  scoped token.
- risk: the profile and the REST binding are a public, unauthenticated discovery document beside the token-protected
  API; what it may reveal is a decision for the story that publishes it.

## Proposed work

### agents-buy-in-the-shop
- intent: A shopper who hands their shopping to an agent cannot buy here — the agent can find products through the MCP server but nothing it can call fills a cart or completes a checkout
- goal: An agent completes a checkout for a shopper with an account, without the shop's pages
- metric: CheckoutConfirmed (for a checkout an agent completed; no target)
- domain_contact: shop-owner

## Proposed description changes

### overview.md — What and for whom
- change: the AI assistant also buys for a shopper with an account, not only reads the catalogue [S11][S5]
- why: the actor's reach changes with this work

### overview.md — Surfaces
- change: a checkout for agents over the Universal Commerce Protocol's REST binding, with the shop's published profile, beside the JSON REST API [S11]
- why: a new surface an actor uses

### tech.md — Integrations
- change: agents reach the checkout over UCP's REST binding, inbound, discovered from a published profile; the payment it carries ends at the stand-in or the configured payment provider [S11][S9]
- why: a new inbound protocol; the list ends in "Nothing else"

## Sources

- [S1] The MCP tool provider — `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/mcp/ProductCatalogMcpToolProvider.java`
- [S2] Epic "The catalogue for AI assistants" — `product/epics/catalogue-for-assistants/epic.md`
- [S3] The cart REST resource — `src/main/java/dev/domaincentric/sample/ecommerce/cart/adapter/incoming/api/ShoppingCartResource.java`
- [S4] The checkout's web routes, its only incoming surface for a shopper — `src/main/java/dev/domaincentric/sample/ecommerce/checkout/adapter/incoming/web/CheckoutRoutes.java`
- [S5] The product description — `product/overview.md`
- [S6] Epic "Shop through the API" — `product/epics/shop-api/epic.md`
- [S7] The project instructions — `AGENTS.md`
- [S8] The checkout glossary, entry CheckoutConfirmed — `product/spec/checkout/glossary.md`
- [S9] The technical decisions — `product/tech.md`
- [S10] Epic "Payment through the payment provider" — `product/epics/provider-payment/epic.md`
- [S11] The requester's answers to the catalogue's questions, option c — `product/decisions/20261103T095431Z-discover.md`
