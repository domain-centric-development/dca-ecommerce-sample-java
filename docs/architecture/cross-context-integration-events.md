# Cross-Context Integration Through Events

How the bounded contexts of this sample communicate through events while staying isolated. The
worked example is the one flow the shop actually runs: a confirmed checkout reconciles the cart and
reduces stock.

## The flow: confirming a checkout

1. The buyer confirms the checkout (Checkout context). The `CheckoutSession` aggregate raises the
   domain event `CheckoutConfirmed`.
2. `CheckoutConfirmedEventPublisher` (Checkout, `adapter/outgoing/event`) translates the domain event
   into the integration event `CheckoutConfirmedEvent` (Checkout, `events`) and publishes it.
3. `StockReductionEventConsumer` (Inventory, `adapter/incoming/event`) reduces the stock of every
   purchased line.
4. `CartCompletionEventConsumer` (Cart, `adapter/incoming/event/cartcheckout`) reconciles the cart:
   the purchased units leave it, later additions survive.

Neither consumer knows the Checkout context. Each listens to a trigger interface it owns —
`StockReductionTrigger` (Inventory) and `CartCompletionTrigger` (Cart) — and `CheckoutConfirmedEvent`
implements both. Spring Modulith dispatches by type, so the consumer never imports the producer
(ADR-024, interface inversion).

```
┌──────────────────┐                             ┌──────────────────────┐
│ Checkout context │                             │ Inventory context    │
│                  │  CheckoutConfirmedEvent     │  StockReductionTrigger│
│ CheckoutSession  │  implements both triggers   │  ┌─────────────────┐ │
│  .confirm()      │────────────────────────────▶│  │StockReduction   │ │
│      │           │                             │  │EventConsumer    │ │
│      ▼           │                             │  └────────┬────────┘ │
│ CheckoutConfirmed│                             │           ▼          │
│      │           │                             │  ReduceStockUseCase  │
│      ▼           │                             └──────────────────────┘
│ CheckoutConfirmed│                             ┌──────────────────────┐
│ EventPublisher   │                             │ Cart context         │
│                  │────────────────────────────▶│  CartCompletionTrigger│
└──────────────────┘                             │  CartCompletion      │
                                                 │  EventConsumer       │
                                                 │      ▼               │
                                                 │  CompleteCartUseCase │
                                                 └──────────────────────┘
```

### Why events instead of direct calls

A direct call from the Checkout use case into Inventory's and Cart's repositories would couple three
contexts into one transaction, make every consumer a change driver for Checkout and leave no record of
what triggered the change. The event lets each consumer run in its own transaction and evolve on its
own. The trade-off is eventual consistency: the stock is reduced shortly after the confirmation, not
inside it.

## The pieces

### Domain event — internal fact

```java
// checkout/domain/event/CheckoutConfirmed.java
public record CheckoutConfirmed(
    UUID eventId,
    CheckoutSessionId sessionId,
    CartId cartId,
    CustomerId customerId,
    Money totalAmount,
    List<LineItemInfo> items,
    Instant occurredOn)
    implements DomainEvent { ... }
```

Raised by the aggregate, carries domain types, has no schema version. It never leaves the context.

### Integration event — published contract

```java
// checkout/events/CheckoutConfirmedEvent.java
@IntegrationEventType(name = "checkout-confirmed", version = 2)
public record CheckoutConfirmedEvent(
    UUID eventId,
    String sessionId,
    String cartId,
    String customerId,
    Money totalAmount,
    List<LineItemInfo> items,
    Instant occurredOn)
    implements IntegrationEvent, CartCompletionTrigger, StockReductionTrigger { ... }
```

Lives in the context's `events` package (a Spring Modulith named interface), carries only shared-kernel
types and primitives, and names its contract and version on the class (ADR-027). Consumers see this
class — or rather the trigger interfaces it implements — and nothing else of Checkout.

### Outgoing event adapter — the translation

```java
// checkout/adapter/outgoing/event/CheckoutConfirmedEventPublisher.java
@Component
public class CheckoutConfirmedEventPublisher {
  @EventListener
  public void on(final CheckoutConfirmed domainEvent) {
    publisher.publishEvent(new CheckoutConfirmedEvent(domainEvent.eventId(), ...));
  }
}
```

This adapter is the anti-corruption layer between the Checkout model and its consumers: the domain
event can change freely, the integration event only by a new version.

### Incoming event adapter — the consumer

```java
// inventory/adapter/incoming/event/StockReductionEventConsumer.java
@Component
public class StockReductionEventConsumer {
  @ApplicationModuleListener
  void on(final StockReductionTrigger event) {
    event.orderLineItems().forEach(item ->
        reduceStockInputPort.execute(
            new ReduceStockCommand(item.productId().value(), item.quantity())));
  }
}
```

`@ApplicationModuleListener` runs after the publishing transaction committed, asynchronously, in a
transaction of its own — one aggregate per transaction. The consumer calls its own use case; it never
touches another context's repository.

## Delivery guarantees

Integration events go through the transactional outbox (ADR-026): the publication is stored with the
publishing transaction, delivered after commit and retried on failure, so a consumer that is down
during the confirmation still reduces the stock later. Consumers are therefore written to be
idempotent — `CompleteCartUseCase` reconciles by position identity and cannot remove a unit twice, and
a replayed event with no remaining intersection changes nothing (ADR-040).

## Rules the architecture tests enforce

- Contexts depend on each other only through `api/` (Open Host Service) and `events/` (published
  language) — never through a sibling's domain, application or adapter package (ADR-011).
- Integration events implement `IntegrationEvent`, carry `@IntegrationEventType`, and live in
  `events/`; domain events implement `DomainEvent` and live in `domain/event/` (ADR-027).
- A consumer-defined trigger interface lives in the consumer's `events/` package; the producer's
  integration event implements it (ADR-024).

## Seeing it

1. Start the shop (`./gradlew bootRun`), add a product to the cart and run through the checkout to
   the confirmation page.
2. Open the backoffice event log (footer link): the `checkout-confirmed` publication appears with its
   two consumers, both completed.
3. The product page shows the reduced stock; the cart no longer contains the purchased units.

## Related

- [ADR-011: Bounded Context Isolation](adr/adr-011-bounded-context-isolation.md)
- [ADR-024: Interface Inversion for Spring Modulith Events](adr/adr-024-interface-inversion-spring-modulith.md)
- [ADR-026: Transactional Outbox for Integration Events](adr/adr-026-transactional-outbox-integration-events.md)
- [ADR-027: Integration-Event Contract Identity](adr/adr-027-integration-event-contract-identity.md)
- [ADR-040: Checkout snapshots and reconciliation](adr/adr-040-checkout-snapshots-and-reconciliation.md)
