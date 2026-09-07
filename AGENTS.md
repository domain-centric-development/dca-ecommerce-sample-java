# AGENTS.md

This file provides guidance to AI coding agents (Claude Code, Codex, and others) when working with code in this repository.

## Documentation Language

**⚠️ All documentation files (`*.md`, JavaDoc, code comments, and any other written artifacts checked into the repository) MUST be written in English.**

This applies to:
- All files under `docs/`
- `README.md` and all other top-level Markdown files
- JavaDoc and inline code comments
- Commit messages and PR descriptions
- ADRs, glossaries, context maps, and any generated documentation

Conversational replies to the user may follow the user's language preference, but persisted artifacts are always English to keep the reference implementation accessible to an international audience.

## Build Commands

```bash
# Build & Test
./gradlew build                              # Build project (compile + tests)
./gradlew build -x test                      # Build without tests
./gradlew test                               # Run unit tests (JUnit 5)
./gradlew test --tests "*ProductTest*"       # Run specific test class
./gradlew test -Pfilter=Cart                 # Filter tests by name substring
./gradlew test-architecture                  # Run ArchUnit architecture tests
./gradlew test-architecture --tests "*ArchitectureRulesTest*"  # DCA rule catalog only

# Run Application
./gradlew bootRun                            # Start app (JDWP debug on port 5005)
docker compose up --build                    # Same shop in a container, http://localhost:8080
docker compose run --rm test                 # Tests without a local JDK
./gradlew -Plog-debug bootRun                # Start with debug logging

# Debugging tests
./gradlew -Plog-debug test                   # Tests with verbose output
./gradlew -Plog-debug test-architecture      # Arch tests with verbose output
```

**Test Reports:**
- Unit tests: `build/reports/test/`
- Architecture tests: `build/reports/test-architecture/`

## Project Overview

This is a **sample e-commerce application** demonstrating best practices for:

- **Domain-Driven Design (DDD)** - Strategic and tactical patterns
- **Hexagonal Architecture** - Ports and Adapters pattern
- **Onion Architecture** - Dependency inversion with layers pointing inward
- **Clean Architecture** - Framework-independent business logic

**Tech Stack:**
- Java 25
- Spring Boot 4.0.2
- Gradle 9.3.1
- Spring Modulith 2.0.3
- Spring AI 2.0.0-M2 (milestone)
- `dev.domaincentric:dca-building-blocks` — architectural markers (DDD tactical/strategic, hexagonal ports)
- `dev.domaincentric:dca-archunit` — the DCA governance rules (ArchUnit), run via JUnit 5
- Both come from Maven Central (`dca-building-blocks` 0.1.1, `dca-archunit` 0.2.0). Working on unreleased rules or markers: `./gradlew -PwithDcaJava <task>` makes `settings.gradle` include the sibling build `../dca-java` and substitute the coordinates
- JSpecify for nullability annotations

**Purpose:**
This project serves as a reference implementation showing how to properly structure an enterprise application using modern architectural patterns.

## Architecture Documentation

### Critical Rule: Always Update Architecture Documentation

**⚠️ IMPORTANT: After making ANY changes to the codebase, you MUST update the architecture documentation.**

### When to Update Documentation

Update architecture documentation **AND README.md** in the following scenarios:

1. **Changing package structure** ⚠️ - Update README.md, docs/architecture/README.md, and docs/architecture/package-structure.md
2. **Adding new bounded contexts** ⚠️ - Update README.md with new context description
3. **Adding new DDD patterns** (Aggregates, Entities, Value Objects, Services, Events, etc.)
4. **Modifying existing patterns** (changing interfaces, adding methods, refactoring)
5. **Adding new architectural layers or components**
6. **Modifying repository interfaces or implementations**
7. **Adding or changing domain events**
8. **Updating application services**
9. **Adding new adapters or ports**
10. **Any significant architectural decision**

**⚠️ CRITICAL: Package/structure changes affect multiple files:**
- `README.md` - Project overview and structure (lines 53-174)
- `docs/architecture/README.md` - Architecture quick reference (lines 24-51)
- `docs/architecture/package-structure.md` - Detailed package organization
- `docs/architecture/architecture-principles.md` - Pattern examples with file paths

### Documentation Structure

```
docs/
└── architecture/
    ├── architecture-principles.md    # Main architecture documentation
    ├── design-decisions.md           # ADRs (Architecture Decision Records)
    ├── bounded-contexts.md           # Context mapping and relationships
    └── patterns/                     # Pattern-specific guides
        ├── repository-pattern.md
        ├── domain-events.md
        └── aggregate-design.md

```

