# Transaction Management

Application services are the transactional boundary - each public method represents a use case running within a single transaction.

## Pattern

```java
@Service
@Transactional  // Class-level: all public methods transactional
public class ProductApplicationService {

    @Transactional  // Write operation (default)
    public Product createProduct(...) {
        // 1. Domain logic
        Product product = productFactory.createProduct(...);

        // 2. Persistence
        productRepository.save(product);

        // 3. Event publishing
        eventPublisher.publishAndClearEvents(product);

        return product;
    }

    @Transactional(readOnly = true)  // Read-only optimization
    public Optional<Product> findProductById(ProductId id) {
        return productRepository.findById(id);
    }
}
```

## Remote Ports: Draw the Boundary by Hand

`@Transactional` on the class is right only while everything inside is local (repositories, stores, event
publishers). A use case that also calls a port which may leave the process — another context's data port, a
payment provider, a mail gateway — must not hold the connection for that round trip. It drops the annotation and
uses `TransactionBoundary` instead — an application-layer execution abstraction, not a port; `SpringTransactionBoundary` from `dca-spring` binds it to `TransactionTemplate` (auto-configured once a transaction manager exists, ADR-037):

```java
@Service                                   // no class-level @Transactional
public class AddItemToCartUseCase implements AddItemToCartInputPort {

  public AddItemToCartResult execute(AddItemToCartCommand input) {
    CartArticle article = articleDataPort.getArticleData(productId).orElseThrow();   // remote-capable, no tx

    return transactionBoundary.inTransaction(() -> {                                                    // short transaction
      ShoppingCart cart = shoppingCartRepository.findById(cartId).orElseThrow();     // (re)load inside
      cart.addItem(productId, quantity, Price.of(article.currentPrice()));
      shoppingCartRepository.save(cart);
      eventPublisher.publishAndClearEvents(cart);
      return toResult(cart);
    });
  }
}
```

Rules: `DCA-USE-013` fails a `@Transactional` use case that calls any output port other than `Repository`,
`Store`, `DomainEventPublisher`, `IntegrationEventPublisher` (`TransactionBoundary` is not a port); `DCA-USE-012` accepts either
`@Transactional` or `TransactionBoundary.inTransaction` as the boundary for a use case that saves or deletes an aggregate or publishes domain events. See ADR-034.

## Read-Only Transactions

Use `@Transactional(readOnly = true)` for query methods:

```java
@Transactional(readOnly = true)
public List<Product> getAllProducts() {
    return productRepository.findAll();
}
```

**Benefits:** Performance optimization, prevents accidental writes, clearer intent

## Transactional Event Listeners

Events are handled only after successful commit:

```java
@Component
public class ProductEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductCreated(ProductCreated event) {
        // Executes only after successful commit
        // Safe to trigger downstream operations
        log.info("Product created: {}", event.productId());
    }
}
```

## Transaction Flow

```
REST Controller (no transaction)
    ↓
@Transactional Application Service ← TRANSACTION BEGINS
    1. Execute domain logic (events raised)
    2. Save to repository
    3. Publish events
    ↓ COMMIT
@TransactionalEventListener ← AFTER_COMMIT
    Handle events
```

## Key Rules

1. **Application Services = Transactional Boundary** - Controllers have NO `@Transactional`; use cases that call remote-capable ports use `TransactionBoundary.inTransaction` instead of the class annotation
2. **Read-Only for Queries** - Use `readOnly = true` for query methods
3. **Events After Commit** - Use `@TransactionalEventListener(phase = AFTER_COMMIT)`
4. **One Transaction Per Use Case** - Each public method is one transaction
5. **Domain Layer Never Transactional** - Domain objects remain framework-independent

## Anti-Patterns to Avoid

❌ **Controller with @Transactional:**
```java
@RestController
@Transactional  // WRONG - transaction boundary too broad
public class ProductResource { }
```

❌ **Remote Call Inside the Transaction:**
```java
@Transactional
public class SubmitPaymentUseCase {
  public void execute(...) {
    paymentProviderRegistry.findById(id);   // WRONG - holds the connection for a remote round trip
    ...
  }
}
```

❌ **Event Listener Without Transactional Phase:**
```java
@EventListener  // WRONG - may execute before commit
public void onProductCreated(ProductCreated event) { }
```

❌ **Domain Layer with @Transactional:**
```java
@Transactional  // WRONG - breaks framework independence
public class Product { }
```

## Related Documentation

- [Architecture Principles](architecture-principles.md) - Application Service patterns
- [ADR-002: Framework-Independent Domain](adr/adr-002-framework-independent-domain.md)
- [ADR-034: Transaction Boundary and Remote Ports](adr/adr-034-transaction-boundary-and-remote-ports.md)
