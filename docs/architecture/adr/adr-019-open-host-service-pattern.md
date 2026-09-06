# ADR-019: Open Host Service Pattern for Cross-Context Communication

**Date**: January 31, 2026
**Status**: ✅ Accepted
**Deciders**: Architecture Team
**Priority**: ⭐⭐⭐⭐

---

## Context

In our e-commerce application, bounded contexts sometimes need synchronous access to data from other contexts. For example, when adding an item to a cart, the Cart context needs to retrieve product information (price, stock availability) from the Product Catalog context.

### DDD Open Host Service Pattern

In Domain-Driven Design, the **Open Host Service** pattern is typically implemented as a **REST API** that exposes a bounded context's capabilities to external consumers. The canonical implementation uses HTTP endpoints with a well-defined published language.

### The Problem

The initial implementation had use cases directly importing from other bounded contexts:

```java
// ❌ BEFORE: Use case imported directly from Product context
package dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart;

import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;  // ❌ Cross-context!

@Service
public class AddItemToCartUseCase {
    private final ProductRepository productRepository;  // ❌ Application layer coupling!
}
```

**Problems:**
1. **Application layer coupling**: Use cases directly depend on other contexts
2. **Violation of hexagonal architecture**: Application layer bypasses adapters
3. **No isolation**: Changes to Product's repository interface affect Cart
4. **Unclear boundaries**: Cross-context dependencies hidden in use case code
5. **Testing difficulty**: Hard to test Cart without real Product context

---

## Decision

**Implement the Open Host Service pattern with provider-side OHS and consumer-side output ports/adapters.**

### Modulith vs Microservices Implementation

This reference implementation uses an **in-process service** rather than a REST API as an optimization for modulith deployment (single JVM, no network overhead). The canonical DDD pattern uses REST, but both approaches share the same consumer-side pattern:

| Aspect | REST API (Canonical DDD) | In-Process Service (This Impl) |
|--------|--------------------------|-------------------------------|
| Transport | HTTP | Direct method call |
| Location | `adapter/incoming/api/` | `adapter/incoming/openhost/` |
| Consumer adapter | Uses RestTemplate/WebClient | Injects service directly |
| Consumer's port | **Same pattern** | **Same pattern** |
| Migration to REST | N/A | Change adapter only |

The **consumer-side pattern (output port + adapter) is identical** regardless of transport. This allows easy migration from modulith to microservices by changing only the adapter implementation.

### Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                      PRODUCT CATALOG CONTEXT                        │
│                                                                     │
│  ┌─────────────┐    ┌─────────────────────────────────────────┐    │
│  │  Domain     │◄───│        Application Layer                │    │
│  │  (Product)  │    │  (ProductRepository, use cases)         │    │
│  └─────────────┘    └─────────────────────────────────────────┘    │
│                                    ▲                                │
│                                    │                                │
│  ┌─────────────────────────────────┴─────────────────────────────┐ │
│  │              adapter/incoming/openhost/                        │ │
│  │                                                                │ │
│  │  @OpenHostService(context = "Product Catalog")                 │ │
│  │  ProductCatalogService                                         │ │
│  │    - getProductInfo(ProductId) → ProductInfo DTO               │ │
│  │    - hasStock(ProductId, int) → boolean                        │ │
│  └────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────│─────────────────────────────────┘
                                    │
                                    │ (calls)
                                    ▼
┌───────────────────────────────────────────────────────────────────────┐
│                          SHOPPING CART CONTEXT                        │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐   │
│  │              adapter/outgoing/product/                         │   │
│  │                                                                │   │
│  │  ProductDataAdapter implements ProductDataPort                 │   │
│  │    - calls ProductCatalogService                               │   │
│  │    - ONLY place that imports from Product context              │   │
│  └──────────────────────────────────────────────────────┬────────┘   │
│                                                          │            │
│                                                          │ implements │
│                                                          ▼            │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │                     Application Layer                            │ │
│  │                                                                  │ │
│  │  ProductDataPort (output port)  ◄───  AddItemToCartUseCase      │ │
│  │    - getProductData(ProductId, qty)   (uses port, not OHS)      │ │
│  └─────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────────────┘
```

### Package Structure

```
dev.domaincentric.sample.ecommerce/
├── product/                              ← PRODUCT CATALOG CONTEXT
│   ├── domain/model/
│   ├── application/
│   │   └── shared/
│   │       └── ProductRepository.java    (output port)
│   └── adapter/
│       └── incoming/
│           └── openhost/                 ← OHS lives here
│               └── ProductCatalogService.java
│
└── cart/                                 ← SHOPPING CART CONTEXT
    ├── domain/model/
    ├── application/
    │   ├── additemtocart/
    │   │   └── AddItemToCartUseCase.java (uses ProductDataPort)
    │   └── shared/
    │       └── ProductDataPort.java      ← Consumer's output port
    └── adapter/
        └── outgoing/
            └── product/                  ← Adapter calls OHS
                └── ProductDataAdapter.java