### How to Update Documentation

1. **Read the current documentation** to understand what exists
2. **Identify affected sections** based on your changes
3. **Update relevant sections** with:
   - New code examples
   - Updated diagrams (if applicable)
   - Revised explanations
   - Additional rules or best practices
4. **Add new sections** if introducing new patterns
5. **Update cross-references** to maintain consistency

### Documentation Update Checklist

After making code changes, verify:

- [ ] `README.md` reflects current project structure and features
- [ ] `docs/architecture/README.md` quick reference is current
- [ ] `docs/architecture/package-structure.md` matches actual packages
- [ ] `architecture-principles.md` reflects current patterns
- [ ] Code examples in documentation match actual implementation
- [ ] New patterns are documented with examples
- [ ] Rules and best practices are updated
- [ ] Package structure diagrams are current
- [ ] References to specific files/classes are accurate
- [ ] Documentation is concise - no bloat, duplicates, or unnecessary sections
- [ ] Links to related documentation instead of repeating information

### Example: Documenting a New Pattern

When adding a new DDD pattern:

```markdown
#### [Pattern Name]

[Brief description of what this pattern is and when to use it]

**Example: [Concrete Example from Codebase]**

```java
// Real code from the project
public class ExampleClass implements Pattern {
    // ...
}
```

**Rules:**
1. [Rule 1]
2. [Rule 2]
...

**Implementation:**
- Interface: `dev.domaincentric.sample.ecommerce.domain.model.ddd.Pattern`
- Example: `dev.domaincentric.sample.ecommerce.domain.model.product.ConcreteExample`
```

---

## Development Workflow

### Standard Development Process

1. **Understand the requirement**
   - Read existing code and documentation
   - Identify affected components
   - Plan the changes

2. **Make code changes**
   - Follow DDD patterns
   - Maintain architectural boundaries
   - Write clean, well-documented code

3. **Update documentation** ⚠️
   - This is NOT optional
   - Update `README.md` if structure or features changed
   - Update `docs/architecture/README.md` if package structure changed
   - Update `docs/architecture/package-structure.md` if package structure changed
   - Update `docs/architecture/architecture-principles.md` for pattern changes
   - Add examples from your actual changes
   - Update any affected diagrams or references

4. **Run tests**
   - `./gradlew build` - Compile and build
   - `./gradlew test-architecture` - Verify architecture rules
   - Ensure all tests pass

5. **Verify the application**
   - `./gradlew bootRun` - Start the application
   - Test the changes manually if needed

### Architecture Test Failures

If architecture tests fail:

1. **Understand the violation** - Read the error message carefully
2. **Determine if the change is correct**:
   - If the code violates architecture rules → Fix the code
   - If the rule is too strict → Discuss with the team before changing tests
3. **Never disable tests** without documenting why
4. **Update documentation** to reflect any architectural decisions

---

## Code Standards

### Domain-Driven Design Patterns

#### Aggregates
- Extend `BaseAggregateRoot<T, ID>`
- Enforce invariants within aggregate boundaries
- Raise domain events for state changes
- Reference other aggregates by ID only

#### Entities
- Implement `Entity<T, ID>`
- Have a unique identity
- Equality based on ID, not attributes

#### Value Objects
- Implement `Value` interface
- Prefer Java records; immutable final classes with their own `equals`/`hashCode` are allowed
- Include validation in constructor
- Equality based on attributes

#### Repositories
- Interface in application layer as output port (`application.shared.*`, see ADR-008)
- Extend `Repository<T, ID>` base interface
- Implementation in outgoing adapters (`adapter.outgoing.*`)
- Use domain language in method names

#### Domain Events
- Implement `DomainEvent` interface
- Named in past tense (e.g., `ProductPriceChanged`)
- Include `eventId` and `occurredOn` (no `version` field — integration events declare their version via `@IntegrationEventType`)
- Immutable (use records)

#### Domain Services
- Implement `DomainService` marker interface
- Stateless (only final fields)
- Framework-independent (no Spring annotations)

### Package Structure Rules

