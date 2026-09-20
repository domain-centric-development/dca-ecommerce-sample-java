# ADR-043: A Committed Default Must Not Start a Real Deployment

**Date**: 2026-09-20 · **Status**: Accepted

## Context

The shop starts without configuration, which is the point of a sample: `./gradlew bootRun` and there is a shop.
Three of the values that make that possible are ones no deployment may keep.

- `app.security.jwt.secret` falls back to a string committed to this repository. A committed secret is a published
  secret: anyone who can read the repository can mint a token this shop accepts, as any customer or as staff.
- `app.security.backoffice.username`/`password` were `admin`/`admin`, written into `application.yml` with no
  environment override at all. The backoffice replays failed event publications, so that login is the most
  privileged door in the shop.
- `app.security.jwt.secure-cookies` defaults to `false`, so the identity and session cookies travel over plain
  HTTP, where the token is readable in transit. The operator session cookie is the servlet container's and its
  `Secure` flag defaults to `false` as well.

None of the three announces itself. The application starts, the pages render, the tests pass, and the shop behaves
exactly as it does on a laptop. The only thing that changed is who else can read the traffic and mint the tokens.

Documenting them as dev-only — which `application.yml` did — does not hold. A comment is read by the person who
already knows.

## Decision

**The development values are named constants, and the shop refuses to start on them unless the run says it is a
development run.**

- `JwtProperties.DEVELOPMENT_SECRET`, `BackofficeSecurityProperties.DEVELOPMENT_USERNAME` and
  `DEVELOPMENT_PASSWORD` hold what `application.yml` falls back to. `DevelopmentDefaultsTest` pins the YAML
  literals to the constants: a guard that compares against a value nobody uses refuses nothing.
- `JwtDevelopmentDefaultsValidator` (Account) and `BackofficeDevelopmentDefaultsValidator` (Backoffice) run at
  context startup. Each refuses the shipped values of its own context and says which environment variable supplies
  a real one — `JWT_SECRET`, `JWT_SECURE_COOKIES`, `BACKOFFICE_USERNAME`/`BACKOFFICE_PASSWORD`,
  `SERVER_SERVLET_SESSION_COOKIE_SECURE`. A fail-fast an operator cannot act on is only an outage.
- **The input is the active profile, and its default is not development.** `dev | inmemory` means a development
  run; anything else, the empty profile set included, is treated as real. A deployment that names its own profile
  — and it must, to get its database — says it is not a laptop, and from that moment the shipped values are
  refused rather than discouraged.
- The two validators state that expression separately. A context does not reach into another context's
  infrastructure to learn what development means, so the string is duplicated on purpose and
  `DevelopmentDefaultsTest` holds the two copies to one answer.
- What declares a development run declares it everywhere: `bootRun` and every test task default to
  `spring.profiles.active=dev` (a checkout is a development run), `compose.yaml` sets `SPRING_PROFILES_ACTIVE`,
  and the IntelliJ run configuration carries the profile. The image itself carries none — `docker run` of it is a
  production run and refuses to start until the three values are supplied.
- `application.yml` gained the two missing overrides (`${BACKOFFICE_USERNAME:…}`, `${BACKOFFICE_PASSWORD:…}`).

The .NET twin decides the same rule from `ASPNETCORE_ENVIRONMENT` through `IValidateOptions` and `ValidateOnStart`
(its ADR-015).

## Consequences

- Positive: the three values that would have shipped silently now stop a deployment while someone is watching it.
  `UnsafeDefaultsIntegrationTest` boots the real application under a non-development profile and asserts each
  refusal; it is red with the validators unregistered, which is the failure mode that matters — a validator nobody
  wires refuses nothing, and the class itself looks identical either way.
- Positive: the two persistence integration tests that run under `@ActiveProfiles("jdbc")` now say
  `{"dev", "jdbc"}`. That is the shape a developer with a real database needs too, and the compose file documents
  it.
- Negative: a checkout now has four places that say "this is a development run" — `build.gradle`, both compose
  files, the run configuration. Forgetting one is a startup failure with a clear message rather than a silent
  weakness, which is the trade this ADR takes, but it is four places.
- Negative: the guard compares against known strings. An operator who sets `JWT_SECRET` to `secret` passes it.
  This catches the value that ships, not weak configuration in general.
- Neutral: the backoffice cookie's `Secure` flag is a container property here (`server.servlet.session.cookie.secure`)
  and an adapter option in .NET (`Backoffice:SecureCookies`). Two mechanisms, one rule; the validators word the
  refusal alike.

## Harness questions

**Catalog**: done — a new pitfall, *A committed default that only a comment forbids*, states the defect, the
startup refusal, and the two traps this work hit (a guard compared against a literal nobody uses; a validator that
is written but never registered). **Rule**: none that is mechanical. What a rule would have to recognise is "this
constant is also a fallback in configuration", which is a string match across two file formats, not a structural
property — the catalog carries it instead. **Marker**: none. Configuration hygiene is not a building block.
