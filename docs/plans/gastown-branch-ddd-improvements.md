# Code Review: gastown Branch - Domain-Centric Architecture Analysis

## Executive Summary

The gastown branch introduces significant architectural improvements including bounded context decomposition (Product → Pricing + Inventory), the Composite Adapter pattern, and the Resolver pattern for fresh pricing. However, there are DDD improvements that could strengthen the domain model.

---

## 1. Branch Changes Overview

### New Bounded Contexts
- **Pricing Context**: `ProductPrice` aggregate, `PricingService` OHS
- **Inventory Context**: `StockLevel` aggregate, `InventoryService` OHS

### New Patterns Introduced
1. **Composite Adapter Pattern**: `CompositeCheckoutArticleDataAdapter` aggregates data from 3 OHS services
2. **Resolver Pattern**: `CheckoutArticlePriceResolver` functional interface for domain-level price resolution
3. **Anti-Corruption Layer (ACL)**: `CartDataAdapter` isolates checkout from cart's domain model

---

## 2. Issues Identified

### Issue 1: ArticleData is NOT a Domain Object

**Current Location**: Nested record inside `CheckoutArticleDataPort` (application layer)
```java
// checkout/application/shared/CheckoutArticleDataPort.java
record ArticleData(ProductId, String name, Money currentPrice, int availableStock, boolean isAvailable) {}
```

**Problem**:
- `ArticleData` lives in the application layer (inside a port interface)
- Domain layer cannot use it directly
- Forces conversion in use cases between `ArticleData` (application) → `ArticlePrice` (domain)
- Business logic scattered in use cases instead of domain objects

**Evidence** (StartCheckoutUseCase.java:89-106):
```java
// Use case does the conversion manually
for (CartData.CartItemData cartItem : cart.items()) {
    ArticleData articleData = articleDataMap.get(cartItem.productId());
    CheckoutLineItem lineItem = CheckoutLineItem.of(..., articleData.currentPrice(), ...);
    subtotal = subtotal.add(lineItem.lineTotal());
}
```

### Issue 2: Missing "ExtendedCart" / "CheckoutCart" Domain Concept

**The User's Metaphor**: A smart shopping cart with a display that shows:
- Running total as you shop
- Current prices when you scan
- Alerts if price changed since you picked an item
- Warns if stock is running low

**Current State**: No domain object embodies this concept. Instead:
- `CheckoutLineItem` stores only snapshot data (name, price)
- `CheckoutSession` has `calculateOrderTotal(resolver)` but requires external resolver injection
- No domain object answers: "Has the price changed since I added this item?"

### Issue 3: Business Logic in Use Cases (Not Domain)

**StartCheckoutUseCase** calculates subtotal in application layer:
```java
Money subtotal = Money.euro(0.0);
for (CartData.CartItemData cartItem : cart.items()) {
    // ... conversion logic ...
    subtotal = subtotal.add(lineItem.lineTotal());
}
```

This should be domain logic in a `CheckoutCart.calculateSubtotal()` method.

---

## 3. Recommended Improvements

### 3.1 Create `CheckoutArticle` Domain Value Object

**Purpose**: Promote `ArticleData` to a proper domain concept.

**File**: `checkout/domain/model/CheckoutArticle.java`

```java
public record CheckoutArticle(
    ProductId productId,
    String name,
    Money currentPrice,
    int availableStock,
    boolean isAvailable
) implements Value {

  public boolean hasStockFor(int quantity) {
    return isAvailable && availableStock >= quantity;
  }
}
```

**Benefits**:
- Domain layer can use article data directly
- Enables domain methods like `hasStockFor(quantity)`
- Follows ACL pattern - checkout's view of external data

---

### 3.2 Create `EnrichedCheckoutLineItem` Value Object

**Purpose**: Combine line item with current article data for comparison logic.

**File**: `checkout/domain/model/EnrichedCheckoutLineItem.java`

