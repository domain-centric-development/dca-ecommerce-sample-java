# Architecture Principles

This document describes the architectural patterns and principles used in the AI Architecture Sample Project.

## Table of Contents

1. [Overview](#overview)
2. [Domain-Driven Design (DDD)](#domain-driven-design-ddd)
   - [Tactical Patterns](#tactical-patterns)
     - [Enriched Domain Model Pattern](#enriched-domain-model-pattern)
     - [Interface Inversion for Cross-Module Events](#interface-inversion-for-cross-module-events)
3. [Clean Architecture (Use Cases)](#clean-architecture-use-cases)
4. [Hexagonal Architecture](#hexagonal-architecture)
   - [Advanced Adapter Patterns](#advanced-adapter-patterns)
     - [Composite Adapter Pattern](#composite-adapter-pattern)
     - [Enriched Read Model Pattern](#enriched-read-model-pattern)
     - [Factory for Cross-Context Assembly](#factory-for-cross-context-assembly)
5. [Onion Architecture](#onion-architecture)
6. [Layered Architecture](#layered-architecture)
7. [Package Structure](#package-structure)
8. [Bounded Contexts](#bounded-contexts)
9. [Architectural Rules](#architectural-rules)

---

## Overview

This sample project demonstrates a modern, maintainable architecture for enterprise applications using three complementary architectural patterns:

- **Domain-Driven Design (DDD)**: Focus on the core domain and domain logic
- **Clean Architecture**: Use case driven approach with explicit input/output models
- **Hexagonal Architecture**: Separation of business logic from technical concerns (Ports & Adapters)
- **Onion Architecture**: Dependencies flow inward toward the domain core

These patterns work together to create a flexible, testable, and maintainable codebase that can evolve with changing business requirements.

---

## Domain-Driven Design (DDD)

Domain-Driven Design is an approach to software development that centers the development on programming a domain model that has a rich understanding of the processes and rules of the domain.

### Strategic Patterns

#### Bounded Contexts

A bounded context is an explicit boundary within which a domain model is defined and applicable. Our e-commerce application has two bounded contexts plus a shared kernel:

1. **Shared Kernel** (`dev.domaincentric.sample.ecommerce.sharedkernel.domain.model`)
   - Small, carefully curated domain model shared across contexts
   - Value Objects: `Money`, `ProductId`, `Price`
   - **Pattern**: Shared Kernel (Eric Evans, DDD Chapter 14)
   - **Trade-off**: Creates coupling but ensures consistency for universal concepts

2. **Product Catalog Context** (`dev.domaincentric.sample.ecommerce.product.domain.model`)
   - Manages the product catalogue; pricing and stock live in their own contexts
   - Aggregate Root: `Product`
   - Value Objects: `SKU`, `ProductName`, `ProductDescription`, `Category`, `ImageUrl`
   - Depends on: Shared Kernel

3. **Shopping Cart Context** (`dev.domaincentric.sample.ecommerce.cart.domain.model`)
   - Manages shopping carts and checkout
   - Aggregate Root: `ShoppingCart`
   - Entity: `CartItem`
   - Value Objects: `CartId`, `CartItemId`, `Quantity`, `CartStatus`
   - Depends on: Shared Kernel

**Context Mapping Strategy:**
- **Shared Kernel Pattern**: `Money`, `ProductId`, `Price` are shared across contexts
- Cart references Product by `ProductId` only (from Shared Kernel)
- Product and Cart contexts are **isolated** - no direct dependencies
- Both contexts may access Shared Kernel
- No direct aggregate-to-aggregate references

**Executable Context Map:** Cross-context relationships are declared as package annotations —
`@Upstream` (downstream side: translation strategy and consumed channel) and `@Partnership`
(symmetric governance) — and enforced by `ContextMapArchUnitTest` against the code and Spring
Modulith's `allowedDependencies`. [context-map.md](context-map.md) is generated from these
declarations. See [ADR-032](adr/adr-032-executable-context-map.md).

**Why Shared Kernel?**
- **Consistency**: Single definition of `Money` prevents currency handling bugs
- **Reduces Duplication**: Avoid reimplementing universal concepts
- **Clear Boundaries**: Explicit separation of shared vs. context-specific

**What Belongs in Shared Kernel:**
- ✅ Universal value objects (`Money`, monetary primitives)
- ✅ Cross-context identifiers (`ProductId` used by both Product and Cart)
- ✅ Common domain primitives with universal meaning

**What Does NOT Belong in Shared Kernel:**
- ❌ Aggregates (each belongs to exactly one context)
- ❌ Context-specific business logic
- ❌ Infrastructure or technical concerns

### Tactical Patterns

#### Aggregate Root

An aggregate is a cluster of domain objects that can be treated as a single unit. The aggregate root is the entry point to the aggregate.

**Example: Product Aggregate**

```java
public final class Product implements AggregateRoot<Product, ProductId> {
    private final ProductId id;
    private final SKU sku;
    private ProductName name;
    private ProductDescription description;
    private Category category;

    // Price and stock are deliberately absent: they belong to the Pricing and
    // Inventory contexts, and this aggregate references neither.
    public void updateName(ProductName newName) {
        if (newName == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = newName;
        registerEvent(ProductNameChanged.now(id, newName));
    }
}
```

**Example: ShoppingCart Aggregate**

```java
public final class ShoppingCart extends BaseAggregateRoot<ShoppingCart, CartId> {
    private final CartId id;
    private final CustomerId customerId;
    private final List<CartItem> items;  // CartItem is an entity within this aggregate
    private CartStatus status;

    public void addItem(ProductId productId, Quantity quantity, Price price) {
        ensureCartIsActive();

        // Check if product already in cart
        Optional<CartItem> existingItem = findItemByProductId(productId);
        if (existingItem.isPresent()) {
            // Increase quantity of existing item
            existingItem.get().updateQuantity(Quantity.of(
                existingItem.get().quantity().value() + quantity.value()));
        } else {
            // Add new item
            items.add(new CartItem(CartItemId.generate(), productId, quantity, price));
        }

        // Raise domain event
        registerEvent(CartItemAddedToCart.now(this.id, productId, quantity));
    }
}
```

**Rules:**
1. External objects can only reference the aggregate root (never entities within)
2. Aggregate boundaries ensure invariants are maintained
3. Aggregates reference other aggregates by identity only (e.g., ShoppingCart references Product via ProductId)
4. Aggregates raise domain events for important state changes

**Implementation:** See `dev.domaincentric.dca.buildingblocks.ddd.tactical.AggregateRoot`

#### Entity

An entity is an object that has a distinct identity that runs through time and different states.

**Example: CartItem Entity**

```java
public final class CartItem implements Entity<CartItem, CartItemId> {
    private final CartItemId id;
    private final ProductId productId;
    private Quantity quantity;
    private final Price priceAtAddition;

    // Package-private constructor enforces aggregate boundary
    CartItem(CartItemId id, ProductId productId, Quantity quantity, Price priceAtAddition) {
        // ...
    }
}
```

**Rules:**
1. Has a unique identity
2. Identity remains constant through state changes
3. Equality based on identity, not attributes

**Implementation:** See `dev.domaincentric.dca.buildingblocks.ddd.tactical.Entity`

#### Value Object

A value object is an immutable object that describes some characteristic or attribute but has no conceptual identity.

**Example: Money Value Object (from Shared Kernel)**

```java
public record Money(@NonNull BigDecimal amount, @NonNull Currency currency) implements Value {
    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be null or negative");
        }
        // Normalize scale to 2 decimal places
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Money add(final Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(amount.add(other.amount), currency);
    }
}
```

**Rules:**
1. Immutable - use Java records
2. Equality based on attributes, not identity
3. Can be shared between aggregates and contexts (Shared Kernel)
4. Contains validation logic
5. Universal value objects belong in Shared Kernel

**Implementation:**
- Interface: `dev.domaincentric.dca.buildingblocks.ddd.tactical.Value`
- Shared Value Objects: `dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money`, `ProductId`, `Price`
- Context-specific Value Objects: In their respective bounded contexts

#### Repository

A repository encapsulates the logic for accessing domain objects from a data store, presenting a collection-like interface. Repositories mediate between the domain and data mapping layers, providing the illusion of an in-memory collection of aggregates.

**Base Repository Interface**

All repositories extend a common base interface that provides essential operations:

```java
public interface Repository<T extends AggregateRoot<T, ID>, ID extends Id> {
    // Find aggregate by unique identifier
    Optional<T> findById(@NonNull ID id);

    // Save aggregate (add or update)
    T save(@NonNull T aggregate);

    // Remove aggregate from collection
    void deleteById(@NonNull ID id);
}
```

**Example: Product Repository Interface**

Specific repositories add domain-specific query methods using ubiquitous language:

```java
public interface ProductRepository extends Repository<Product, ProductId> {
    // Common methods inherited from Repository:
    // - Optional<Product> findById(ProductId id)
    // - Product save(Product product)
    // - void deleteById(ProductId id)

    // Domain-specific query methods:
    Optional<Product> findBySku(@NonNull SKU sku);
    List<Product> findByCategory(@NonNull Category category);
    List<Product> findAll();
    boolean existsBySku(@NonNull SKU sku);
}
```

**A repository hands out copies, not references**

This is where the collection illusion stops (ADR-031). A `Map` returns the instance it holds, so a
caller who mutates an aggregate has already changed the store and `save` is decoration. A database
returns a *new* object on every read, so the same code loses the change silently. An adapter that
behaves like the map hides the bug until the day it is replaced.

Every adapter therefore maps back through the aggregate's `reconstitute` factory — the JDBC one
because a row leaves it no choice, the in-memory one on purpose:

```java
// JdbcAccountRepository — a row becomes a fresh aggregate
private Account toDomain(final AccountRow row) {
    return Account.reconstitute(row.id(), row.email(), row.owner(), /* ... */);
}

// InMemoryAccountRepository — copies on the way in and on the way out,
// so that it fails wherever a database would fail
@Override
public Optional<Account> findById(final AccountId id) {
    return Optional.ofNullable(accounts.get(id)).map(InMemoryAccountRepository::copyOf);
}
```

Both adapters run the same `AccountRepositoryContractTest`: the contract belongs to the port, and
an implementation that cannot satisfy it does not implement the port.

**Rules:**
1. Interface lives in the application layer as an output port (ADR-008)
2. Implementation lives in infrastructure/adapter layer (secondary adapter)
3. One repository per aggregate root (not per entity)
4. Collection-oriented interface (not generic CRUD)
5. Use ubiquitous language in method names
6. Return immutable collections when appropriate
7. Base interface provides common operations (findById, save, deleteById)
8. Reads return copies — a mutation that was not saved must not be visible (ADR-031)

**Benefits:**
- DRY principle: Common methods defined once in base interface
- Type safety: Generic interface ensures type-safe implementations
- Consistency: All repositories follow the same pattern
- Clear contracts: Base interface defines what all repositories must provide
- Fluent API: save() returning aggregate enables method chaining

**Implementation:**
- Base Interface: `dev.domaincentric.dca.buildingblocks.hexagonal.port.out.Repository`
- Domain Interfaces: `ProductRepository`, `ShoppingCartRepository`
- Implementations: `InMemoryProductRepository`, `JpaShoppingCartRepository` (in `adapter.outgoing.persistence`)

#### Store

A **Store** is the second persistence-shaped output port in DCA, sitting alongside Repository. Both extend `OutputPort`, but the business semantics differ:

- **Repository** — collection-like interface for **Aggregate Roots** (identity + lifecycle: `findById`, `save`, `delete`).
- **Store** — records or queries **operational data** that has no aggregate lifecycle of its own (Value Objects, events, technical state).

**When to use Store instead of Repository:**

The stored object has no own identity-based lifecycle. You don't load it by ID, mutate it, and save it back — you append records and query aggregates over them.

**Base Store Interface**

A Store extends `OutputPort` directly (not the `Repository` marker):

```java
package dev.domaincentric.dca.buildingblocks.hexagonal.port.out;

/**
 * Marker interface for Stores — output ports that record or query
 * operational data without an own aggregate lifecycle.
 *
 * <p>Use Store for Value Objects, Events, or technical state.
 * Use Repository for Aggregate Roots.
 */
public interface Store extends OutputPort {}
```

**Example: Login Protection Store**

```java
public interface LoginProtectionStore extends Store {
    void record(LoginAttempt attempt);
    int  countRecentFailures(Email email, Duration window);
    boolean isLoginBlocked(Email email);
}
```

`LoginAttempt` is a Value Object — there is no `LoginAttempt.findById(...)` because individual attempts have no identity worth retrieving. The Store records them and aggregates over them.

**Decision matrix:**

| Criterion | Repository | Store |
|---|---|---|
| Stored object | Aggregate Root | Value Object / operational data |
| Identity & lifecycle | yes — `findById`, `save`, `delete` | no — `record`, `count`, `exists` |
| Marker | `extends Repository<T, ID>` | `extends Store` |
| Examples | `ProductRepository`, `ShoppingCartRepository` | `LoginProtectionStore`, `AuditLogStore`, `EventStore` |

**Rules of thumb:**

1. Need `findById()`? → Repository (the object has identity).
2. Need `record()` or `count()`? → Store (the object is recorded, not managed).
3. In doubt: if the stored object implements the `Value` marker or is a record, it's almost always a Store.

**Naming as Ubiquitous Language:**
A reader should know from the interface name alone whether they're dealing with a managed aggregate (Repository) or recorded data (Store) — without opening the implementation.

**Enforced by `DddTacticalPatternsArchUnitTest`:**

1. *Store interfaces must extend the Store marker, not Repository* — an interface named `*Store`
   that carries the `Repository` marker promises identity-based load/save it does not offer.
2. *Store interfaces must reside in the application layer's shared output-port package* — same
   placement as Repository: the contract belongs to the application layer, the implementation to an
   adapter.
3. *Store implementations must reside in the `adapter.outgoing` package*.
4. *Store interfaces must not declare `findById` or `save` methods* — those are Repository
   semantics. A Store with them is a Repository under the wrong name, and its stored object should
   then be an Aggregate Root.

Until these rules existed the Repository/Store distinction was documented doctrine only: nothing
made `./gradlew test-architecture` fail when a Store took on aggregate-lifecycle methods.

> **Note on `EventStore` (Event Sourcing):** The `EventStore` from Event Sourcing is a *specialization* of Store — one specifically for Domain Events that supports aggregate reconstruction. The general `Store` is the broader pattern.

#### Domain Service

A domain service contains domain logic that doesn't naturally fit within an entity or value object.

**Example: Cart Total Calculator**

```java
public class CartTotalCalculator implements DomainService {
    public Money calculateTotal(final ShoppingCart cart) {
        if (cart.items().isEmpty()) {
            return Money.zero(Currency.getInstance("EUR"));
        }

        return cart.items().stream()
            .map(item -> item.priceAtAddition().multiply(item.quantity().value()))
            .reduce(Money::add)
            .orElse(Money.zero(Currency.getInstance("EUR")));
    }
}
```

**Rules:**
1. Stateless - only final fields for dependencies
2. Framework-independent (no Spring annotations)
3. Operates on domain objects
4. Named after activities, not entities

**Implementation:** See `dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainService`

#### Domain Event

A domain event is something that happened in the domain that domain experts care about. Events enable eventual consistency, loose coupling between aggregates and bounded contexts, and support audit trails.

**Example: Product Price Changed Event**

```java
public final record ProductPriceChanged(
    @NonNull UUID eventId,
    @NonNull ProductId productId,
    @NonNull Price oldPrice,
    @NonNull Price newPrice,
    @NonNull Instant occurredOn
) implements DomainEvent {
    public static ProductPriceChanged now(
        ProductId productId,
        Price oldPrice,
        Price newPrice) {
        return new ProductPriceChanged(
            UUID.randomUUID(),
            productId,
            oldPrice,
            newPrice,
            Instant.now()
        );
    }
}
```

**Example: Shopping Cart Events**

```java
// Event raised when a product is added to cart
public record CartItemAddedToCart(
    @NonNull UUID eventId,
    @NonNull CartId cartId,
    @NonNull ProductId productId,
    @NonNull Quantity quantity,
    @NonNull Instant occurredOn
) implements DomainEvent {
    public static CartItemAddedToCart now(CartId cartId, ProductId productId, Quantity quantity) {
        return new CartItemAddedToCart(UUID.randomUUID(), cartId, productId, quantity, Instant.now());
    }
}

// Event raised when a product is removed from cart
public record ProductRemovedFromCart(
    @NonNull UUID eventId,
    @NonNull CartId cartId,
    @NonNull ProductId productId,
    @NonNull Instant occurredOn
) implements DomainEvent {
    public static ProductRemovedFromCart now(CartId cartId, ProductId productId) {
        return new ProductRemovedFromCart(UUID.randomUUID(), cartId, productId, Instant.now());
    }
}
```

**Rules:**
1. Named in past tense (something that happened)
2. Immutable (use records or final classes)
3. Contains timestamp (occurredOn) and unique ID (eventId)
4. Framework-independent
5. Internal to a bounded context — no versioning needed (see Integration Events for cross-context versioning)

**Event Publishing Pattern:**

```java
// 1. Aggregate raises event during state change
public void changePrice(Price newPrice) {
    Price oldPrice = this.price;
    this.price = newPrice;
    registerEvent(ProductPriceChanged.now(this.id, oldPrice, newPrice));
}

// 2. Application Service saves and publishes events
Product product = productRepository.save(product);
eventPublisher.publishAndClearEvents(product);
```

**Every use case that saves an aggregate must publish — unconditionally.** Not only where an event
is expected: whether an action raised one is the aggregate's business, and a use case that publishes
"only when needed" breaks silently the day that action starts raising an event. An aggregate saved
while still holding its events loses them; worse, with the in-memory repository the same instance
stays in the map, so a later use case can publish them out of context. A repository must not clear
events either — on the load path it would swallow what a use case still owes.
Enforced by `UseCasePatternsArchUnitTest` → *"Use cases that save an aggregate must publish its
domain events"*.

**Event Handling:**

```java
@Component
public class ProductEventConsumer {
    @EventListener
    public void onProductPriceChanged(ProductPriceChanged event) {
        // Handle cross-aggregate coordination
        // Update read models
        // Send notifications
        // Trigger business processes
    }
}
```

**Benefits:**
- Enables eventual consistency across aggregates (Vernon's Rule #4)
- Loose coupling between bounded contexts
- Audit trail and event sourcing capability
- Asynchronous processing support
- Time-travel debugging

**Implementation:**
- Interface: `dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent`
- Publisher Interface (SPI): `dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher`
- Publisher Implementation: `dev.domaincentric.sample.ecommerce.sharedkernel.adapter.outgoing.event.SpringDomainEventPublisher`
- Examples: `ProductCreated`, `ProductPriceChanged`, `CartItemAddedToCart`, `CartCheckedOut`

**Event Publishing Infrastructure:**

The event publishing infrastructure follows the Dependency Inversion Principle to keep the application layer framework-independent:

```java
// Interface (outbound port) in dev.domaincentric.dca.buildingblocks.hexagonal.port.out - application layer depends on this
public interface DomainEventPublisher extends OutputPort {
    void publish(DomainEvent event);
    void publishAndClearEvents(AggregateRoot<?, ?> aggregate);
}

// Implementation in sharedkernel.adapter.outgoing.event - uses Spring framework
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(DomainEvent event) {
        eventPublisher.publishEvent(event);
    }

    @Override
    public void publishAndClearEvents(AggregateRoot<?, ?> aggregate) {
        aggregate.domainEvents().forEach(this::publish);
        aggregate.clearDomainEvents();
    }
}
```

**Benefits:**
- Application layer remains framework-independent (depends on interface, not Spring)
- Easy to mock for testing
- Can swap implementations (e.g., message broker, in-memory for tests)
- Follows Hexagonal Architecture (port defined in dev.domaincentric.dca.buildingblocks.hexagonal.port.out, adapter in sharedkernel.adapter.outgoing.event)

#### Integration Event

Integration Events are **adapter-layer DTOs** published across bounded context boundaries. Unlike domain events, they do not extend `DomainEvent` — they are a separate concept created by outgoing event adapters when a domain event is consumed. This separation acts as an Anti-Corruption Layer between a context's domain model and external consumers.

**Key Differences from Domain Events:**

| Aspect | Domain Events | Integration Events |
|--------|--------------|-------------------|
| **Layer** | Domain (`domain.event`) | Adapter (`adapter.outgoing.event`) |
| **Interface** | `DomainEvent` | `IntegrationEvent` (separate hierarchy) |
| **Naming** | No suffix (`CartCheckedOut`) | `Event` suffix (`CartCheckedOutEvent`) |
| **Versioning** | None — can change freely | `@IntegrationEventType(name, version)` as a class property, not a data field (ADR-027) |
| **Creation** | Raised by aggregates | Created by outgoing event adapters via `from()` factory |
| **Consumption** | Within same context | By incoming event adapters in other contexts |

**Event Flow:**

```
Aggregate raises DomainEvent (e.g., CartCheckedOut)
  → Outgoing EventPublisher adapter listens (@EventListener)
    → Creates IntegrationEvent via from() factory (CartCheckedOutEvent)
      → Publishes IntegrationEvent via ApplicationEventPublisher
        → Incoming EventConsumer in other context receives it
```

**Example: Domain Event (domain layer)**

```java
// cart/domain/event/CartCheckedOut.java — internal, no version
public record CartCheckedOut(
    UUID eventId,
    CartId cartId,
    CustomerId customerId,
    Money totalAmount,
    int itemCount,
    List<ItemInfo> items,
    Instant occurredOn
) implements DomainEvent {

    public record ItemInfo(ProductId productId, int quantity) {}

    public static CartCheckedOut now(CartId cartId, ...) {
        return new CartCheckedOut(UUID.randomUUID(), cartId, ..., Instant.now());
    }
}
```

**Example: Integration Event (adapter layer)**

```java
// cart/adapter/outgoing/event/CartCheckedOutEvent.java — versioned public contract
// The schema version is a class property (@IntegrationEventType), never a data field (ADR-027).
@IntegrationEventType(name = "cart-checked-out", version = 1)
public record CartCheckedOutEvent(
    UUID eventId,
    CartId cartId,
    CustomerId customerId,
    Money totalAmount,
    int itemCount,
    List<ItemInfo> items,
    Instant occurredOn
) implements IntegrationEvent {

    public record ItemInfo(ProductId productId, int quantity) {}

    public static CartCheckedOutEvent from(CartCheckedOut domainEvent) {
        List<ItemInfo> items = domainEvent.items().stream()
            .map(i -> new ItemInfo(i.productId(), i.quantity()))
            .toList();
        return new CartCheckedOutEvent(
            domainEvent.eventId(), domainEvent.cartId(), ...,
            items, domainEvent.occurredOn());
    }
}
```

**Example: Outgoing Event Publisher (adapter)**

```java
// cart/adapter/outgoing/event/CartCheckedOutEventPublisher.java
@Component
public class CartCheckedOutEventPublisher {
    private final ApplicationEventPublisher publisher;

    @EventListener
    public void on(CartCheckedOut domainEvent) {
        publisher.publishEvent(CartCheckedOutEvent.from(domainEvent));
    }
}
```

**Example: Incoming Event Consumer (other context)**

```java
// inventory/adapter/incoming/event/CheckoutConfirmedEventConsumer.java
@Component
public class CheckoutConfirmedEventConsumer {
    private final ReduceStockInputPort reduceStockUseCase;

    @EventListener
    public void onCheckoutConfirmed(CheckoutConfirmedEvent event) {
        for (CheckoutConfirmedEvent.LineItemInfo item : event.items()) {
            reduceStockUseCase.execute(
                new ReduceStockCommand(item.productId().value().toString(), item.quantity()));
        }
    }
}
```

**Rules:**
1. Integration events live in `adapter.outgoing.event` (not `domain.event`)
2. Implement `IntegrationEvent` (not `DomainEvent`) — separate interface hierarchies
3. Named with `Event` suffix (e.g., `CartCheckedOutEvent`)
4. Created via `from(DomainEvent)` factory method in outgoing event adapters
5. Include `int version` field for schema evolution
6. Use DTOs with Shared Kernel types or primitives only

**Implementation:**
- Interface: `dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEvent`
- Example Event: `dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.event.CartCheckedOutEvent`
- Example Publisher: `dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.event.CartCheckedOutEventPublisher`
- Example Consumer: `dev.domaincentric.sample.ecommerce.inventory.adapter.incoming.event.CheckoutConfirmedEventConsumer`

#### Interface Inversion for Cross-Module Events

When a consumer module listens to a producer's integration event directly, it creates a dependency from consumer to producer. Spring Modulith's `@ApplicationModuleListener` dispatches by type, so if the consumer listens to the producer's event class, that class must be in the consumer's `allowedDependencies`.

The **Interface Inversion pattern** eliminates this dependency: the consumer defines a trigger interface in its own `events/` named interface, and the producer's event implements it.

**How It Works:**

1. Consumer module defines a trigger interface in `{module}/events/` (published via `@NamedInterface("events")`)
2. Producer module's integration event implements the trigger interface(s)
3. Consumer listens to its own trigger interface via `@ApplicationModuleListener`
4. Each listener runs in its own transaction (one aggregate per transaction)

**Example: Checkout publishes, Cart and Inventory consume**

```
checkout/events/CheckoutConfirmedEvent
    implements CartCompletionTrigger, StockReductionTrigger

cart/events/CartCompletionTrigger          ← defined by Cart
inventory/events/StockReductionTrigger     ← defined by Inventory
```

Cart and Inventory never import anything from Checkout. Instead, Checkout depends on `cart::events` and `inventory::events` to implement their trigger interfaces.

**Trigger interface (consumer-defined):**

```java
// cart/events/CartCompletionTrigger.java — Cart defines what it needs
public interface CartCompletionTrigger {
    String cartId();
}
```

**Producer event implements trigger interfaces:**

```java
// checkout/events/CheckoutConfirmedEvent.java
public record CheckoutConfirmedEvent(...)
    implements IntegrationEvent, CartCompletionTrigger, StockReductionTrigger { }
```

**Consumer listens to its own interface:**

```java
// cart/adapter/incoming/event/cartcheckout/CartCompletionEventConsumer.java
@Component
public class CartCompletionEventConsumer {
    @ApplicationModuleListener
    void on(final CartCompletionTrigger event) {
        completeCartInputPort.execute(new CompleteCartCommand(event.cartId()));
    }
}
```

**Module Dependency Graph:**

```
checkout → cart::api, cart::events, product::api, pricing::api, inventory::api, inventory::events
cart     → product::api, pricing::api, inventory::api
product  → pricing::api, inventory::api
pricing, inventory → (leaf modules, no business context deps)
```

Dependencies flow toward leaf modules. Checkout depends on consumer trigger interfaces (`cart::events`, `inventory::events`), not the other way around. This keeps Cart and Inventory decoupled from the checkout process.

**Rules:**
1. Trigger interfaces live in `{module}/events/` with `@NamedInterface("events")`
2. Each trigger interface exposes only the data the consumer needs
3. The producer's integration event implements all relevant trigger interfaces
4. Consumers use `@ApplicationModuleListener` on the trigger interface type
5. Each listener runs in its own transaction (Spring Modulith default)

**Implementation:**
- `dev.domaincentric.sample.ecommerce.cart.events.CartCompletionTrigger`
- `dev.domaincentric.sample.ecommerce.inventory.events.StockReductionTrigger`
- `dev.domaincentric.sample.ecommerce.checkout.events.CheckoutConfirmedEvent` (implements both)
- `dev.domaincentric.sample.ecommerce.cart.adapter.incoming.event.cartcheckout.CartCompletionEventConsumer`
- `dev.domaincentric.sample.ecommerce.inventory.adapter.incoming.event.StockReductionEventConsumer`

#### Factory

A factory encapsulates complex object creation logic.

**Example: Product Factory**

```java
public class ProductFactory implements Factory {

    public Product createProduct(
        final SKU sku,
        final ProductName name,
        final ProductDescription description,
        final Category category,
        final ImageUrl imageUrl,
        final Money initialPrice,
        final int initialStock
    ) {
        final ProductId id = ProductId.generate();
        final Product product = new Product(id, sku, name, description, category, imageUrl);

        // Price and stock are not the product's state — they are carried out on the
        // creation event, so Pricing and Inventory can seed their own aggregates.
        product.registerEvent(ProductCreated.now(id, sku, name, initialPrice, initialStock));
        return product;
    }
}
```

**Rules:**
1. Encapsulates complex creation logic
2. Ensures invariants are satisfied at creation
3. Framework-independent
4. Stateless

**Implementation:** See `dev.domaincentric.dca.buildingblocks.ddd.tactical.Factory`

#### Specification

A specification encapsulates business rules and can be combined for complex queries.

**Example: Product Available Specification**

```java
public class ProductAvailableSpecification implements Specification<Product> {
    @Override
    public boolean isSatisfiedBy(final Product product) {
        return product.isAvailable();
    }
}
```

**Rules:**
1. Encapsulates business rule
2. Can be combined (AND, OR, NOT)
3. Framework-independent
4. Reusable across use cases

**Implementation:** See `dev.domaincentric.dca.buildingblocks.ddd.tactical.Specification`

#### Enriched Domain Model Pattern

The Enriched Domain Model Pattern creates domain objects that combine aggregate state with external context data. These are **first-class domain concepts** that can contain business logic—not just presentation views.

**Key Insight: Enriched Models Are Domain Concepts**

When business rules require data from multiple bounded contexts (e.g., "can only checkout if items are in stock"), the aggregate alone cannot enforce these rules—it doesn't have access to external data.

The solution: **Enriched Domain Models** that combine aggregate state with external data and own cross-context business logic:

```
Aggregate (owns mutations)  +  External Data  →  Enriched Model (owns cross-context rules)
```

**Separation of Responsibilities:**

| Concern | Owner | Example |
|---------|-------|---------|
| **State mutations** | Aggregate | `cart.addItem()`, `cart.removeItem()` |
| **Aggregate invariants** | Aggregate | "quantity must be positive" |
| **Cross-context rules** | Enriched Model | "can checkout if all items in stock" |
| **Cross-context calculations** | Enriched Model | "current total with live prices" |

**Example: EnrichedCart with Business Logic**

```java
public record EnrichedCart(
    CartId cartId,
    CustomerId customerId,
    List<EnrichedCartItem> items,
    CartStatus status
) implements Value {

  /**
   * Factory method combines aggregate state with external article data.
   */
  public static EnrichedCart from(
      final ShoppingCart cart,
      final Map<ProductId, CartArticle> articleData) {
    // ... validation and assembly
  }

  // ========== BUSINESS LOGIC (cross-context rules) ==========

  /**
   * Business rule: Can only checkout if all items are available and in stock.
   */
  public boolean canCheckout() {
    return !items.isEmpty()
        && items.stream().allMatch(EnrichedCartItem::isAvailableForCheckout);
  }

  /**
   * Business rule: Detect price changes since items were added.
   */
  public boolean hasAnyPriceChanges() {
    return items.stream().anyMatch(EnrichedCartItem::hasPriceChanged);
  }

  /**
   * Calculation requiring external data: current total with live prices.
   */
  public Money currentSubtotal() {
    return items.stream()
        .map(EnrichedCartItem::currentLineTotal)
        .reduce(Money.zero(DEFAULT_CURRENCY), Money::add);
  }
}

public record EnrichedCartItem(
    CartItemId itemId,
    ProductId productId,
    String name,
    Money priceAtAddition,    // From aggregate
    Money currentPrice,        // From external context
    int quantity,
    int availableStock,
    boolean isAvailable
) implements Value {

  public boolean hasPriceChanged() {
    return !currentPrice.equals(priceAtAddition);
  }

  public boolean isAvailableForCheckout() {
    return isAvailable && availableStock >= quantity;
  }

  public Money currentLineTotal() {
    return currentPrice.multiply(quantity);
  }
}
```

**Example: EnrichedProduct with Business Logic**

```java
public record EnrichedProduct(
    ProductId productId,
    String sku,
    String name,
    String description,
    String category,
    Money currentPrice,
    int stockQuantity,
    boolean isAvailable
) implements Value {

  public static EnrichedProduct from(final Product product, final ProductArticle article) {
    // ... validation and assembly
  }

  // ========== BUSINESS LOGIC ==========

  /**
   * Business rule: Product can be purchased if available and in stock.
   */
  public boolean canPurchase() {
    return isAvailable && stockQuantity > 0;
  }

  /**
   * Business rule: Check if requested quantity can be fulfilled.
   */
  public boolean hasStockFor(int requestedQuantity) {
    return stockQuantity >= requestedQuantity;
  }
}
```

**Example: Use Case Creating Enriched Model**

```java
@Service
@Transactional(readOnly = true)
public class GetCartByIdUseCase implements GetCartByIdInputPort {
  private final ShoppingCartRepository shoppingCartRepository;
  private final ArticleDataPort articleDataPort;

  @Override
  public GetCartByIdResult execute(final GetCartByIdQuery query) {
    final ShoppingCart cart = shoppingCartRepository.findById(CartId.of(query.cartId()))
        .orElse(null);
    if (cart == null) {
      return GetCartByIdResult.notFound();
    }

    // Fetch external data from other bounded contexts
    final Set<ProductId> productIds = cart.items().stream()
        .map(item -> item.productId())
        .collect(Collectors.toSet());
    final Map<ProductId, CartArticle> articleData = articleDataPort.getArticleData(productIds);

    // Create enriched domain model - contains business logic for cross-context rules
    final EnrichedCart enrichedCart = EnrichedCart.from(cart, articleData);
    return GetCartByIdResult.found(enrichedCart);
  }
}
```

**Example: Use Case Using Enriched Model for Business Decisions**

```java
@Service
@Transactional
public class CheckoutCartUseCase implements CheckoutCartInputPort {
  private final ShoppingCartRepository cartRepository;
  private final ArticleDataPort articleDataPort;

  @Override
  public CheckoutCartResult execute(final CheckoutCartCommand command) {
    final ShoppingCart cart = cartRepository.findById(command.cartId())
        .orElseThrow(() -> new CartNotFoundException(command.cartId()));

    // Create enriched model to evaluate cross-context business rules
    final Map<ProductId, CartArticle> articleData = articleDataPort.getArticleData(cart.productIds());
    final EnrichedCart enrichedCart = EnrichedCart.from(cart, articleData);

    // Business rule enforcement using enriched model
    if (!enrichedCart.canCheckout()) {
      return CheckoutCartResult.cannotCheckout(enrichedCart.getCheckoutBlockers());
    }

    // Proceed with checkout - aggregate handles state mutation
    cart.checkout();
    cartRepository.save(cart);

    return CheckoutCartResult.success(enrichedCart.currentSubtotal());
  }
}
```

**Rules:**
1. Enriched models are immutable Value Objects (use Java records)
2. Factory methods combine aggregate state with external data
3. Business logic for cross-context rules belongs in the enriched model
4. Aggregates own state mutations; enriched models own cross-context evaluations
5. Enriched models reside in `domain/model` package (they ARE domain concepts)

**Benefits:**
- **Rich domain model**: Business logic lives in domain objects, not scattered in services
- **Clear responsibility split**: Aggregate = mutations, Enriched Model = cross-context rules
- **Testable**: Enriched models are pure functions, easy to unit test with different data combinations
- **Encapsulated**: External code only sees enriched model, not aggregate internals
- **Avoids anemic aggregates**: Business logic is in the domain, just distributed appropriately

**When to Use:**
- ✅ **When business rules require data from multiple bounded contexts**
- ✅ When calculations need external data (current prices, stock levels)
- ✅ When the aggregate alone cannot enforce all business rules
- ✅ When team prefers straightforward, pragmatic solutions

**Why Enriched Domain Model for Cross-Context Business Rules:**

When business rules require data from multiple bounded contexts, the aggregate cannot enforce these rules alone. The Enriched Domain Model becomes the natural home for this logic:

```
Aggregate + ExternalData  →  EnrichedModel.from(...)  →  Business Logic  →  ViewModel
                                                         (in enriched model)
```

The enriched model is not just for display -- it is a domain concept that owns cross-context business rules. See [ADR-021](adr/adr-021-enriched-domain-model-pattern.md) for the full decision record.

**Implementation:**
- Product: `dev.domaincentric.sample.ecommerce.product.domain.model.EnrichedProduct`
- Cart: `dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart`
- Checkout: `dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot`

#### Complete Data Flow: Use Case → ViewModel → Template

The Enriched Domain Model integrates with the adapter layer through page-specific ViewModels:

```
Domain          Application          Adapter (incoming.web)       Template
────────        ───────────          ─────────────────────        ────────
Aggregate   →   Use Case creates  →  Controller converts     →   Template
+ External      EnrichedModel        Model → ViewModel            renders
```

**Example: CartPageController using EnrichedCart**

```java
// 1. Use Case creates enriched domain model with business logic
@Service
public class GetCartByIdUseCase implements GetCartByIdInputPort {
    @Override
    public GetCartByIdResult execute(GetCartByIdQuery query) {
        ShoppingCart cart = cartRepository.findById(query.cartId()).orElse(null);
        if (cart == null) return GetCartByIdResult.notFound();

        Map<ProductId, CartArticle> articleData = articleDataPort.getArticleData(cart.productIds());
        EnrichedCart enrichedCart = EnrichedCart.from(cart, articleData);
        return GetCartByIdResult.found(enrichedCart);
    }
}

// 2. Controller converts enriched model to page-specific ViewModel
@Controller
public class CartPageController {
    @GetMapping("/cart")
    public String showCart(Model model) {
        GetCartByIdResult result = getCartByIdUseCase.execute(query);

        // Convert enriched domain model → ViewModel (primitives only)
        CartPageViewModel viewModel = CartPageViewModel.from(result.cart());
        model.addAttribute("shoppingCart", viewModel);
        return "cart/view";
    }
}

// 3. ViewModel exposes enriched model data as primitives
public record CartPageViewModel(
    String cartId,
    List<LineItemViewModel> lineItems,
    BigDecimal currentSubtotal,
    String currencyCode,
    boolean canCheckout,           // From enriched model's business logic
    boolean hasAnyPriceChanges     // From enriched model's business logic
) {
    public static CartPageViewModel from(EnrichedCart cart) {
        return new CartPageViewModel(
            cart.cartId().value().toString(),
            cart.items().stream().map(LineItemViewModel::from).toList(),
            cart.currentSubtotal().amount(),
            cart.currentSubtotal().currency().getCurrencyCode(),
            cart.canCheckout(),         // Business logic result
            cart.hasAnyPriceChanges()   // Business logic result
        );
    }
}
```

**ViewModel Rules:**
1. Reside in `adapter.incoming.web` alongside the controller
2. Use primitives only (`String`, `BigDecimal`, `int`, `boolean`)
3. Named `{Page}ViewModel` (e.g., `CartPageViewModel`, `ReviewPageViewModel`)
4. Factory method converts enriched domain model to ViewModel
5. Business logic results (e.g., `canCheckout`) are converted to primitive booleans

See [dto-vs-viewmodel-analysis.md](dto-vs-viewmodel-analysis.md) for detailed patterns.

**Implementation:**
- Product: `dev.domaincentric.sample.ecommerce.product.domain.model.EnrichedProduct`
- Product ViewModels: `ProductCatalogPageViewModel`, `ProductDetailPageViewModel`
- Cart: `dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart`
- Cart ViewModels: `CartPageViewModel`, `CartMergePageViewModel`
- Checkout: `dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot`
- Checkout ViewModels: `BuyerInfoPageViewModel`, `DeliveryPageViewModel`, `PaymentPageViewModel`, `ReviewPageViewModel`, `ConfirmationPageViewModel`


#### Custom Annotations (Shared Kernel Common Layer)

Custom framework-agnostic annotations belong in the Shared Kernel when they represent cross-cutting technical concerns used across multiple bounded contexts.

**Example: @AsyncInitialize Annotation**

The `@AsyncInitialize` annotation marks components for asynchronous initialization after bean construction, enabling non-blocking startup of services that require heavy initialization tasks.

**Annotation Definition (Shared Kernel):**

```java
// sharedkernel/common/annotation/AsyncInitialize.java
package dev.domaincentric.sample.ecommerce.sharedkernel.common.annotation;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AsyncInitialize {
  int priority() default 100;
  String description() default "";
}
```

**Processor Implementation (Infrastructure):**

```java
// infrastructure/support/AsyncInitializationProcessor.java
@Component
public class AsyncInitializationProcessor implements BeanPostProcessor, Ordered {

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    AsyncInitialize annotation =
        AnnotationUtils.findAnnotation(bean.getClass(), AsyncInitialize.class);

    if (annotation != null) {
      Method initMethod = bean.getClass().getMethod("asyncInitialize");
      initMethod.invoke(bean);
    }
    return bean;
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}
```

**Usage Example (Adapter):**

```java
// product/adapter/outgoing/persistence/InMemoryProductRepository.java
@Repository
@AsyncInitialize(priority = 50, description = "Warm up product cache")
public class InMemoryProductRepository implements ProductRepository {

  @Async
  public void asyncInitialize() {
    logger.info("Starting async initialization of ProductRepository cache...");
    // Preload frequently accessed products
    // Build search indexes
    // Warm up caches
    logger.info("ProductRepository cache warmup completed.");
  }
}

// cart/adapter/outgoing/persistence/InMemoryShoppingCartRepository.java
@Repository
@AsyncInitialize(priority = 100, description = "Initialize shopping cart metrics")
public class InMemoryShoppingCartRepository implements ShoppingCartRepository {

  @Async
  public void asyncInitialize() {
    logger.info("Starting async initialization of ShoppingCartRepository...");
    // Initialize metrics collectors
    // Preload abandoned cart data
    logger.info("ShoppingCartRepository initialization completed.");
  }
}
```

**Rules:**
1. Annotation definition in `sharedkernel.infrastructure` (framework-agnostic)
2. Processor implementation in `infrastructure.support` (framework-specific)
3. Pure Java annotation with no Spring dependencies
4. Method name convention: `asyncInitialize()` annotated with `@Async`
5. Priority determines initialization order (lower values first)

**Benefits:**
- Framework-independent annotation (pure Java metadata)
- Application layer can use it without depending on infrastructure
- Easy to test and mock
- Separation of concerns: annotation vs. processing logic
- Follows Dependency Inversion Principle

**Implementation:**
- Annotation: `dev.domaincentric.sample.ecommerce.sharedkernel.infrastructure.AsyncInitialize`
- Processor: `dev.domaincentric.sample.ecommerce.infrastructure.support.AsyncInitializationProcessor`
- Configuration: `dev.domaincentric.sample.ecommerce.infrastructure.config.AsyncConfiguration`
- Examples: `InMemoryProductRepository`, `InMemoryShoppingCartRepository`

---

## Clean Architecture (Use Cases)

Clean Architecture, as defined by Robert C. Martin, emphasizes organizing code around use cases - explicit representations of what users can do with the system. This section describes how we implement the Use Case pattern in the application layer.

### Use Case Pattern

A **Use Case** represents a single user action or system operation. In Clean Architecture, use cases are the primary organizing principle of the application layer, replacing traditional "service" classes with many methods.

#### Core Concepts

**Generic UseCase Contract:**
All use cases implement a generic `UseCase<I,O>` interface with a single `execute()` method. This provides a consistent contract while keeping implementations focused and testable.

**Input/Output Models:**
Use cases accept Input models and return Output models to decouple the application layer from presentation and infrastructure concerns.

**Single Responsibility:**
Each use case class represents one specific business operation (Command or Query), following the Single Responsibility Principle.

### Example: Create Product Use Case

**Base UseCase Interface**

```java
public interface UseCase<I, O> {
    @NonNull O execute(@NonNull I input);
}
```

**Input Model**

```java
public record CreateProductInput(
    String sku,
    String name,
    String description,
    BigDecimal priceAmount,
    String priceCurrency,
    String category,
    int stockQuantity
) {
    // Validation in compact constructor
    public CreateProductInput {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU cannot be null or blank");
        }
        // ... more validation
    }
}
```

**Output Model**

```java
public record CreateProductOutput(
    String productId,
    String sku,
    String name,
    String description,
    BigDecimal priceAmount,
    String priceCurrency,
    String category,
    int stockQuantity
) {}
```

**Use Case Implementation**

```java
@Service
@Transactional
public class CreateProductUseCase implements UseCase<CreateProductInput, CreateProductOutput> {
    private final ProductRepository productRepository;
    private final ProductFactory productFactory;
    private final DomainEventPublisher eventPublisher;

    @Override
    public @NonNull CreateProductOutput execute(@NonNull CreateProductInput input) {
        // 1. Validate business rules
        SKU sku = new SKU(input.sku());
        if (productRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("Product with SKU already exists");
        }

        // 2. Convert input to domain objects
        ProductName name = new ProductName(input.name());
        Price price = new Price(new Money(input.priceAmount(), Currency.getInstance(input.priceCurrency())));
        // ...

        // 3. Execute domain logic
        Product product = productFactory.createProduct(sku, name, ...);

        // 4. Persist
        productRepository.save(product);

        // 5. Publish events
        eventPublisher.publishAndClearEvents(product);

        // 6. Map to output
        return new CreateProductOutput(
            product.id().value().toString(),
            product.sku().value(),
            ...
        );
    }
}
```

### Use Case Categories

#### Command Use Cases (Write)

Commands modify system state and publish domain events.

**Examples:**
- `CreateProductUseCase` - Creates a new product
- `UpdateProductPriceUseCase` - Changes product price
- `AddItemToCartUseCase` - Adds item to cart
- `CheckoutCartUseCase` - Completes cart checkout
- `ChangePasswordUseCase` - Replaces an account's password; a wrong current password or a rejected
  new one is reported as an outcome of `ChangePasswordResult`, not as an exception crossing the port
- `ChangeProfileUseCase` (`account.application.changeprofile`) - Changes the basic information of
  an account behind `/account/profile`: its email address and the date of birth of its `Owner`. All
  submitted values are validated before the aggregate is mutated, so a rejected value leaves the
  whole profile untouched; the uniqueness check skips the caller's own address. The owner's **name
  is not changeable**: the command carries no name component and the aggregate offers no operation
  that would accept one

**Characteristics:**
- Transactional (`@Transactional`, or `TransactionBoundary.inTransaction` around save + publish when the use case also calls remote-capable ports — ADR-034)
- Validate business rules
- Modify aggregate state
- Publish domain events
- Return result data

#### Query Use Cases (Read)

Queries retrieve data without modifying state.

**Examples:**
- `GetProductByIdUseCase` - Retrieves a product
- `GetAllProductsUseCase` - Lists all products
- `GetCartByIdUseCase` - Retrieves a cart
- `GetProfileUseCase` (`account.application.getprofile`) - Projects the profile fields the
  `/account/profile` page renders — the owner's name for display, the email and date of birth for
  editing; an account that cannot log in is reported as absent

**Characteristics:**
- Read-only (`@Transactional(readOnly = true)`)
- No state changes
- No domain events
- Return read models

### Input/Output Model Design

#### Input Models

**Rules:**
1. Immutable (Java records preferred)
2. Contain only primitive types and Strings (no domain objects)
3. Validate format in compact constructor
4. Reside in `application` package
5. Named with "Input" suffix

**Purpose:**
- Decouple use case from presentation layer DTOs
- Define explicit contract for use case
- Enable easy testing

**Example:**

```java
public record AddItemToCartInput(
    String cartId,
    String productId,
    int quantity
) {
    public AddItemToCartInput {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
}
```

#### Output Models

**Rules:**
1. Immutable (Java records preferred)
2. Contain only data needed by presentation layer
3. Values, never identities: primitives, Strings, nested part records (named by content — `CartItemSummary`, `LineItemData`), value objects (`Money`, `ProductId`) and read models (`EnrichedCart`, `CheckoutCartSnapshot`); no aggregate root or entity, also not inside `List<T>`/`Optional<T>` (`DCA-USE-015`, checked transitively)
4. Reside in `application` package
5. Named with "Result" suffix at the top level only
6. Command results are small — ids, status, what the caller needs next; the view comes from a query

**Purpose:**
- Prevent leaking domain entities to outer layers
- Control what data is exposed
- Enable API versioning

**Example:**

```java
public record CreateProductOutput(
    String productId,
    String sku,
    String name,
    BigDecimal priceAmount,
    String priceCurrency
) {}
```

#### Shaping the Result

The Checkout wizard's commands (`SubmitBuyerInfo`, `SubmitDelivery`, `SubmitPayment`, `ConfirmCheckout`) answer
`(sessionId, currentStep, status)`; the page controllers redirect and the next page asks `GetCheckoutSession`,
whose result wraps the `CheckoutCartSnapshot` read model (a `Value` in `domain/readmodel`, built by
`CheckoutCartSnapshot.from(session)`) — the snapshot is the result field, nothing is flattened a second time.
Cart results carry `Money` instead of amount/currency pairs and are built by static `from(...)` factories on the
result; the part record `CartItemSummary`, shared by several use cases, lives in `application/shared` and is
named by content. `GetCartByIdResult` delivers the `EnrichedCart` read model together with a `CartTotals` part
(current and original subtotal, their difference, the contained tax) that the use case assembles with the
`CartTotalCalculator` domain service.

Incoming adapters read and format what a result delivers and operate no domain object. Reading includes the own,
parameterless queries of a delivered read model — `CartPageViewModel` calls `currentLineTotal()`,
`priceDifference()`, `priceIncreased()` and `isValidForCheckout()` on the enriched cart, derivations of the value's
own state; it never compares or combines two delivered values itself.
Whether a checkout step may be opened is decided by the `GetCheckoutSession` query: the use case invokes the
`CheckoutStepValidator` domain service and delivers a `StepAccess` value; the page controller maps it to a route.
No web adapter injects a domain service (`DCA-HEX-012`), constructs an aggregate, entity or domain value, or
combines values into a new business fact. Outgoing adapters are different: the repositories map and reconstitute aggregates while implementing
their output ports.

### Benefits

1. **Explicit Contracts**: Each use case is a clear, testable contract
2. **Interface Segregation**: Clients depend only on what they use
3. **Decoupling**: Input/Output models decouple layers
4. **Testability**: Easy to mock and test use cases
5. **Documentation**: Use cases document what the system does
6. **API Stability**: Output models provide versioning boundary

### Use Cases with Input/Output Ports Pattern

**Traditional Application Service:**
```java
@Service
public class ProductApplicationService {
    public Product createProduct(...) { }
    public void updatePrice(...) { }
    public Product findById(...) { }
    public List<Product> findAll() { }
    // Many methods in one class
}
```

**Use Case with Input Ports Approach:**
```java
// Input Port interface (marker interface)
interface InputPort<INPUT, OUTPUT> {
    OUTPUT execute(INPUT input);
}

// Specific Input Port for the use case
interface UpdateProductPriceInputPort extends InputPort<UpdateProductPriceCommand, UpdateProductPriceResponse> {
    UpdateProductPriceResponse execute(UpdateProductPriceCommand input);
}

// Use Case implementation
@Service
public class UpdateProductPriceUseCase implements UpdateProductPriceInputPort {
    private final ProductRepository productRepository;  // Output Port
    private final DomainEventPublisher eventPublisher;  // Output Port

    public UpdateProductPriceResponse execute(UpdateProductPriceCommand input) { ... }
}
```

**Advantages of Input Port / Use Case Approach:**
- Single Responsibility Principle (one use case per class)
- Interface Segregation (clients inject only the specific input ports they need)
- Easier to test (smaller, focused classes)
- Clearer naming (port and use case names reflect business operation)
- Better suited for microservices (can deploy use cases independently)
- Explicit Hexagonal Architecture ports (Input Ports = primary ports, Output Ports = secondary ports)
- Consistent contract enforces input/output models across all use cases

### Implementation

**Base Interface:** `dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase<INPUT, OUTPUT>`

**Product Use Cases:**
- Input Port: `CreateProductInputPort extends UseCase<CreateProductCommand, CreateProductResult>`
- Implementation: `CreateProductUseCase implements CreateProductInputPort`
- Input Port: `UpdateProductPriceInputPort extends UseCase<UpdateProductPriceCommand, UpdateProductPriceResult>`
- Implementation: `UpdateProductPriceUseCase implements UpdateProductPriceInputPort`
- Input Port: `GetProductByIdInputPort extends UseCase<GetProductByIdQuery, GetProductByIdResult>`
- Implementation: `GetProductByIdUseCase implements GetProductByIdInputPort`
- Input Port: `GetAllProductsInputPort extends UseCase<GetAllProductsQuery, GetAllProductsResult>`
- Implementation: `GetAllProductsUseCase implements GetAllProductsInputPort`

**Shopping Cart Use Cases:**
- Input Port: `CreateCartInputPort extends UseCase<CreateCartCommand, CreateCartResult>`
- Implementation: `CreateCartUseCase implements CreateCartInputPort`
- Input Port: `AddItemToCartInputPort extends UseCase<AddItemToCartCommand, AddItemToCartResult>`
- Implementation: `AddItemToCartUseCase implements AddItemToCartInputPort`
- Input Port: `CheckoutCartInputPort extends UseCase<CheckoutCartCommand, CheckoutCartResult>`
- Implementation: `CheckoutCartUseCase implements CheckoutCartInputPort`
- Input Port: `GetCartByIdInputPort extends UseCase<GetCartByIdQuery, GetCartByIdResult>`
- Implementation: `GetCartByIdUseCase implements GetCartByIdInputPort`

**Naming Convention:**
- Input port interfaces end with "InputPort"
- Use case implementation classes end with "UseCase"
- Command input models end with "Command" (for write operations)
- Query input models end with "Query" (for read operations)
- Output models end with "Result" (see [ADR-020](adr/adr-020-use-case-result-naming.md))
- All input ports extend `UseCase<INPUT, OUTPUT>`
- All use cases implement their corresponding input port interface

### Organization by Bounded Context

Use cases are organized into subpackages matching the bounded contexts from the domain layer. This creates a clear alignment between the application layer and domain layer.

**Package Structure:**
```
application/
├── createproduct/                    # Use case: Create Product
│   ├── CreateProductInputPort        # Input port interface
│   ├── CreateProductUseCase          # Use case implementation
│   ├── CreateProductCommand          # Input model
│   └── CreateProductResult           # Output model
├── updateproductprice/               # Use case: Update Product Price
│   ├── UpdateProductPriceInputPort
│   ├── UpdateProductPriceUseCase
│   ├── UpdateProductPriceCommand
│   └── UpdateProductPriceResult
├── additemtocart/                    # Use case: Add Item to Cart
│   ├── AddItemToCartInputPort
│   ├── AddItemToCartUseCase
│   ├── AddItemToCartCommand
│   └── AddItemToCartResult
└── shared/                           # Shared output ports
    ├── ProductRepository             # Product repository interface
    └── ShoppingCartRepository        # Cart repository interface
```

**Benefits:**

1. **Use Cases as First-Class Citizens** - Use cases are at the top level, mirroring how bounded contexts are organized
2. **High Cohesion** - Everything related to one use case (port, implementation, models) is co-located
3. **Clear Shared Concerns** - The `shared/` folder makes cross-cutting dependencies explicit
4. **Simpler Navigation** - Flatter structure with fewer nested folders
5. **Mirrors Bounded Context Pattern** - Same organizational principle at both context and use case levels
6. **Feature-Oriented** - Easy to find and work on complete features

**Rules:**

1. Each use case has its own package in the `application` layer
2. Input port interface, use case implementation, and models reside together in the use case package
3. Shared output ports (repositories, gateways) are defined in `application.shared` package
4. Use cases may only orchestrate domain objects from their own bounded context
5. Cross-context coordination happens via domain events, not direct use case calls
6. Adapters depend on input ports, not on use case implementations directly
7. The `shared` folder concept mirrors the `sharedkernel` pattern at the bounded context level

**Location:**
- Product Use Cases: `dev.domaincentric.sample.ecommerce.product.application.{usecasename}` (flat — few use cases)
- Cart Use Cases: `dev.domaincentric.sample.ecommerce.cart.application.{feature}.{usecasename}` — grouped into the
  features `shopping`, `cartrecovery`, `cartcheckout`, `operations`
- Checkout Use Cases: `dev.domaincentric.sample.ecommerce.checkout.application.{feature}.{usecasename}` — grouped into
  `session`, `checkoutcompletion`, `cartsync`
- Shared Output Ports: `dev.domaincentric.sample.ecommerce.{context}.application.shared` (context-wide, never per feature)

**Features.** A feature is an optional, domain-named group of related use cases inside one bounded context —
a navigation boundary below the layer, not a layer, module or aggregate owner. A context uses either the flat
form `application/{usecase}` or the grouped form `application/{feature}/{usecase}`, never both (`DCA-USE-014`);
the feature (or, in a flat context, use-case) packages below `application` must be free of cycles
(`DCA-CYC-005`). Incoming adapters may mirror features *below* their protocol (`adapter/incoming/web/{feature}`),
the domain is organised by concept and never mirrored by feature. Cart and Checkout are grouped because their
flat lists had grown past a dozen entries; the other contexts stay flat.

### Relationship to Hexagonal Architecture

In Hexagonal Architecture terminology:
- **UseCase Interface** = Base contract for all Input Ports (Primary Ports)
- **Use Case Classes** = Concrete Input Ports (e.g., CreateProductUseCase, GetCartByIdUseCase)
- **Input/Output Models** = Port Data Structures
- **Use Case Implementations** = Application Core
- **REST Controllers** = Primary Adapters (invoke use cases via dependency injection)

This alignment ensures the patterns work together cohesively. Clients inject specific use case instances they need, maintaining Interface Segregation despite using a generic contract.

---

## Hexagonal Architecture

Hexagonal Architecture (Ports and Adapters) separates the core business logic from external concerns through explicit ports and adapters.

### Core Concepts

#### Ports

Ports are interfaces that define how the application can be used or how it can use external systems.

**Primary Ports (Driving Side):**
- Application services that define use cases
- Location: `dev.domaincentric.sample.ecommerce.application`

**Secondary Ports (Driven Side):**
- Repository interfaces and other output ports
- Location: `dev.domaincentric.sample.ecommerce.{context}.application.shared` (see ADR-008)

#### Adapters

Adapters are implementations that connect external systems to the ports.

**Primary Adapters (Driving / Incoming):**
- REST Controllers, Web MVC, MCP Server
- Location: `dev.domaincentric.sample.ecommerce.{boundedcontext}.adapter.incoming`

**Secondary Adapters (Driven / Outgoing):**
- Repository implementations
- Location: `dev.domaincentric.sample.ecommerce.{boundedcontext}.adapter.outgoing`

### Example: Product Management Flow

1. **Primary Adapter (REST Controller)** receives HTTP request
   ```java
   @RestController
   @RequestMapping("/api/products")
   public class ProductResource {
       private final UpdateProductPriceInputPort updateProductPriceInputPort;

       @PutMapping("/{id}/price")
       public ResponseEntity<ProductDto> updatePrice(
           @PathVariable String id,
           @RequestBody UpdatePriceRequest request) {

           UpdateProductPriceCommand command = new UpdateProductPriceCommand(
               id, request.newAmount(), request.currency());
           UpdateProductPriceResponse response = updateProductPriceInputPort.execute(command);

           return ResponseEntity.ok(ProductConverter.toDto(response));
       }
   }
   ```

2. **Primary Port (Input Port Interface)** defines use case contract
   ```java
   public interface UpdateProductPriceInputPort
       extends InputPort<UpdateProductPriceCommand, UpdateProductPriceResponse> {
       UpdateProductPriceResponse execute(UpdateProductPriceCommand input);
   }
   ```

3. **Use Case Implementation** executes business logic
   ```java
   @Service
   public class UpdateProductPriceUseCase implements UpdateProductPriceInputPort {
       private final ProductRepository productRepository;  // Output Port
       private final DomainEventPublisher eventPublisher;  // Output Port

       public UpdateProductPriceResponse execute(UpdateProductPriceCommand input) {
           Product product = productRepository.findById(ProductId.of(input.productId()))
               .orElseThrow(() -> new IllegalArgumentException("Product not found"));

           product.changePrice(new Price(new Money(input.newPriceAmount(),
               Currency.getInstance(input.newPriceCurrency()))));

           productRepository.save(product);
           eventPublisher.publishAndClearEvents(product);

           return new UpdateProductPriceResponse(...);
       }
   }
   ```

4. **Secondary Port (Output Port Interface)** defines infrastructure contract
   ```java
   public interface ProductRepository extends OutputPort, Repository<Product, ProductId> {
       Product save(@NonNull Product product);
       Optional<Product> findById(@NonNull ProductId id);
   }
   ```

5. **Secondary Adapter (Repository Implementation)** persists data
   ```java
   @Repository
   public class InMemoryProductRepository implements ProductRepository {
       private final ConcurrentHashMap<ProductId, Product> products = new ConcurrentHashMap<>();

       @Override
       public Product save(Product product) {
           products.put(product.id(), product);
           return product;
       }

       @Override
       public Optional<Product> findById(ProductId id) {
           return Optional.ofNullable(products.get(id));
       }
   }
   ```

### Benefits

1. **Testability**: Core business logic can be tested without external dependencies
2. **Flexibility**: Adapters can be swapped without changing core logic
3. **Technology Independence**: Business logic doesn't depend on frameworks
4. **Clear Boundaries**: Explicit separation of concerns

### Advanced Adapter Patterns

#### Composite Adapter Pattern

A composite adapter aggregates data from multiple external sources (bounded contexts) through a single output port. This isolates cross-context coupling to the adapter layer.

**Example: CompositeCheckoutArticleDataAdapter**

```java
@Component
public class CompositeCheckoutArticleDataAdapter implements CheckoutArticleDataPort {
    private final ProductCatalogService productCatalogService;  // OHS
    private final PricingService pricingService;                // OHS
    private final InventoryService inventoryService;            // OHS

    @Override
    public Map<ProductId, CheckoutArticle> getArticleData(Collection<ProductId> productIds) {
        // Fetch from all three Open Host Services
        Map<ProductId, PriceInfo> prices = pricingService.getPrices(productIds);
        Map<ProductId, StockInfo> stocks = inventoryService.getStock(productIds);

        Map<ProductId, CheckoutArticle> result = new HashMap<>();
        for (ProductId productId : productIds) {
            Optional<ProductInfo> productInfo = productCatalogService.getProductInfo(productId);
            if (productInfo.isPresent()) {
                result.put(productId, buildCheckoutArticle(productId, productInfo.get(),
                    prices.get(productId), stocks.get(productId)));
            }
        }
        return result;
    }
}
```

**Rules:**
1. Implements a single output port from the consuming context
2. Delegates to Open Host Services (OHS) from other bounded contexts
3. Translates external DTOs into context-specific domain value objects
4. Is the ONLY place in the context that imports from external contexts
5. Resides in `adapter/outgoing` package

**Benefits:**
- Single integration point for cross-context data
- Application layer remains ignorant of external context structure
- Easy to mock for testing
- Changes to external services only affect this adapter

**Implementation:** `dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.product.CompositeCheckoutArticleDataAdapter`

#### Enriched Read Model Pattern

An enriched read model combines aggregate data with externally-sourced data to enable domain logic that spans multiple data sources. The enrichment happens at the domain level, not in adapters.

**Example: EnrichedCheckoutLineItem**

```java
public record EnrichedCheckoutLineItem(
    @NonNull CheckoutLineItem lineItem,      // From aggregate
    @NonNull CheckoutArticle currentArticle  // From external context
) implements Value {

    // Domain logic using both data sources
    public boolean hasPriceChanged() {
        return !currentArticle.currentPrice().equals(lineItem.unitPrice());
    }

    public boolean hasSufficientStock() {
        return currentArticle.hasStockFor(lineItem.quantity());
    }

    public boolean isValidForCheckout() {
        return currentArticle.isAvailable() && hasSufficientStock();
    }

    public Money currentLineTotal() {
        return currentArticle.currentPrice().multiply(lineItem.quantity());
    }
}
```

**Example: CheckoutCart (Enriched Collection)**

```java
public record CheckoutCart(
    @NonNull CartId cartId,
    @NonNull CustomerId customerId,
    @NonNull List<EnrichedCheckoutLineItem> items
) implements Value {

    public boolean isValidForCheckout() {
        return !items.isEmpty() && items.stream()
            .allMatch(EnrichedCheckoutLineItem::isValidForCheckout);
    }

    public boolean hasAnyPriceChanges() {
        return items.stream().anyMatch(EnrichedCheckoutLineItem::hasPriceChanged);
    }

    public Money calculateCurrentSubtotal() {
        return items.stream()
            .map(EnrichedCheckoutLineItem::currentLineTotal)
            .reduce(Money.zero(DEFAULT_CURRENCY), Money::add);
    }
}
```

**Rules:**
1. Enriched models are Value Objects (immutable records)
2. Combine aggregate data with context-specific translations of external data
3. Domain logic operates on combined data without knowing its origin
4. Validation ensures data consistency (e.g., matching ProductIds)
5. External data is represented as context-local value objects (e.g., `CheckoutArticle`)

**Benefits:**
- Rich domain logic spanning multiple data sources
- Clear separation: assembly in application layer, logic in domain
- Immutable, testable value objects
- No direct coupling to external bounded contexts

**Implementation:**
- `dev.domaincentric.sample.ecommerce.checkout.domain.model.EnrichedCheckoutLineItem`
- `dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutCart`
- `dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutArticle`

#### Factory for Cross-Context Assembly

A factory encapsulates the complex assembly logic for creating enriched domain objects from multiple data sources. This keeps use cases thin and assembly logic testable.

**Example: CheckoutCartFactory**

```java
public final class CheckoutCartFactory implements Factory {

    public CheckoutCart create(
        @NonNull CartId cartId,
        @NonNull CustomerId customerId,
        @NonNull List<CheckoutLineItem> lineItems,
        @NonNull Map<ProductId, CheckoutArticle> articleData) {

        validateArticleDataComplete(lineItems, articleData);
        List<EnrichedCheckoutLineItem> enrichedItems = createEnrichedItems(lineItems, articleData);
        return CheckoutCart.of(cartId, customerId, enrichedItems);
    }

    public CheckoutCart fromSession(
        @NonNull CheckoutSession session,
        @NonNull Map<ProductId, CheckoutArticle> articleData) {
        return create(session.cartId(), session.customerId(),
            session.lineItems(), articleData);
    }

    private void validateArticleDataComplete(
        List<CheckoutLineItem> lineItems,
        Map<ProductId, CheckoutArticle> articleData) {
        // Ensure all line items have corresponding article data
    }

    private List<EnrichedCheckoutLineItem> createEnrichedItems(
        List<CheckoutLineItem> lineItems,
        Map<ProductId, CheckoutArticle> articleData) {
        // Pair each line item with its article data
    }
}
```

**Rules:**
1. Implements `Factory` marker interface
2. Validates completeness of data before assembly
3. Creates enriched domain objects by combining data sources
4. Framework-independent (no Spring annotations)
5. Resides in domain layer

**Benefits:**
- Keeps use cases thin (orchestration only)
- Assembly logic is testable in isolation
- Single responsibility for complex object creation
- Validates data completeness at assembly time

**Implementation:** `dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutCartFactory`

---

## Onion Architecture

Onion Architecture ensures that dependencies flow inward toward the domain core, with no outward dependencies from inner layers.

### Layers

```
┌─────────────────────────────────────┐
│  Infrastructure & Adapters          │  ← Outermost layer
│  (adapter, infrastructure)          │
├─────────────────────────────────────┤
│  Application Services                │  ← Use cases
│  (application)                       │
├─────────────────────────────────────┤
│  Domain Model                        │  ← Core business logic
│  (domain.model)                      │
└─────────────────────────────────────┘
       ↓ Dependencies flow inward
```

#### Domain Model (Core)

**Location:** `dev.domaincentric.sample.ecommerce.{context}.domain.model`

**Contains:**
- Aggregates, Entities, Value Objects
- Domain Services
- Domain Events
- Factories, Specifications

(Repository interfaces are NOT part of the domain model — they are output ports in the application layer, see ADR-008.)

**Rules:**
- NO dependencies on outer layers
- NO framework dependencies (Spring, JPA, etc.)
- Pure business logic

#### Application Layer

**Location:** `dev.domaincentric.sample.ecommerce.application`

**Contains:**
- Application Services (use cases)
- Use case orchestration

**Rules:**
- Depends ONLY on domain model
- May use sharedkernel.application.port (outbound ports)
- NO dependencies on adapters
- NO infrastructure implementation details

#### Infrastructure & Adapters

**Location:**
- `dev.domaincentric.sample.ecommerce.{boundedcontext}.adapter.incoming` (REST, Web, MCP, event adapters)
- `dev.domaincentric.sample.ecommerce.{boundedcontext}.adapter.outgoing` (Repository implementations)
- `dev.domaincentric.sample.ecommerce.infrastructure` (Spring configuration)

**Contains:**
- REST Controllers and DTOs
- Repository implementations
- Framework configuration

**Rules:**
- Depends on application and domain layers
- Contains framework-specific code
- Implements ports defined in inner layers

#### Shared Kernel Application Ports

**Location:** `dev.domaincentric.sample.ecommerce.sharedkernel.application.port`

The `sharedkernel.application.port` package contains **outbound ports** (interfaces) used across all bounded contexts, making it part of the Shared Kernel (Strategic DDD pattern).

**Purpose:**
- Provides port interfaces shared across multiple bounded contexts
- Enables the application layer to remain framework-independent
- Acts as "ports" in Hexagonal Architecture terminology
- Part of the Shared Kernel (shared abstractions used by all contexts)

**Pattern:**
```
Application Layer → sharedkernel.application.port (interface) ← infrastructure/adapters (implementation)
```

**Interfaces in this package:**
- `Repository<T, ID>` - Base repository interface for all aggregate repositories
- `UseCase<INPUT, OUTPUT>` - Base interface for input ports (use cases)
- `DomainEventPublisher` - Interface for publishing domain events

**Example:**
```java
// dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher (outbound port)
public interface DomainEventPublisher extends OutputPort {
    void publish(DomainEvent event);
    void publishAndClearEvents(AggregateRoot<?, ?> aggregate);
}

// sharedkernel.adapter.outgoing.event.SpringDomainEventPublisher (adapter implementation)
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher eventPublisher;
    // Spring-specific implementation...
}
```

**Rules:**
1. **dev.domaincentric.dca.buildingblocks.hexagonal.port must contain ONLY interfaces** (enforced by ArchUnit)
2. No concrete classes, no annotations, no framework dependencies
3. Implementations reside in `sharedkernel.adapter` or context adapter packages
4. Application layer may depend on `dev.domaincentric.dca.buildingblocks.hexagonal.port`, never on implementations
5. Only ports used by **multiple bounded contexts** belong here (not context-specific ports)

**Benefits:**
- Application layer remains testable (easy to mock interfaces)
- Can swap infrastructure implementations without changing application code
- Follows Dependency Inversion Principle (depend on abstractions, not concretions)
- Supports framework-independent business logic
- Shared Kernel pattern reduces duplication across bounded contexts

**What Belongs in dev.domaincentric.dca.buildingblocks.hexagonal.port:**
- ✅ Base Repository interface (used by all aggregate repositories)
- ✅ Base UseCase interface (input port marker)
- ✅ DomainEventPublisher (used by all application services)
- ✅ Other abstractions needed across multiple bounded contexts

**What Does NOT Belong:**
- ❌ Concrete classes
- ❌ Spring annotations (@Component, @Service)
- ❌ Framework-specific code
- ❌ Context-specific interfaces (those belong in the context's application layer)

---

## Layered Architecture

Traditional layered architecture with strict dependency rules.

### Layer Dependencies

```
Primary Adapters (Web)
      ↓
Application Services
      ↓
Domain Model
      ↑
Secondary Adapters (Persistence)
```

**Rules:**
1. **Domain** may not depend on any other layer
2. **Application** may depend on Domain only
3. **Primary Adapters** may depend on Application and Domain
4. **Secondary Adapters** implement interfaces from Domain
5. **Adapters may NOT communicate directly** with each other

---

## Package Structure

The top level is organised by bounded context, not by layer — each context carries its own
domain, application and adapter layers:

```
dev.domaincentric.sample.ecommerce
├── sharedkernel/            # Markers, universal value objects, shared adapters
├── {boundedcontext}/        # product, cart, checkout, account, portal,
│   ├── domain/              # inventory, pricing, backoffice
│   ├── application/
│   └── adapter/
│       ├── incoming/
│       └── outgoing/
└── infrastructure/          # Global, cross-cutting framework configuration
```

Inside a context:

```
{boundedcontext}/
├── domain/
│   ├── model/               # Aggregates, entities, value objects, enriched models
│   ├── readmodel/           # Optional: read model types
│   ├── specification/       # Optional: specifications
│   ├── service/             # Domain services
│   └── event/               # Domain events
├── application/
│   ├── {usecasename}/       # One folder per use case (lowercase)
│   │   ├── *InputPort.java
│   │   ├── *UseCase.java
│   │   ├── *Command.java / *Query.java
│   │   └── *Result.java
│   └── shared/              # Shared output ports (repositories, stores, data ports)
├── api/                     # Published in-process interface (Open Host Service)
├── events/                  # Published integration events
└── adapter/                 # Sub-packages are a convention, no rule checks them
    ├── incoming/            # api/ (REST), web/, mcp/, event/
    └── outgoing/            # persistence/, event/, client/
```

See [package-structure.md](package-structure.md) for the full tree, the per-context breakdown and
the file-location quick reference.

---

## Bounded Contexts

### Shared Kernel

**Responsibility:** Universal domain concepts shared across contexts

**Pattern:** Shared Kernel (DDD Strategic Pattern)

**Value Objects:**
- `Money` - monetary value with currency (universal concept)
- `ProductId` - product identifier (cross-context reference)
- `Price` - wraps Money with domain-specific validation

**Design Decision:**
- **Why Shared Kernel?** Ensures consistency for universal concepts across contexts
- **Trade-off:** Creates coupling but prevents duplication and inconsistency
- **Alternative Considered:** Separate Ways (duplicate in each context) - rejected due to high risk of currency handling bugs
- **References:** Eric Evans (DDD Chapter 14), Vaughn Vernon (Implementing DDD Chapter 2)

**Rules:**
- Must remain small and carefully curated
- No dependencies on specific bounded contexts
- Only universal concepts with consistent meaning

### Product Catalog Context

**Responsibility:** Product management, pricing, inventory

**Aggregates:**
- `Product` - manages product information, pricing, and stock

**Key Value Objects:**
- `SKU` - stock keeping unit
- `ProductName` - product name
- `ProductDescription` - product description
- `Category` - product category

**Dependencies:**
- Shared Kernel: `Money`, `ProductId`, `Price`

**Use Cases:**
- Create product
- Update product price
- Update product stock
- Find products by category
- Check product availability

### Shopping Cart Context

**Responsibility:** Shopping cart operations and checkout

**Aggregates:**
- `ShoppingCart` - manages cart items and checkout process

**Entities:**
- `CartItem` - item within shopping cart

**Key Value Objects:**
- `CartId` - unique cart identifier
- `CustomerId` - customer identifier
- `Quantity` - item quantity
- `CartStatus` - cart state (ACTIVE, CHECKED_OUT, ABANDONED)

**Dependencies:**
- Shared Kernel: `Money`, `ProductId`, `Price`

**Use Cases:**
- Create cart
- Add item to cart
- Update item quantity
- Remove item from cart
- Checkout cart
- Calculate cart total

**Integration with Product Context:**
- Cart references Product by `ProductId` only (from Shared Kernel)
- Does NOT access Product aggregate directly
- Complete isolation enforced by ArchUnit tests
- Uses `Price` from Shared Kernel for price snapshots

---

## Architectural Rules

### Enforced by ArchUnit Tests

All architectural rules are automatically tested and enforced using ArchUnit.

#### DDD Strategic Patterns (Bounded Contexts)

These rules discover contexts dynamically from `@BoundedContext`, so a context added tomorrow is
covered without being registered anywhere. No rule names a context.

1. **Shared Kernel must be context-independent** - no dependencies on any bounded context
2. **No context's domain layer may reach another context** - not even the other context's `api/`;
   translating an Open Host Service is the application layer's or an adapter's job
3. **No context's application layer may reach another context directly** - define output ports and
   use adapters
4. **Outgoing adapters may only use another context's `api/` or `events/`** - never its domain or
   application layer
5. **Every context may access the Shared Kernel** - it carries `@SharedKernel`, not
   `@BoundedContext`, so it never appears among a rule's forbidden targets
6. **Shared Kernel must be minimal** - only universal concepts with consistent meaning

All of these use `dependOnClassesThat`, not `accessClassesThat`. ArchUnit counts an *access* as a
method call or field access, so a field, parameter or record component of a foreign type is not an
access — it is a dependency. An isolation rule written with `accessClassesThat` stays green while a
class holds the forbidden type outright.

#### DDD Tactical Patterns

1. **Aggregate Roots** must implement `AggregateRoot` interface
2. **Entities** must implement `Entity` interface
3. **Value Objects** must be immutable (records or final classes)
4. **Value Objects** must implement `Value` interface
5. **Repositories** must be interfaces in the application layer (output ports, see ADR-008)
6. **Repository implementations** must be in outgoing adapters
7. **Aggregates reference other aggregates by ID only** (Vernon's Rule #2)
8. **Aggregates must not hold references to repositories or output ports** - dependencies are passed as method parameters
9. **Domain model classes must not have public setters** - state changes go through intention-revealing methods
10. **Repository methods must not expose a non-root Entity** - checked recursively through type
    arguments, so `Optional<CartItem>` and `List<CartItem>` fail too. Deliberately a prohibition:
    a boolean, a count, a `PageResult` or an Enriched Domain Model (ADR-021) are legitimate returns
11. **Stores** must extend the `Store` marker (not `Repository`), live in `application.shared`, keep their implementation in `adapter.outgoing`, and must not declare `findById`/`save`

#### Domain Layer Rules

1. Domain must NOT depend on infrastructure
2. Domain must NOT depend on adapters
3. Domain must NOT use Spring annotations
4. Domain must NOT use JPA annotations
5. Domain must be framework-independent

#### Application Layer Rules

1. Use Cases (InputPort implementations) must end with "UseCase"
2. Use Cases must be annotated with `@Service`
3. Application layer must NOT depend on adapters
4. Application layer may only use ports (not infrastructure implementations)

#### Clean Architecture (Use Case) Rules

1. InputPort interfaces must end with "InputPort"
2. Use Case classes must reside in `application` package
3. Commands must end with "Command" and be immutable (records or final classes)
4. Queries must end with "Query" and be immutable
5. Results must end with "Result" and be immutable
6. Commands, Queries, and Results must reside in `application` package
7. HTTP Response models must end with "Response" and reside in incoming adapters
8. Application layer must NOT depend on DTOs (presentation concern)
9. Command/Query/Result models must contain only primitives, Strings, value types, or nested records (no domain entities)
10. Results must not expose aggregate roots or entities — checked transitively through nested and part records and generic arguments (`DCA-USE-015`)

#### Hexagonal Architecture Rules

1. Primary adapters may only call application services
2. Secondary adapters implement domain repository interfaces
3. Adapters must NOT communicate directly with each other
4. Repository implementations must be in `adapter.outgoing`
5. Controllers and Resources must never access repositories directly - they drive the application through input ports only
6. Incoming adapters must not depend on domain services (`DCA-HEX-012`) - they read and format the result; the use case owns the domain collaboration. Outgoing adapters may construct and reconstitute domain objects

#### Onion Architecture Rules

1. Dependencies flow inward toward domain
2. Domain layer has NO outward dependencies
3. Application layer depends only on domain
4. Outer layers depend on inner layers

#### Layered Architecture Rules

1. Infrastructure may not be accessed by any layer
2. Primary adapters may not be accessed by any layer
3. Secondary adapters may not be accessed by any layer
4. Application services may only be accessed by primary adapters
5. **sharedkernel.application.port must contain only interfaces** (Shared Kernel outbound ports pattern)
6. **`@Transactional` only in the application layer** - the use case owns the unit of work; outgoing persistence adapters are the documented exception (multi-statement atomicity, joins the caller's transaction via REQUIRED propagation). A `@Transactional` use case calls no remote-capable port (`DCA-USE-013`); such use cases draw the boundary with `TransactionBoundary.inTransaction` after the remote reads (ADR-034)

#### Naming Conventions

1. Use Cases must end with "UseCase"
2. Repository interfaces must end with "Repository"
3. Controllers must end with "Controller", REST controllers with "Resource"
4. DTOs must end with "Dto" and reside in adapter packages
5. Converters must end with "Converter" and reside in adapter packages
6. Domain Services must implement `DomainService`
7. Domain Events must implement `DomainEvent`
8. Factories must implement `Factory`
9. Specifications must end with "Specification"
10. No technical bucket packages (`entities`, `valueobjects`, `helpers`, `util`) - package by domain concept
11. No `Manager`/`Helper`/`Util`/`Impl` suffixes in the domain layer - name by specialty from the ubiquitous language

#### Advanced DDD Patterns

1. **Domain Events** must be immutable (final or records)
2. **Domain Events** must have timestamp field
3. **Domain Events** must be framework-independent
4. **Domain Services** must be stateless (only final fields)
5. **Domain Services** must be framework-independent
6. **Factories** must be in domain.model package
7. **Factories** must be stateless
8. **Specifications** must be framework-independent

---

## References

### Books

1. **Domain-Driven Design: Tackling Complexity in the Heart of Software** by Eric Evans
   - Original DDD book defining strategic and tactical patterns

2. **Implementing Domain-Driven Design** by Vaughn Vernon
   - Practical guide to implementing DDD patterns
   - Aggregate design rules and patterns

3. **Hexagonal Architecture** by Alistair Cockburn
   - Original description of Ports and Adapters pattern

### Key Concepts

- **Ubiquitous Language**: Shared language between developers and domain experts
- **Bounded Context**: Explicit boundary for model validity
- **Aggregate**: Cluster of objects treated as a unit, owns state mutations
- **Repository**: Collection-like interface for aggregates
- **Domain Event**: Something that happened in the domain
- **Value Object**: Immutable descriptor without identity
- **Entity**: Object with distinct identity
- **Aggregate Root**: Entry point to aggregate
- **Enriched Domain Model**: Domain object combining aggregate state with external context data; owns cross-context business rules

### Design Principles

- **Dependency Inversion Principle**: High-level modules should not depend on low-level modules
- **Single Responsibility Principle**: Each class should have one reason to change
- **Open/Closed Principle**: Open for extension, closed for modification
- **Separation of Concerns**: Different concerns in different modules
- **Tell, Don't Ask**: Objects should tell other objects what to do

---

## Conclusion

This architecture provides:

1. **Maintainability**: Clear separation of concerns and dependencies
2. **Testability**: Business logic can be tested without infrastructure
3. **Flexibility**: Technology decisions can be changed without affecting business logic
4. **Scalability**: Clear boundaries enable team scaling
5. **Evolvability**: Domain model can evolve with business requirements

The combination of DDD, Hexagonal Architecture, and Onion Architecture creates a robust foundation for building complex enterprise applications that remain maintainable over time.
