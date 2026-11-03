# Discovery: Agents browse and buy in the shop

<!-- Started from request 20261103T095428585Z-discover by Christoph Blömer, 2026-11-03. The catalogue's open
questions wait in product/decisions/20261103T095431Z-discover.md; this report is continued once they are answered.
No web search was available in this run: every statement about UCP or ACP comes from the model's knowledge (to
2026-06) and is listed under `## Risks and open questions` as something to verify, not as evidence. -->

## Problem

Wished deliverable, in the requester's words: "I want agent to browse and buy in the shop. Maybe be via UCP."
The problem behind it is not stated yet. Read from the project: an AI assistant can read the catalogue through
the MCP server [S1][S2], but nothing an agent can call fills a cart or completes a checkout — the cart is reachable
only through the REST API with a shopper's token [S3], and the checkout only through the five web pages [S4]. A
person who hands their shopping to an agent therefore cannot buy here; the agent can at best name a product and
send the person to the pages. How often that happens and what it costs is unknown — the shop is a demonstration
[S5], and its purpose is to make the agent harness deterministic [S7].

## Users and evidence

- The **AI assistant** is a named actor today, limited to reading the catalogue [S5]; the epic that gave it the
  catalogue measures "an assistant's answer names a product with its price and stock" [S2].
- The **shopper** checks out as a guest or with an account [S5]; on whose behalf an agent buys — and with which
  identity — is open.
- What the product already does: the MCP server has the tools `all-products` and `product-by-id`, read-only [S1];
  the REST API runs carts for a signed-in partner application, measured by `CartItemAddedToCart` [S3][S6]; checkout
  has no incoming adapter besides the web pages and the cart event consumer [S4].
- Evidence that agents try to buy here: none handed over. No interview, ticket or log is in the project.

## Options

- Do nothing — an agent keeps answering what the shop sells [S2] and a person still buys through the pages.
- Own MCP tools for cart and checkout, over the existing MCP server [S1]: new incoming adapters onto the use cases
  that exist (add to cart, start checkout, submit buyer info, delivery, payment, confirm) [S4]. Smallest change;
  no standard an outside agent recognises.
- Universal Commerce Protocol (UCP) — the deliverable as named: a standard checkout capability an agent discovers
  from the shop's published profile, with an MCP or REST binding. Recognisable to any UCP agent; ties the checkout's
  steps to a protocol's session shape, and its payment handling presupposes a real payment credential flow, which
  the product excludes [S5]. Claims about UCP itself are unverified (see risks).
- Agentic Commerce Protocol (ACP) — the comparable standard from the other side of the market, a checkout session
  an agent creates, updates and completes. Same trade-off as UCP; unverified (see risks).
- Agent prepares, person confirms — the agent browses and fills the cart, then hands the person a link to the
  shop's review page; no agent-side payment at all. Keeps the payment rule [S5]; the agent does not "buy" alone.

## Outcome

- `CheckoutConfirmed` — proposed, for a checkout an agent completed: the glossary's event for placing the order
  [S8]. Whether the shop must tell an agent's order from a person's (a new fact, a field on the event, or the
  channel it came through) is open and decides how the epic is measured.

## Risks and open questions

- open: the problem behind the wish — who suffers today, and from what — only Christoph Blömer can say; asked in
  the project record.
- open: on whose behalf an agent buys — a guest, a shopper's account through the existing token, or a delegated
  identity — and whether a person confirms before the order is placed.
- open: the protocol — UCP, ACP, own MCP tools, or the agent-prepares-person-confirms option.
- open: payment — the product excludes real payment and stores no payment data [S5]; an agent protocol's payment
  token or mandate would have to end at the stand-in or the configured provider [S9].
- open: the domain contact — earlier epics name `shop-owner`, `checkout-lead`, `integration-partner` [S2][S6][S10].
- assumption (model knowledge, not verified in this run): UCP was published in January 2026 by Google with
  retailers and platforms as an open standard; a business publishes a profile under `/.well-known/ucp` naming its
  capabilities (checkout, identity linking, order) and its transports (REST, MCP, A2A); payment runs through
  pluggable payment handlers, among them the Agent Payments Protocol (AP2). To verify at ucp.dev before any epic.
- assumption (model knowledge, not verified in this run): ACP was published in September 2025 by OpenAI and Stripe;
  an agent creates, updates, completes and cancels a checkout session over REST, and payment passes as a delegated,
  scoped token. To verify at agenticcommerce.dev before any epic.
- risk: a protocol adapter is a framework-specific edge; the rules, markers and catalogue stay general [S7] — the
  protocol's vocabulary belongs in the adapter, never in the domain or a rule name.
- risk: the .NET twin implements the same specification [S9]; whatever is chosen is built twice.

## Proposed work

### agents-buy-in-the-shop
- intent: An agent a person sends to shop here can find products but cannot place an order — the cart and the checkout have no surface an agent can drive
- goal: An agent fills a cart and completes a checkout for a shopper through a surface built for agents, without the shop's pages
- metric: CheckoutConfirmed (for a checkout an agent completed; how that is told apart is open)
- domain_contact: open

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
