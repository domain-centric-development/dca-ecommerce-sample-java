# DCA Shop

<!-- The product description of this reference implementation. Product decisions only; the code
design is in the architecture documentation under docs/. -->

## What and for whom

An online shop that is small enough to read end to end and complete enough to show every pattern of
Domain-Centric Architecture in working code. Its actors: a **shopper**, who browses the catalogue,
fills a cart and checks out, as a guest or with an account; an **operator**, who looks after prices,
stock and the running application; and an **AI assistant**, which reads the catalogue through a tool
interface.

## Surfaces

- Server-rendered web pages for the shopper — home page, catalogue, product page, cart, a five-step
  checkout, registration, login and account self-service — on desktop and phone.
- Back-office pages for the operator: the event publication log and administrative views.
- A JSON REST API with token authentication for the same shopper operations.
- An MCP server that exposes the product catalogue to AI assistants.

## How it works

A shopper adds products to a cart that belongs to the browser until they log in; on login a guest
cart is merged into the account's. Checkout takes the cart through buyer, delivery, shipping, payment
and review, and confirming it reduces the stock. Prices and stock are owned by their own parts of the
shop and read by the catalogue, the cart and the checkout when they need them. State is kept on the
server; the pages hold no state of their own beyond the session and a theme choice. The shop starts
with sample data and keeps nothing across a restart.

## Look and feel

Plain, readable storefront pages with one shared stylesheet, a default theme and a few alternatives
the shopper can pick, and smooth page transitions where the browser supports them. English only. Forms are usable
with the keyboard, and every element a test addresses carries a stable `data-test` attribute.

## Qualities

- Authentication by a signed token, in a cookie for the pages and in the header for the API; every
  resource states who may use it, and a shopper only ever reaches their own cart and account.
- Passwords are stored hashed; no payment data is stored — payment is handed to a provider (a stand-in
  in the sample).
- Performance and availability are those of a demonstration: one instance, no scaling promise.
- The architecture rules run with every build and fail it on a violation.

## Not part of the product

- Real payment, real shipping, invoicing and returns.
- Multiple currencies, languages or tenants.
- Durable production data: the sample is reset on every start.
- Anything that exists only to look like a feature — the shop exists to show the architecture.
