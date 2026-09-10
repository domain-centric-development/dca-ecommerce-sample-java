# DCA E-Commerce Sample (Java)

The reference implementation of **Domain-Centric Architecture (DCA)** — a synthesis of **Domain-Driven Design**, **Hexagonal Architecture** and **Clean Architecture** — as a working e-commerce application, with **MCP (Model Context Protocol)** server integration for AI assistant interaction.

*Written with AI assistance — drafted mainly by Claude, reviewed and directed by the author since
2025. The architecture rules in this repository's build are part of how that work is verified.*

## Overview

This project showcases best practices for structuring a Spring Boot application with clean architecture principles. It implements eight bounded contexts:
- **Product Catalog** - Product management with enriched views (pricing + stock from other contexts)
- **Shopping Cart** - Customer shopping cart management with article price resolution
- **Checkout** - Multi-step checkout flow with session management
- **Account** - User registration, authentication and account self-service (overview, profile, change password)
- **Portal** - Application home page and navigation
- **Inventory** - Stock level management (Open Host Service)
- **Pricing** - Product pricing management (Open Host Service)
- **Backoffice** - Event publication log viewer and administrative tools

### Key Features

- **AI-Accessible Product Catalog** via MCP server (Spring AI 2.0.0-M2)
- **Spring Modulith** for framework-enforced module boundaries and event-driven cross-module communication
- **Complete Architecture Testing** with ArchUnit (10 test suites) and Spring Modulith verification
- **31 Architecture Decision Records** documenting design choices
- **Shared Kernel** pattern for cross-context value objects
- **Framework-Independent Domain** layer (no Spring/JPA in core)
- **Multi-step Checkout Flow** with 5 steps and session management
- **Specification Pattern with Visitor** for database-agnostic cart filtering
- **Multiple Persistence Strategies** (InMemory, JPA, JDBC) — cart via JPA, account via JDBC, both
  against H2; every adapter hands out copies, never the stored instance (ADR-031)

## Architecture Patterns

### Domain-Driven Design (DDD)

**Strategic Patterns:**
- **Bounded Contexts**: Product Catalog, Shopping Cart, Checkout, Account, Portal, Inventory, Pricing, Backoffice (the last a generic subdomain — operating the application itself)
- **Shared Kernel**: Cross-context value objects (Money, Price, ProductId, UserId)
- **Context Mapping**: Declared as `@Upstream`/`@Partnership` package annotations, enforced by ArchUnit, and rendered as a generated [context map](docs/architecture/context-map.md) (see [ADR-032](docs/architecture/adr/adr-032-executable-context-map.md))
- **Open Host Service**: ProductCatalogService, InventoryService, PricingService, and CartService provide cross-context APIs

**Tactical Patterns:**
- **Aggregates**: Product, ShoppingCart, CheckoutSession, Account, StockLevel, ProductPrice
- **Entities**: CartItem, CheckoutLineItem
- **Value Objects**: ProductId, SKU, Price, Money, Quantity, Category, BuyerInfo, DeliveryAddress, Email, HashedPassword, etc.
- **Repositories**: Interfaces in application layer, implementations in adapters
- **Domain Services**: CartTotalCalculator, CheckoutStepValidator
- **Domain Events**: ProductCreated, CartCheckedOut, CartItemAddedToCart, CartItemQuantityChanged, ProductRemovedFromCart, CartCleared, CheckoutSessionStarted, CheckoutConfirmed, AccountRegistered, PriceChanged, StockChanged, etc.
- **Factories**: ProductFactory, EnrichedCartFactory, CheckoutCartFactory
- **Specifications**: CartSpecification (with Visitor pattern: ActiveCart, HasMinTotal, HasAnyAvailableItem, LastUpdatedBefore, CustomerAllowsMarketing); StockLevelSpecification (AvailableQuantityBelow, visited by StockLevelSpecificationVisitor)

### Clean Architecture

- **Use Cases** (Input Ports): Explicit use case interfaces with single responsibility
- **Input/Output Models**: Commands, Queries, and Result objects decouple layers
- **Framework Independence**: Domain layer is framework-agnostic; application layer uses minimal framework annotations pragmatically
- **Dependency Rule**: Dependencies point inward (Infrastructure → Adapters → Application → Domain)
- **Use Case Organization**: One use case per operation (CreateProductUseCase, AddItemToCartUseCase, StartCheckoutUseCase, etc.)

### Hexagonal Architecture (Ports and Adapters)

- **Input Ports**: Use case interfaces (defined in application layer)
- **Output Ports**: Repository and service interfaces (defined in application/shared)
- **Incoming Adapters** (Primary): REST Controllers, MCP Server, Web MVC, Event Consumers, Open Host Services
- **Outgoing Adapters** (Secondary): In-memory, JPA, and JDBC repository implementations

### Onion Architecture

Layers (from innermost to outermost):
1. **Domain Model** (per bounded context) - Pure business logic, framework-independent
2. **Application Services** (per bounded context) - Use case orchestration
3. **Adapters** (per bounded context) - External interfaces
4. **Shared Kernel** - Cross-context shared concepts
5. **Infrastructure** - Cross-cutting concerns

## Project Structure