```

---

## Rationale

### 1. **Open Host Service as Incoming Adapter**

The OHS is placed in `adapter/incoming/openhost/` because:
- Other contexts "call into" this context (incoming direction)
- It translates domain objects to DTOs (like REST controllers)
- It's a published API with a stable contract

**Eric Evans (DDD Chapter 14)**:
> "Define a protocol that gives access to your subsystem as a set of SERVICES. Open the protocol so that all who need to integrate with you can use it."

### 2. **Consumer Owns Its Port (Interface Segregation)**

The consuming context (Cart) defines its own `ProductDataPort`:
- Cart specifies exactly what it needs
- Cart is not coupled to Product's full API
- Product can evolve OHS without breaking Cart

```java
// Cart defines what IT needs (Interface Segregation)
public interface ProductDataPort extends OutputPort {
    record ProductData(ProductId productId, Price price, boolean hasStock) {}
    Optional<ProductData> getProductData(ProductId productId, int requestedQuantity);
}
```

### 3. **Adapter Isolates Cross-Context Coupling**

All cross-context imports are confined to the adapter layer:

```java
// ProductDataAdapter - ONLY place in Cart that imports from Product
import dev.domaincentric.sample.ecommerce.product.adapter.incoming.openhost.ProductCatalogService;

@Component
public class ProductDataAdapter implements ProductDataPort {
    private final ProductCatalogService productCatalogService;
    // delegates to OHS, translates to Cart's types
}
```

### 4. **Use Cases Stay Pure**

Use cases only know their own context's ports:

```java
// AddItemToCartUseCase - NO imports from Product context
public class AddItemToCartUseCase {
    private final ProductDataPort productDataPort;  // Own port, not OHS

    public AddItemToCartResponse execute(AddItemToCartCommand input) {
        ProductData data = productDataPort.getProductData(productId, qty);
        // ...
    }
}
```

---

## Consequences

### Positive

✅ **Application layer isolation**: Use cases don't import from other contexts
✅ **Clear contracts**: OHS defines stable published API
✅ **Interface segregation**: Consumer defines exactly what it needs
✅ **Testability**: Easy to mock `ProductDataPort` in Cart tests
✅ **Single point of coupling**: All cross-context imports in one adapter
✅ **Hexagonal compliance**: Follows ports and adapters pattern correctly

### Neutral

⚠️ **More files**: Output port + adapter per consumed context
⚠️ **Explicit wiring**: Must wire adapter to port in configuration
⚠️ **Translation layer**: Adapter translates OHS DTOs to consumer's types

### Negative

❌ **Additional indirection**: Call goes through port and adapter
- **Mitigation**: Indirection is the cost of loose coupling
- **Trade-off Accepted**: Isolation benefits outweigh indirection cost

❌ **Synchronous coupling**: Consumer waits for provider response
- **Mitigation**: Use events for eventual consistency where possible
- **Justified**: Synchronous needed for validation before cart update

---

## Implementation

### Provider: Open Host Service

```java
package dev.domaincentric.sample.ecommerce.product.adapter.incoming.openhost;

@OpenHostService(
    context = "Product Catalog",
    description = "Provides product information for other bounded contexts"
)
@Service
public class ProductCatalogService {

    private final ProductRepository productRepository;

    // DTO - never expose domain objects
    public record ProductInfo(
        ProductId productId,
        String name,
        Price price,
        int availableStock
    ) {}

    public Optional<ProductInfo> getProductInfo(ProductId productId) {
        return productRepository.findById(productId)
            .map(product -> new ProductInfo(
                productId,
                product.name().value(),
                product.price(),
                product.stock().quantity()
            ));
    }

    public boolean hasStock(ProductId productId, int quantity) {
        return productRepository.findById(productId)
            .map(product -> product.hasStockFor(quantity))
            .orElse(false);
    }
}
```

### Consumer: Output Port

```java
package dev.domaincentric.sample.ecommerce.cart.application.shared;

