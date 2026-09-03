# Architectural Evolution

This document traces the evolution of the dca-ecommerce-sample-java across 6 branches, from basic Hexagonal Architecture to Spring Modulith.

## Timeline

```
Oct 27 ─── Nov 13, 2025    hexagonal-architecture     2 contexts, flat services
Oct 29 ─── Nov 13, 2025    usecase-example             UseCase<I,O> pattern
Nov 7  ─── Nov 13, 2025    dca-example                 Top-level contexts, ports
Nov 12 ─── Nov 13, 2025    domain-centric-arch-v2      Experimental restructure
Nov 13, 2025 ─── Feb 2026  dca-evolved                 7 contexts, full patterns
Feb 22 ─── Feb 23, 2026    spring-modulith             Module boundaries, 8 contexts
```

---

## Branch 1: hexagonal-architecture

**Period:** Oct 27 – Nov 13, 2025 (16 commits) | **Java 21, Spring Boot 3.5.6, Gradle 9.1**

The foundation. Two bounded contexts (Product, Cart) with a flat application layer and `portadapter/` as the adapter package.

### Package Structure

```
dev.domaincentric.sample.ecommerce/
├── application/
│   ├── ProductApplicationService          # 8 methods (createProduct, getAll, getById, ...)
│   └── ShoppingCartApplicationService     # 9 methods (createCart, addItem, checkout, ...)
├── domain/model/
│   ├── shared/ddd/                        # AggregateRoot, Entity, Value, Repository, ...
│   ├── product/                           # Product, ProductRepository, PricingService, ...
│   └── cart/                              # ShoppingCart, CartItem, ShoppingCartRepository, ...
├── portadapter/
│   ├── incoming/
│   │   ├── api/product/                   # ProductResource (REST)
│   │   ├── api/cart/                      # ShoppingCartResource (REST)
│   │   ├── mcp/                           # ProductCatalogMcpTools
│   │   └── web/product/                   # ProductPageController
│   └── outgoing/
│       ├── product/                       # InMemoryProductRepository
│       └── cart/                          # InMemoryShoppingCartRepository
└── infrastructure/
    ├── api/                               # DomainEventPublisher (interface)
    ├── config/                            # Spring configurations
    └── event/                             # CartEventListener, ProductEventListener
```

### Key Characteristics

- **Application layer:** Single `*ApplicationService` class per context with multiple methods — no Interface Segregation
- **Domain model location:** Contexts nested inside `domain.model/` rather than at the top level
- **Adapter package:** Named `portadapter/` (later renamed to `adapter/`)
- **DDD markers:** In `domain.model.shared.ddd` (later moved to `sharedkernel.marker`)
- **Events:** Domain events (ProductCreated, CartCheckedOut) published through `DomainEventPublisher` in `infrastructure.api`
- **ArchUnit suites:** 10 test classes enforcing architectural rules from day one
- **ADRs:** 16 design decisions documented (ADR-001 through ADR-016)

### What Worked

Established the core hexagonal architecture with clear separation of domain, application, and adapter layers. ArchUnit tests from the start kept the architecture honest.

### What Needed Improvement

Fat application services violated Interface Segregation. Contexts inside `domain.model/` made the architecture harder to navigate. The `portadapter/` naming was non-standard.

---

## Branch 2: usecase-example

**Period:** Oct 29 – Nov 13, 2025 | **Java 21, Spring Boot 3.5.6**

Introduced the `UseCase<I, O>` interface pattern — one class per use case instead of fat application services.

### What Changed

The flat `*ApplicationService` was replaced by individual use case classes:

```
dev.domaincentric.sample.ecommerce/
├── application/
│   ├── UseCase.java                       # Base interface: UseCase<Input, Output>
│   ├── product/
│   │   ├── CreateProductUseCase           + CreateProductInput + CreateProductOutput
│   │   ├── GetAllProductsUseCase          + GetAllProductsInput + GetAllProductsOutput
│   │   ├── GetProductByIdUseCase          + GetProductByIdInput + GetProductByIdOutput
│   │   └── UpdateProductPriceUseCase      + UpdateProductPriceInput + UpdateProductPriceOutput
│   └── cart/
│       ├── CreateCartUseCase              + CreateCartInput + CreateCartOutput
│       ├── GetCartByIdUseCase             + GetCartByIdInput + GetCartByIdOutput
│       ├── AddItemToCartUseCase           + AddItemToCartInput + AddItemToCartOutput
│       └── CheckoutCartUseCase            + CheckoutCartInput + CheckoutCartOutput
├── domain/model/  (unchanged)
├── portadapter/   (unchanged)
└── infrastructure/ (unchanged)
```