```
src/main/java/dev/domaincentric/sample/ecommerce/
├── sharedkernel/                         # Shared Kernel (cross-context)
│   │                                     # (architectural markers come from dev.domaincentric:dca-building-blocks —
│   │                                     #  ddd.tactical, ddd.strategic[.relationships], hexagonal.port.in/out)
│   ├── infrastructure/
│   │   └── AsyncInitialize.java          # Sample-specific framework marker (not a DCA building block)
│   ├── application/
│   │   └── shared/                       # App-specific ports shared by several contexts
│   │       └── IdentityProvider.java     # Current caller's identity (not a generic marker)
│   ├── domain/
│   │   ├── model/                        # Shared value objects
│   │   │   ├── ProductId.java            # Cross-context ID
│   │   │   ├── UserId.java               # Cross-context ID
│   │   │   ├── Money.java                # Cross-context value
│   │   │   ├── Price.java                # Cross-context value
│   │   │   ├── PageResult.java           # Pagination result
│   │   │   └── PagingRequest.java        # Pagination request
│   │   └── specification/                # Composable specification pattern
│   │       ├── CompositeSpecification.java
│   │       ├── AndSpecification.java
│   │       ├── OrSpecification.java
│   │       ├── NotSpecification.java
│   │       └── SpecificationVisitor.java
│   (DomainEventPublisher and TransactionBoundary implementations come from dca-spring)
│
├── product/                              # Product Catalog bounded context
│   ├── api/                              # Spring Modulith @NamedInterface API
│   │   └── ProductCatalogService.java    # Open Host Service
│   ├── events/                           # Spring Modulith integration events
│   │   └── ProductCreatedEvent.java
│   ├── domain/
│   │   ├── model/                        # Domain model
│   │   │   ├── Product.java              # Aggregate Root
│   │   │   ├── SKU.java                  # Value Objects
│   │   │   ├── ProductName.java
│   │   │   ├── ProductDescription.java
│   │   │   ├── ProductArticle.java
│   │   │   ├── EnrichedProduct.java      # Enriched read model
│   │   │   ├── Category.java
│   │   │   ├── ImageUrl.java
│   │   │   └── ProductFactory.java       # Factory
│   │   └── event/                        # Domain events
│   │       ├── ProductCreated.java
│   │       ├── ProductNameChanged.java
│   │       ├── ProductDescriptionChanged.java
│   │       └── ProductCategoryChanged.java
│   ├── application/                      # Application layer
│   │   ├── createproduct/                # Use case: Create Product
│   │   │   ├── CreateProductInputPort.java
│   │   │   ├── CreateProductUseCase.java
│   │   │   ├── CreateProductCommand.java
│   │   │   └── CreateProductResult.java
│   │   ├── getallproducts/               # Use case: Get All Products
│   │   │   ├── GetAllProductsInputPort.java
│   │   │   ├── GetAllProductsUseCase.java
│   │   │   ├── GetAllProductsQuery.java
│   │   │   └── GetAllProductsResult.java
│   │   ├── getproductbyid/               # Use case: Get Product By ID
│   │   │   ├── GetProductByIdInputPort.java
│   │   │   ├── GetProductByIdUseCase.java
│   │   │   ├── GetProductByIdQuery.java
│   │   │   └── GetProductByIdResult.java
│   │   └── shared/                       # Shared output ports
│   │       ├── ProductRepository.java
│   │       ├── PricingDataPort.java      # Port for pricing data from Pricing context
│   │       └── ProductStockDataPort.java # Port for stock data from Inventory context
│   ├── infrastructure/                   # Per-context infrastructure
│   │   └── ProductDomainConfiguration.java
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters (primary)
│       │   ├── api/
│       │   │   ├── ProductResource.java
│       │   │   ├── CreateProductRequest.java
│       │   │   ├── ProductDto.java
│       │   │   └── ProductDtoConverter.java
│       │   ├── mcp/
│       │   │   └── ProductCatalogMcpToolProvider.java
│       │   ├── web/
│       │   │   ├── ProductPageController.java
│       │   │   ├── ProductCatalogPageViewModel.java
│       │   │   └── ProductDetailPageViewModel.java
│       │   └── event/
│       │       └── ProductEventConsumer.java
│       └── outgoing/                     # Outgoing adapters (secondary)
│           ├── event/
│           │   └── ProductCreatedEventPublisher.java
│           ├── persistence/
│           │   └── InMemoryProductRepository.java
│           ├── pricing/
│           │   └── PricingDataAdapter.java    # Adapter to Pricing context
│           └── inventory/
│               └── InventoryStockDataAdapter.java  # Adapter to Inventory context
│
├── cart/                                 # Shopping Cart bounded context
│   ├── api/                              # Spring Modulith @NamedInterface API
│   │   └── CartService.java             # Open Host Service
│   ├── events/                           # Spring Modulith integration events
│   │   ├── CartCheckedOutEvent.java
│   │   ├── CartCompletionTrigger.java
│   │   └── CartContentsChangedEvent.java
│   ├── domain/
│   │   ├── model/                        # Domain model
│   │   │   ├── ShoppingCart.java         # Aggregate Root
│   │   │   ├── CartItem.java             # Entity
│   │   │   ├── CartId.java               # Value Objects
│   │   │   ├── CartItemId.java
│   │   │   ├── CustomerId.java
│   │   │   ├── Quantity.java
│   │   │   ├── CartStatus.java
│   │   │   ├── ArticlePrice.java         # Price from Pricing context
│   │   │   ├── ArticlePriceResolver.java # Resolver for fresh prices
│   │   │   ├── CartArticle.java          # Article data for cart items
│   │   │   ├── EnrichedCart.java         # Enriched read model
│   │   │   ├── EnrichedCartFactory.java  # Factory for enriched carts
│   │   │   ├── EnrichedCartItem.java     # Enriched cart item with prices
│   │   │   └── CartValidationResult.java # Validation result
│   │   ├── specification/                # Cart specifications (Visitor pattern)
│   │   │   ├── CartSpecification.java    # Base specification interface
│   │   │   ├── CartSpecificationVisitor.java  # Visitor for database-agnostic filtering
│   │   │   ├── ActiveCart.java
│   │   │   ├── HasMinTotal.java
│   │   │   ├── HasAnyAvailableItem.java
│   │   │   ├── LastUpdatedBefore.java
│   │   │   └── CustomerAllowsMarketing.java
│   │   ├── service/                      # Domain services
│   │   │   └── CartTotalCalculator.java
│   │   └── event/                        # Domain events
│   │       ├── CartCheckedOut.java
│   │       ├── CartItemAddedToCart.java
│   │       ├── CartItemQuantityChanged.java
│   │       ├── ProductRemovedFromCart.java
│   │       ├── CartCleared.java
│   │       ├── CartCompleted.java
│   │       └── CartAbandoned.java
│   ├── application/                      # Application layer — use cases grouped by feature
│   │   ├── shopping/                     # Feature: filling and reading the shopping cart
│   │   │   ├── createcart/               # Use case: Create Cart
│   │   │   │   ├── CreateCartInputPort.java
│   │   │   │   ├── CreateCartUseCase.java
│   │   │   │   ├── CreateCartCommand.java
│   │   │   │   └── CreateCartResult.java
│   │   │   ├── getorcreateactivecart/    # Use case: Get or Create Active Cart
│   │   │   │   ├── GetOrCreateActiveCartInputPort.java
│   │   │   │   ├── GetOrCreateActiveCartUseCase.java
│   │   │   │   ├── GetOrCreateActiveCartCommand.java
│   │   │   │   └── GetOrCreateActiveCartResult.java
│   │   │   ├── getcartbyid/              # Use case: Get Cart By ID
│   │   │   │   ├── GetCartByIdInputPort.java
│   │   │   │   ├── GetCartByIdUseCase.java
│   │   │   │   ├── GetCartByIdQuery.java
│   │   │   │   └── GetCartByIdResult.java
│   │   │   ├── additemtocart/            # Use case: Add Item to Cart
│   │   │   │   ├── AddItemToCartInputPort.java
│   │   │   │   ├── AddItemToCartUseCase.java
│   │   │   │   ├── AddItemToCartCommand.java
│   │   │   │   └── AddItemToCartResult.java
│   │   │   └── removeitemfromcart/       # Use case: Remove Item from Cart
│   │   │       ├── RemoveItemFromCartInputPort.java
│   │   │       ├── RemoveItemFromCartUseCase.java
│   │   │       ├── RemoveItemFromCartCommand.java
│   │   │       └── RemoveItemFromCartResult.java
│   │   ├── cartrecovery/                 # Feature: recovering a guest cart on login (merge)
│   │   │   ├── recovercart/              # Use case: Recover Cart on Login
│   │   │   │   ├── RecoverCartOnLoginInputPort.java
│   │   │   │   ├── RecoverCartOnLoginUseCase.java
│   │   │   │   ├── RecoverCartOnLoginCommand.java
│   │   │   │   └── RecoverCartOnLoginResult.java
│   │   │   ├── getcartmergeoptions/      # Use case: Get Cart Merge Options
│   │   │   │   ├── GetCartMergeOptionsInputPort.java
│   │   │   │   ├── GetCartMergeOptionsUseCase.java
│   │   │   │   ├── GetCartMergeOptionsQuery.java
│   │   │   │   └── GetCartMergeOptionsResult.java
│   │   │   └── mergecarts/               # Use case: Merge Carts
│   │   │       ├── MergeCartsInputPort.java
│   │   │       ├── MergeCartsUseCase.java
│   │   │       ├── MergeCartsCommand.java
│   │   │       ├── MergeCartsResult.java
│   │   │       └── CartMergeStrategy.java
│   │   ├── cartcheckout/                 # Feature: submitting snapshots and reconciling purchased contents
│   │   │   ├── checkoutcart/             # Use case: Checkout Cart
│   │   │   │   ├── CheckoutCartInputPort.java
│   │   │   │   ├── CheckoutCartUseCase.java
│   │   │   │   ├── CheckoutCartCommand.java
│   │   │   │   └── CheckoutCartResult.java
│   │   │   └── completecart/             # Use case: Complete Cart (after checkout)
│   │   │       ├── CompleteCartInputPort.java
│   │   │       ├── CompleteCartUseCase.java
│   │   │       ├── CompleteCartCommand.java
│   │   │       └── CompleteCartResult.java
│   │   ├── operations/                   # Feature: operating the shop (staff-only queries)
│   │   │   └── getallcarts/              # Use case: Get All Carts
│   │   │       ├── GetAllCartsInputPort.java
│   │   │       ├── GetAllCartsUseCase.java
│   │   │       ├── GetAllCartsQuery.java
│   │   │       └── GetAllCartsResult.java
│   │   └── shared/                       # Context-wide output ports (never per feature)
│   │       ├── ShoppingCartRepository.java
│   │       └── ArticleDataPort.java      # Port for article data (prices + stock)
│   ├── infrastructure/                   # Per-context infrastructure
│   │   └── CartDomainConfiguration.java
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters
│       │   ├── api/
│       │   │   ├── ShoppingCartResource.java
│       │   │   ├── AddToCartRequest.java
│       │   │   ├── ShoppingCartDto.java
│       │   │   ├── ShoppingCartListDto.java
│       │   │   ├── CartItemDto.java
│       │   │   └── ShoppingCartDtoConverter.java
│       │   ├── web/                      # Protocol first, feature below it
│       │   │   ├── shopping/
│       │   │   │   ├── CartPageController.java
│       │   │   │   ├── CartPageViewModel.java
│       │   │   │   ├── MiniBasketControllerAdvice.java
│       │   │   │   └── MiniBasketItemViewModel.java
│       │   │   └── cartrecovery/
│       │   │       ├── CartMergePageController.java
│       │   │       └── CartMergePageViewModel.java
│       │   └── event/
│       │       ├── CartEventConsumer.java  # Logs the context's own domain events (no feature)
│       │       └── cartcheckout/
│       │           └── CartCompletionEventConsumer.java
│       └── outgoing/                     # Outgoing adapters
│           ├── event/
│           │   ├── CartCheckedOutEventPublisher.java
│           │   └── CartContentsChangedEventPublisher.java
│           ├── product/
│           │   └── CompositeArticleDataAdapter.java  # Composite adapter for article data
│           └── persistence/
│               ├── InMemoryShoppingCartRepository.java
│               ├── JdbcShoppingCartRepository.java
│               ├── jpa/                  # JPA persistence alternative
│               │   ├── JpaShoppingCartRepository.java
│               │   ├── CartJpaRepository.java
│               │   ├── CartEntity.java
│               │   ├── CartItemEntity.java
│               │   └── CartSpecToJpa.java    # Specification visitor for JPA
│               └── jdbc/
│                   └── CartSpecToJdbc.java   # Specification visitor for JDBC
│
├── checkout/                             # Checkout bounded context
│   ├── events/                           # Spring Modulith integration events
│   │   └── CheckoutConfirmedEvent.java
│   ├── domain/
│   │   ├── model/                        # Domain model
│   │   │   ├── CheckoutSession.java      # Aggregate Root
│   │   │   ├── CheckoutLineItem.java     # Entity
│   │   │   ├── CheckoutSessionId.java    # Value Objects
│   │   │   ├── CheckoutLineItemId.java
│   │   │   ├── CheckoutStep.java         # Enum: BUYER_INFO, DELIVERY, PAYMENT, REVIEW, CONFIRMATION
│   │   │   ├── CheckoutSessionStatus.java
│   │   │   ├── CheckoutTotals.java
│   │   │   ├── CheckoutValidationResult.java
│   │   │   ├── BuyerInfo.java
│   │   │   ├── DeliveryAddress.java
│   │   │   ├── ShippingOption.java
│   │   │   ├── PaymentSelection.java
│   │   │   ├── PaymentProviderId.java
│   │   │   ├── CustomerId.java
│   │   │   ├── CartId.java
│   │   │   ├── CheckoutArticle.java      # Article data for checkout
│   │   │   ├── CheckoutArticlePriceResolver.java  # Price resolver
│   │   │   ├── CheckoutCart.java          # Cart snapshot for checkout
│   │   │   ├── CheckoutCartFactory.java   # Factory for checkout cart
│   │   │   └── EnrichedCheckoutLineItem.java  # Enriched line item with prices
│   │   ├── readmodel/                    # Read Models
│   │   │   ├── CheckoutCartSnapshot.java # Cart snapshot for display
│   │   │   └── LineItemSnapshot.java     # Line item snapshot for display
│   │   ├── service/                      # Domain services
│   │   │   └── CheckoutStepValidator.java
│   │   └── event/                        # Domain events
│   │       ├── CheckoutSessionStarted.java
│   │       ├── BuyerInfoSubmitted.java
│   │       ├── DeliverySubmitted.java
│   │       ├── PaymentSubmitted.java
│   │       ├── CheckoutConfirmed.java
│   │       ├── CheckoutCompleted.java
│   │       ├── CheckoutAbandoned.java
│   │       └── CheckoutExpired.java
│   ├── application/                      # Application layer — use cases grouped by feature
│   │   ├── session/                      # Feature: starting and reading checkout sessions
│   │   │   ├── startcheckout/            # Use case: Start Checkout
│   │   │   │   ├── StartCheckoutInputPort.java
│   │   │   │   ├── StartCheckoutUseCase.java
│   │   │   │   ├── StartCheckoutCommand.java
│   │   │   │   └── StartCheckoutResult.java
│   │   │   ├── getactivecheckoutsession/ # Use case: Get Active Checkout Session
│   │   │   │   ├── GetActiveCheckoutSessionInputPort.java
│   │   │   │   ├── GetActiveCheckoutSessionUseCase.java
│   │   │   │   ├── GetActiveCheckoutSessionQuery.java
│   │   │   │   └── GetActiveCheckoutSessionResult.java
│   │   │   ├── getcheckoutsession/       # Use case: Get Checkout Session
│   │   │   │   ├── GetCheckoutSessionInputPort.java
│   │   │   │   ├── GetCheckoutSessionUseCase.java
│   │   │   │   ├── GetCheckoutSessionQuery.java
│   │   │   │   └── GetCheckoutSessionResult.java
│   │   │   └── getconfirmedcheckoutsession/# Use case: Get Confirmed Checkout Session
│   │   │       ├── GetConfirmedCheckoutSessionInputPort.java
│   │   │       ├── GetConfirmedCheckoutSessionUseCase.java
│   │   │       ├── GetConfirmedCheckoutSessionQuery.java
│   │   │       └── GetConfirmedCheckoutSessionResult.java
│   │   ├── checkoutcompletion/           # Feature: the steps that complete a checkout
│   │   │   ├── submitbuyerinfo/          # Use case: Submit Buyer Info
│   │   │   │   ├── SubmitBuyerInfoInputPort.java
│   │   │   │   ├── SubmitBuyerInfoUseCase.java
│   │   │   │   ├── SubmitBuyerInfoCommand.java
│   │   │   │   └── SubmitBuyerInfoResult.java
│   │   │   ├── getshippingoptions/       # Use case: Get Shipping Options
│   │   │   │   ├── GetShippingOptionsInputPort.java
│   │   │   │   ├── GetShippingOptionsUseCase.java
│   │   │   │   ├── GetShippingOptionsQuery.java
│   │   │   │   └── GetShippingOptionsResult.java
│   │   │   ├── submitdelivery/           # Use case: Submit Delivery
│   │   │   │   ├── SubmitDeliveryInputPort.java
│   │   │   │   ├── SubmitDeliveryUseCase.java
│   │   │   │   ├── SubmitDeliveryCommand.java
│   │   │   │   └── SubmitDeliveryResult.java
│   │   │   ├── getpaymentproviders/      # Use case: Get Payment Providers
│   │   │   │   ├── GetPaymentProvidersInputPort.java
│   │   │   │   ├── GetPaymentProvidersUseCase.java
│   │   │   │   ├── GetPaymentProvidersQuery.java
│   │   │   │   └── GetPaymentProvidersResult.java
│   │   │   ├── submitpayment/            # Use case: Submit Payment
│   │   │   │   ├── SubmitPaymentInputPort.java
│   │   │   │   ├── SubmitPaymentUseCase.java
│   │   │   │   ├── SubmitPaymentCommand.java
│   │   │   │   └── SubmitPaymentResult.java
│   │   │   └── confirmcheckout/          # Use case: Confirm Checkout
│   │   │       ├── ConfirmCheckoutInputPort.java
│   │   │       ├── ConfirmCheckoutUseCase.java
│   │   │       ├── ConfirmCheckoutCommand.java
│   │   │       └── ConfirmCheckoutResult.java
│   │   ├── cartsync/                     # Feature: legacy cart-change compatibility (snapshots stay unchanged)
│   │   │   └── synccheckoutwithcart/     # Use case: Sync Checkout with Cart
│   │   │       ├── SyncCheckoutWithCartInputPort.java
│   │   │       ├── SyncCheckoutWithCartUseCase.java
│   │   │       ├── SyncCheckoutWithCartCommand.java
│   │   │       └── SyncCheckoutWithCartResult.java
│   │   └── shared/                       # Context-wide output ports (never per feature)
│   │       ├── CheckoutSessionRepository.java
│   │       ├── CartDataPort.java
│   │       ├── CartData.java
│   │       ├── CheckoutArticleDataPort.java  # Port for article data (prices + stock)
│   │       ├── ProductInfoPort.java
│   │       ├── PaymentProvider.java
│   │       └── PaymentProviderRegistry.java
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters
│       │   ├── web/                      # Protocol first, feature below it
│       │   │   ├── session/
│       │   │   │   └── StartCheckoutPageController.java
│       │   │   └── checkoutcompletion/
│       │   │       ├── BuyerInfoPageController.java
│       │   │       ├── BuyerInfoPageViewModel.java
│       │   │       ├── DeliveryPageController.java
│       │   │       ├── DeliveryPageViewModel.java
│       │   │       ├── PaymentPageController.java
│       │   │       ├── PaymentPageViewModel.java
│       │   │       ├── ReviewPageController.java
│       │   │       ├── ReviewPageViewModel.java
│       │   │       ├── ConfirmationPageController.java
│       │   │       └── ConfirmationPageViewModel.java
│       │   └── event/
│       │       └── cartsync/
│       │           └── CartChangeEventConsumer.java
│       └── outgoing/                     # Outgoing adapters
│           ├── event/
│           │   └── CheckoutConfirmedEventPublisher.java
│           ├── persistence/
│           │   └── InMemoryCheckoutSessionRepository.java
│           ├── cart/
│           │   └── CartDataAdapter.java
│           ├── product/
│           │   ├── CompositeCheckoutArticleDataAdapter.java  # Composite adapter
│           │   └── ProductInfoAdapter.java
│           └── payment/
│               ├── MockPaymentProvider.java
│               └── InMemoryPaymentProviderRegistry.java
│
├── account/                              # Account bounded context
│   ├── domain/
│   │   ├── model/                        # Domain model
│   │   │   ├── Account.java              # Aggregate Root
│   │   │   ├── AccountId.java            # Value Objects
│   │   │   ├── Email.java
│   │   │   ├── Owner.java               # Name (fixed) + date of birth
│   │   │   ├── HashedPassword.java
│   │   │   └── AccountStatus.java
│   │   ├── specification/                # Domain specifications
│   │   │   └── UsableDateOfBirth.java    # Known, not in the future
│   │   ├── service/                      # Domain services
│   │   │   └── PasswordHasher.java       # Interface for password hashing
│   │   └── event/                        # Domain events
│   │       ├── AccountRegistered.java
│   │       ├── AccountLinkedToIdentity.java
│   │       ├── AccountLoggedIn.java
│   │       ├── AccountPasswordChanged.java
│   │       ├── AccountEmailChanged.java
│   │       ├── AccountOwnerDateOfBirthChanged.java
│   │       ├── AccountSuspended.java
│   │       ├── AccountReactivated.java
│   │       └── AccountClosed.java
│   ├── application/                      # Application layer
│   │   ├── registeraccount/              # Use case: Register Account
│   │   │   ├── RegisterAccountInputPort.java
│   │   │   ├── RegisterAccountUseCase.java
│   │   │   ├── RegisterAccountCommand.java
│   │   │   └── RegisterAccountResult.java
│   │   ├── authenticateaccount/          # Use case: Authenticate Account
│   │   │   ├── AuthenticateAccountInputPort.java
│   │   │   ├── AuthenticateAccountUseCase.java
│   │   │   ├── AuthenticateAccountCommand.java
│   │   │   └── AuthenticateAccountResult.java
│   │   ├── getaccountoverview/           # Use case: Get Account Overview (read)
│   │   │   ├── GetAccountOverviewInputPort.java
│   │   │   ├── GetAccountOverviewUseCase.java
│   │   │   ├── GetAccountOverviewQuery.java
│   │   │   └── GetAccountOverviewResult.java
│   │   ├── changepassword/               # Use case: Change Password
│   │   │   ├── ChangePasswordInputPort.java
│   │   │   ├── ChangePasswordUseCase.java
│   │   │   ├── ChangePasswordCommand.java
│   │   │   └── ChangePasswordResult.java
│   │   ├── getprofile/                   # Use case: Get Profile (read, /account/profile)
│   │   │   ├── GetProfileInputPort.java
│   │   │   ├── GetProfileUseCase.java
│   │   │   ├── GetProfileQuery.java
│   │   │   └── GetProfileResult.java
│   │   ├── changeprofile/                # Use case: Change Profile (email, date of birth)
│   │   │   ├── ChangeProfileInputPort.java
│   │   │   ├── ChangeProfileUseCase.java
│   │   │   ├── ChangeProfileCommand.java
│   │   │   └── ChangeProfileResult.java
│   │   └── shared/                       # Shared output ports
│   │       ├── AccountRepository.java
│   │       ├── RegisteredUserValidator.java
│   │       ├── TokenService.java
│   │       └── IdentitySession.java
│   ├── infrastructure/                   # Per-context infrastructure
│   │   └── SecurityConfiguration.java
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters
│       │   ├── api/
│       │   │   ├── AuthResource.java
│       │   │   ├── LoginRequest.java
│       │   │   ├── LoginResponse.java
│       │   │   ├── RegisterRequest.java
│       │   │   └── RegisterResponse.java
│       │   └── web/
│       │       ├── LoginPageController.java
│       │       ├── LogoutPageController.java
│       │       ├── RegisterPageController.java
│       │       ├── MyAccountPageController.java
│       │       ├── MyAccountPageViewModel.java
│       │       ├── ChangePasswordPageController.java
│       │       ├── ChangePasswordPageViewModel.java
│       │       ├── ProfilePageController.java
│       │       ├── ProfilePageViewModel.java
│       │       └── AccountNavigation.java
│       └── outgoing/                     # Outgoing adapters
│           ├── persistence/
│           │   ├── JdbcAccountRepository.java     # default (ADR-031)
│           │   └── InMemoryAccountRepository.java # "inmemory" profile
│           └── security/
│               ├── SpringSecurityPasswordHasher.java
│               ├── AccountBasedRegisteredUserValidator.java
│               ├── SpringSecurityIdentityProvider.java
│               ├── JwtTokenService.java
│               ├── JwtIdentitySession.java
│               ├── JwtIdentity.java
│               ├── JwtIdentityType.java
│               ├── JwtProperties.java
│               └── JwtAuthenticationFilter.java
│
├── inventory/                            # Inventory bounded context
│   ├── api/                              # Spring Modulith @NamedInterface API
│   │   └── InventoryService.java         # Open Host Service
│   ├── events/                           # Spring Modulith integration events
│   │   ├── StockReductionTrigger.java
│   │   └── StockInitializationTrigger.java
│   ├── domain/
│   │   ├── model/                        # Domain model
│   │   │   ├── StockLevel.java           # Aggregate Root
│   │   │   ├── StockLevelId.java         # Value Objects
│   │   │   └── StockQuantity.java
│   │   └── event/                        # Domain events
│   │       ├── StockLevelCreated.java
│   │       ├── StockChanged.java
│   │       ├── StockIncreased.java
│   │       ├── StockDecreased.java
│   │       ├── StockReserved.java
│   │       └── StockReleased.java
│   ├── application/                      # Application layer
│   │   ├── setstocklevel/                # Use case: Set Stock Level
│   │   │   ├── SetStockLevelInputPort.java
│   │   │   ├── SetStockLevelUseCase.java
│   │   │   ├── SetStockLevelCommand.java
│   │   │   └── SetStockLevelResult.java
│   │   ├── reducestock/                  # Use case: Reduce Stock
│   │   │   ├── ReduceStockInputPort.java
│   │   │   ├── ReduceStockUseCase.java
│   │   │   ├── ReduceStockCommand.java
│   │   │   └── ReduceStockResult.java
│   │   ├── getstockforproducts/          # Use case: Get Stock for Products
│   │   │   ├── GetStockForProductsInputPort.java
│   │   │   ├── GetStockForProductsUseCase.java
│   │   │   ├── GetStockForProductsQuery.java
│   │   │   └── GetStockForProductsResult.java
│   │   └── shared/                       # Shared output ports
│   │       └── StockLevelRepository.java
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters
│       │   └── event/
│       │       ├── StockReductionEventConsumer.java
│       │       └── StockInitializationEventConsumer.java
│       └── outgoing/                     # Outgoing adapters
│           └── persistence/
│               └── InMemoryStockLevelRepository.java
│
├── pricing/                              # Pricing bounded context
│   ├── api/                              # Spring Modulith @NamedInterface API
│   │   └── PricingService.java           # Open Host Service
│   ├── events/                           # Spring Modulith integration events
│   │   └── PriceInitializationTrigger.java
│   ├── domain/
│   │   ├── model/                        # Domain model
│   │   │   ├── ProductPrice.java         # Aggregate Root
│   │   │   └── PriceId.java              # Value Objects
│   │   └── event/                        # Domain events
│   │       ├── PriceCreated.java
│   │       └── PriceChanged.java
│   ├── application/                      # Application layer
│   │   ├── setproductprice/              # Use case: Set Product Price
│   │   │   ├── SetProductPriceInputPort.java
│   │   │   ├── SetProductPriceUseCase.java
│   │   │   ├── SetProductPriceCommand.java
│   │   │   └── SetProductPriceResult.java
│   │   ├── getpricesforproducts/         # Use case: Get Prices for Products
│   │   │   ├── GetPricesForProductsInputPort.java
│   │   │   ├── GetPricesForProductsUseCase.java
│   │   │   ├── GetPricesForProductsQuery.java
│   │   │   └── GetPricesForProductsResult.java
│   │   └── shared/                       # Shared output ports
│   │       └── ProductPriceRepository.java
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters
│       │   └── event/
│       │       └── PriceInitializationEventConsumer.java
│       └── outgoing/                     # Outgoing adapters
│           └── persistence/
│               └── InMemoryProductPriceRepository.java
│
├── portal/                               # Portal bounded context
│   └── adapter/                          # Adapters
│       └── incoming/                     # Incoming adapters
│           └── web/
│               └── HomePageController.java
│
├── backoffice/                           # Backoffice context (generic subdomain: operating the app)
│   ├── application/
│   │   ├── geteventpublications/         # Use case: Get Event Publications
│   │   │   ├── GetEventPublicationsInputPort.java
│   │   │   ├── GetEventPublicationsUseCase.java
│   │   │   ├── GetEventPublicationsQuery.java
│   │   │   └── GetEventPublicationsResult.java
│   │   └── shared/                       # Shared output ports
│   │       └── EventPublicationLogRepository.java
│   ├── infrastructure/                   # Per-context infrastructure
│   │   ├── BackofficeSecurityConfiguration.java
│   │   └── BackofficeSecurityProperties.java
│   └── adapter/                          # Adapters
│       ├── incoming/                     # Incoming adapters
│       │   └── web/
│       │       ├── EventPublicationPageController.java
│       │       └── EventPublicationPageViewModel.java
│       └── outgoing/                     # Outgoing adapters
│           └── persistence/
│               └── JdbcEventPublicationLogRepository.java
│
└── infrastructure/                       # Infrastructure (cross-cutting)
    ├── EcommerceSampleApplication.java    # Spring Boot main class
    ├── config/                           # Spring @Configuration classes
    │   ├── TransactionConfiguration.java
    │   ├── AsyncConfiguration.java
    │   └── Pug4jConfiguration.java
    ├── init/                             # Initialization
    │   └── SampleDataInitializer.java    # Sample data seeding
    └── support/                          # Framework support components
        └── AsyncInitializationProcessor.java
```