```java
public record EnrichedCheckoutLineItem(
    CheckoutLineItem lineItem,
    CheckoutArticle currentArticle
) implements Value {

  public Money currentLineTotal() {
    return currentArticle.currentPrice().multiply(lineItem.quantity());
  }

  public boolean hasPriceChanged() {
    return !lineItem.unitPrice().equals(currentArticle.currentPrice());
  }

  public Money priceDifference() {
    return currentArticle.currentPrice().subtract(lineItem.unitPrice());
  }

  public boolean hasSufficientStock() {
    return currentArticle.hasStockFor(lineItem.quantity());
  }

  public boolean isValidForCheckout() {
    return currentArticle.isAvailable() && hasSufficientStock();
  }
}
```

**Benefits**:
- Encapsulates price comparison logic
- Domain answers: "Has price changed?" "Is stock sufficient?"
- Implements the "smart cart display" metaphor

---

### 3.3 Create `CheckoutCart` Domain Value Object

**Purpose**: The "smart shopping cart" - enriched read model with business logic.

**File**: `checkout/domain/model/CheckoutCart.java`

```java
public record CheckoutCart(
    CartId cartId,
    CustomerId customerId,
    List<EnrichedCheckoutLineItem> items
) implements Value {

  // --- Calculations ---
  public Money calculateCurrentSubtotal() { ... }
  public Money calculateOriginalSubtotal() { ... }
  public Money totalPriceDifference() { ... }

  // --- Validation ---
  public boolean hasAnyPriceChanges() {
    return items.stream().anyMatch(EnrichedCheckoutLineItem::hasPriceChanged);
  }

  public List<EnrichedCheckoutLineItem> itemsWithPriceChanges() { ... }

  public boolean isValidForCheckout() {
    return !items.isEmpty() &&
           items.stream().allMatch(EnrichedCheckoutLineItem::isValidForCheckout);
  }

  public List<EnrichedCheckoutLineItem> invalidItems() { ... }
  public List<EnrichedCheckoutLineItem> unavailableItems() { ... }
}
```

**Benefits**:
- Rich domain object with business methods
- Implements "smart cart" metaphor completely
- Validation logic in domain, not use cases

---

### 3.4 Create `CheckoutCartFactory`

**Purpose**: Factory to assemble `CheckoutCart` from multiple data sources.

**File**: `checkout/domain/model/CheckoutCartFactory.java`

```java
public class CheckoutCartFactory implements Factory {

  public CheckoutCart create(
      CartId cartId,
      CustomerId customerId,
      List<CheckoutLineItem> lineItems,
      Map<ProductId, CheckoutArticle> articleData
  ) {
    List<EnrichedCheckoutLineItem> enrichedItems = lineItems.stream()
        .map(item -> new EnrichedCheckoutLineItem(
            item,
            articleData.get(item.productId())))
        .toList();
    return new CheckoutCart(cartId, customerId, enrichedItems);
  }
}
```

**Benefits**:
- Follows Factory pattern (DDD tactical pattern)
- Encapsulates complex assembly
- Framework-independent

---

### 3.5 Update `CheckoutArticleDataPort`

**Change**: Return domain objects, not DTOs.

```java
public interface CheckoutArticleDataPort extends OutputPort {
    Map<ProductId, CheckoutArticle> getArticleData(Collection<ProductId> productIds);
}
```

---

## 4. Updated Use Case (Example)

```java
@Service
public class StartCheckoutUseCase implements StartCheckoutInputPort {
    private final CheckoutCartFactory checkoutCartFactory;
    // ... other dependencies

    @Override
    public StartCheckoutResult execute(StartCheckoutCommand command) {
        CartData cart = cartDataPort.findById(...);

        Map<ProductId, CheckoutArticle> articleData =
            checkoutArticleDataPort.getArticleData(productIds);

        List<CheckoutLineItem> lineItems = createLineItems(cart, articleData);

        // Create enriched cart using factory
        CheckoutCart checkoutCart = checkoutCartFactory.create(
            cart.cartId(), cart.customerId(), lineItems, articleData);

        // Domain validation
        if (!checkoutCart.isValidForCheckout()) {
            throw new ValidationException(checkoutCart.invalidItems());
        }

        // Create session with domain-calculated subtotal
        CheckoutSession session = CheckoutSession.start(
            cart.cartId(),
            cart.customerId(),
            lineItems,
            checkoutCart.calculateCurrentSubtotal());

        return mapToResult(session, checkoutCart);
    }
}
```

---

