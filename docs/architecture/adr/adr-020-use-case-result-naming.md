# ADR-020: Use Case Output Naming Convention (*Result instead of *Response)

**Date**: February 2, 2026
**Status**: ✅ Accepted
**Deciders**: Architecture Team

---

## Context

The application layer uses Command/Query objects for input and needs a consistent naming convention for output objects. Previously, output classes were named `*Response` (e.g., `CreateProductResponse`, `GetCartByIdResponse`).

### Problem

The `*Response` suffix creates terminology overlap with HTTP layer:

```
HTTP Layer (Adapters):     Request → Response
Application Layer:         Command → Response  ← Confusing!
```

This ambiguity can lead to:
1. **Conceptual confusion** between HTTP responses and use case outputs
2. **Import conflicts** when both exist in the same file
3. **Unclear layer separation** in code reviews and documentation

### Industry Context

Research into Clean Architecture and DDD implementations shows no single standard:
- "Response" is associated with the **presenter pattern** (Clean Architecture)
- "Result" is associated with **direct return / CQRS patterns**
- Both are commonly used in practice

---

## Decision

**Use `*Result` suffix for all use case output classes in the application layer.**

### Naming Convention

| Layer | Input | Output |
|-------|-------|--------|
| **HTTP Adapters** | `*Request` | `*Response` / `*ApiResult` |
| **Application (Use Cases)** | `*Command` / `*Query` | `*Result` |

### Examples

```java
// Application Layer - Use Cases
public interface CreateProductInputPort extends UseCase<CreateProductCommand, CreateProductResult> {}
public interface GetCartByIdInputPort extends UseCase<GetCartByIdQuery, GetCartByIdResult> {}

// Adapter Layer - HTTP
public record CreateProductRequest(String sku, String name, ...) {}
public record LoginApiResult(String token, String userId) {}  // HTTP-specific response
```

---

## Rationale

### 1. **Clear Layer Separation**

HTTP layer owns Request/Response terminology:
```java
// Adapter (HTTP) - uses Response
@PostMapping
public ResponseEntity<ProductDto> create(@RequestBody CreateProductRequest request) {
    CreateProductResult result = useCase.execute(command);
    return ResponseEntity.ok(toDto(result));
}
```

Application layer owns Command/Query/Result terminology:
```java
// Application - uses Result
public CreateProductResult execute(CreateProductCommand command) {
    Product product = productFactory.create(...);
    return CreateProductResult.from(product);
}
```

### 2. **Better Semantic Pairing**

- `Command` → `Result` (natural pairing: "I commanded, here's the result")
- `Query` → `Result` (natural pairing: "I queried, here's the result")

vs.

