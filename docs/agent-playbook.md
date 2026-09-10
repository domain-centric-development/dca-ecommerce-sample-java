# Working in this sample: documentation duties, recurring tasks, troubleshooting

The detail behind `AGENTS.md`: which document a change pulls along, what the recurring tasks look
like step by step, what to check when something fails, and the shared semantics both samples are
held to.

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
    ├── adr/                          # ADRs (Architecture Decision Records), README.md is the index
    ├── context-map.md                # Generated context map (architecture tests write it)
    └── *.md                          # Topic guides (transaction-management, cross-context-integration-events, …)
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

A domain term such as `PortfolioManager` is valid; the remaining technical suffix
restrictions still apply. Operation implementations are discovered by InputPort
assignability or the configured use-case suffix. Optional organisational segments
are configured with `withOperationContainers(...)` / `WithOperationContainers(...)`
and removed before measuring flat/grouped operation depth. Supporting subfolders do
not define operations. One context must still use one depth. A Repository or Store
used by one use case may live with it; `application/shared` is the reuse default.

WP-34 policy: use-case stereotypes are optional; configuration registration is equally valid.
NAM-002 is a non-failing Java diagnostic, not a wiring guarantee. Outgoing adapters may
reuse global/own infrastructure. Domain metadata rules classify configured roles on
types and members (including composed metadata), allow unclassified metadata, and assign
exclusive ownership to ADV-004/011/015/018 before ONI-003.
## Shared semantics since WP-39

`../dca-sample-specification/` is the semantic authority; read its CONTRIBUTING.md, vectors and checkout-lifecycle.md before
business changes. The user owns semantics. The specification is **unpublished and not part of the build** (decided
2026-09-10): nothing is downloaded, no revision is pinned, and a plain checkout builds without it. The specification tests
run only with `-Pspecification.path=../dca-sample-specification` and are skipped otherwise; no vector may lack a test
adapter. Update both samples' adapters, schema compatibility records and glossaries together.

An explicit checkout action captures immutable positions, quantities and prices into a session. Cart edits do not
create or mutate sessions. A new action supersedes the previous OPEN/Active session; confirmed/completed orders remain.
Confirmation and replacement serialize through the same repository operation, including transaction completion.
Superseded confirmation has no completion effect. Abandonment/expiry closes only an open session and leaves cart contents.

Cart reconciliation intersects purchased unit intervals with the current stable position id. Later additions (also of
the same product), removed/re-added positions and other contents survive. Replay and overlapping completed snapshots
cannot remove a unit twice. JDBC/JPA cart persistence preserves the interval allocation watermark; in-memory persistence
retains the same domain state. Legacy CheckedOut/Completed cart statuses remain readable, but snapshot checkout leaves
an active cart editable and never completes the whole cart.

Confirmation retrieves current price/availability/stock facts before its local transaction. Pure domain services consume
immutable line/fact snapshots. Any changed price or shortage reports affected lines and leaves state, totals and events
unchanged. The buyer explicitly starts a fresh checkout against the new prices. Success stores the recomputed total and
publishes the same total; there is no no-argument confirmation path. Local in-memory repository serialization is not a
claim of durable distributed transactions or universal rollback of unenlisted resources.
