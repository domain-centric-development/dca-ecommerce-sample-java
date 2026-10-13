# product — catalogue

### Rule: The shop starts with its sample catalogue

#### catalogue-lists-the-seeded-range
- Given the shop has started and seeded the sample catalogue of `seed-catalogue.json`
- When a visitor opens the catalogue page
- Then it shows 21 product cards
Test: dev.domaincentric.sample.ecommerce.e2e.SeededCatalogE2ETest#catalogListsTheSeededProductsInNameOrder

### Rule: Products are listed by name, compared character by character

#### catalogue-is-in-name-order
- Given the seeded sample catalogue
- When a visitor opens the catalogue page
- Then the card titles are in ordinal order of the product name
- And the first card is `"Bounded Context" Enamel Pin`
Test: dev.domaincentric.sample.ecommerce.product.ProductCatalogPageIntegrationTest#listsTheCardTitlesInOrdinalNameOrder

### Rule: A card shows what the product is

#### card-shows-name-description-and-image
- Given the seeded product "Domain-Driven Design"
- When a visitor opens the catalogue page
- Then its card shows the name "Domain-Driven Design", its description and its image
Test: dev.domaincentric.sample.ecommerce.product.ProductCatalogPageIntegrationTest#cardShowsTheProductsNameDescriptionAndImage

#### card-leads-to-the-product
- Given the seeded sample catalogue
- When a visitor opens the catalogue page
- Then every card offers a "View Details" link to that product's page
Test: dev.domaincentric.sample.ecommerce.product.ProductCatalogPageIntegrationTest#everyCardOffersAViewDetailsLinkToThatProductsPage

### Rule: The page is recognisable as the catalogue

#### catalogue-page-heading
- Given the seeded sample catalogue
- When a visitor opens the catalogue page
- Then the page title is "Product Catalog", the heading reads "Our Products" and the breadcrumb reads "Home / Products"
Test: dev.domaincentric.sample.ecommerce.product.ProductCatalogPageIntegrationTest#pageIsTitledHeadedAndPlacedAsTheCatalogue

### Rule: The catalogue page carries the catalogue's name as its title

#### catalogue-tab-reads-product-catalog
Title: The catalogue page is titled Product Catalog
- Given the seeded sample catalogue
- When a visitor opens the catalogue page
- Then the page title is "Product Catalog"
Test: dev.domaincentric.sample.ecommerce.e2e.CatalogTitleE2ETest#catalogPageIsTitledProductCatalog

### Rule: The shop offers its catalogue as two tools, to anyone

#### the-server-offers-two-catalogue-tools
- Given the shop has started
- When an assistant without any credentials connects to the MCP server at `/mcp` (streamable HTTP) and lists its tools
- Then the server introduces itself as "dca-ecommerce-sample-mcp-server", version "1.0.0"
- And it offers the tool `all-products` described as "Get all products in the catalog. Returns complete product information including SKU, name, price, category, and available stock."
- And the tool `product-by-id` with one parameter `id`, described as "Get detailed product information by product ID. Requires the internal product UUID. Returns complete product details including all attributes."
Test: dev.domaincentric.sample.ecommerce.product.ProductCatalogMcpIntegrationTest#serverIntroducesItselfAndOffersTheTwoCatalogueTools

### Rule: all-products returns every product with its price and stock, in catalogue order

#### all-products-lists-the-range-with-price-and-stock
- Given the shop has started and seeded the sample catalogue of `seed-catalogue.json`
- When an assistant calls `all-products`
- Then the answer holds 21 products in ordinal order of the product name, the first named `"Bounded Context" Enamel Pin`
- And the entry for "Domain-Driven Design" has an `id`, `sku` "BOOK-001", its `description`, an `imageUrl`, `price` 54.99, `currency` "EUR", `category` "Books", `stockQuantity` 20 and `isAvailable` true
Test: dev.domaincentric.sample.ecommerce.e2e.CatalogueToolsE2ETest#allProductsListsTheSeededRangeWithPriceAndStock

### Rule: product-by-id returns the one product with that id

#### product-by-id-returns-the-product
- Given the `id` of "Wooden Hexagon Desk Model" as `all-products` returns it
- When an assistant calls `product-by-id` with that `id`
- Then the answer is that product: `sku` "HEXAGON-001", `price` 39.99, `currency` "EUR", `category` "Desk & Office", `stockQuantity` 8 and `isAvailable` true
Test: dev.domaincentric.sample.ecommerce.product.ProductCatalogMcpIntegrationTest#productByIdAnswersTheProductWithItsPriceAndStock

#### an-unknown-id-returns-no-product
- Given the seeded sample catalogue
- When an assistant calls `product-by-id` with `id` "no-such-product"
- Then the call succeeds and its answer is `null`
Test: dev.domaincentric.sample.ecommerce.product.ProductCatalogMcpIntegrationTest#productByIdAnswersNullForAnIdThatNamesNoProduct

#### a-blank-id-is-a-tool-error
- Given the seeded sample catalogue
- When an assistant calls `product-by-id` with `id` ""
- Then the call ends in a tool error whose message is "Product ID cannot be null or blank"
Test: dev.domaincentric.sample.ecommerce.product.ProductCatalogMcpIntegrationTest#productByIdRefusesABlankIdWithAToolError

### Rule: Anyone may read the catalogue, in the same order as the catalogue page

#### listing-returns-the-range-in-name-order
- Given the shop has started and seeded the sample catalogue of `seed-catalogue.json`
- When a client without a token sends `GET /api/products`
- Then the shop answers 200 with a JSON array of 21 products
- And they are in ordinal order of the product name, the first named `"Bounded Context" Enamel Pin`
Test: dev.domaincentric.sample.ecommerce.e2e.CatalogueApiE2ETest#listingReturnsTheSeededRangeInOrdinalNameOrder

### Rule: A product carries its master data, its price and its stock

#### a-listed-product-carries-price-and-stock
- Given the seeded product "Domain-Driven Design"
- When a client sends `GET /api/products`
- Then its entry has an `id`, `sku` "BOOK-001", `name` "Domain-Driven Design", its `description`, an `imageUrl`, `price` 54.99, `currency` "EUR", `category` "Books", `stockQuantity` 20 and `isAvailable` true
Test: dev.domaincentric.sample.ecommerce.product.ProductCatalogApiIntegrationTest#aListedProductCarriesItsMasterDataPriceAndStock

#### one-product-by-its-id
- Given the `id` of "Hexagonal Architecture Explained" as the list returns it
- When a client without a token sends `GET /api/products/<that id>`
- Then the shop answers 200 with that product: `sku` "BOOK-005", `price` 34.99, `currency` "EUR", `category` "Books", `stockQuantity` 26 and `isAvailable` true
Test: dev.domaincentric.sample.ecommerce.product.ProductCatalogApiIntegrationTest#oneProductIsReadByTheIdTheListReturns

### Rule: An unknown product is not found

#### an-unknown-product-is-not-found
- Given the seeded sample catalogue
- When a client sends `GET /api/products/no-such-product`
- Then the shop answers 404 with an empty body
Test: dev.domaincentric.sample.ecommerce.product.ProductCatalogApiIntegrationTest#anUnknownProductIsAnsweredWithNotFoundAndNoBody