```
dev.domaincentric.sample.ecommerce
├── sharedkernel/             # Shared Kernel (cross-context)
│   ├── infrastructure/       # Sample-specific marker (AsyncInitialize); DCA markers come from dca-building-blocks
│   ├── domain/               # Shared value objects and specifications
│   └── adapter/outgoing/     # Shared adapters (e.g., SpringDomainEventPublisher)
├── {boundedcontext}/         # Each bounded context (product, cart, checkout, account, portal, inventory, pricing, backoffice)
│   ├── domain/               # Domain model (aggregates, entities, events)
│   ├── application/          # Use cases, ports, orchestration — flat ({usecase}/) or grouped by feature ({feature}/{usecase}/)
│   └── adapter/              # Incoming and outgoing adapters
│       ├── incoming/         # Controllers, event consumers, MCP tools
│       └── outgoing/         # Repository implementations
└── infrastructure/           # Global infrastructure (cross-cutting)
    ├── config/               # @Configuration classes
    ├── support/              # Framework support (processors)
    └── security/             # Security infrastructure
```

### API and Authorization

`/api/**` and `/mcp/**` are authenticated by an `Authorization: Bearer` header and by nothing else: no cookie
reaches them and none is issued, which is the only reason they are exempt from CSRF (ADR-035). Changing the
exemption and the cookie-free treatment apart is the mistake to watch for in review.

The filter chain says who *is* here, never what they may do: the JWT filter gives every request an
authentication, so `anyRequest().authenticated()` is satisfied by an anonymous visitor as well. **A guard goes
where its inputs are** (ADR-036) — the discriminator is whether the check needs the aggregate, not whether it
feels "business" or "technical":

- **Claims only** → the incoming adapter. `POST /api/products` and `GET /api/carts` require
  `IdentityProvider.Identity#hasRole(ROLE_STAFF)`. That is a property of the exposure: the same use case is
  legitimate for a console or batch job with no HTTP identity.
- **Ownership of a resource** → the use case, always, because no adapter may be the only thing standing between a
  caller and a stranger's data. The caller is part of the command (`GetCartByIdQuery(cartId, customerId)`,
  `CheckoutCartCommand(cartId, customerId)`, `StartCheckoutCommand(cartId, customerId)`) and the use case asks a
  scoped question — `ShoppingCartRepository#findByIdForCustomer`, not `findById` plus an `if`. The Cart's Open
  Host Service demands the customer for the same reason, so Checkout inherits the rule. `findById` stays for the
  system paths that act on nobody's behalf (`CompleteCart` from an integration event).
- **The refusal is rendered at the edge:** a cart that is not the caller's answers `404`, not `403` — a `403`
  would confirm the id exists.

Catalog reads are public. Resources and MCP tool providers depend on `*InputPort` interfaces, never on the
`*UseCase` classes.

### Dependency Rules

1. **Domain** → No dependencies (framework-independent)
2. **Application** → Depends on domain + infrastructure.api only
3. **Infrastructure** → Depends on domain
4. **Adapters** → Depend on application and domain
5. **Adapters** → Must NOT communicate directly with each other

---

## Testing Requirements

### Architecture Tests (ArchUnit)

Location: `src/test-architecture/java/dev/domaincentric/sample/ecommerce/`

The rules themselves live in the library `dev.domaincentric:dca-archunit` (114 rules in 10 sets, ids
`DCA-<SET>-<NNN>`: LAY, ONI, HEX, TAC, STR, MAP, ADV, USE, NAM, CYC). This project only *runs* them:

- `ArchitectureRulesTest` — extends `DcaArchitectureTest`, one dynamic test per rule, grouped into a
  container per rule set; excludes nothing. Its `additionalSelection()` shows the configuration a
  consuming project would use (`DcaRuleSelection`: scope, severity, exceptions, freeze baseline).
- `EcommerceLayout` — the base package and `DcaLayout` all three tests share.
- `ContextMapDocumentationTest` — renders `docs/architecture/context-map.md` via `ContextMapRenderer`
  and fails when the committed file was stale (fix: commit the regenerated file).
- `SpringModulithVerificationTest` — Spring Modulith module boundaries (sample-specific, not a DCA rule).

To switch a rule off, return `DcaRuleSelection.all().excluding("<id>", "<reason>")` from
`additionalSelection()` in `ArchitectureRulesTest` — the rule then shows up as an aborted test with
that reason — and record the decision in an ADR. The same is configurable without code in
`dca-archunit.properties` on the test class path; never override `selection()` itself, which would
replace that file instead of adding to it.
Rule changes belong in `dca-java`, not here.

**Run architecture tests:**
```bash
./gradlew test-architecture
```

### Unit Tests

- Test domain logic in isolation
- Mock external dependencies
- Focus on business rules and invariants

### Integration Tests

- Test application services with real repository implementations
- Verify event publishing and handling
- Test REST endpoints

---

## Documentation Requirements

### Code Documentation

