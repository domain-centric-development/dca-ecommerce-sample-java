# DTOs vs ViewModels: REST vs MVC Analysis

**Question:** Should MVC Controllers use DTOs/Converters like REST Controllers?

**Answer:**
- ✅ **REST Controllers**: Always use DTOs (current approach is correct)
- ✅ **MVC Controllers**: Use page-specific ViewModels (recommended pattern)

---

## Architectural Pattern: Use Case → Result → ViewModel → Template

The recommended data flow from use case to template:

```
Use Case → Result(Snapshot) → Controller → ViewModel → Template
         (domain read model)              (primitives only)
```

**Key Principles:**
1. **Use cases** return domain read models (snapshots) wrapped in Result objects
2. **Controllers** convert snapshots to page-specific ViewModels
3. **ViewModels** contain only primitives (String, BigDecimal, int) - no domain objects
4. **Template attribute names** match the ViewModel's purpose

---

## Current Implementation

### REST Controller - Uses DTOs ✅

```java
@RestController
@RequestMapping("/api/products")
public class ProductResource {
  private final GetAllProductsInputPort getAllProductsUseCase;
  private final ProductDtoConverter converter;

  @GetMapping
  public ResponseEntity<List<ProductDto>> getAllProducts() {
    GetAllProductsResult result = getAllProductsUseCase.execute(new GetAllProductsQuery());
    return ResponseEntity.ok(converter.toDto(result));
  }
}
```

**Why DTOs?**
- Stable API contract independent of domain
- Flattened structure for JSON serialization
- Versioning capability
- Security (hide internal details)

### MVC Controller - Uses Page-Specific ViewModels ✅

```java
@Controller
@RequestMapping("/cart")
public class CartPageController {
  private final GetCartByIdInputPort getCartByIdUseCase;

  @GetMapping
  public String showCart(final Model model) {
    // 1. Execute use case - returns Result with domain read model (snapshot)
    GetCartByIdResult result = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));

    if (!result.found()) {
      return "error/404";
    }

    // 2. Convert snapshot to page-specific ViewModel in adapter layer
    CartPageViewModel viewModel = CartPageViewModel.fromEnrichedCart(result.cart());

    // 3. Attribute name matches ViewModel purpose
    model.addAttribute("shoppingCart", viewModel);
    return "cart/view";
  }
}
```

**ViewModel Definition (in adapter.incoming.web):**

```java
public record CartPageViewModel(
    String cartId,
    String status,
    List<LineItemViewModel> lineItems,
    TotalsViewModel totals,
    int itemCount,
    int totalQuantity,
    boolean hasAnyPriceChanges,
    boolean canCheckout
) {
  // Factory method converts domain read model to ViewModel
  public static CartPageViewModel fromEnrichedCart(final EnrichedCart cart) {
    return new CartPageViewModel(
        cart.cartId().value().toString(),  // Primitives only
        cart.status().name(),
        cart.items().stream().map(LineItemViewModel::fromItem).toList(),
        TotalsViewModel.from(cart.totals()),
        cart.itemCount(),
        cart.totalQuantity(),
        cart.hasAnyPriceChanges(),
        cart.canCheckout()
    );
  }

  // Nested ViewModels for complex data
  public record LineItemViewModel(
      String itemId,
      String productId,
      String productName,
      int quantity,
      BigDecimal unitPrice,
      BigDecimal lineTotal,
      String currencyCode,
      boolean hasPriceChanged,
      boolean hasSufficientStock,
      boolean isAvailable
  ) { /* ... */ }

  public record TotalsViewModel(
      BigDecimal currentSubtotal,
      BigDecimal originalSubtotal,
      String currencyCode
  ) { /* ... */ }
}
```

**Pug Template (simple property access):**
```pug
.cart-summary
  p Cart ID: #{shoppingCart.cartId()}
  p Total Items: #{shoppingCart.itemCount()}

each item in shoppingCart.lineItems()
  h3= item.productName()
  p Quantity: #{item.quantity()}
  p Price: #{item.unitPrice()} #{item.currencyCode()}
```

---

## Comparison

| Aspect | REST (DTOs) | MVC (ViewModels) | MVC (Domain Objects) |
|--------|-------------|------------------|---------------------|
| **Contract Stability** | High | High | Low |
| **Template Clarity** | N/A | High - simple properties | Low - nested method calls |
| **Domain Exposure** | None | None | High |
| **Data Types** | Primitives | Primitives | Domain objects |
| **Location** | `adapter.incoming.api` | `adapter.incoming.web` | N/A |
| **Conversion** | In adapter | In adapter | None |

---

## ViewModel Design Rules

### 1. Location
ViewModels reside in `adapter.incoming.web` alongside the controller that uses them.

```
{context}/adapter/incoming/web/
├── CartPageController.java
├── CartPageViewModel.java           # Page-specific ViewModel
├── CartMergePageController.java
└── CartMergePageViewModel.java      # Different page, different ViewModel
```

### 2. Naming Convention
- ViewModel name indicates the page/purpose: `{Page}ViewModel`
- Attribute name in template matches purpose: `model.addAttribute("shoppingCart", viewModel)`

| Page | ViewModel | Attribute Name |
|------|-----------|----------------|
| Cart view | `CartPageViewModel` | `shoppingCart` |
| Cart merge | `CartMergePageViewModel` | `cartMerge` |
| Buyer info | `BuyerInfoPageViewModel` | `buyerInfoPage` |
| Order review | `ReviewPageViewModel` | `orderReview` |
| Confirmation | `ConfirmationPageViewModel` | `confirmedCheckout` |

### 3. Data Types
ViewModels use only primitives and primitive wrappers:
- ✅ `String`, `int`, `boolean`, `BigDecimal`
- ✅ Nested ViewModel records
- ✅ `List<NestedViewModel>`
- ❌ Domain objects (`CartId`, `Money`, `ProductId`)
- ❌ Domain enums (convert to `String`)

### 4. Factory Methods
Each ViewModel has a factory method to convert from domain read model:

```java
public static CartPageViewModel fromEnrichedCart(final EnrichedCart cart) {
  // Convert domain objects to primitives
}
```

---

## Benefits of Page-Specific ViewModels

1. **Tailored Data**: Each page gets exactly the data it needs
2. **Simple Templates**: Property access with primitives, no nested domain calls
3. **Decoupling**: Template doesn't depend on domain structure
4. **Testability**: ViewModels are easy to construct for testing
5. **Evolution**: Domain can change without breaking templates
6. **Type Safety**: Compile-time checking of template data

---

## Related Documentation

- [Architecture Principles](architecture-principles.md) - Enriched Domain Model Pattern
- [Package Structure](package-structure.md) - ViewModel location
- [Pug4j Integration](../integrations/pug4j-integration.md) - Template usage