public interface ProductDataPort extends OutputPort {

    record ProductData(
        ProductId productId,
        Price price,
        boolean hasStock
    ) {}

    Optional<ProductData> getProductData(ProductId productId, int requestedQuantity);
}
```

### Consumer: Outgoing Adapter

```java
package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.product;

@Component
public class ProductDataAdapter implements ProductDataPort {

    private final ProductCatalogService productCatalogService;

    public ProductDataAdapter(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @Override
    public Optional<ProductData> getProductData(ProductId productId, int requestedQuantity) {
        return productCatalogService.getProductInfo(productId)
            .map(info -> new ProductData(
                productId,
                info.price(),
                info.availableStock() >= requestedQuantity
            ));
    }
}
```

### @OpenHostService Annotation

```java
package dev.domaincentric.sample.ecommerce.sharedkernel.stereotype;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpenHostService {
    String context();
    String description() default "";
}
```

### ArchUnit Enforcement

```groovy
def "Open Host Services must reside in adapter.incoming.openhost packages"() {
    expect:
    classes()
        .that().areAnnotatedWith(OpenHostService)
        .should().resideInAPackage("..adapter.incoming.openhost..")
        .because("Open Host Services are incoming adapters exposing context capabilities")
        .check(allClasses)
}

def "Outgoing adapters accessing other contexts must only use OpenHostService classes"() {
    // For each context's outgoing adapters, verify they only access
    // @OpenHostService annotated classes (or Shared Kernel) from other contexts
}
```

---

## Alternatives Considered

### Alternative 1: Direct Repository Access

**Description**: Cart imports Product's repository directly

```java
// ❌ Rejected
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
```

**Rejected Because**:
- Tight coupling at application layer
- Violates bounded context isolation
- Cart depends on Product's internal interfaces

### Alternative 2: Shared Service Interface in Shared Kernel

**Description**: Define cross-context service interface in Shared Kernel

**Rejected Because**:
- Bloats Shared Kernel with non-universal concepts
- Shared Kernel should only contain value objects
- Service interfaces are context-specific

### Alternative 3: Events Only (No Synchronous Access)

**Description**: Cart caches product data via events, never queries Product

**Rejected Because**:
- Need real-time stock check before adding to cart
- Eventual consistency not acceptable for stock validation
- Still need events for other scenarios (price updates)

### Alternative 4: REST API Between Contexts

**Description**: Product exposes REST endpoint, Cart calls via HTTP

**Deferred (Not Rejected)**:
- REST is the **canonical DDD Open Host Service pattern**
- Overhead for same-process communication in current modulith
- In-process service achieves same goal with less latency
- REST API can be added when services are split (consumer pattern stays the same)

---

## Verification

```bash
$ ./gradlew test-architecture

✅ Open Host Services must reside in adapter.incoming.openhost packages: PASSED
✅ Application layer must not access other bounded contexts directly: PASSED
✅ Outgoing adapters accessing other contexts must only use OpenHostService classes: PASSED
✅ Bounded contexts must be isolated (domain and application layers): PASSED

BUILD SUCCESSFUL
```

---

## Related ADRs

- [ADR-007: Hexagonal Architecture](adr-007-hexagonal-architecture.md) - OHS is an incoming adapter
- [ADR-011: Bounded Context Isolation](adr-011-bounded-context-isolation.md) - OHS enables isolation
- [ADR-016: Shared Kernel Pattern](adr-016-shared-kernel-pattern.md) - Shared value objects used by OHS

---

## References

### Domain-Driven Design Literature

**Eric Evans - Domain-Driven Design (2003)**:
- **Chapter 14: Maintaining Model Integrity**
- **Section: Open Host Service**
- Quote: "Define a protocol that gives access to your subsystem as a set of SERVICES. Open the protocol so that all who need to integrate with you can use it."

**Vaughn Vernon - Implementing Domain-Driven Design (2013)**:
- **Chapter 13: Integrating Bounded Contexts**
- Quote: "When a BOUNDED CONTEXT provides a set of services for other contexts to consume, it's acting as an OPEN HOST SERVICE."

### Hexagonal Architecture

**Alistair Cockburn - Hexagonal Architecture**:
- OHS is a driving (incoming) adapter
- Consumer's adapter calling OHS is a driven (outgoing) adapter

---

**Approved by**: Architecture Team
**Date**: January 31, 2026
**Version**: 1.0
**Relates to**: ADR-007, ADR-011, ADR-016
