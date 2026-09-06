# ADR-022: ViewModel Pattern for Web Adapters

**Date**: February 7, 2026
**Status**: Accepted
**Deciders**: Architecture Team

---

## Context

Web MVC controllers need to pass data to Pug templates. Initially, use case Result objects were used directly in templates, which caused two problems:

1. **Presentation logic leaked into the application layer** -- formatting, display flags, and template-specific fields polluted Result records
2. **Templates depended on domain types** -- Pug templates referenced `Money`, `ProductId`, and other domain objects that should not leak into the view layer

---

## Decision

**Introduce page-specific ViewModel records in `adapter.incoming.web/` that transform enriched domain models into primitive-only data for templates.**

### Rules

1. Each page gets its own ViewModel tailored to that page's needs
2. ViewModels use only primitives (`String`, `BigDecimal`, `int`, `boolean`) -- no domain types
3. A static factory method converts the domain read model to the ViewModel
4. ViewModels live in the adapter layer, not the application layer

### Implementation

```java
// cart/adapter/incoming/web/shopping/CartPageViewModel.java
public record CartPageViewModel(
    String cartId, String customerId,
    List<CartItemViewModel> items,
    String subtotal, boolean hasPriceChanges
) {
  public static CartPageViewModel fromEnrichedCart(EnrichedCart cart) {
    // Converts domain types to primitives for template
  }
}
```

```java
// Controller converts Result -> ViewModel
CartPageViewModel viewModel = CartPageViewModel.fromEnrichedCart(result.cart());
model.addAttribute("shoppingCart", viewModel);
```

### Package Structure

```
{context}/adapter/incoming/web/
├── CartPageController.java           # Controller
├── CartPageViewModel.java            # ViewModel for cart page
├── CartMergePageController.java      # Different page
└── CartMergePageViewModel.java       # Different ViewModel
```

### Current ViewModels

- Product: `ProductCatalogPageViewModel`, `ProductDetailPageViewModel`
- Cart: `CartPageViewModel`, `CartMergePageViewModel`
- Checkout: `BuyerInfoPageViewModel`, `DeliveryPageViewModel`, `PaymentPageViewModel`, `ReviewPageViewModel`, `ConfirmationPageViewModel`

---

## Consequences

### Positive

- Templates use only primitives -- no domain type leakage
- Each page has exactly the data it needs -- no over-fetching
- Clear separation: Result (application) vs ViewModel (adapter) vs DTO (API)

### Negative

- Additional record per page -- more files in the web package
- Factory method duplicates some mapping logic

---

## Related ADRs

- [ADR-020: Use Case Result Naming](adr-020-use-case-result-naming.md) -- Results feed into ViewModels
- [ADR-021: Enriched Domain Model Pattern](adr-021-enriched-domain-model-pattern.md) -- ViewModels consume enriched models
- [ADR-001: API/Web Package Separation](adr-001-api-web-package-separation.md) -- ViewModels live in web package, DTOs in api package
