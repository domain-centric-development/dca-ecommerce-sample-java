# Package Structure

Package organization of the DCA e-commerce sample (Java).

## Full Structure

```
dev.domaincentric.sample.ecommerce
│
├── sharedkernel/                    # Shared Kernel (cross-context)
│   ├── marker/                     # Architectural Markers
│   │   ├── tactical/              # DDD Tactical Patterns
│   │   │   ├── Id, Entity, Value
│   │   │   ├── AggregateRoot, BaseAggregateRoot
│   │   │   ├── DomainService, Factory, Specification
│   │   │   └── DomainEvent, IntegrationEvent
│   │   ├── strategic/             # DDD Strategic Patterns
│   │   │   └── BoundedContext, SharedKernel, OpenHostService
│   │   ├── port/                  # Port Markers
│   │   │   ├── in/                # Input Ports (InputPort, UseCase)
│   │   │   └── out/               # Output Port Markers (OutputPort, Repository, DomainEventPublisher)
│   │   └── infrastructure/        # Infrastructure Markers
│   │       └── AsyncInitialize
│   ├── application/
│   │   └── shared/                # App-specific ports shared by several contexts (IdentityProvider)
│   ├── domain/
│   │   ├── model/                 # Shared Value Objects
│   │   │   └── Money, Price, ProductId, UserId
│   │   └── specification/         # Shared Specification Patterns
│   │       └── CompositeSpecification, AndSpecification, OrSpecification, NotSpecification, SpecificationVisitor
│   └── adapter/
│       └── outgoing/
│           └── event/
│               └── SpringDomainEventPublisher
│
├── product/                         # Product Catalog Bounded Context
│   ├── domain/
│   │   ├── model/                  # Domain Model
│   │   │   ├── Product (Aggregate Root)
│   │   │   ├── SKU, ProductName, ProductDescription (Value Objects)
│   │   │   ├── Category (Value Object)
│   │   │   ├── ProductFactory
│   │   │   ├── ProductArticle (external article data)
│   │   │   └── EnrichedProduct (Enriched Domain Model with factory)
│   │   └── event/                  # Domain Events
│   │       └── ProductCreated
│   ├── application/                # Application Layer
│   │   ├── createproduct/         # Use case: Create Product
│   │   │   ├── CreateProductInputPort
│   │   │   ├── CreateProductUseCase
│   │   │   ├── CreateProductCommand
│   │   │   └── CreateProductResult
│   │   ├── getallproducts/        # Use case: Get All Products
│   │   │   ├── GetAllProductsInputPort
│   │   │   ├── GetAllProductsUseCase
│   │   │   ├── GetAllProductsQuery
│   │   │   └── GetAllProductsResult
│   │   ├── getproductbyid/        # Use case: Get Product By ID
│   │   │   ├── GetProductByIdInputPort
│   │   │   ├── GetProductByIdUseCase
│   │   │   ├── GetProductByIdQuery
│   │   │   └── GetProductByIdResult
│   │   ├── updateproductprice/    # Use case: Update Product Price
│   │   │   ├── UpdateProductPriceInputPort
│   │   │   ├── UpdateProductPriceUseCase
│   │   │   ├── UpdateProductPriceCommand
│   │   │   └── UpdateProductPriceResult
│   │   └── shared/                # Shared output ports
│   │       ├── ProductRepository
│   │       ├── PricingDataPort
│   │       └── ProductStockDataPort
│   ├── infrastructure/            # Per-context infrastructure
│   │   └── ProductDomainConfiguration
│   ├── api/                       # Published in-process interface (Open Host Service)
│   │   └── ProductCatalogService
│   ├── events/                    # Published integration events
│   └── adapter/
│       ├── incoming/               # Incoming Adapters (Primary)
│       │   ├── api/
│       │   │   ├── ProductResource
│       │   │   ├── CreateProductRequest
│       │   │   ├── ProductDto, ProductDtoConverter
│       │   ├── mcp/
│       │   │   └── ProductCatalogMcpToolProvider
│       │   ├── web/
│       │   │   ├── ProductPageController
│       │   │   ├── ProductCatalogPageViewModel
│       │   │   └── ProductDetailPageViewModel
│       │   └── event/
│       │       └── ProductEventConsumer
│       └── outgoing/               # Outgoing Adapters (Secondary)
│           ├── persistence/
│           │   └── InMemoryProductRepository
│           ├── pricing/
│           │   └── PricingDataAdapter
│           └── inventory/
│               └── InventoryStockDataAdapter
│
├── cart/                            # Shopping Cart Bounded Context
│   ├── events/                      # @NamedInterface("events") — trigger interfaces
│   │   ├── CartContentsChangedEvent # Integration Event for cart changes
│   │   └── CartCompletionTrigger    # Interface Inversion: consumer-defined trigger
│   ├── domain/
│   │   ├── model/                  # Domain Model
│   │   │   ├── ShoppingCart (Aggregate Root)
│   │   │   ├── CartItem (Entity)
│   │   │   ├── CartId, CartItemId, CustomerId, Quantity (Value Objects)
│   │   │   ├── CartStatus
│   │   │   ├── ArticlePrice, CartArticle, ArticlePriceResolver
│   │   │   ├── CartValidationResult
│   │   │   ├── EnrichedCart (Enriched Domain Model)
│   │   │   ├── EnrichedCartItem
│   │   │   └── EnrichedCartFactory
│   │   ├── specification/          # Cart Specifications (Visitor pattern)
│   │   │   ├── CartSpecification, CartSpecificationVisitor
│   │   │   ├── ActiveCart, HasMinTotal
│   │   │   ├── HasAnyAvailableItem, LastUpdatedBefore
│   │   │   └── CustomerAllowsMarketing
│   │   ├── service/                # Domain Services
│   │   │   └── CartTotalCalculator
│   │   └── event/                  # Domain Events
│   │       ├── CartCheckedOut, CartItemAddedToCart
│   │       ├── CartItemQuantityChanged, ProductRemovedFromCart
│   │       └── CartCleared
│   ├── application/                # Application Layer
│   │   ├── createcart/
│   │   │   ├── CreateCartInputPort, CreateCartUseCase
│   │   │   ├── CreateCartCommand, CreateCartResult
│   │   ├── additemtocart/
│   │   │   ├── AddItemToCartInputPort, AddItemToCartUseCase
│   │   │   ├── AddItemToCartCommand, AddItemToCartResult
│   │   ├── checkoutcart/
│   │   │   ├── CheckoutCartInputPort, CheckoutCartUseCase
│   │   │   ├── CheckoutCartCommand, CheckoutCartResult
│   │   ├── getallcarts/
│   │   │   ├── GetAllCartsInputPort, GetAllCartsUseCase
│   │   │   ├── GetAllCartsQuery, GetAllCartsResult
│   │   ├── getcartbyid/
│   │   │   ├── GetCartByIdInputPort, GetCartByIdUseCase
│   │   │   ├── GetCartByIdQuery, GetCartByIdResult
│   │   ├── getorcreateactivecart/
│   │   │   ├── GetOrCreateActiveCartInputPort, GetOrCreateActiveCartUseCase
│   │   │   ├── GetOrCreateActiveCartCommand, GetOrCreateActiveCartResult
│   │   ├── removeitemfromcart/
│   │   │   ├── RemoveItemFromCartInputPort, RemoveItemFromCartUseCase
│   │   │   ├── RemoveItemFromCartCommand, RemoveItemFromCartResult
│   │   ├── mergecarts/
│   │   │   ├── MergeCartsInputPort, MergeCartsUseCase
│   │   │   ├── MergeCartsCommand, MergeCartsResult
│   │   ├── getcartmergeoptions/
│   │   │   ├── GetCartMergeOptionsInputPort, GetCartMergeOptionsUseCase
│   │   │   ├── GetCartMergeOptionsQuery, GetCartMergeOptionsResult
│   │   ├── completecart/
│   │   │   ├── CompleteCartInputPort, CompleteCartUseCase
│   │   │   ├── CompleteCartCommand, CompleteCartResult
│   │   ├── recovercart/
│   │   │   ├── RecoverCartInputPort, RecoverCartUseCase
│   │   │   ├── RecoverCartCommand, RecoverCartOnLoginResult
│   │   └── shared/                 # Shared output ports
│   │       ├── ShoppingCartRepository
│   │       └── ArticleDataPort
│   ├── infrastructure/            # Per-context infrastructure
│   │   └── CartDomainConfiguration
│   └── adapter/
│       ├── incoming/
│       │   ├── api/
│       │   │   ├── ShoppingCartResource
│       │   │   ├── AddToCartRequest
│       │   │   ├── ShoppingCartDto, ShoppingCartListDto
│       │   │   ├── CartItemDto, ShoppingCartDtoConverter
│       │   ├── web/
│       │   │   ├── CartPageController, CartPageViewModel
│       │   │   ├── CartMergePageController, CartMergePageViewModel
│       │   └── event/
│       │       ├── CartEventConsumer
│       │       └── CartCompletionEventConsumer
│       └── outgoing/
│           ├── product/
│           │   └── CompositeArticleDataAdapter
│           └── persistence/
│               ├── InMemoryShoppingCartRepository
│               ├── JdbcShoppingCartRepository
│               ├── jpa/
│               │   ├── JpaShoppingCartRepository, CartJpaRepository
│               │   ├── CartEntity, CartItemEntity
│               │   └── CartSpecToJpa
│               └── jdbc/
│                   └── CartSpecToJdbc
│
├── checkout/                        # Checkout Bounded Context
│   ├── events/                      # @NamedInterface("events") — integration events
│   │   └── CheckoutConfirmedEvent   # Implements CartCompletionTrigger, StockReductionTrigger
│   ├── domain/
│   │   ├── model/                  # Domain Model
│   │   │   ├── CheckoutSession (Aggregate Root)
│   │   │   ├── CheckoutLineItem (Entity)
│   │   │   ├── CheckoutSessionId, CheckoutLineItemId (Value Objects)
│   │   │   ├── CheckoutStep, CheckoutSessionStatus
│   │   │   ├── CheckoutTotals, CheckoutValidationResult
│   │   │   ├── BuyerInfo, DeliveryAddress, ShippingOption
│   │   │   ├── PaymentSelection, PaymentProviderId
│   │   │   ├── CustomerId, CartId
│   │   │   ├── CheckoutArticle, CheckoutArticlePriceResolver
│   │   │   ├── CheckoutCart, CheckoutCartFactory
│   │   │   └── EnrichedCheckoutLineItem
│   │   ├── readmodel/              # Read Models
│   │   │   ├── CheckoutCartSnapshot
│   │   │   └── LineItemSnapshot
│   │   ├── service/                # Domain Services
│   │   │   └── CheckoutStepValidator
│   │   └── event/                  # Domain Events
│   │       ├── CheckoutSessionStarted, BuyerInfoSubmitted
│   │       ├── DeliverySubmitted, PaymentSubmitted
│   │       ├── CheckoutConfirmed, CheckoutCompleted
│   │       └── CheckoutAbandoned, CheckoutExpired
│   ├── application/                # Application Layer
│   │   ├── startcheckout/
│   │   │   ├── StartCheckoutInputPort, StartCheckoutUseCase
│   │   │   ├── StartCheckoutCommand, StartCheckoutResult
│   │   ├── submitbuyerinfo/
│   │   │   ├── SubmitBuyerInfoInputPort, SubmitBuyerInfoUseCase
│   │   │   ├── SubmitBuyerInfoCommand, SubmitBuyerInfoResult
│   │   ├── submitdelivery/
│   │   │   ├── SubmitDeliveryInputPort, SubmitDeliveryUseCase
│   │   │   ├── SubmitDeliveryCommand, SubmitDeliveryResult
│   │   ├── submitpayment/
│   │   │   ├── SubmitPaymentInputPort, SubmitPaymentUseCase
│   │   │   ├── SubmitPaymentCommand, SubmitPaymentResult
│   │   ├── confirmcheckout/
│   │   │   ├── ConfirmCheckoutInputPort, ConfirmCheckoutUseCase
│   │   │   ├── ConfirmCheckoutCommand, ConfirmCheckoutResult
│   │   ├── getcheckoutsession/
│   │   │   ├── GetCheckoutSessionInputPort, GetCheckoutSessionUseCase
│   │   │   ├── GetCheckoutSessionQuery, GetCheckoutSessionResult
│   │   ├── getactivecheckoutsession/
│   │   │   ├── GetActiveCheckoutSessionInputPort, GetActiveCheckoutSessionUseCase
│   │   │   ├── GetActiveCheckoutSessionQuery, GetActiveCheckoutSessionResult
│   │   ├── getconfirmedcheckoutsession/
│   │   │   ├── GetConfirmedCheckoutSessionInputPort, GetConfirmedCheckoutSessionUseCase
│   │   │   ├── GetConfirmedCheckoutSessionQuery, GetConfirmedCheckoutSessionResult
│   │   ├── getshippingoptions/
│   │   │   ├── GetShippingOptionsInputPort, GetShippingOptionsUseCase
│   │   │   ├── GetShippingOptionsQuery, GetShippingOptionsResult
│   │   ├── getpaymentproviders/
│   │   │   ├── GetPaymentProvidersInputPort, GetPaymentProvidersUseCase
│   │   │   ├── GetPaymentProvidersQuery, GetPaymentProvidersResult
│   │   ├── synccheckoutwithcart/
│   │   │   ├── SyncCheckoutWithCartInputPort, SyncCheckoutWithCartUseCase
│   │   │   ├── SyncCheckoutWithCartCommand, SyncCheckoutWithCartResult
│   │   └── shared/                 # Shared output ports
│   │       ├── CheckoutSessionRepository
│   │       ├── CartDataPort, CartData
│   │       ├── CheckoutArticleDataPort
│   │       ├── ProductInfoPort
│   │       ├── PaymentProvider, PaymentProviderRegistry
│   └── adapter/
│       ├── incoming/
│       │   ├── web/
│       │   │   ├── StartCheckoutPageController
│       │   │   ├── BuyerInfoPageController, BuyerInfoPageViewModel
│       │   │   ├── DeliveryPageController, DeliveryPageViewModel
│       │   │   ├── PaymentPageController, PaymentPageViewModel
│       │   │   ├── ReviewPageController, ReviewPageViewModel
│       │   │   └── ConfirmationPageController, ConfirmationPageViewModel
│       │   └── event/
│       │       └── CartChangeEventConsumer
│       └── outgoing/
│           ├── persistence/
│           │   └── InMemoryCheckoutSessionRepository
│           ├── cart/
│           │   └── CartDataAdapter
│           ├── product/
│           │   ├── CompositeCheckoutArticleDataAdapter
│           │   └── ProductInfoAdapter
│           └── payment/
│               ├── MockPaymentProvider
│               └── InMemoryPaymentProviderRegistry
│
├── account/                         # Account Bounded Context
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Account (Aggregate Root)
│   │   │   ├── AccountId, Email, HashedPassword (Value Objects)
│   │   │   └── AccountStatus
│   │   ├── service/
│   │   │   └── PasswordHasher
│   │   └── event/
│   │       ├── AccountRegistered
│   │       └── AccountLinkedToIdentity
│   ├── application/
│   │   ├── registeraccount/
│   │   │   ├── RegisterAccountInputPort, RegisterAccountUseCase
│   │   │   ├── RegisterAccountCommand, RegisterAccountResult
│   │   ├── authenticateaccount/
│   │   │   ├── AuthenticateAccountInputPort, AuthenticateAccountUseCase
│   │   │   ├── AuthenticateAccountCommand, AuthenticateAccountResult
│   │   ├── getaccountoverview/
│   │   │   ├── GetAccountOverviewInputPort, GetAccountOverviewUseCase
│   │   │   ├── GetAccountOverviewQuery, GetAccountOverviewResult
│   │   ├── changepassword/
│   │   │   ├── ChangePasswordInputPort, ChangePasswordUseCase
│   │   │   ├── ChangePasswordCommand, ChangePasswordResult
│   │   └── shared/
│   │       ├── AccountRepository
│   │       ├── RegisteredUserValidator, TokenService
│   │       └── IdentitySession
│   ├── infrastructure/            # Per-context infrastructure
│   │   └── SecurityConfiguration
│   └── adapter/
│       ├── incoming/
│       │   ├── api/
│       │   │   ├── AuthResource
│       │   │   ├── LoginRequest, LoginResponse
│       │   │   └── RegisterRequest, RegisterResponse
│       │   └── web/
│       │       ├── LoginPageController, LogoutPageController
│       │       ├── RegisterPageController
│       │       ├── MyAccountPageController, MyAccountPageViewModel
│       │       ├── ChangePasswordPageController, ChangePasswordPageViewModel
│       │       └── AccountNavigation
│       └── outgoing/
│           ├── persistence/
│           │   ├── JdbcAccountRepository      # default (ADR-031)
│           │   └── InMemoryAccountRepository  # "inmemory" profile
│           └── security/
│               ├── SpringSecurityPasswordHasher
│               └── AccountBasedRegisteredUserValidator
│
├── inventory/                       # Inventory Bounded Context
│   ├── api/                         # @NamedInterface("api") — Open Host Service
│   │   └── InventoryService
│   ├── events/                      # @NamedInterface("events") — trigger interfaces
│   │   ├── StockReductionTrigger        # Interface Inversion: consumer-defined trigger
│   │   └── StockInitializationTrigger   # Interface Inversion: consumer-defined trigger
│   ├── domain/
│   │   ├── model/
│   │   │   ├── StockLevel (Aggregate Root)
│   │   │   ├── StockLevelId, StockQuantity (Value Objects)
│   │   └── event/
│   │       ├── StockLevelCreated, StockChanged
│   │       ├── StockIncreased, StockDecreased
│   │       └── StockReserved
│   ├── application/
│   │   ├── setstocklevel/
│   │   │   ├── SetStockLevelInputPort, SetStockLevelUseCase
│   │   │   ├── SetStockLevelCommand, SetStockLevelResult
│   │   ├── reducestock/
│   │   │   ├── ReduceStockInputPort, ReduceStockUseCase
│   │   │   ├── ReduceStockCommand, ReduceStockResult
│   │   ├── getstockforproducts/
│   │   │   ├── GetStockForProductsInputPort, GetStockForProductsUseCase
│   │   │   ├── GetStockForProductsQuery, GetStockForProductsResult
│   │   └── shared/
│   │       └── StockLevelRepository
│   └── adapter/
│       ├── incoming/
│       │   └── event/
│       │       ├── StockReductionEventConsumer
│       │       └── StockInitializationEventConsumer
│       └── outgoing/
│           └── persistence/
│               └── InMemoryStockLevelRepository
│
├── pricing/                         # Pricing Bounded Context
│   ├── api/                         # @NamedInterface("api") — Open Host Service
│   │   └── PricingService
│   ├── events/                      # @NamedInterface("events") — trigger interfaces
│   │   └── PriceInitializationTrigger  # Interface Inversion: consumer-defined trigger
│   ├── domain/
│   │   ├── model/
│   │   │   ├── ProductPrice (Aggregate Root)
│   │   │   └── PriceId (Value Object)
│   │   └── event/
│   │       ├── PriceCreated
│   │       └── PriceChanged
│   ├── application/
│   │   ├── setproductprice/
│   │   │   ├── SetProductPriceInputPort, SetProductPriceUseCase
│   │   │   ├── SetProductPriceCommand, SetProductPriceResult
│   │   ├── getpricesforproducts/
│   │   │   ├── GetPricesForProductsInputPort, GetPricesForProductsUseCase
│   │   │   ├── GetPricesForProductsQuery, GetPricesForProductsResult
│   │   └── shared/
│   │       └── ProductPriceRepository
│   └── adapter/
│       ├── incoming/
│       │   └── event/
│       │       └── PriceInitializationEventConsumer
│       └── outgoing/
│           └── persistence/
│               └── InMemoryProductPriceRepository
│
├── portal/                          # Portal Bounded Context
│   └── adapter/
│       └── incoming/
│           └── web/
│               └── HomePageController
│
├── backoffice/                      # Backoffice Bounded Context
│   ├── application/
│   │   ├── geteventpublications/
│   │   │   ├── GetEventPublicationsInputPort, GetEventPublicationsUseCase
│   │   │   ├── GetEventPublicationsQuery, GetEventPublicationsResult
│   │   └── shared/
│   │       └── EventPublicationLogRepository
│   ├── infrastructure/              # Per-context infrastructure
│   │   ├── BackofficeSecurityConfiguration
│   │   └── BackofficeSecurityProperties
│   └── adapter/
│       ├── incoming/
│       │   └── web/
│       │       ├── EventPublicationPageController
│       │       └── EventPublicationPageViewModel
│       └── outgoing/
│           └── persistence/
│               └── JdbcEventPublicationLogRepository
│
└── infrastructure/                  # Infrastructure (cross-cutting)
    ├── EcommerceSampleApplication
    ├── config/                    # Spring @Configuration classes
    │   ├── SecurityConfiguration, TransactionConfiguration
    │   ├── AsyncConfiguration, DomainConfiguration
    │   └── Pug4jConfiguration
    ├── init/
    │   └── SampleDataInitializer
    ├── support/
    │   └── AsyncInitializationProcessor
    └── security/
        ├── SpringSecurityIdentityProvider
        ├── JwtIdentity, JwtIdentityType
        └── jwt/
            ├── JwtTokenService, JwtIdentitySession
            ├── JwtProperties
            └── JwtAuthenticationFilter
```