- `Command` → `Response` (less natural: commands don't typically "respond")

### 3. **Alignment with Functional Patterns**

The `Result` naming aligns with functional programming patterns like `Result<Success, Error>` used in many modern architectures for explicit error handling.

### 4. **Consistency with Output Ports**

The codebase already uses `*Result` for output port return types (e.g., `PaymentResult`), making this change consistent across the application layer.

---

## Consequences

### Positive

✅ **Clearer layer boundaries** - Terminology clearly indicates which layer you're in
✅ **Reduced confusion** - No overlap between HTTP and application concepts
✅ **Better semantic fit** - Command/Query → Result is more natural
✅ **Consistent naming** - Aligns with existing output port patterns
✅ **Future-proof** - Works well with Result<T, E> error handling patterns

### Neutral

⚠️ **Refactoring required** - All 29 Response classes needed renaming
⚠️ **Documentation updates** - README, CLAUDE.md, and book chapters updated

### Negative

❌ **Breaking change** - Required updating all imports and references
❌ **Differs from some examples** - Some Clean Architecture examples use Response

---

## Implementation

### Classes Renamed (29 total)

**Product Context (5):**
- `CreateProductResponse` → `CreateProductResult`
- `GetProductByIdResponse` → `GetProductByIdResult`
- `GetAllProductsResponse` → `GetAllProductsResult`
- `UpdateProductPriceResponse` → `UpdateProductPriceResult`
- `ReduceProductStockResponse` → `ReduceProductStockResult`

**Cart Context (11):**
- `CreateCartResponse` → `CreateCartResult`
- `GetCartByIdResponse` → `GetCartByIdResult`
- `GetAllCartsResponse` → `GetAllCartsResult`
- `AddItemToCartResponse` → `AddItemToCartResult`
- `RemoveItemFromCartResponse` → `RemoveItemFromCartResult`
- `CheckoutCartResponse` → `CheckoutCartResult`
- `MergeCartsResponse` → `MergeCartsResult`
- `CompleteCartResponse` → `CompleteCartResult`
- `RecoverCartOnLoginResponse` → `RecoverCartOnLoginResult`
- `GetOrCreateActiveCartResponse` → `GetOrCreateActiveCartResult`
- `GetCartMergeOptionsResponse` → `GetCartMergeOptionsResult`

**Checkout Context (11):**
- `StartCheckoutResponse` → `StartCheckoutResult`
- `GetActiveCheckoutSessionResponse` → `GetActiveCheckoutSessionResult`
- `GetCheckoutSessionResponse` → `GetCheckoutSessionResult`
- `GetConfirmedCheckoutSessionResponse` → `GetConfirmedCheckoutSessionResult`
- `SubmitBuyerInfoResponse` → `SubmitBuyerInfoResult`
- `SubmitDeliveryResponse` → `SubmitDeliveryResult`
- `SubmitPaymentResponse` → `SubmitPaymentResult`
- `ConfirmCheckoutResponse` → `ConfirmCheckoutResult`
- `GetPaymentProvidersResponse` → `GetPaymentProvidersResult`
- `GetShippingOptionsResponse` → `GetShippingOptionsResult`
- `SyncCheckoutWithCartResponse` → `SyncCheckoutWithCartResult`

**Account Context (2):**
- `RegisterAccountResponse` → `RegisterAccountResult`
- `AuthenticateAccountResponse` → `AuthenticateAccountResult`

### Documentation Updated

- `README.md` - Project structure and examples
- `CLAUDE.md` (root and dca-ecommerce-sample-java) - Naming conventions
- `dca-guide/README.md` - Pattern documentation
- DCA guide, "Use Case Pattern with Input Ports" and "Shaping the Result" — the `*Result` convention this ADR adopts

---

## Alternatives Considered

### Alternative 1: Keep *Response (Status Quo)

**Rejected**: Creates confusion with HTTP layer terminology.

### Alternative 2: Use *Output

```java
public record CreateProductOutput(ProductId id, ...) {}
```

**Rejected**: Too generic, doesn't convey the outcome nature of the data.

### Alternative 3: Use *Dto

```java
public record CreateProductDto(ProductId id, ...) {}
```

**Rejected**: DTO is an adapter concept; use case outputs are application layer.

---

## References

### Industry Examples

- **Milan Jovanović** (Clean Architecture author): Uses `Result<T>` pattern
- **Made Tech Clean Architecture Guide**: Uses "Response" with presenter pattern
- **Functional Programming**: `Result<Success, Error>` monad pattern

### Related ADRs

- [ADR-012: Use Case Input/Output Models](adr-012-use-case-input-output-models.md) - Command/Query pattern (proposed)
- [ADR-007: Hexagonal Architecture](adr-007-hexagonal-architecture.md) - Port/Adapter separation

---

## Validation

### Success Criteria

- [x] All use case output classes use `*Result` suffix
- [x] Build passes (`./gradlew build`)
- [x] Architecture tests pass (`./gradlew test-architecture`)
- [x] Documentation updated across all sub-projects
- [x] Naming conventions documented in CLAUDE.md files

---

**Accepted by**: Architecture Team
**Date**: February 2, 2026
**Version**: 1.0