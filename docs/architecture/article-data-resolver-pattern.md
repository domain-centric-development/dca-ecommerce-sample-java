# Article Data Resolver Pattern

## Problem

Cart and Checkout need fresh pricing and availability data from external services, but the domain layer must remain pure (no external dependencies).

**Current state:** Cart stores `priceAtAddition` snapshot; Product context provides all data.

**Target state:**
- Pricing from **Pricing bounded context** (OHS)
- Availability from **Inventory bounded context** (OHS)
- Product context: only name, SKU, category
- Fresh data for all calculations (not cached)

## Solution: Resolver Pattern

```
┌─────────────────────────────────────────────────────────────────────┐
│ APPLICATION LAYER                                                    │
│  1. Fetch fresh data via ArticleDataPort                            │
│  2. Build ArticlePriceResolver from fetched data                    │
│  3. Pass resolver to aggregate method                               │
│  4. Aggregate executes business logic with fresh data               │
└─────────────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│ ADAPTER LAYER                                                        │
│  CompositeArticleDataAdapter aggregates:                            │
│  • ProductCatalogService (name, SKU)                                │
│  • PricingService (price)                                           │
│  • InventoryService (stock, availability)                           │
└─────────────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│ DOMAIN LAYER                                                         │
│  ShoppingCart.calculateTotal(ArticlePriceResolver resolver)         │
│  ShoppingCart.validateForCheckout(ArticlePriceResolver resolver)    │
└─────────────────────────────────────────────────────────────────────┘
```

## Key Components

### Domain: ArticlePriceResolver

```java
@FunctionalInterface
public interface ArticlePriceResolver {
    ArticlePrice resolve(ProductId productId);

    record ArticlePrice(Money price, boolean isAvailable, int availableStock) implements Value {}
}
```

### Application: ArticleDataPort

```java
public interface ArticleDataPort extends OutputPort {
    record ArticleData(ProductId productId, String name, Money currentPrice,
                       int availableStock, boolean isAvailable) {}

    Map<ProductId, ArticleData> getArticleData(Collection<ProductId> productIds);
}
```

### Adapter: CompositeArticleDataAdapter

```java
@Component
public class CompositeArticleDataAdapter implements ArticleDataPort {
    private final ProductCatalogService productCatalogService;
    private final PricingService pricingService;
    private final InventoryService inventoryService;

    @Override
    public Map<ProductId, ArticleData> getArticleData(Collection<ProductId> productIds) {
        // Fetch from all OHS and combine
    }
}
```

### Use Case Pattern

```java
@Service
public class CheckoutCartUseCase implements CheckoutCartInputPort {
    @Override
    public CheckoutCartResult execute(CheckoutCartCommand command) {
        ShoppingCart cart = repository.findById(command.cartId()).orElseThrow();

        // 1. Fetch fresh data
        Map<ProductId, ArticleData> articleData = articleDataPort.getArticleData(cart.productIds());

        // 2. Build resolver
        ArticlePriceResolver resolver = productId -> {
            ArticleData data = articleData.get(productId);
            return new ArticlePrice(data.currentPrice(), data.isAvailable(), data.availableStock());
        };

        // 3. Domain operation with fresh data
        cart.checkout(resolver);

        repository.save(cart);
        return mapToResult(cart, articleData);
    }
}
```

## New Bounded Contexts

### Pricing Context

```
pricing/
├── domain/model/ProductPrice.java          # Aggregate
├── application/getpricesforproducts/       # Bulk price lookup
├── application/shared/ProductPriceRepository.java
└── api/PricingService.java                 # OHS (published in-process interface)
```

### Inventory Context

```
inventory/
├── domain/model/StockLevel.java            # Aggregate
├── application/getstockforproducts/        # Bulk stock lookup
├── application/reservestock/               # Checkout reservations
├── application/shared/StockLevelRepository.java
└── api/InventoryService.java               # OHS (published in-process interface)
```

## Design Decisions

| Decision | Rationale |
|----------|-----------|
| Keep `priceAtAddition` | Auditing: shows price when customer added item |
| Functional interface | Domain stays pure, no external dependencies |
| Composite adapter | Single point of aggregation for multiple OHS |
| Bulk fetch | Avoid N+1 calls when fetching article data |

## Related Documentation

- [Open Host Service Pattern](adr/adr-019-open-host-service-pattern.md)
- [Cross-Context Integration Events](cross-context-integration-events.md)
- [Architecture Principles](architecture-principles.md)