### The Pattern

```java
public interface UseCase<I, O> {
    O execute(I input);
}

public class CreateProductUseCase implements UseCase<CreateProductInput, CreateProductOutput> {
    @Override
    public CreateProductOutput execute(CreateProductInput input) { ... }
}
```

### Key Differences from hexagonal-architecture

| Aspect | hexagonal-architecture | usecase-example |
|--------|----------------------|-----------------|
| Application unit | `*ApplicationService` (many methods) | `*UseCase` (one method) |
| Input/Output | Method parameters | Dedicated `*Input` / `*Output` records |
| ISP compliance | No | Yes — each use case is a separate interface |
| File count | 62 | 85 |

### What Remained Unchanged

Domain model still inside `domain.model/`, adapters still in `portadapter/`, no InputPort/OutputPort distinction yet.

---

## Branch 3: dca-example

**Period:** Nov 7 – Nov 13, 2025 | **Java 21, Spring Boot 3.5.6, Spring Cloud 2025.0.0**

The defining structural shift: bounded contexts moved to the top level, each with their own `domain/`, `application/`, and `adapter/` sub-packages. Introduced explicit Input Ports, Output Ports, and cross-context integration events.

### Package Structure

```
dev.domaincentric.sample.ecommerce/
├── product/
│   ├── domain/
│   │   └── model/                         # Product, ProductRepository, events, ...
│   ├── application/
│   │   ├── port/in/                       # CreateProductInputPort, GetProductByIdInputPort, ...
│   │   ├── port/out/                      # ProductRepository
│   │   └── usecase/
│   │       ├── createproduct/             # CreateProductUseCase + CreateProductCommand + CreateProductResponse
│   │       ├── getproductbyid/            # GetProductByIdUseCase + GetProductByIdCommand + ...
│   │       └── ...
│   └── adapter/
│       ├── incoming/                      # ProductResource, ProductPageController, MCP tools
│       └── outgoing/                      # InMemoryProductRepository
├── cart/
│   ├── domain/ → application/ → adapter/  # Same structure
├── portal/
│   └── adapter/incoming/web/              # HomeController (new context)
├── sharedkernel/
│   └── marker/                            # DDD marker interfaces
└── infrastructure/
```

### Key Innovations

1. **Top-level contexts:** Each bounded context (`product/`, `cart/`, `portal/`) is a top-level package with its own layered structure
2. **Explicit ports:** `port/in/` for InputPorts, `port/out/` for OutputPorts
3. **Use case wrapper:** `usecase/{name}/` directories containing UseCase + Command + Response
4. **Naming shift:** Input `*Command` (writes) / `*Query` (reads), Output `*Response`
5. **Integration events:** `IntegrationEvent` pattern for cross-context communication
6. **Portal context:** Dedicated context for the home page (3rd bounded context)

### Key Differences from usecase-example

| Aspect | usecase-example | dca-example |
|--------|----------------|-------------|
| Context location | `domain.model.{context}` | `{context}/` (top-level) |
| Ports | Implicit | Explicit `port/in/` and `port/out/` |
| Use case location | `application/{context}/` | `{context}/application/usecase/{name}/` |
| Input naming | `*Input` | `*Command` / `*Query` |
| Output naming | `*Output` | `*Response` |
| Cross-context | Direct calls | Integration events |
| Contexts | 2 | 3 (+ portal) |
| File count | 85 | 138 |

### What Worked

True bounded context isolation with independent layers. The port distinction clarified the hexagonal architecture.

### What Felt Heavy

Three levels of nesting (`application/port/in/`, `application/usecase/{name}/`) added navigational overhead. The `usecase/` wrapper directory felt unnecessary.

---

## Branch 4: domain-centric-architecture-v2

