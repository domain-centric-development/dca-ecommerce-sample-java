package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The catalogue an AI assistant reads from the running shop: an MCP client without credentials
 * calls the shop's {@code all-products} tool at {@code /mcp}. No browser: the actor is an
 * assistant.
 */
@DisplayName("Catalogue tools E2E Tests")
class CatalogueToolsE2ETest {

  @Test
  @DisplayName("All products lists the range with price and stock")
  void allProductsListsTheSeededRangeWithPriceAndStock() throws Exception {
    final JsonNode products = callAllProducts();

    final List<String> names = new ArrayList<>();
    products.forEach(product -> names.add(product.get("name").asText()));
    assertEquals(21, names.size(), "The seed creates 21 products: " + names);
    assertEquals(names.stream().sorted().toList(), names, "Products are in ordinal name order");
    assertEquals("\"Bounded Context\" Enamel Pin", names.get(0), "The quoted names sort first");

    final JsonNode ddd = productNamed(products, "Domain-Driven Design");
    assertFalse(ddd.path("id").asText().isBlank(), "has an id");
    assertEquals("BOOK-001", ddd.get("sku").asText());
    assertFalse(ddd.path("description").asText().isBlank(), "has a description");
    assertFalse(ddd.path("imageUrl").asText().isBlank(), "has an image URL");
    assertEquals(0, new BigDecimal("54.99").compareTo(ddd.get("price").decimalValue()), "price");
    assertEquals("EUR", ddd.get("currency").asText());
    assertEquals("Books", ddd.get("category").asText());
    assertEquals(20, ddd.get("stockQuantity").asInt());
    assertTrue(ddd.get("isAvailable").asBoolean(), "is available");
  }

  private static JsonNode callAllProducts() throws Exception {
    final McpSyncClient client =
        McpClient.sync(
                HttpClientStreamableHttpTransport.builder(ShopUnderTest.baseUrl())
                    .endpoint("/mcp")
                    .build())
            .requestTimeout(Duration.ofSeconds(30))
            .build();
    try {
      client.initialize();
      final McpSchema.CallToolResult answer =
          client.callTool(new McpSchema.CallToolRequest("all-products", Map.of()));
      assertFalse(Boolean.TRUE.equals(answer.isError()), "all-products answers without error");
      final String text = ((McpSchema.TextContent) answer.content().getFirst()).text();
      return new ObjectMapper().readTree(text);
    } finally {
      client.closeGracefully();
    }
  }

  private static JsonNode productNamed(final JsonNode products, final String name) {
    for (final JsonNode product : products) {
      if (name.equals(product.get("name").asText())) {
        return product;
      }
    }
    return fail("no product named " + name);
  }
}