## Getting Started

### Prerequisites
- Java 25
- Gradle 9.3.1 or higher

### Running the Application

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### Running with Docker

No local JDK needed — the `Dockerfile` builds the jar and runs it on a JRE:

```bash
docker compose up --build                 # http://localhost:8080
docker compose run --rm test              # unit tests + architecture rules, dependencies cached in a volume
docker compose run --rm gradle bootJar    # any other Gradle task
```

`docker build -t dca-shop-java .` builds the image alone. Podman works the same way (`podman compose`).

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

## API Documentation

`/api/**` is authenticated by an `Authorization: Bearer` token and by **nothing else** — no browser cookie
reaches it and none is issued, which is the only reason it may skip the CSRF token (ADR-035). Authorization is
stated by each resource, not by the filter chain: the JWT filter gives every request an authentication, so
`authenticated()` is satisfied by an anonymous visitor too (ADR-036).

| Route | Who |
|---|---|
| `GET /api/products`, `GET /api/products/{id}` | anyone — the same assortment the shop pages show |
| `POST /api/products` | staff role |
| `GET /api/carts` (every cart in the shop) | staff role |
| `POST /api/carts`, `GET /api/carts/{id}`, `POST /{id}/items`, `DELETE /{id}/items/{productId}`, `POST /{id}/checkout`, `GET /api/carts/customer/{id}/active` | the caller, on their own cart — a stranger's cart answers `404`, never `403` |
| `POST /api/auth/{login,register,logout}` | anyone; the token comes back in the body, no cookie is set |

