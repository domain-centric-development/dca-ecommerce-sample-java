# Architecture Documentation

Comprehensive architectural documentation for the DCA e-commerce sample (Java).

## Core Documentation

**[architecture-principles.md](architecture-principles.md)** - Main architecture reference
- Domain-Driven Design (Strategic and Tactical patterns)
- Hexagonal Architecture (Ports and Adapters)
- Onion Architecture (Dependency Inversion)
- Package structure and bounded contexts
- Architectural rules enforced by ArchUnit

**[comprehensive-architecture-analysis.md](comprehensive-architecture-analysis.md)** - Architecture assessment and analysis

**[transaction-management.md](transaction-management.md)** - Transaction handling patterns

**[package-structure.md](package-structure.md)** - Detailed package organization

**[dto-vs-viewmodel-analysis.md](dto-vs-viewmodel-analysis.md)** - DTO vs ViewModel analysis

**[Architecture Decision Records](adr/README.md)** - All ADRs documenting key decisions

## Quick Reference

**Package Structure:**

Each bounded context (product, cart, checkout, account, inventory, pricing, portal, backoffice) follows the same
structure, with the layers it needs:
```
{context}/
├── domain/             # Domain layer (innermost)
│   ├── model/          # Aggregates, entities, value objects, enriched models
│   ├── readmodel/      # Domain read models (snapshots) and builders
│   ├── service/        # Domain services
│   └── event/          # Domain events
├── application/        # Application layer
│   ├── {usecasename}/  # Use case package (e.g., createproduct)
│   │   ├── *InputPort.java   # Input port interface
│   │   ├── *UseCase.java     # Use case implementation
│   │   ├── *Command.java/*Query.java  # Input model
│   │   └── *Result.java      # Output model (wraps domain read model)
│   └── shared/         # Shared output ports
│       └── *Repository.java  # Repository interfaces
└── adapter/            # Adapter layer (outermost)
    ├── incoming/       # Incoming adapters (primary/driving)
    │   ├── api/        # REST API (DTOs, *Resource)
    │   ├── web/        # Web MVC (*Controller, *ViewModel)
    │   ├── mcp/        # MCP server
    │   └── event/      # Domain event consumers
    └── outgoing/       # Outgoing adapters (secondary/driven)
        └── persistence/ # Repository implementations
```

**Data Flow for Web MVC:**
```
Use Case → Result(Snapshot) → Controller → ViewModel → Template
```
- Use cases return domain read models wrapped in Result objects
- Controllers convert snapshots to page-specific ViewModels
- ViewModels use primitives only (String, BigDecimal, int) - no domain objects
- See [dto-vs-viewmodel-analysis.md](dto-vs-viewmodel-analysis.md) for details

**Shared Kernel:** Cross-context value objects and the specification base in `sharedkernel/`; the DDD and hexagonal
marker interfaces come from the library `dev.domaincentric:dca-building-blocks`

**Full structure:** See [package-structure.md](package-structure.md) for complete details

**Key Principles:**
1. Dependencies flow inward toward domain core
2. Domain layer is framework-independent
3. One repository per aggregate root
4. Aggregates reference each other by ID only
5. Domain events enable eventual consistency

**Architecture Tests:**
```bash
./gradlew test-architecture
```

## Related Documentation

- [CLAUDE.md](../../CLAUDE.md) - Guidelines for AI assistants
- [README.md](../../README.md) - Project overview
- [Integration Guides](../integrations/README.md) - Technology integrations
