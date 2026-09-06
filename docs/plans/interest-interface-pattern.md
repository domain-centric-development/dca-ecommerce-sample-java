# Plan: Interest Interface Pattern (State Mediator)

## Overview

This plan outlines the documentation and implementation of the **Interest Interface Pattern** (also known as Mediator, Double-Dispatch, or Callback pattern) from Vaughn Vernon's "Implementing Domain-Driven Design".

**Core Concept:** Aggregates don't expose internal state via getters. Instead, they "push" state to interested parties through an interface, keeping the aggregate in control of what is exposed.

---

## 1. Pattern Description

### Problem
- Getters on aggregates expose internal structure
- Clients become coupled to aggregate implementation details
- Violates "Tell, Don't Ask" principle
- Read model assembly happens outside the aggregate

### Solution
- Aggregate provides a `provideStateTo(Interest interest)` method
- Interest interface defines `receive*()` methods for states of interest
- Aggregate calls these methods to push its state
- Read Models implement the Interest interface

### Example
```java
// Aggregate - no getters
public class CheckoutSession {
    public void provideStateTo(CheckoutStateInterest interest) {
        interest.receiveSessionId(this.id);
        interest.receiveCustomer(this.customerId);
        for (CheckoutLineItem item : this.lineItems) {
            interest.receiveLineItem(item.productId(), item.name(), item.unitPrice(), item.quantity());
        }
        interest.receiveSubtotal(this.calculateSubtotal());
    }
}

// Interest Interface
public interface CheckoutStateInterest {
    void receiveSessionId(CheckoutSessionId id);
    void receiveCustomer(CustomerId customerId);
    void receiveLineItem(ProductId productId, String name, Money price, int quantity);
    void receiveSubtotal(Money subtotal);
}

// Read Model Builder implements Interest
public class CheckoutCartBuilder implements CheckoutStateInterest {
    // ... receives state and builds CheckoutCart
}
```

---

## 2. Documentation Tasks

### 2.1 Update dca-book

| File | Section | Changes |
|------|---------|---------|
| `05-domain-layer.md` | Aggregates | Add "Interest Interface Pattern" subsection explaining how aggregates expose state without getters |
| `05-domain-layer.md` | Tell Don't Ask | Reference Interest Interface as implementation of this principle |
| `06-application-layer.md` | Read Models | Show how use cases use Interest Interface to build read models |
| `07-adapter-layer.md` | - | No changes needed (pattern is domain/application layer) |
| `appendix-d-cheat-sheet.md` | Common Patterns | Add Interest Interface Pattern with code example |
| `glossary.md` (if exists) | - | Add "Interest Interface", "State Mediator" terms |

### 2.2 Update dca-guide

| File | Section | Changes |
|------|---------|---------|
| `README.md` | After "Enriched Read Model Pattern" | Add "Interest Interface Pattern" section with full example |
| `README.md` | Aggregate Rules | Add rule: "Prefer Interest Interface over getters for state exposure" |

### 2.3 Update dca-ecommerce-sample-java Documentation

| File | Changes |
|------|---------|
| `docs/architecture/architecture-principles.md` | Add Interest Interface Pattern to DDD Tactical Patterns section |

---

## 3. Implementation Tasks (Refactoring)

### 3.1 Shared Kernel Changes

| Action | File | Description |
|--------|------|-------------|
| Create | `sharedkernel/marker/tactical/StateInterest.java` | Marker interface for Interest interfaces |
| Create | `sharedkernel/marker/tactical/ReadModelBuilder.java` | Marker interface for builders that implement Interest |

### 3.2 Checkout Context Refactoring

| Action | File | Description |
|--------|------|-------------|
| Create | `checkout/domain/model/CheckoutStateInterest.java` | Interest interface for CheckoutSession |
| Modify | `checkout/domain/model/CheckoutSession.java` | Add `provideStateTo(CheckoutStateInterest)` method, deprecate/remove getters |
| Create | `checkout/domain/readmodel/CheckoutCartBuilder.java` | Builder implementing CheckoutStateInterest |
| Modify | `checkout/domain/model/CheckoutCart.java` | Move to `checkout/domain/readmodel/` |
| Modify | `checkout/application/session/getcheckoutsession/GetCheckoutSessionUseCase.java` | Use builder pattern with Interest interface |

### 3.3 Cart Context Refactoring

| Action | File | Description |
|--------|------|-------------|
| Create | `cart/domain/model/CartStateInterest.java` | Interest interface for ShoppingCart |
| Modify | `cart/domain/model/ShoppingCart.java` | Add `provideStateTo(CartStateInterest)` method |
| Create | `cart/domain/readmodel/EnrichedCartBuilder.java` | Builder implementing CartStateInterest |
| Modify | Use cases | Update to use builder pattern |

### 3.4 Package Structure Change

```
{context}/
├── domain/
│   ├── model/           # Aggregates, Entities, Value Objects
│   │   ├── CheckoutSession.java      # Aggregate (no getters)
│   │   └── CheckoutStateInterest.java # Interest Interface
│   ├── readmodel/       # NEW: Read Models and Builders
│   │   ├── CheckoutCart.java         # Immutable Read Model
│   │   └── CheckoutCartBuilder.java  # Implements Interest Interface
│   └── event/           # Domain Events
```