### Authentication

```bash
# Register (or /login with an existing account) — the response body carries the token
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"ada@example.com","password":"Secret123","firstName":"Ada","lastName":"Lovelace","dateOfBirth":"1815-12-10"}'
```

The staff role has no provisioning path in this sample: an operator token is minted out of band (see
`ApiAuthorizationIntegrationTest`).

### Product API

```bash
# Public
curl http://localhost:8080/api/products
curl http://localhost:8080/api/products/{productId}

# Staff only
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $STAFF_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "TEST-001",
    "name": "Test Product",
    "description": "A test product",
    "price": 99.99,
    "category": "Books",
    "stock": 10
  }'
```

### Shopping Cart API

Every route below acts on the cart of the token's own identity.

```bash
# Create the caller's cart (no customerId — it is the caller's)
curl -X POST http://localhost:8080/api/carts -H "Authorization: Bearer $TOKEN"

# The caller's active cart
curl http://localhost:8080/api/carts/customer/$USER_ID/active -H "Authorization: Bearer $TOKEN"

# One of the caller's carts; somebody else's answers 404
curl http://localhost:8080/api/carts/{cartId} -H "Authorization: Bearer $TOKEN"

curl -X POST http://localhost:8080/api/carts/{cartId}/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": "{productId}", "quantity": 2}'

curl -X DELETE http://localhost:8080/api/carts/{cartId}/items/{productId} -H "Authorization: Bearer $TOKEN"

curl -X POST http://localhost:8080/api/carts/{cartId}/checkout -H "Authorization: Bearer $TOKEN"

# Every cart in the shop — staff only
curl http://localhost:8080/api/carts -H "Authorization: Bearer $STAFF_TOKEN"
```

