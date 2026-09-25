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

## Integrations

A payment provider over REST, outbound. A payment is `POST /payments` with the amount and currency;
`201` with a payment reference authorizes it, `402` refuses it, and no answer within 2 seconds counts
as unavailable. Where the provider's address is not configured, a stand-in inside the sample takes
payments. MCP clients reach the catalogue over streamable HTTP. Nothing else.

## Version policy

The latest stable releases of Java, Spring Boot and Gradle; milestone releases only where the MCP
support needs them (Spring AI). The DCA packages are pinned to a released version.