## Bounded Context Organization

Each bounded context follows the same internal structure:

```
{context}/
├── domain/             # Domain layer (innermost)
│   ├── model/          # Aggregates, entities, value objects, enriched models
│   ├── readmodel/      # Optional: read model types (e.g., CheckoutCartSnapshot)
│   ├── specification/  # Optional: specifications
│   ├── service/        # Domain services
│   └── event/          # Domain events
├── application/        # Application layer
│   ├── {usecasename}/  # Use case package (e.g., getcartbyid)
│   │   ├── *InputPort.java    # Input port interface
│   │   ├── *UseCase.java      # Use case implementation
│   │   ├── *Query.java/*Command.java  # Input model
│   │   └── *Result.java       # Output model
│   └── shared/         # Shared output ports (repositories, data ports)
├── api/                # Published in-process interface: Open Host Service (@OpenHostService)
├── events/             # Published integration events
└── adapter/            # Adapter layer (outermost) — sub-packages are a convention, no rule checks them
    ├── incoming/       # Incoming adapters (primary/driving)
    │   ├── api/        # REST API (DTOs, Resources) — an Open Host Service over the network
    │   ├── web/        # Web MVC (Controllers, ViewModels)
    │   ├── mcp/        # MCP server (McpToolProviders)
    │   └── event/      # Domain event consumers
    └── outgoing/       # Outgoing adapters (secondary/driven)
        └── persistence/ # Repository implementations
```