**Default: don't write a comment — make the code say it.** `public` is not the same as "published":
most `public` members exist only because Java has no narrower modifier and Spring needs access.
JavaDoc that restates the signature is a comment smell and gets deleted on sight.

**JavaDoc is required for:**

1. **Published API** — types and methods other bounded contexts or external callers depend on:
   `api/` (Open Host Services), `events/` (integration events), ports
   (`*InputPort`, output ports in `application/shared/`). Document purpose, parameters, return
   values, exceptions, and domain events raised.
2. **Non-obvious rules** — an invariant, a constraint, a deliberate tradeoff, or a value whose
   number needs justifying. Write the *why*, not the *what*:
   `MAX_BYTE_LENGTH = 72 // bytes, not characters: BCrypt rejects input beyond 72 bytes`.
3. **Domain patterns** — aggregates, entities, value objects, domain events, domain services and
   specifications reference their DDD concept, so a reader knows which pattern applies.
4. **Architecture decisions** in code comments, with a link to the ADR where one exists.

**JavaDoc is not required for** self-evident accessors (`navigable()`, `found()`), records whose
component names already say it, obvious factory methods (`notFound()`), or internal use-case and
adapter plumbing. Adding it there costs a line to read and a line to keep true.

**A comment that has gone stale is worse than no comment.** When editing a file, verify its existing
comments still hold — especially claims about what is covered or handled elsewhere.

### Architecture Documentation

**Main Document:** `docs/architecture/architecture-principles.md`

This document must include:

1. **Overview** - Project purpose and patterns used
2. **Domain-Driven Design** section with:
   - Strategic Patterns (Bounded Contexts, Context Mapping)
   - Tactical Patterns (Aggregates, Entities, Value Objects, Repositories, etc.)
   - Code examples from the actual codebase
   - Rules and best practices
3. **Hexagonal Architecture** section
4. **Onion Architecture** section
5. **Package Structure** with visual representation
6. **Bounded Contexts** description
7. **Architectural Rules** enforced by ArchUnit

### Documentation Writing Style

**⚠️ CRITICAL: All documentation must be concise and straight to the point.**

When creating or updating documentation:

1. **Go straight to the point** - No lengthy introductions or marketing-style content
2. **Only necessary information** - Remove bloat, verbose explanations, and redundant sections
3. **No duplicates** - Never repeat information already documented elsewhere
4. **Link instead of repeating** - Reference other documentation files for details covered there
5. **Compact format** - Use concise examples, skip excessive "benefits" sections
6. **No gibberish** - Remove filler content, excessive summaries, and author metadata

**Example of What to Avoid:**

❌ **Bloated Documentation:**
- Long "Table of Contents" for short documents
- "Overview" sections repeating what's already clear
- Multiple "Summary" or "Key Takeaways" sections
- Verbose "Benefits" and "Why Use This" sections
- Redundant examples showing the same concept
- Author information, version metadata (unless critical)

✅ **Concise Documentation:**
- Direct topic headings
- One clear example per concept
- References to related docs: "See [architecture-principles.md](architecture-principles.md) for DDD patterns"
- Minimal but complete - all necessary info, nothing more

**Template for Integration Documentation:**

```markdown
# [Technology] Integration

[One-line description]

## Dependencies
[Build configuration]

## Configuration
[Configuration code with file paths]

## Usage Example
[One clear example]

## Quick Reference
[Essential syntax/commands]

## Related Documentation
- [Link to related doc 1]
- [Link to related doc 2]
```

### Design Decisions

For significant architectural decisions, create an Architecture Decision Record (ADR) in `docs/architecture/design-decisions.md`:

```markdown
## ADR-XXX: [Decision Title]

**Date:** YYYY-MM-DD

**Status:** Accepted | Proposed | Deprecated | Superseded

**Context:**
[What is the issue that we're seeing that is motivating this decision?]

**Decision:**
[What is the change that we're proposing and/or doing?]

**Consequences:**
[What becomes easier or more difficult to do because of this change?]
```

---

## Best Practices

### Domain Modeling

1. **Use Ubiquitous Language** - Names in code match business terminology
2. **Enforce Invariants** - Aggregates maintain consistency
3. **Raise Domain Events** - Capture important business occurrences
4. **Reference by ID** - Aggregates reference each other by identity only
5. **Keep Aggregates Small** - Focus on transactional consistency boundaries

### Event-Driven Design