**Period:** Nov 12–13, 2025 (2 days) | **Java 21, Spring Boot 3.5.6**

A short-lived experimental branch that answered one question: can we flatten the application layer without losing clarity?

### The Experiment

Removed the `port/in/`, `port/out/`, and `usecase/` wrapper directories:

```
cart/application/
├── additemtocart/
│   ├── AddItemToCartInputPort.java        # InputPort lives WITH its use case
│   ├── AddItemToCartUseCase.java
│   ├── AddItemToCartCommand.java
│   └── AddItemToCartResponse.java
├── checkoutcart/
│   ├── CheckoutCartInputPort.java
│   ├── CheckoutCartUseCase.java
│   ├── CheckoutCartCommand.java
│   └── CheckoutCartResponse.java
└── shared/
    └── ShoppingCartRepository.java        # OutputPorts collected here
```

### What Changed from dca-example

| Removed | Replaced By |
|---------|-------------|
| `application/port/in/` | InputPort moved into use case folder |
| `application/port/out/` | OutputPorts moved to `application/shared/` |
| `application/usecase/` wrapper | Use case folders directly under `application/` |

### Why It Stopped

The experiment succeeded. The flat structure was cleaner and easier to navigate. Development stopped after 2 days because the pattern was validated — there was no need to continue building on this branch. The pattern was adopted wholesale by `dca-evolved`.

---

## Branch 5: dca-evolved

**Period:** Nov 13, 2025 – Feb 23, 2026 (3+ months) | **Java 25, Spring Boot 4.0.2, Gradle 9.3.1**

The main development branch. Adopted the flat application structure from `domain-centric-architecture-v2` and expanded from 3 to 7 bounded contexts with advanced DDD patterns.

### Package Structure (Stabilized)

```
dev.domaincentric.sample.ecommerce/
├── product/
│   ├── domain/model/                      # Product, value objects, events, specifications
│   ├── application/
│   │   ├── createproduct/                 # CreateProductInputPort, CreateProductUseCase, ...Command, ...Result
│   │   ├── getproductbyid/               # ...
│   │   ├── updateproductprice/           # ...
│   │   └── shared/                        # ProductRepository, ProductDataPort
│   ├── api/                               # ProductCatalogService (Open Host Service, published in-process)
│   ├── events/                            # ProductCreatedEvent, ... (published integration events)
│   └── adapter/
│       ├── incoming/
│       │   ├── api/                       # ProductResource (REST)
│       │   ├── web/                       # ProductPageController
│       │   └── mcp/                       # ProductCatalogMcpToolProvider
│       └── outgoing/
│           └── persistence/               # InMemoryProductRepository
├── cart/
│   ├── domain/model/                      # ShoppingCart, CartItem, CartArticle (enriched)
│   ├── application/
│   │   ├── additemtocart/
│   │   ├── getcartbyid/
│   │   ├── getenrichedcart/              # EnrichedCart with ArticleDataPort
│   │   └── shared/                        # ShoppingCartRepository, ArticleDataPort
│   └── adapter/...
├── checkout/                              # Multi-step checkout flow
├── account/                               # Registration, authentication (JWT)
├── inventory/                             # Stock level management
├── pricing/                               # Product pricing
├── portal/                                # Home page, navigation
├── sharedkernel/
│   ├── marker/                            # tactical/, strategic/, port/in/, port/out/
│   ├── domain/model/                      # Money, ProductId, UserId, ...
│   └── adapter/outgoing/                  # SpringDomainEventPublisher
└── infrastructure/
    ├── config/                            # Global configurations
    ├── support/                           # Framework support
    └── security/                          # Security infrastructure
```

### Use Case Pattern (Final Form)

```java
// InputPort — extends UseCase<Command, Result>
public interface CreateProductInputPort extends UseCase<CreateProductCommand, CreateProductResult> {}

// UseCase — implements InputPort
public class CreateProductUseCase implements CreateProductInputPort {
    @Override
    public CreateProductResult execute(CreateProductCommand command) { ... }
}

// Command — input record
public record CreateProductCommand(String name, Money price) {}

// Result — output record (renamed from *Response per ADR-020)
public record CreateProductResult(ProductId id) {}
```

### Key Innovations

