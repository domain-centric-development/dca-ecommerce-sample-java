# ADR-037: Adopt dca-spring and dca-archunit-spring-modulith

**Date**: 2026-09-08 · **Status**: Accepted · **Extends**: ADR-033, ADR-034

## Context

ADR-033 moved the markers and the rule catalog to the published `dca-java` libraries and left the sample with
two hand-written runtime classes the rules demand of every project: `SpringDomainEventPublisher`
(`sharedkernel/adapter/outgoing/event`) and `SpringTransactionBoundary` (`sharedkernel/infrastructure/transaction`,
ADR-034). It also kept a hand-written `SpringModulithVerificationTest` around `ApplicationModules.of(...).verify()`.
None of the three contains a domain type; the .NET sample and a greenfield project wrote the same code again.

`dca-java` now publishes them: `dev.domaincentric:dca-spring` (runtime — the two adapters, an in-memory boundary
and a Spring Boot auto-configuration) and `dev.domaincentric:dca-archunit-spring-modulith` (test — `DcaSpringModulithTest` with
the test-class filter Modulith needs). The building blocks and the rules stay framework-free; a build check
in `dca-java` enforces it.

## Decision

The sample consumes both artifacts and deletes its private copies. It is the reference implementation and
cannot keep a copy of a published class.

- `implementation 'dev.domaincentric:dca-spring'` — `DcaSpringAutoConfiguration` registers
  `SpringDomainEventPublisher` and, because the JPA starter provides a `PlatformTransactionManager`,
  `SpringTransactionBoundary`. The shared kernel keeps no `adapter/` and no `infrastructure/transaction/`.
- `testArchitectureImplementation 'dev.domaincentric:dca-archunit-spring-modulith'` — `SpringModulithVerificationTest`
  becomes a `DcaSpringModulithTest` subclass with the same `EcommerceLayout`.

Dependencies of a DCA Spring project, in full: `dca-building-blocks` + `dca-spring` in production,
`dca-archunit` + `dca-archunit-spring-modulith` in the test source set.

## Consequences

- Two fewer places where the save → dispatch → clear ordering and the nesting semantics of the boundary are
  written by hand; both are now pinned by the library's tests.
- The shared kernel holds only project-specific code: `IdentityProvider`, value objects, specifications,
  `AsyncInitialize`.
- The sample cannot demonstrate the in-memory failure mode (`@Transactional` inert without a transaction
  manager) because JPA gives it a manager; the library's auto-configuration javadoc and the guide carry that
  knowledge.
- Until the two artifacts are on Maven Central the build resolves them through `-PwithDcaJava`.
