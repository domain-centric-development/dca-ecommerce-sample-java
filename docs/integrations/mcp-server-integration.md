# MCP Server Integration

## Overview

This project exposes the product catalog as an **MCP (Model Context Protocol) server**, allowing AI assistants like Claude to query products through standardized tools.

**Implementation:**
- Spring AI 1.1.0-M3 with `spring-ai-starter-mcp-server-webmvc`
- HTTP + Server-Sent Events (SSE) protocol
- Incoming adapter in Hexagonal Architecture
- Read-only access to product catalog

---

## Streamable HTTP Protocol

The MCP server uses **streamable HTTP** transport, which combines HTTP with Server-Sent Events (SSE).

### How It Works

**Client → Server (HTTP POST):**
- Client sends JSON-RPC messages via `POST http://localhost:8080/mcp`
- Includes `Accept` headers for `application/json` and `text/event-stream`
- Can send single or batched requests/notifications

**Server → Client (Response):**
Server responds in one of two ways:
1. **Single JSON response** - For quick operations
2. **SSE stream** - For long-running operations or multiple messages

### Key Features

**Bidirectional Communication:**
- Server can send requests and notifications back to client over SSE stream
- Supports both request-response and streaming patterns

**Multiple Clients:**
- Single server instance can handle concurrent client connections
- Each connection uses the same HTTP endpoint

**Session Management:**
- Optional session IDs for stateful interactions
- Session IDs are globally unique and cryptographically secure

**Resumability:**
- Supports reconnection via `Last-Event-ID` header
- Allows redelivery of missed messages on same stream

### In This Project

```
Claude (Client)
    │
    │ HTTP POST /mcp
    │ (JSON-RPC request)
    ▼
Spring Boot MCP Server
    │
    │ Response Options:
    ├─→ JSON (quick operations like "get product by SKU")
    └─→ SSE Stream (long operations or multiple messages)
    │
    ▼
Claude receives response
```

**References:**
- MCP Streamable HTTP Spec: https://modelcontextprotocol.io/specification/2025-03-26/basic/transports#streamable-http

---

## Architecture Integration

The MCP server is implemented as a **primary adapter** in the Hexagonal Architecture:

```
┌─────────────────────────────────────────────────┐
│         Primary Adapters (Incoming)             │
│  ┌──────────┐  ┌───────────┐  ┌─────────┐       │
│  │ REST API │  │ MCP Server│  │ Web UI  │       │
│  └────┬─────┘  └────┬──────┘  └────┬────┘       │
└───────┼─────────────┼──────────────┼────────────┘
        │             │              │
        └─────────────┼──────────────┘
                      ▼
        ┌──────────────────────────┐
        │   Input Ports            │
        │ (GetAllProductsInputPort,│
        │  GetProductByIdInputPort)│
        └──────────────────────────┘
                      │
                      ▼
        ┌──────────────────────────┐
        │   Use Cases              │
        │ (GetAllProductsUseCase,  │
        │  GetProductByIdUseCase)  │
        └──────────────────────────┘
                      │
                      ▼
        ┌──────────────────────────┐
        │    Domain Model          │
        │  (Product, SKU, etc.)    │
        └──────────────────────────┘
```

**Package:** `dev.domaincentric.sample.ecommerce.product.adapter.incoming.mcp`

**Why Primary Adapter?**
- Incoming port for AI clients
- Domain stays framework-independent
- Easy to replace or remove without affecting core logic

---

## Implementation

### 1. MCP Tools Class

**File:** `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/mcp/ProductCatalogMcpTools.java`

```java
@Component
public class ProductCatalogMcpTools {

  private final GetAllProductsUseCase getAllProductsUseCase;
  private final GetProductByIdUseCase getProductByIdUseCase;
  private final ProductDtoConverter productDtoConverter;

  public ProductCatalogMcpTools(
      final GetAllProductsUseCase getAllProductsUseCase,
      final GetProductByIdUseCase getProductByIdUseCase,
      final ProductDtoConverter productDtoConverter) {
    this.getAllProductsUseCase = getAllProductsUseCase;
    this.getProductByIdUseCase = getProductByIdUseCase;
    this.productDtoConverter = productDtoConverter;
  }

  @McpTool(
    name = "all-products",
    description = "Get all products in the catalog. Returns complete product information..."
  )
  public List<ProductDto> getAllProducts() {
    GetAllProductsQuery query = new GetAllProductsQuery();
    GetAllProductsResponse response = getAllProductsUseCase.execute(query);
    return response.products().stream().map(productDtoConverter::toDto).toList();
  }

  @McpTool(
    name = "product-by-sku",
    description = "Find a product by its SKU (Stock Keeping Unit). SKU must contain..."
  )
  public ProductDto findProductBySku(@NonNull final String sku) {
    return productApplicationService
        .findProductBySku(SKU.of(sku))
        .map(productDtoConverter::toDto)
        .orElse(null);
  }

  @McpTool(
    name = "product-by-category",
    description = "Find all products in a specific category. Available categories..."
  )
  public List<ProductDto> findProductsByCategory(@NonNull final String categoryName) {
    final Category category = Category.of(categoryName);
    final List<Product> products = productApplicationService.findProductsByCategory(category);
    return products.stream().map(productDtoConverter::toDto).toList();
  }

  @McpTool(
    name = "product-by-id",
    description = "Get detailed product information by product ID. Requires the internal product UUID..."
  )
  public ProductDto getProductById(@NonNull final String id) {
    return productApplicationService
        .findProductById(ProductId.of(id))
        .map(productDtoConverter::toDto)
        .orElse(null);
  }
}
```

