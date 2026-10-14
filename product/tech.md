# DCA Shop — technical decisions

## Stack

Java 25 with Spring Boot 4 and Spring Modulith, built with Gradle (the wrapper in the repository).
The DCA building blocks and the DCA ArchUnit rules come as published packages. It is the reference
implementation: the .NET sample follows its behaviour.

## Frontend approach

Server-rendered pages from Pug templates, with a little progressive enhancement (theme choice, view
transitions) and no client framework. The REST API and the MCP server are separate surfaces over the
same use cases, not a backend for a single-page application.

## Persistence

In-process only: in-memory repositories, with the cart on JPA and the account on JDBC against an
embedded H2 database, to show that the persistence choice stays behind the ports. No external
database, no second store beside these.

## Runtime

One Spring Boot process on port 8080, started with `./gradlew bootRun` or as a container
(`docker compose up`).

It is built to run as several instances behind a load balancer without sticky sessions: nothing a later request
needs lives in one instance's memory. A message for the page a redirect leads to travels with the redirect as a
fixed code in its URL (`?notice=<code>`), never as the sentence and never in a server session: the page turns the code
into its own sentence and shows nothing for a code it does not know. The valid codes are one enum in the incoming
web adapter, each value with its code and its sentence; reading the parameter is a lookup in that enum, never a
switch over strings. The checkout steps' notices are `CheckoutStepNotice`, the cart pages' `CartPageNotice`, the
account pages' `AccountPageNotice`; the checkout sends the customer back to the cart with the cart page's codes
(`CartNotice`). A notice that names a number carries it beside the code (`&count=<n>`). What must outlast a request
lives in the database or in a cookie every instance can read.

## Two shops, one shop

This shop has a twin in the other language that implements the same specification. Where a plan shapes an
element differently from the twin (another mechanism, another split, another name), the plan's `## Notes`
answers three questions, "none" included: does the knowledge catalog need a node, could a rule check it, would
a marker make it checkable. The difference is closed at that source before it is closed in the code.

## Integrations

A payment provider over REST, outbound. A payment is `POST /payments` with the body
`{"amount": "17.98", "currency": "EUR"}` — the amount a decimal string with two places, never a JSON number,
which would pass through binary floating point — and the currency an ISO 4217 code;
`201` with a payment reference authorizes it, `402` refuses it, and no answer within 2 seconds counts
as unavailable. Where the provider's address is not configured, a stand-in inside the sample takes
payments. MCP clients reach the catalogue over streamable HTTP. Nothing else.

## Version policy

The latest stable releases of Java, Spring Boot and Gradle; milestone releases only where the MCP
support needs them (Spring AI). The DCA packages are pinned to a released version.