## Primary Adapters: API vs Web vs MCP vs Event

| Aspect | API (`adapter.incoming.api`) | Web (`adapter.incoming.web`) | MCP (`adapter.incoming.mcp`) | Event (`adapter.incoming.event`) |
|--------|-------------|--------------|--------------|--------------|
| **Annotation** | `@RestController` | `@Controller` | `@Component` | `@Component` |
| **Naming** | `*Resource` | `*PageController` | `*McpToolProvider` | `*EventConsumer` |
| **Returns** | JSON/XML (DTOs) | HTML (templates) | DTOs (JSON-RPC) | void |
| **URL** | `/api/*` | `/*` | `/mcp` | N/A |
| **Content-Type** | `application/json` | `text/html` | `application/json` | N/A |
| **Consumers** | Apps, systems | Browsers (humans) | AI assistants | Domain events |
| **Data Model** | DTOs | Page-specific ViewModels | DTOs | Domain events |

### Web Adapter Structure (ViewModels)

MVC Controllers use page-specific ViewModels to pass data to templates:

```
{context}/adapter/incoming/web/
├── CartPageController.java           # Controller for cart view page
├── CartPageViewModel.java            # ViewModel with only primitives
├── CartMergePageController.java      # Controller for cart merge page
└── CartMergePageViewModel.java       # Different page, different ViewModel
```