**Key Points:**
- `@Component` - Spring manages lifecycle
- `@McpTool` - Marks methods as AI-callable tools
- Returns DTOs, not domain objects
- Delegates all logic to application service

### 2. Tool Implementation Pattern

Each tool follows this pattern:

```java
@McpTool(name = "tool-name", description = "What it does...")
public ReturnType toolMethod(String externalInput) {
  // 1. Convert external input to domain value object
  DomainValueObject domainInput = DomainValueObject.of(externalInput);

  // 2. Call application service
  Optional<DomainObject> result = applicationService.operation(domainInput);

  // 3. Convert domain result to DTO
  return result.map(converter::toDto).orElse(null);
}
```

**Why DTOs?**
- External API stability (domain can change internally)
- Optimized for JSON serialization
- Decouples external representation from domain model

---

## Configuration

### 1. Gradle Dependencies

**File:** `build.gradle`

```gradle
ext {
  set('springAiVersion', "1.1.0-M3")
}

dependencies {
  implementation 'org.springframework.ai:spring-ai-starter-mcp-server-webmvc'
}

dependencyManagement {
  imports {
    mavenBom "org.springframework.ai:spring-ai-bom:${springAiVersion}"
  }
}
```

### 2. Spring Configuration

**File:** `src/main/resources/application.yml`

```yaml
spring:
  ai:
    mcp:
      server:
        protocol: streamable           # HTTP + SSE (not stdio)
        type: sync                     # Synchronous execution
        stdio: false                   # No process-based communication
        name: dca-ecommerce-sample-mcp-server
        version: 1.0.0
        streamable-http:
          mcp-endpoint: /mcp           # Endpoint path
```

**Server runs on:** `http://localhost:8080/mcp`

### 3. Client Configuration

**File:** `.mcp.json` (project root)

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

This file tells MCP clients (like Claude Code) how to connect to the server.

---

## Available Tools

### all-products
- **Description:** Get all products in the catalog
- **Parameters:** None
- **Returns:** `List<ProductDto>`
- **Example:** Returns 21 products with full details

### product-by-sku
- **Description:** Find product by SKU
- **Parameters:** `sku` (String) - e.g., "BOOK-001"
- **Returns:** `ProductDto` or `null`
- **Format:** Uppercase letters, numbers, hyphens only

### product-by-category
- **Description:** Find products in category
- **Parameters:** `categoryName` (String)
- **Returns:** `List<ProductDto>`
- **Categories:** Books, Modeling, Apparel, Desk & Office, Stickers & Pins

### product-by-id
- **Description:** Get product by internal ID
- **Parameters:** `id` (String) - UUID format
- **Returns:** `ProductDto` or `null`

---

## Running the Server

### Start the Application

```bash
./gradlew bootRun
```

Server starts on `http://localhost:8080` with MCP endpoint at `/mcp`.

### Verify It's Running

```bash
curl http://localhost:8080/actuator/health
```

Expected:
```json
{"status": "UP"}
```

### Connect with Claude Code

1. Ensure `.mcp.json` is in project root
2. Start Claude Code in the project directory
3. Claude automatically connects and discovers tools
4. Ask Claude: "Show me my products"

---

## Adding New Tools

To add a new MCP tool:

**1. Add method to `ProductCatalogMcpTools`:**

```java
@McpTool(
  name = "products-in-stock",
  description = "Get all products with available stock (quantity > 0)"
)
public List<ProductDto> getProductsInStock() {
  return productApplicationService.getAllProducts().stream()
      .filter(p -> p.getStock().quantity() > 0)
      .map(productDtoConverter::toDto)
      .toList();
}
```

**2. Restart the application:**

```bash
./gradlew bootRun
```

**3. Tool is automatically available** - MCP clients discover it immediately.

---

## Design Decisions

### Why Read-Only Tools?

Current implementation provides **read-only** access:

- **Safety:** Prevents accidental data modification by AI
- **Simplicity:** Easier to test and reason about
- **Extensibility:** Write operations can be added later with proper safeguards (confirmations, audit logging)

### Why @McpTool Instead of @Tool?

Spring AI provides both:
- `@Tool` - Generic Spring AI function calling
- `@McpTool` - Specific to MCP server implementation

This project uses `@McpTool` for explicit MCP server integration.

### Architecture Decision Records

Related ADRs:
- [ADR-007: Hexagonal Architecture](../architecture/adr/adr-007-hexagonal-architecture.md) - Primary/Secondary adapters
- [ADR-002: Framework-Independent Domain](../architecture/adr/adr-002-framework-independent-domain.md) - Why domain doesn't know about Spring AI

---

## References

### Documentation
- Spring AI MCP Server: https://docs.spring.io/spring-ai/reference/1.1/api/mcp/mcp-streamable-http-server-boot-starter-docs.html
- Spring AI Tools: https://docs.spring.io/spring-ai/reference/1.1/api/tools.html

### Project Files
- **MCP Tools:** `src/main/java/dev/domaincentric/sample/ecommerce/product/adapter/incoming/mcp/ProductCatalogMcpTools.java`
- **Use Cases:** `src/main/java/dev/domaincentric/sample/ecommerce/product/application/usecase/`
  - `GetAllProductsUseCase.java`
  - `GetProductByIdUseCase.java`
- **Config:** `src/main/resources/application.yml`
- **Client Config:** `.mcp.json`

### Related Docs
- [Architecture Principles](../architecture/architecture-principles.md)
- [Hexagonal Architecture](../architecture/adr/adr-007-hexagonal-architecture.md)