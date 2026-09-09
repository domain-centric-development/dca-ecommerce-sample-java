> **Status:** Bootstrap Draft — automatically extracted from source code.
> Must be ratified by a domain expert before terms become binding. Definitions
> are first approximations and may require refinement.

# Bounded Context: Product

The Product context is the source of truth for product identity (`ProductId`,
`SKU`) and descriptive master data (name, description, category, image). Prices
live in the Pricing context, stock levels in the Inventory context. Read models
that combine this data are provided within the Product context as enriched
models.

## Aggregates

### Product

**Definition:** An identifiable product listed in the catalog with descriptive
master data (name, description, category, image) and a unique SKU. Source of
truth for product identity and descriptive attributes.

**Type:** Aggregate Root

**Identity:** `ProductId` (Shared Kernel)

**Related terms:**
- `SKU` — external business identifier
- `ProductName`, `ProductDescription`, `Category`, `ImageUrl` — descriptive Value Objects
- `EnrichedProduct` — enriched read model for display
- `ProductFactory` — creation with initial price and stock

**Operations:** `updateName`, `updateDescription`, `updateCategory`

**Notes:** Prices and stock levels are NOT managed by the Product aggregate —
they reside in the Pricing and Inventory contexts respectively.

## Entities

_(No non-root entities in the Product context — the aggregate boundary is
flat.)_

## Value Objects

### SKU

**Definition:** Unique business article key (Stock Keeping Unit) of a product.
Used for identification in inventory and logistics processes.

**Type:** Value Object

**Notes:** Format: uppercase letters, digits, and hyphens only. Must be unique
across the entire project.

### ProductName

**Definition:** The customer-facing display name of a product. Maximum 255
characters.

**Type:** Value Object

### ProductDescription

**Definition:** Descriptive marketing and detail text of a product. Maximum
2000 characters, may be empty.

**Type:** Value Object

### Category

**Definition:** Assortment category to which a product is functionally assigned
(e.g., "Books", "Modeling"). Used to structure the catalog.

**Type:** Value Object

**Notes:** Predefined categories exist as factory methods; open to additional
values.

### ImageUrl

**Definition:** URL of a product's display image. May be empty (products
without an image).

**Type:** Value Object

### ProductArticle

**Definition:** External article view of a product, bundling current price,
available stock, and availability status from the Pricing and Inventory
contexts — as input value for enriched read models.

**Type:** Value Object

**Related terms:** `EnrichedProduct`

### EnrichedProduct

**Definition:** Enriched read model of a product with current price, stock,
and availability. Carries cross-context business rules such as "can be
purchased" or "stock is sufficient for quantity x".

**Type:** Value Object (Read Model)

**Related terms:** `Product`, `ProductArticle`

**Notes:** Risk of synonyms — sometimes referred to as "Product View" or
"Product Display Model"; prefer `EnrichedProduct`.

## Domain Events

### ProductCreated

**Definition:** A new product has been created in the catalog. Carries the
initial price (for the Pricing context) and initial stock (for the Inventory
context) for cross-context initialization.

**Type:** Domain Event

### ProductNameChanged

**Definition:** The display name of a product has been changed. Contains the
old and new name.

**Type:** Domain Event

### ProductDescriptionChanged

**Definition:** The description of a product has been changed. Contains the
old and new description.

**Type:** Domain Event

### ProductCategoryChanged

**Definition:** The categorization of a product has been changed. Contains
the old and new category.

**Type:** Domain Event

## Factories

### ProductFactory

**Definition:** Creates new product aggregates. Ensures that on creation both
the initial price (Pricing) and initial stock (Inventory) are propagated to
the respective contexts via `ProductCreated`.

**Type:** Factory

## Concepts (no code artifact)

### Product Availability

**Definition:** Business rule determining whether a product is sellable —
available and in stock.

**Type:** Concept

**Notes:** Not implemented. Would be a Specification once its semantics are
settled: "available" and "in stock" are owned by different contexts (Product
and Inventory), so the rule needs a decision on where it lives before it can
be written. Open question for the domain expert.

### Cross-Context Read Model

**Definition:** Read-oriented composite of value objects combining data from
several contexts (Product, Pricing, Inventory) for display, without the source
aggregate depending on foreign contexts.

**Type:** Concept

**Related terms:** `EnrichedProduct`, `ProductArticle`

### Shared contract revision (2026-09-09)

Price wraps strictly positive Money; Money is ISO 4217, non-negative, two decimals half-up, maximum 999999999999.99.
Default quantities must be rejected before mutation/reconstitution. ProductCreated is raised by aggregate creation;
product-created v1 exposes only eventId, occurredOn, productId, amount, currency and initialStock.
