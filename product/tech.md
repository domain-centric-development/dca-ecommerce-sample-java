# DCA Shop — technical decisions

## Stack

Java 25 with Spring Boot 4 and Spring Modulith, built with Gradle (the wrapper in the repository).
The DCA building blocks and the DCA ArchUnit rules come as published packages. It is the reference
implementation: the .NET sample follows its behaviour.

Refusals: returned — expected refusals are cases of a closed use-case result (ADR-047)

## Frontend approach

Server-rendered pages from Pug templates, with a little progressive enhancement (theme choice, view
transitions) and no client framework. The REST API and the MCP server are separate surfaces over the
same use cases, not a backend for a single-page application.

Browsers: a broad public, older phones and browsers included.

Styling: BEM names with cascade layers — `@scope` drops a whole component in a browser without it, and a shop
serves older ones.

Gallery: none yet — Storybook's server renderer when the shop gets one.

Measured against the frontend craft that follows these three lines, the shop keeps: BEM blocks with modifiers
in one stylesheet shared by both samples; semantic tokens as custom properties on `:root` with brand themes
chosen by `data-theme`; `data-test` attributes as the browser tests' only selectors; `prefers-reduced-motion`.
It does not keep yet: cascade layers, container queries and named widths (the stylesheet has several viewport
breakpoints), three token tiers (hex values also stand outside the token block), state in attributes only (a
few `is-`/`active` classes remain), a light/dark scheme, and structural checks over the stylesheet. Closing a
gap is a story through the factory, not a side change.

## Persistence

PostgreSQL, the same database the .NET shop runs on. Each repository is a JDBC adapter with SQL
written by hand and the mapping in the adapter, through the aggregate's `reconstitute`; uniqueness
claims are database constraints. The schema comes from Flyway, one versioned script per context
under `db/migration/`, plain SQL with the same file names in both shops; the event publication
table is Spring Modulith's. Tests and `./gradlew bootTestRun` start PostgreSQL in a container
(Testcontainers, Docker or Podman); `./gradlew bootRun` and the container connect to the database
`DATABASE_URL` names. The sample catalogue is seeded into an empty database only.

## Runtime

One Spring Boot process on port 8080, started with `./gradlew bootRun` or as a container
(`docker compose up`).

It is built to run as several instances behind a load balancer without sticky sessions: nothing a later request
needs lives in one instance's memory. A message for the page a redirect leads to travels with the redirect in a
sealed cookie — encrypted and signed with a key every instance shares, taken from configuration — never in the
address and never in a server session. The cookie holds the notice's code, and a number where the sentence names
one, never the sentence: the page turns the code into its own sentence and shows nothing for a code it does not
know. A notice is addressed to the one page the redirect leads to, shown there once and then gone; a page never
shows a notice meant for another. The valid codes are one enum in the incoming web adapter, each value with its
code and its sentence; reading a code is a lookup in that enum, never a switch over strings. The checkout steps'
notices are `CheckoutStepNotice`, the cart pages' `CartPageNotice`, the account pages' `AccountPageNotice`; the
checkout sends the customer back to the cart with the cart page's codes (`CartNotice`). A checkout step that is
denied and redirects passes no earlier notice on; only its own refusal applies. What must outlast a request
lives in the database or in a cookie every instance can read.

## Two shops, one shop

This shop has a twin in the other language that implements the same specification. Where a plan shapes an
element differently from the twin (another mechanism, another split, another name), the plan's `## Notes`
answers three questions, "none" included: does the knowledge catalog need a node, could a rule check it, would
a marker make it checkable. The difference is closed at that source before it is closed in the code.

**The pages are one contract.** Both shops send the browser the same HTML: the same BEM blocks, elements and
modifiers, the same component shapes and states (a state as an attribute or a modifier, never a style of its own),
the same `data-test` attributes, which only the tests read and no style selects. Styles use the design tokens on
`:root`, never a raw value where a token exists. The one deliberate difference is the corner ribbon's modifier.
The stylesheet and the images under `src/main/resources/static/` are the source of both shops' assets; the twin holds
a byte-for-byte copy, and a cross-checkout check fails when the two differ. A change to them is made here and copied.

## Integrations

A payment provider over REST, outbound. A payment is `POST /payments` with the body
`{"amount": "17.98", "currency": "EUR", "merchantReference": "<checkout session id>"}` — the amount a decimal
string with two places, never a JSON number, which would pass through binary floating point; the currency an
ISO 4217 code; the merchant reference the checkout reference: the checkout session's id as text, the same in
every payment request of that checkout;
`201` with a payment reference authorizes it, `402` refuses it, and no answer within 2 seconds counts
as unavailable. An authorized payment is cancelled with `POST /payments/{payment reference}/cancel`, without a
body: `204` cancels it, and no answer within 2 seconds counts as unavailable. Where the provider's address is not
configured, a stand-in inside the sample takes payments and their cancellations. A mail provider over REST, outbound, for the mails the shop sends on its own. A mail is `POST /messages` with the body
`{"to": "<address>", "template": "<name>", "key": "<idempotency key>", "data": {…}}` — the template names
the mail (`order-confirmation`), the data carries its values as text, and the key is the same for every attempt of
the same mail, so the provider sends it once however often it is asked; `202` accepts it, and anything else or no
answer within 2 seconds counts as unavailable. Where the provider's address is not configured, a stand-in inside
the sample takes the mails and sends nothing. MCP clients reach the catalogue over streamable HTTP. Nothing else.

## Version policy

The latest stable releases of Java, Spring Boot and Gradle; milestone releases only where the MCP
support needs them (Spring AI). The DCA packages are pinned to a released version.