**ViewModel Pattern:**
- Each page has its own ViewModel tailored to that page's data needs
- ViewModels use only primitives (`String`, `BigDecimal`, `int`, `boolean`)
- Factory method converts domain read model -> ViewModel
- Template attribute name matches the ViewModel purpose

**Example:**
```java
// Controller converts Result -> ViewModel
CartPageViewModel viewModel = CartPageViewModel.fromEnrichedCart(result.cart());
model.addAttribute("shoppingCart", viewModel);  // Attribute name = "shoppingCart"
```

See [DTO vs ViewModel Analysis](dto-vs-viewmodel-analysis.md) for detailed patterns.

## ArchUnit Enforcement

Naming conventions and architectural rules are enforced by ArchUnit tests:

```groovy
def "REST Controllers must end with 'Resource'"() {
  classes()
    .that().resideInAPackage("..adapter.incoming.api..")
    .and().areAnnotatedWith(RestController.class)
    .should().haveSimpleNameEndingWith("Resource")
    .check(allClasses)
}

def "MVC Controllers must end with 'Controller'"() {
  classes()
    .that().resideInAPackage("..adapter.incoming.web..")
    .and().areAnnotatedWith(Controller.class)
    .should().haveSimpleNameEndingWith("Controller")
    .check(allClasses)
}

def "Repository Interfaces must reside in application shared package"() {
  classes()
    .that().implement(Repository.class)
    .and().areInterfaces()
    .should().resideInAnyPackage("..application.shared..")
    .check(allClasses)
}

def "Repository Implementations must reside in adapter.outgoing package"() {
  classes()
    .that().implement(Repository.class)
    .and().areNotInterfaces()
    .should().resideInAnyPackage("..adapter.outgoing..")
    .check(allClasses)
}
```