1. **Enriched Domain Models:** `CartArticle` — a Value Object combining cross-context data (product name, price, stock) into the domain model. Populated via `ArticleDataPort`.

2. **Open Host Services:** `ProductCatalogService`, `PricingService`, `InventoryService` — outgoing adapters exposing bounded context data to other contexts.

3. **ViewModel Pattern:** Records in `adapter/incoming/web/` for view rendering, separate from domain models and DTOs.

4. **Result Naming (ADR-020):** Renamed `*Response` to `*Result` across all use cases — `*Response` implies HTTP, `*Result` is transport-neutral.

5. **Tech Upgrades:** Java 21→25, Spring Boot 3.5.6→4.0.2, Spring Framework 7, Jakarta EE 11, Hibernate 7, JUnit 6, Spock 2.4

### Growth

| Metric | dca-example | dca-evolved |
|--------|-------------|-------------|
| Bounded contexts | 3 | 7 |
| Java files | 138 | 402 |
| ADRs | 16 | 23 |
| ArchUnit suites | 10 | 10 |

### ADRs Added (ADR-017 through ADR-023)

- **ADR-020:** Result naming convention (`*Result` not `*Response`)
- **ADR-021:** Enriched Domain Model pattern
- **ADR-022:** ViewModel Pattern
- **ADR-023:** Optional Results for queries

---

## Branch 6: spring-modulith

**Period:** Feb 22–23, 2026 (7 commits on top of dca-evolved) | **+ Spring Modulith 2.0.3**

Added formal module boundaries with Spring Modulith, solving the cross-context event coupling problem with the Interface Inversion Pattern.

### What Changed

1. **`@ApplicationModule`** annotations on every bounded context's `package-info.java`:

```java
// cart/package-info.java
@ApplicationModule(allowedDependencies = {
    "sharedkernel",
    "infrastructure",
    "product :: api",
    "pricing :: api",
    "inventory :: api"
})
package dev.domaincentric.sample.ecommerce.cart;
```

2. **Named Interfaces** (`@NamedInterface`) exposing controlled APIs:

```
product :: api     → ProductCatalogService
product :: events  → ProductCreatedEvent
cart :: api        → CartService
cart :: events     → CartCompletionTrigger
inventory :: api   → InventoryService
inventory :: events → StockReductionTrigger
pricing :: api     → PricingService
checkout :: events → trigger interfaces
```

3. **Interface Inversion Pattern (ADR-024)** — the key innovation:

```
Problem:  checkout publishes CheckoutConfirmedEvent
          cart needs to listen → cart depends on checkout
          inventory needs to listen → inventory depends on checkout
          But checkout already depends on cart → cycle!

Solution: Consumers define trigger interfaces in their own events package.
          The event implements all trigger interfaces.
```

```java
// Defined by cart (the consumer)
public interface CartCompletionTrigger { String cartId(); }

// Defined by inventory (the consumer)
public interface StockReductionTrigger { List<OrderItem> orderLineItems(); }

// Published by checkout — implements both consumer interfaces
public record CheckoutConfirmedEvent(...)
    implements IntegrationEvent, CartCompletionTrigger, StockReductionTrigger {}

// Cart listens to its OWN interface (no checkout dependency)
@ApplicationModuleListener
void on(CartCompletionTrigger event) { cartRepository.complete(event.cartId()); }
```

4. **`@ApplicationModuleListener`** replaces `@EventListener` for cross-module events — guarantees separate transactions (one aggregate change per transaction).

5. **JDBC Event Publication Log** — persisted event outbox via `spring-modulith-starter-jdbc` (H2).

6. **Backoffice Context** — 8th bounded context, provides a UI to view the event publication log.

7. **`SpringModulithVerificationTest`** — ArchUnit suite #11, verifies module structure:

```groovy
def "Application module structure should be valid"() {
    expect:
    ApplicationModules.of("dev.domaincentric.sample.ecommerce").verify()
}
```

### Structural Moves

- Security infrastructure: `infrastructure.security` → `account.adapter.outgoing.security` (belongs to account context)
- Domain configuration: global `infrastructure.config` → per-context `{context}.infrastructure`

---

## Evolution Summary

### Package Structure Progression