---

## 4. Enriched Read Model Integration

The Interest Interface pattern combines with the Enriched Read Model pattern:

```java
// Extended Interest for enrichment
public interface EnrichedCheckoutStateInterest extends CheckoutStateInterest {
    void receiveCurrentArticleData(ProductId productId, Money currentPrice, int stock, boolean available);
}

// Builder receives both snapshot state and current data
public class EnrichedCheckoutCartBuilder implements EnrichedCheckoutStateInterest {

    // From aggregate (snapshot)
    @Override
    public void receiveLineItem(ProductId id, String name, Money price, int qty) {
        items.computeIfAbsent(id, LineItemData::new).setSnapshot(name, price, qty);
    }

    // From external services (current)
    @Override
    public void receiveCurrentArticleData(ProductId id, Money price, int stock, boolean available) {
        items.computeIfAbsent(id, LineItemData::new).setCurrent(price, stock, available);
    }

    public EnrichedCheckoutCart build() {
        return new EnrichedCheckoutCart(/* ... */);
    }
}
```

---

## 5. User Stories (for prd.json)

| ID | Title | Description |
|----|-------|-------------|
| US-101 | CheckoutStateInterest Interface | Create Interest interface for CheckoutSession aggregate |
| US-102 | CheckoutSession provideStateTo Method | Add method to push state to Interest interface |
| US-103 | CheckoutCartBuilder | Create builder implementing CheckoutStateInterest |
| US-104 | Refactor GetCheckoutSessionUseCase | Use builder pattern instead of direct getters |
| US-105 | CartStateInterest Interface | Create Interest interface for ShoppingCart aggregate |
| US-106 | ShoppingCart provideStateTo Method | Add method to push state to Interest interface |
| US-107 | EnrichedCartBuilder | Create builder implementing CartStateInterest |
| US-108 | Refactor Cart Use Cases | Use builder pattern |
| US-109 | Document Interest Interface Pattern | Update dca-book and implementation guide |
| US-110 | StateInterest Marker Interface | Add marker to shared kernel |

**Dependencies:**
- US-101 → US-102 → US-103 → US-104
- US-105 → US-106 → US-107 → US-108
- US-110 should be first (shared kernel)
- US-109 can be done in parallel after implementation

---

## 6. References

### Primary Sources
- [Use a Mediator to Publish Aggregate Internal State - IDDD Ch.14 (O'Reilly)](https://www.oreilly.com/library/view/implementing-domain-driven-design/9780133039900/ch14lev2sec3.html)
- Vaughn Vernon, "Implementing Domain-Driven Design", Chapter 14: Application

### Related Patterns
- [A Pattern to Decouple Aggregates from Clients (buildplease.com)](https://buildplease.com/pages/dto-assembly-mediator/)
- [Aggregate Persistence Patterns - State Interface (GitHub)](https://github.com/pierregillon/Aggregate.Persistence.Patterns)
- [DDD Aggregate Implementation (InformIT)](https://www.informit.com/articles/article.aspx?p=2020371&seqNum=8)
- [Double Dispatch in DDD (Ardalis)](https://ardalis.com/double-dispatch-in-c-and-ddd/)

### Principles
- [Tell, Don't Ask (Martin Fowler)](https://martinfowler.com/bliki/TellDontAsk.html)
- [Law of Demeter](https://en.wikipedia.org/wiki/Law_of_Demeter)

### Related Articles (from earlier research)
- [Schlank statt aufgebläht: Was Aggregate und Read Models wirklich sind (Heise)](https://www.heise.de/blog/Schlank-statt-aufgeblaeht-Was-Aggregate-und-Read-Models-wirklich-sind-11157678.html)
- [Explicit Architecture - DDD, Hexagonal, Onion, Clean, CQRS (Herberto Graca)](https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/)

---

## 7. Benefits Summary

| Aspect | Before (Getters) | After (Interest Interface) |
|--------|------------------|---------------------------|
| Encapsulation | Aggregate exposes structure | Aggregate controls exposure |
| Coupling | Clients know internals | Clients know only interface |
| Read Models | Assembled externally | Aggregate pushes to builder |
| Testing | Mock aggregate getters | Mock simple interface |
| Evolution | Changing aggregate breaks clients | Interface is stable contract |
| Tell Don't Ask | Violated | Enforced |

---

## 8. Open Questions

1. **Naming Convention:** Should we use `StateInterest`, `StateMediator`, or `StateReceiver`?
2. **Method Naming:** `provideStateTo()`, `exportTo()`, `renderTo()`, or `publishStateTo()`?
3. **Builder Location:** `domain/readmodel/` or `application/readmodel/`?
4. **Gradual Migration:** Keep getters temporarily with `@Deprecated` or remove immediately?

---

## 9. Verification

After implementation:
1. Run `./gradlew build` - all tests pass
2. Run `./gradlew test-architecture` - verify pattern compliance
3. Verify no public getters on aggregates (except ID)
4. Documentation review for consistency across all three documentation sources