**Verify:**
```bash
./gradlew test-architecture
```

## File Location Quick Reference

**Where to put a new...**

| File Type | Location | Example |
|-----------|----------|---------|
| REST controller | `{context}.adapter.incoming.api/` | `ProductResource.java` |
| MVC controller | `{context}.adapter.incoming.web/` | `ProductPageController.java` |
| MCP tool provider | `{context}.adapter.incoming.mcp/` | `ProductCatalogMcpToolProvider.java` |
| Open Host Service (in-process) | `{context}.api/` | `ProductCatalogService.java` |
| Event consumer | `{context}.adapter.incoming.event/` | `ProductEventConsumer.java` |
| DTO | `{context}.adapter.incoming.api/` | `ProductDto.java` |
| ViewModel | `{context}.adapter.incoming.web/` | `ProductCatalogPageViewModel.java` |
| Request DTO | `{context}.adapter.incoming.api/` | `CreateProductRequest.java` |
| Pug template | `resources/templates/{context}/` | `templates/product/catalog.pug` |
| Domain aggregate | `{context}.domain.model/` | `Product.java` |
| Value object | `{context}.domain.model/` | `SKU.java` |
| Enriched model | `{context}.domain.model/` | `EnrichedProduct.java` |
| Domain event | `{context}.domain.event/` | `ProductCreated.java` |
| Domain service | `{context}.domain.service/` | `PricingService.java` |
| Factory | `{context}.domain.model/` | `ProductFactory.java` |
| Repository interface | `{context}.application.shared/` | `ProductRepository.java` |
| Output port | `{context}.application.shared/` | `PricingDataPort.java` |
| Repository impl | `{context}.adapter.outgoing.persistence/` | `InMemoryProductRepository.java` |
| Use case | `{context}.application.{name}/` | `createproduct/CreateProductUseCase.java` |
| Command/Query | `{context}.application.{name}/` | `createproduct/CreateProductCommand.java` |
| Result | `{context}.application.{name}/` | `createproduct/CreateProductResult.java` |
| Shared value object | `sharedkernel.domain.model/` | `Money.java` |

## Hexagonal Architecture (Ports & Adapters)

- Input ports defined in `application.{usecasename}/` (use case interfaces)
- Output ports defined in `application.shared/` (repository and data port interfaces)
- Incoming adapters implement/use input ports
- Outgoing adapters implement output ports
- Dependencies point inward toward domain

See [ADR-001: API/Web Package Separation](adr/adr-001-api-web-package-separation.md)

## Related Documentation

- [Architecture Principles](architecture-principles.md) - Hexagonal Architecture patterns
- [DTO vs ViewModel Analysis](dto-vs-viewmodel-analysis.md) - When to use each
- [MCP Server Integration](../integrations/mcp-server-integration.md) - MCP package details
