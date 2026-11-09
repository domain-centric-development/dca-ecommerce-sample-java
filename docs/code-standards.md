# Code standards in this sample

How code in this repository is written: the conventions, the practices worth keeping and the
patterns that get rejected in review. `AGENTS.md` carries the short version and points here; the
patterns themselves are the guide's, this file is how they look in *this* code base.

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
├── sharedkernel/             # Shared Kernel (cross-context); DCA markers come from dca-building-blocks
│   ├── domain/               # Shared value objects and specifications
│   └── application/shared/   # IdentityProvider (the DomainEventPublisher/TransactionBoundary impls come from dca-spring)
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
  the staff role — `@PreAuthorize("hasRole('STAFF')")`, the code of the shared kernel's `Role.STAFF`. That is a
  property of the exposure: the same use case is legitimate for a console or batch job with no HTTP identity.
- **Ownership of a resource** → the use case, always, because no adapter may be the only thing standing between a
  caller and a stranger's data. The caller is part of the command (`GetCartByIdQuery(cartId, customerId)`,
  `CheckoutCartCommand(cartId, customerId)`, `StartCheckoutCommand(cartId, customerId)`) and the use case asks a
  scoped question — `ShoppingCartRepository#findByIdForCustomer`, not `findById` plus an `if`. The Cart's Open
  Host Service demands the customer for the same reason, so Checkout inherits the rule. `ShoppingCart` and
  `CheckoutSession` implement `Owned<CustomerId>`, and `DCA-TAC-026` holds their repositories to the scoped lookup.
  `CompleteCart`, run from an integration event, loads through the owner as well: the trigger carries the customer.
  `findById` stays only for system paths that hold no owner to name.
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
## Best Practices

### Domain Modeling

1. **Use Ubiquitous Language** - Names in code match business terminology
2. **Enforce Invariants** - Aggregates maintain consistency
3. **Raise Domain Events** - Capture important business occurrences
4. **Reference by ID** - Aggregates reference each other by identity only
5. **Keep Aggregates Small** - Focus on transactional consistency boundaries
6. **Name Failures by the Broken Rule** - Domain and port exceptions carry the rule in the domain's words, with the suffix `Exception` (`CartNotModifiableException`) — the method's `failure_suffix: Exception`; a refusal case of a use-case result carries the same words without the suffix (`CartNotFound`, `DuplicateSku`)

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

### Refusals (ADR-047)

This shop returns its expected refusals; it does not throw them.

❌ **Don't:**
- Throw a `UseCaseException` for a refusal a caller answers — not found, not the caller's, a precondition, a
  uniqueness the store sees
- Return a refusal as a flag, an `Optional` or an outcome enum beside the success data
- Write before every refusal is decided ("decide, then write") — the transaction net rolls a returned `Refusal`
  back, but it is the net, not the design
- Ignore the result of a use case that can refuse — in production code or in a test helper
- Catch `PortException`, `InputPortException` or `OutputPortException` generically in an adapter
- Put a `default` in a switch over a use-case result

✅ **Do:**
- Return a sealed `<Op>Result`: the success case named for what happened, one record per refusal, named like the
  failure without `Exception`, carrying ids and figures, never words
- Mark every refusal case with `Refusal` (`DCA-ERR-008`); a use case without a refusal keeps a plain record
- Catch a domain refusal (`DomainException` subtype) and a failure an output port declares (`OutputPortException`
  subtype) by its own name, once, in the use case, and return its case
- Switch exhaustively in the incoming adapter and take the words from the context's one place for them
  (`*Problems`, the page's sentence); keep a `catch` only for what is still thrown
- Turn a refusal into an `IllegalStateException` where nobody can answer it (an event consumer, an open host
  service, start-up data)
- In tests: assert the returned case; a stub of an input port returns the case; a setup call asserts the success
  case

---