1. **Events are Immutable** - Use records or final classes
2. **Events Capture Facts** - Named in past tense
3. **Events Enable Decoupling** - Bounded contexts coordinate via events
4. **Events Support Audit** - Include timestamp and event ID
5. **Publish After Persistence** - Only publish events for successfully saved aggregates

### Layered Architecture

1. **Dependencies Point Inward** - Toward the domain core
2. **Framework-Free Domain** - No Spring, JPA, or infrastructure in domain
3. **Ports Define Contracts** - Interfaces in application layer (ports), implementations in adapters
4. **Application Services Orchestrate** - Thin coordination layer
5. **Adapters Are Replaceable** - Easy to swap implementations

---

## Common Tasks

### Adding a New Aggregate

1. Create aggregate root class extending `BaseAggregateRoot`
2. Create value objects for properties
3. Create entity classes for aggregate entities
4. Create repository interface in application layer (output port)
5. Implement repository in outgoing adapter
6. Create domain events for important state changes
7. Create factory if creation is complex
8. Update application service to use aggregate
9. **Update `architecture-principles.md`** with new aggregate example
10. Run architecture tests

### Adding a New Domain Event

1. Create event record implementing `DomainEvent`
2. Include eventId, occurredOn, and domain-specific data (no version field)
3. Add static factory method (e.g., `now()`)
4. Raise event in aggregate when state changes
5. Publish events in application service after save
6. Create event listener if needed
7. **Update `architecture-principles.md`** with event example
8. Test event publishing

### Adding a New Repository Method

1. Add method to repository interface in application layer (output port)
2. Use domain language in method name
3. Implement method in repository implementation
4. Update application service to use new method
5. **Update `architecture-principles.md`** if pattern changes
6. Add tests

---

## Anti-Patterns to Avoid

### Domain Layer

❌ **Don't:**
- Use Spring annotations in domain (@Service, @Component, @Entity)
- Use JPA annotations in domain (@Entity, @Table, @Column)
- Reference infrastructure classes from domain
- Create anemic domain models (getters/setters only)
- Have aggregates directly reference other aggregates

✅ **Do:**
- Keep domain framework-independent
- Put business logic in domain objects
- Reference other aggregates by ID
- Enforce invariants in domain
- Use value objects for concepts

### Repository Pattern

❌ **Don't:**
- Create repositories for entities (only for aggregate roots)
- Use generic CRUD names unless appropriate for domain
- Put business logic in repository
- Return infrastructure objects from repository

✅ **Do:**
- Create one repository per aggregate root
- Use ubiquitous language in method names
- Return domain objects
- Keep repositories simple (data access only)

### Application Services

❌ **Don't:**
- Put business logic in application services
- Have application services call other application services
- Make application services stateful
- Access adapters from application services

✅ **Do:**
- Keep application services thin (orchestration only)
- Delegate business logic to domain
- Publish domain events after save
- Only use infrastructure.api (not implementations)

---

## Troubleshooting

### Architecture Test Failures

**Problem:** `Domain must not have dependencies on Infrastructure`
- **Solution:** Remove infrastructure imports from domain layer

**Problem:** `Application Services must only use infrastructure.api (not infrastructure implementations)`
- **Solution:** Move class from `infrastructure.*` to `infrastructure.api.*`

**Problem:** `Entities must have an ID field`
- **Solution:** Add an `id` field to entity class

**Problem:** `Aggregate Roots must not have fields with other Aggregate Root types`
- **Solution:** Reference other aggregates by ID, not by direct reference

### Build Failures

**Problem:** Compilation errors after refactoring
- **Solution:** Update all references, check imports

**Problem:** Spring can't find bean
- **Solution:** Check @Component/@Service annotations, verify package scanning

---

## Summary

### Key Principles

1. ✅ **Always update architecture documentation** after code changes
2. ✅ **Keep documentation concise** - straight to the point, no bloat or duplicates
3. ✅ Keep domain layer framework-independent
4. ✅ Follow DDD patterns and principles
5. ✅ Respect architectural boundaries
6. ✅ Run architecture tests before committing
7. ✅ Use ubiquitous language throughout
8. ✅ Document design decisions
9. ✅ Keep aggregates small and focused
10. ✅ Raise domain events for important occurrences
11. ✅ Maintain clean, well-documented code

### Documentation Workflow

```
Code Change → Update architecture-principles.md → Run Tests → Commit
      ↑                                                         |
      └─────────────────────────────────────────────────────────┘
                    (Documentation is part of the change!)
```

---

**Remember:** Good architecture is about communication. Keep the documentation up-to-date so the next person (or AI) working on this code understands the decisions and patterns used.