### MCP (Model Context Protocol) API

The product catalog is accessible via MCP server for AI assistants like Claude. The server runs on `http://localhost:8080/mcp` using **streamable HTTP** (HTTP + Server-Sent Events).

#### Available MCP Tools

**all-products**
- Returns all products in the catalog with complete details
- No parameters required

**product-by-sku**
- Find product by SKU (e.g., "BOOK-001")
- Parameters: `sku` (String) - must contain uppercase letters, numbers, hyphens only

**product-by-category**
- Find all products in a category
- Parameters: `categoryName` (String)
- Valid categories: Books, Modeling, Apparel, Desk & Office, Stickers & Pins

**product-by-id**
- Get product by internal UUID
- Parameters: `id` (String) - UUID format

#### Connecting AI Assistants

Create `.mcp.json` in the project root:

```json
{
  "mcpServers": {
    "product-catalog": {
      "type": "http",
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

AI assistants will automatically discover and connect to the MCP server.

**See:** [docs/integrations/mcp-server-integration.md](docs/integrations/mcp-server-integration.md) for detailed MCP server documentation.

## Sample Data

The shop sells what this architecture is made of. The application initializes with 21 sample
products across five categories:
- **Books** (8): the sources the architecture was synthesized from — Evans, Martin, Vernon (twice),
  Cockburn/Garrido de Paz, Khononov, Fowler, Skelton/Pais
- **Modeling** (4): Event Storming sticky-note kit, hexagon whiteboard magnets, context map poster,
  DDD pattern card deck
- **Apparel** (3): "Ports & Adapters" T-shirt, "Domain over Framework" hoodie, hexagon cap
- **Desk & Office** (4): "Ubiquitous Language" mug, hexagon coasters, hex-grid notebook, wooden
  hexagon desk model
- **Stickers & Pins** (2): hexagon sticker sheet, "Bounded Context" enamel pin

## Architecture Documentation

For comprehensive architecture documentation, see:
- **[docs/architecture/](docs/architecture/)** - Complete architecture documentation
- **[docs/architecture/architecture-principles.md](docs/architecture/architecture-principles.md)** - Detailed patterns and principles
- **[CLAUDE.md](CLAUDE.md)** - Development guidelines and best practices

### Quick Reference

**Domain Layer** (per bounded context) - Framework-independent business logic
- No Spring/JPA annotations in domain models
- All business rules in domain objects
- Organized into: domain.model, domain.service, domain.event, domain.specification
- Dependencies point inward toward domain

**Application Layer** (per bounded context) - Use case orchestration
- Thin coordination layer (no business logic)
- Manages transactions and domain event publishing
- Defines ports: Input ports (use cases) and output ports (repositories)
- One use case class per operation

**Adapter Layer** (per bounded context) - External interfaces
- Incoming Adapters (Primary):
  - `adapter.incoming.api` - REST APIs (@RestController) returning JSON via DTOs
  - `adapter.incoming.web` - Web MVC (@Controller) returning HTML via page-specific ViewModels
  - `adapter.incoming.mcp` - MCP Server for AI assistants
  - `adapter.incoming.event` - Domain event consumers
  - `adapter.incoming.event.acl` - Anti-corruption layer event translators
- Outgoing Adapters (Secondary):
  - `adapter.outgoing.persistence` - Repository implementations (InMemory, JPA, JDBC)
  - `adapter.outgoing.event` - Integration event publishers
  - `adapter.outgoing.cart`, `adapter.outgoing.product` - Cross-context data adapters
  - `adapter.outgoing.payment` - Payment provider adapters
  - `adapter.outgoing.security` - Security-related adapters
- DTO/ViewModel conversion happens in adapters (not application layer)
- ViewModels use primitives only, created from domain read models
- Adapters don't communicate directly

**Open Host Services** (Spring Modulith `@NamedInterface` API packages)
- `{context}.api` - Cross-context APIs exposed as Spring Modulith named interfaces
- ProductCatalogService, CartService, InventoryService, PricingService

**Integration Events** (Spring Modulith event packages)
- `{context}.events` - Integration events published via Spring Modulith for cross-module communication
- Separate from internal domain events in `domain.event`

**Shared Kernel** - Cross-context shared concepts
- Architectural markers come from the library `dev.domaincentric:dca-building-blocks` (`dev.domaincentric.dca.buildingblocks`):
  `ddd.tactical` (Entity, Value, AggregateRoot, DomainEvent, …), `ddd.strategic` (BoundedContext),
  `ddd.strategic.relationships` (SharedKernel, OpenHostService, Upstream, ExternalUpstream, Partnership),
  `hexagonal.port.in` (InputPort, UseCase), `hexagonal.port.out` (OutputPort, Repository, Store, DomainEventPublisher, IntegrationEventPublisher)
- `sharedkernel.infrastructure` - Sample-specific framework marker (AsyncInitialize)
- `sharedkernel.application.shared` - Application-specific ports shared across contexts (IdentityProvider) — not part of the generic marker set
- `sharedkernel.domain.model` - Shared value objects (Money, Price, ProductId, UserId, PageResult, PagingRequest)
- `sharedkernel.domain.specification` - Composable specification pattern
- Runtime adapters for the shared ports come from `dev.domaincentric:dca-spring` (`SpringDomainEventPublisher`, `SpringTransactionBoundary`, auto-configured) — the sample holds no copy

**Infrastructure Layer** - Cross-cutting concerns
- `infrastructure.config` - Spring @Configuration classes
- `infrastructure.support` - Framework support components (processors, listeners)

## Testing

### Architecture Tests

Architecture rules are **actively enforced** using ArchUnit (10 test suites) and Spring Modulith (module boundary verification):

```bash
./gradlew test-architecture
```

**ArchUnit Test Suites:**
- **DddTacticalPatternsArchUnitTest** - Aggregate, Entity, Value Object, Repository patterns
- **DddAdvancedPatternsArchUnitTest** - Domain Events, Services, Factories, Specifications
- **DddStrategicPatternsArchUnitTest** - Bounded Context isolation
- **HexagonalArchitectureArchUnitTest** - Ports and Adapters rules
- **OnionArchitectureArchUnitTest** - Dependency flow rules
- **LayeredArchitectureArchUnitTest** - Layer access rules
- **NamingConventionsArchUnitTest** - Naming standards
- **PackageCyclesArchUnitTest** - Circular dependency detection
- **UseCasePatternsArchUnitTest** - Application service patterns

**Spring Modulith Verification:**
- **SpringModulithVerificationTest** - Module boundary and cycle verification, a `DcaSpringModulithTest` subclass from `dca-archunit-spring-modulith`

**Shared Infrastructure:**
- **BaseArchUnitTest** - Common test constants and helpers

These tests verify:
- Domain layer has no framework dependencies
- Aggregates only reference other aggregates by ID
- Value objects are immutable records
- Repository interfaces live in application/shared
- No circular package dependencies
- Naming conventions are followed
- Bounded contexts are properly isolated
- Spring Modulith module boundaries are respected

### Unit Tests
```bash
./gradlew test
```

## Key Design Decisions

1. **In-Memory Storage**: Uses ConcurrentHashMap for simplicity; production would use JPA/database
2. **Security**: Permits all requests for demo purposes
3. **Price Snapshot**: Cart items store price at time of addition (common e-commerce pattern)
4. **Package-Private Constructors**: CartItem can only be created through ShoppingCart
5. **Immutable Value Objects**: All value objects are Java records
6. **MCP as Primary Adapter**: MCP server implemented as incoming adapter, maintaining clean architecture
7. **Read-Only MCP Tools**: AI access limited to queries for safety
8. **Shared Kernel**: Cross-context value objects shared between bounded contexts
9. **Multi-step Checkout Flow**: 5 steps (Buyer Info → Delivery → Payment → Review → Confirmation) with session management
10. **Specification Pattern with Visitor**: Database-agnostic filtering via CartSpecification and CartSpecificationVisitor
11. **Multiple Persistence Strategies**: InMemory (default), JPA, and JDBC implementations for shopping cart
12. **OpenHost Service Pattern**: Cross-context APIs via Spring Modulith `@NamedInterface` API packages
13. **UserId Continuity on Registration**: When a user registers, their anonymous UserId is preserved
14. **Spring Modulith Integration Events**: Cross-module communication via dedicated `events` packages, separate from internal domain events
15. **Interface Inversion for Event Listeners**: Event consumers listen on integration events published by source context (ADR-024)

**Architecture Decision Records:** See [docs/architecture/adr/README.md](docs/architecture/adr/README.md) for 24 documented architectural decisions.

## Business Rules Demonstrated

### Product Aggregate
- Price must be positive
- Stock cannot be negative
- SKU must be unique

### Shopping Cart Aggregate
- Cannot modify checked-out or completed cart
- Cannot checkout empty cart
- Each product appears once (quantities combined)
- Cart stores price snapshot at time of addition

### Checkout Session Aggregate
- Cannot skip steps - must complete in order (Buyer Info → Delivery → Payment → Review → Confirmation)
- Can go back to modify previous steps (before confirmation)
- Cannot modify confirmed, completed, or expired sessions
- Must have at least one line item to start checkout

### Account Aggregate
- Email must be unique across all accounts
- Password is hashed before storage (never stored in plain text)
- Account status transitions: PENDING → ACTIVE → SUSPENDED/DELETED
- An account belongs to an `Owner` (first name, last name, date of birth), captured at registration
- The owner's **name can never be changed**: no operation on the aggregate accepts an `Owner` or
  mentions a name, so `register` is the only way a name enters the system. The profile page
  `/account/profile` shows it as text without an input
- The profile page changes what may change — the email address and the owner's date of birth. The
  buyer's name in the checkout context is a separate, per-order concept and stays editable there
- Changing the email also changes the login credential, so the identity token is re-issued and the
  session stays valid

## Further Reading

### Project Documentation
- **[Architecture Documentation](docs/architecture/)** - Complete architecture guide
- **[Architecture Principles](docs/architecture/architecture-principles.md)** - Detailed DDD, Hexagonal, and Onion patterns
- **[CLAUDE.md](CLAUDE.md)** - Development guidelines and documentation standards

### External Resources
- **[Domain-Driven Design](https://www.domainlanguage.com/ddd/)** by Eric Evans
- **[Implementing Domain-Driven Design](https://vaughnvernon.com/)** by Vaughn Vernon
- **[Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)** by Alistair Cockburn
- **[Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)** by Robert C. Martin
- **[ArchUnit](https://www.archunit.org/)** - Architecture testing framework
- **[Spring Modulith](https://spring.io/projects/spring-modulith)** - Module boundary enforcement

## License

MIT — see [LICENSE](LICENSE). A sample project: read it as a reference for structure, not as a hardened
product.

Contributions are accepted under the MIT licence, and the copyright holder may additionally publish
them under other licences (for example a documentation licence for prose).

Event delivery (2026-09-09): committed capture is recovered independently of wakeup; completion is per listener/consumer. Automatic retry is bounded (five attempts, 200 ms exponential base), terminal failures remain inspectable, and Backoffice offers authenticated, CSRF-protected manual replay of failed work. Original payload/identity and acknowledged consumers are preserved. Provider idempotency is needed to suppress an external duplicate after acceptance-before-ack; local keys alone do not suffice. See the event-delivery ADR for storage limits and replay behavior.

## Shared business specification (review batch 2026-09-09)

Money uses ISO 4217 codes, non-negative amounts, two decimals half-up, and an upper bound of 999999999999.99.
Product creation and Pricing use strictly positive Price. Product-created v1 is the six-field notification described
in the local payload ADR. Invalid default quantities are rejected at entry/reconstitution; account role observations
are immutable snapshots. Aggregate creation owns event registration.

An explicit checkout action captures immutable positions, quantities and prices into a session. Cart edits do not
create or mutate sessions. A new action supersedes the previous OPEN/Active session; confirmed/completed orders remain.
Confirmation and replacement serialize through the same repository operation, including transaction completion.
Superseded confirmation has no completion effect. Abandonment/expiry closes only an open session and leaves cart contents.

Cart reconciliation intersects purchased unit intervals with the current stable position id. Later additions (also of
the same product), removed/re-added positions and other contents survive. Replay and overlapping completed snapshots
cannot remove a unit twice. JDBC/JPA cart persistence preserves the interval allocation watermark; in-memory persistence
retains the same domain state. Legacy CheckedOut/Completed cart statuses remain readable, but snapshot checkout leaves
an active cart editable and never completes the whole cart.

Confirmation retrieves current price/availability/stock facts before its local transaction. Pure domain services consume
immutable line/fact snapshots. Any changed price or shortage reports affected lines and leaves state, totals and events
unchanged. The buyer explicitly starts a fresh checkout against the new prices. Success stores the recomputed total and
publishes the same total; there is no no-argument confirmation path. Local in-memory repository serialization is not a
claim of durable distributed transactions or universal rollback of unenlisted resources.

The language-neutral specification is an independently owned, currently unpublished repository. It is **not part of the
build**: a plain checkout builds and runs without it, and the specification tests are reported as skipped. To run them,
point the build at a local checkout; the vectors are copied into the gitignored `build/specification/` directory and
the adapters in `SharedSpecificationTest`, `CheckoutSpecificationTest` and `RetainedDeliveryIntegrationTest` drive the production code with them.

```bash
./gradlew test test-integration -Pspecification.path=../dca-sample-specification
```
