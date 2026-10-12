---
id: an-assistant-reads-the-catalogue
epic: catalogue-for-assistants
status: adopted
context: product
feature: catalogue
title: An assistant reads the catalogue through tools
depends_on: [CAT-01, prices-shown-from-the-price-owner, availability-shown]
---

# An assistant reads the catalogue through tools

## Story

As an AI assistant I want tools that list the shop's products and fetch one of them, with price and stock, so that
I can answer a customer's question about what the shop sells from the shop's own data.

## Acceptance criteria

<!-- happy-path: all-products-lists-the-range-with-price-and-stock -->

### Rule: The shop offers its catalogue as two tools, to anyone

#### the-server-offers-two-catalogue-tools
- Given the shop has started
- When an assistant without any credentials connects to the MCP server at `/mcp` (streamable HTTP) and lists its tools
- Then the server introduces itself as "dca-ecommerce-sample-mcp-server", version "1.0.0"
- And it offers the tool `all-products` described as "Get all products in the catalog. Returns complete product information including SKU, name, price, category, and available stock."
- And the tool `product-by-id` with one parameter `id`, described as "Get detailed product information by product ID. Requires the internal product UUID. Returns complete product details including all attributes."

### Rule: all-products returns every product with its price and stock, in catalogue order

#### all-products-lists-the-range-with-price-and-stock
- Given the shop has started and seeded the sample catalogue of `seed-catalogue.json`
- When an assistant calls `all-products`
- Then the answer holds 21 products in ordinal order of the product name, the first named `"Bounded Context" Enamel Pin`
- And the entry for "Domain-Driven Design" has an `id`, `sku` "BOOK-001", its `description`, an `imageUrl`, `price` 54.99, `currency` "EUR", `category` "Books", `stockQuantity` 20 and `isAvailable` true

### Rule: product-by-id returns the one product with that id

#### product-by-id-returns-the-product
- Given the `id` of "Wooden Hexagon Desk Model" as `all-products` returns it
- When an assistant calls `product-by-id` with that `id`
- Then the answer is that product: `sku` "HEXAGON-001", `price` 39.99, `currency` "EUR", `category` "Desk & Office", `stockQuantity` 8 and `isAvailable` true

#### an-unknown-id-returns-no-product
- Given the seeded sample catalogue
- When an assistant calls `product-by-id` with `id` "no-such-product"
- Then the call succeeds and its answer is `null`

#### a-blank-id-is-a-tool-error
- Given the seeded sample catalogue
- When an assistant calls `product-by-id` with `id` ""
- Then the call ends in a tool error whose message is "Product ID cannot be null or blank"

## Out of scope

- Creating or changing products through a tool: no tool does it.
- A check of the id's format: any non-blank id that names no product answers `null`.
- The same catalogue over the REST API — `read-the-catalogue-through-the-api`.

## Notes

- Replay story: `MCP-01` (catalogue-for-assistants).
- Inventory: product P18, P19; cross-cutting X3 (the tool endpoint needs no form token).
