# ADR-011: Bounded Context Isolation via Package Structure

**Date**: October 24, 2025
**Status**: ✅ Accepted — amended 2026-09-16 (see the amendment at the end)
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐

---

## Context

Strategic DDD emphasizes **Bounded Contexts** - explicit boundaries within which a domain model exists and terms have specific meaning.

Without clear boundaries:
- Terms have ambiguous meanings
- Models become entangled
- Changes ripple uncontrollably
- Team ownership is unclear

---

## Decision

**Each Bounded Context resides in its own top-level package with strict isolation rules:**

1. **No direct object references across contexts**
2. **Reference other contexts only by ID (via Shared Kernel)**
3. **Integration via Domain Events**
4. **Complete package structure per context** (domain, application, adapter)

### Package Structure

```
dev.domaincentric.sample.ecommerce/
├── sharedkernel/              ← Shared Kernel (cross-context)
│   └── domain/model/
│       ├── ProductId          (shared ID)
│       ├── Money              (shared value)
│       └── Price              (shared value)
│
├── product/                   ← Product Catalog Context
│   ├── domain/model/
│   │   ├── Product            (aggregate)
│   │   ├── SKU                (value object)
│   │   ├── ProductName
│   │   ├── ProductStock
│   │   └── Category
│   ├── domain/event/
│   │   ├── ProductCreated
│   │   └── ProductPriceChanged
│   ├── application/
│   │   └── port/out/
│   │       └── ProductRepository
│   └── adapter/
│       ├── incoming/
│       └── outgoing/
│
└── cart/                      ← Shopping Cart Context
    ├── domain/model/
    │   ├── ShoppingCart       (aggregate)
    │   ├── CartItem           (entity)
    │   ├── CartId             (value object)
    │   ├── Quantity
    │   └── CartStatus
    │   # Uses ProductId from sharedkernel (reference only)
    ├── domain/event/
    │   ├── CartItemAddedToCart
    │   └── CartCheckedOut
    ├── application/
    │   └── port/out/
    │       └── ShoppingCartRepository
    └── adapter/
        ├── incoming/
        └── outgoing/
```

### Context Map

```
┌─────────────────────┐         ┌──────────────────────┐
│  Shopping Cart      │ ────────>│  Product Catalog     │
│  Context            │  U/D     │  Context             │
└─────────────────────┘         └──────────────────────┘
       (uses ProductId reference only)

U/D = Upstream/Downstream (Customer-Supplier relationship)
```

---

## Rationale

### 1. **Clear Boundaries**

Each context has clear responsibility:

**Product Catalog Context**:
- Purpose: Manage product catalog with pricing and inventory
- Ubiquitous Language: Product, SKU, Price, Category, Stock
- Core Aggregate: Product

**Shopping Cart Context**:
- Purpose: Manage customer shopping carts
- Ubiquitous Language: Cart, CartItem, Customer, Quantity
- Core Aggregate: ShoppingCart

### 2. **Reference by ID Only (via Shared Kernel)**

```java
// cart/domain/model/CartItem.java
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;  // ← From Shared Kernel
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;     // ← From Shared Kernel
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;     // ← From Shared Kernel

public final class CartItem implements Entity<CartItem, CartItemId> {

  private final ProductId productId;  // ✅ ID from Shared Kernel (reference only)
  // NOT: private final Product product; ❌

  public Money totalPrice(final Price productPrice) {
    // Price passed as parameter from Product Context
    return productPrice.multiply(quantity.value());
  }
}
```

**Key:**
- `ProductId`, `Price`, `Money` are in **Shared Kernel** (cross-context)
- Cart references Product ONLY via `ProductId`
- No direct `Product` aggregate reference

### 3. **Integration via Events**

```java
// Product Context raises event
@EventListener
public void on(ProductPriceChanged event) {
  // Cart Context can react
  // Invalidate cart totals for this product
  // Notify customers with product in cart
}
```

---

## Consequences

### Positive

✅ **Clear Ownership**: Each context has clear boundaries
✅ **Independent Evolution**: Contexts can evolve separately
✅ **Team Organization**: Teams can own specific contexts
✅ **Reduced Coupling**: No direct dependencies between contexts
✅ **Scalability**: Contexts can become microservices later

### Neutral

⚠️ **Coordination Needed**: Application services coordinate contexts
⚠️ **Eventual Consistency**: Events provide eventual consistency

### Negative

❌ **None identified**

---

## Implementation

### Current Bounded Contexts

**Shared Kernel** (`sharedkernel/`):
- DDD marker interfaces (AggregateRoot, Entity, Value, Repository, etc.)
- Cross-context value objects (ProductId, Money, Price)
- Use case pattern interfaces (InputPort, OutputPort)

**Product Catalog Context** (`product/`):
- 15+ classes (domain, application, adapters)
- Aggregate: Product
- Repository: ProductRepository
- Events: ProductCreated, ProductPriceChanged
- Depends on: Shared Kernel

**Shopping Cart Context** (`cart/`):
- 11+ classes (domain, application, adapters)
- Aggregate: ShoppingCart
- Entity: CartItem
- Events: CartItemAddedToCart, CartCheckedOut
- Depends on: Shared Kernel

**Portal Context** (`portal/`):
- Minimal context for home page
- Adapter-only (no domain model)

### ArchUnit Enforcement

```groovy
def "Bounded Contexts must not directly access each other (except via Shared Kernel)"() {
  expect:
  // Cart must not access Product context directly
  noClasses()
    .that().resideInAPackage("..cart..")
    .should().dependOnClassesThat().resideInAPackage("..product..")
  and:
  // Product must not access Cart context directly
  noClasses()
    .that().resideInAPackage("..product..")
    .should().dependOnClassesThat().resideInAPackage("..cart..")
    .check(allClasses)
}

def "Contexts can depend on Shared Kernel"() {
  // Both contexts may access sharedkernel
  // This is allowed and enforced by other tests
}

def "Contexts should only reference via IDs"() {
  // Enforced by aggregate reference rules (ADR-003)
  // ProductId in Shared Kernel allows cart to reference product by ID
}
```

---

## References

- **Evans - DDD**: "Bounded Contexts are explicit boundaries"
- **Vernon - Implementing DDD**: "Context mapping shows relationships"

### Related ADRs

- [ADR-003: Aggregate Reference by Identity Only](adr-003-aggregate-reference-by-id.md)
- [ADR-005: Domain Events Publishing Strategy](adr-005-domain-events-publishing.md)
- [ADR-016: Shared Kernel Pattern](adr-016-shared-kernel-pattern.md)

---

**Approved by**: Architecture Team
**Date**: October 24, 2025
**Version**: 1.0

## 2026-09-16 amendment: the Cart context no longer raises `CartCheckedOut`

The Cart inventory above lists `CartCheckedOut` among the context's domain events. That event, its
integration twin `CartCheckedOutEvent`, the `CheckoutCart` use case and `POST /api/carts/{id}/checkout`
were removed: the checkout is started by the Checkout context, which reads the cart through Cart's Open
Host Service and captures a snapshot into a `CheckoutSession`. The Cart context reacts only to the
confirmation (`CartCompletionTrigger`). The isolation decision itself is unchanged.