## 5. New Patterns to Document in domain-centric-architecture

### Pattern 1: Composite Adapter Pattern
Document `CompositeCheckoutArticleDataAdapter` - aggregating data from multiple OHS services.

### Pattern 2: Enriched Read Model Pattern
Document `CheckoutCart` - combining persisted data with fresh external data for rich domain logic.

### Pattern 3: Factory for Cross-Context Assembly
Document using factories to assemble domain objects from cross-context data fetched via ports.

### Pattern 4: Resolver Pattern (Already exists)
Document `CheckoutArticlePriceResolver` - functional interface for domain-level data resolution.

---

## 6. Design Decisions

- **Keep both patterns**: `CheckoutArticlePriceResolver` (for `confirm()` method) AND `CheckoutCart` (for validation/display)
- **Add to both contexts**: Create `EnrichedCartItem` in Cart context for consistency

---

## 7. Files to Create/Modify

### Checkout Context
| Action | File | Purpose |
|--------|------|---------|
| **Create** | `checkout/domain/model/CheckoutArticle.java` | Domain value object for article data |
| **Create** | `checkout/domain/model/EnrichedCheckoutLineItem.java` | Line item + current article combined |
| **Create** | `checkout/domain/model/CheckoutCart.java` | Smart cart with business logic |
| **Create** | `checkout/domain/model/CheckoutCartFactory.java` | Factory for assembling CheckoutCart |
| **Modify** | `checkout/application/shared/CheckoutArticleDataPort.java` | Return CheckoutArticle instead of ArticleData |
| **Modify** | `checkout/adapter/outgoing/product/CompositeCheckoutArticleDataAdapter.java` | Return domain objects |
| **Modify** | `checkout/application/session/startcheckout/StartCheckoutUseCase.java` | Use factory and domain validation |
| **Keep** | `checkout/domain/model/CheckoutArticlePriceResolver.java` | Keep for `confirm()` backward compat |

### Cart Context
| Action | File | Purpose |
|--------|------|---------|
| **Create** | `cart/domain/model/CartArticle.java` | Domain value object (like CheckoutArticle) |
| **Create** | `cart/domain/model/EnrichedCartItem.java` | Cart item + current article combined |
| **Create** | `cart/domain/model/EnrichedCart.java` | Smart cart for Cart context |
| **Create** | `cart/domain/model/EnrichedCartFactory.java` | Factory for EnrichedCart |
| **Modify** | `cart/application/shared/ArticleDataPort.java` | Return CartArticle instead of ArticleData |
| **Modify** | `cart/adapter/outgoing/product/CompositeArticleDataAdapter.java` | Return domain objects |

### Documentation
| Action | File | Purpose |
|--------|------|---------|
| **Update** | `docs/architecture/architecture-principles.md` | Document new patterns |
| **Update** | `dca-guide/README.md` | Add patterns if applicable |

---

## 8. Verification Plan

1. **Run existing tests**: `./gradlew test` - ensure no regressions
2. **Run architecture tests**: `./gradlew test-architecture` - verify patterns
3. **Add unit tests** for new domain objects:
   - Checkout: `CheckoutArticleTest`, `EnrichedCheckoutLineItemTest`, `CheckoutCartTest`, `CheckoutCartFactoryTest`
   - Cart: `CartArticleTest`, `EnrichedCartItemTest`, `EnrichedCartTest`, `EnrichedCartFactoryTest`
4. **Update integration tests** to verify enriched cart behavior in both contexts

---

## 9. Summary

| Question | Answer |
|----------|--------|
| **Why isn't ArticleData a Domain Object?** | Currently nested in application port; should be promoted to `CheckoutArticle`/`CartArticle` value objects |
| **Where is ExtendedCart?** | Missing; should create `CheckoutCart` and `EnrichedCart` with business methods |
| **How should it be created?** | `CheckoutCartFactory`/`EnrichedCartFactory` assembles from line items + article data |
| **New patterns to document?** | Composite Adapter, Enriched Read Model, Factory for Cross-Context Assembly |
| **Keep Resolver?** | Yes, `CheckoutArticlePriceResolver` remains for `confirm()` method |
| **Both contexts?** | Yes, add enriched cart pattern to both Cart and Checkout contexts |