| Branch | Context Location | Application Pattern | Adapter Package |
|--------|-----------------|---------------------|-----------------|
| hexagonal-architecture | `domain.model.{ctx}` | Flat `*ApplicationService` | `portadapter/` |
| usecase-example | `domain.model.{ctx}` | `UseCase<I,O>` per operation | `portadapter/` |
| dca-example | `{ctx}/` (top-level) | `port/in/` + `port/out/` + `usecase/{name}/` | `adapter/` |
| domain-centric-arch-v2 | `{ctx}/` (top-level) | Flat `{name}/` + `shared/` | `adapter/` |
| dca-evolved | `{ctx}/` (top-level) | Flat `{name}/` + `shared/` (refined) | `adapter/` |
| spring-modulith | `{ctx}/` (top-level) | Same + `@ApplicationModule` | `adapter/` |

### Naming Convention Evolution

| Branch | Input | Output | Port |
|--------|-------|--------|------|
| hexagonal-architecture | Method params | Return types | None |
| usecase-example | `*Input` | `*Output` | `UseCase<I,O>` |
| dca-example | `*Command` / `*Query` | `*Response` | `*InputPort` / `*OutputPort` |
| domain-centric-arch-v2 | `*Command` / `*Query` | `*Response` | `*InputPort` → use case folder |
| dca-evolved | `*Command` / `*Query` | `*Result` (ADR-020) | `*InputPort extends UseCase` |
| spring-modulith | Same as dca-evolved | Same | Same + `@NamedInterface` |

### Scale Progression

| Branch | Contexts | Java Files | ADRs | ArchUnit Suites |
|--------|----------|-----------|------|-----------------|
| hexagonal-architecture | 2 | 62 | 16 | 10 |
| usecase-example | 2 | 85 | 16 | 10 |
| dca-example | 3 | 138 | 16 | 10 |
| domain-centric-arch-v2 | 3 | 140 | 16 | 10 |
| dca-evolved | 7 | 402 | 23 | 10 |
| spring-modulith | 8 | ~410 | 24 | 11 |

### Tech Stack Progression

| Branch | Java | Spring Boot | Key Addition |
|--------|------|------------|--------------|
| hexagonal-architecture | 21 | 3.5.6 | ArchUnit, Spring AI |
| usecase-example | 21 | 3.5.6 | UseCase interface |
| dca-example | 21 | 3.5.6 | Spring Cloud, IntegrationEvent |
| domain-centric-arch-v2 | 21 | 3.5.6 | — |
| dca-evolved | 25 | 4.0.2 | Spring Framework 7, JUnit 6, JJWT |
| spring-modulith | 25 | 4.0.2 | Spring Modulith 2.0.3 |

---

## Key Lessons

**1. Start with the right granularity, refine later.**
The hexagonal-architecture branch proved the layered approach works. The usecase-example branch decomposed fat services. Each step was additive, not rewrite.

**2. Package structure is a communication tool.**
Moving contexts to the top level (dca-example) made bounded context boundaries immediately visible in the IDE. Developers see `product/`, `cart/`, `checkout/` — not `domain.model.product`, `application.product`.

**3. Less nesting is better.**
The dca-example introduced `port/in/`, `port/out/`, and `usecase/` wrappers. The domain-centric-architecture-v2 experiment proved these wrappers added navigational cost without enough benefit. Flat won.

**4. Naming matters.**
`*Input`/`*Output` → `*Command`/`*Response` → `*Command`/`*Result`. Each rename carried meaning: `*Result` is transport-neutral, `*Response` implies HTTP.

**5. Events need architectural support.**
Simple `@EventListener` worked for single-context events but created dependency cycles across contexts. Spring Modulith's `@ApplicationModuleListener` + Interface Inversion Pattern solved this cleanly.

**6. Validate experiments quickly.**
The domain-centric-architecture-v2 branch lasted 2 days and 4 commits. It answered its question (can we flatten the application layer?) and stopped. The pattern was adopted by dca-evolved without carrying forward experimental baggage.

**7. ArchUnit tests compound in value.**
Starting with 10 test suites in the first branch meant every subsequent refactoring was verified automatically. By the spring-modulith branch, 11 suites guard the entire architecture.
