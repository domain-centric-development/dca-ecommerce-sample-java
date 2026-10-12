package dev.domaincentric.sample.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * The catalogue tools an AI assistant calls over MCP, against the wired application on a free port,
 * by a client without any credentials.
 */
// A context of its own: another test class that adds a product to a shared context would change
// the seeded range this class reads.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties =
        "spring.datasource.url=jdbc:h2:mem:catalogue_mcp_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@DisplayName("Product catalogue tools")
class ProductCatalogMcpIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @LocalServerPort private int port;

  private McpSyncClient client;

  @BeforeEach
  void connect() {
    client =
        McpClient.sync(
                HttpClientStreamableHttpTransport.builder("http://localhost:" + port)
                    .endpoint("/mcp")
                    .build())
            .requestTimeout(Duration.ofSeconds(30))
            .build();
  }

  @AfterEach
  void disconnect() {
    client.closeGracefully();
  }

  @Test
  @DisplayName("The server offers two catalogue tools")
  void serverIntroducesItselfAndOffersTheTwoCatalogueTools() {
    final McpSchema.InitializeResult introduction = client.initialize();
    final Map<String, McpSchema.Tool> tools =
        client.listTools().tools().stream()
            .collect(java.util.stream.Collectors.toMap(McpSchema.Tool::name, tool -> tool));

    assertThat(introduction.serverInfo().name()).isEqualTo("dca-ecommerce-sample-mcp-server");
    assertThat(introduction.serverInfo().version()).isEqualTo("1.0.0");
    assertThat(tools).containsKey("all-products");
    assertThat(tools.get("all-products").description())
        .isEqualTo(
            "Get all products in the catalog. Returns complete product information including SKU,"
                + " name, price, category, and available stock.");
    assertThat(tools).containsKey("product-by-id");
    assertThat(tools.get("product-by-id").inputSchema().properties()).containsOnlyKeys("id");
    assertThat(tools.get("product-by-id").description())
        .isEqualTo(
            "Get detailed product information by product ID. Requires the internal product UUID."
                + " Returns complete product details including all attributes.");
  }

  @Test
  @DisplayName("Product by id returns the product")
  void productByIdAnswersTheProductWithItsPriceAndStock() throws Exception {
    client.initialize();
    final String id = idOfListedProduct("Wooden Hexagon Desk Model");

    final McpSchema.CallToolResult answer =
        client.callTool(new McpSchema.CallToolRequest("product-by-id", Map.of("id", id)));

    assertThat(answer.isError()).isNotEqualTo(Boolean.TRUE);
    final JsonNode product = answerOf(answer);
    assertThat(product.path("id").asText()).isEqualTo(id);
    assertThat(product.path("sku").asText()).isEqualTo("HEXAGON-001");
    assertThat(product.path("price").decimalValue()).isEqualByComparingTo(new BigDecimal("39.99"));
    assertThat(product.path("currency").asText()).isEqualTo("EUR");
    assertThat(product.path("category").asText()).isEqualTo("Desk & Office");
    assertThat(product.path("stockQuantity").asInt()).isEqualTo(8);
    assertThat(product.path("isAvailable").isBoolean()).isTrue();
    assertThat(product.path("isAvailable").asBoolean()).isTrue();
  }

  @Test
  @DisplayName("An unknown id returns no product")
  void productByIdAnswersNullForAnIdThatNamesNoProduct() throws Exception {
    client.initialize();

    final McpSchema.CallToolResult answer =
        client.callTool(
            new McpSchema.CallToolRequest("product-by-id", Map.of("id", "no-such-product")));

    assertThat(answer.isError()).isNotEqualTo(Boolean.TRUE);
    assertThat(answerOf(answer).isNull()).as("answer %s", answerOf(answer)).isTrue();
  }

  @Test
  @DisplayName("A blank id is a tool error")
  void productByIdRefusesABlankIdWithAToolError() {
    client.initialize();

    final McpSchema.CallToolResult answer =
        client.callTool(new McpSchema.CallToolRequest("product-by-id", Map.of("id", "")));

    assertThat(answer.isError()).isTrue();
    assertThat(textOf(answer)).isEqualTo("Product ID cannot be null or blank");
  }

  private String idOfListedProduct(final String name) throws Exception {
    for (final JsonNode listed :
        answerOf(client.callTool(new McpSchema.CallToolRequest("all-products", Map.of())))) {
      if (name.equals(listed.path("name").asText())) {
        return listed.path("id").asText();
      }
    }
    throw new AssertionError("all-products lists no product named " + name);
  }

  private static JsonNode answerOf(final McpSchema.CallToolResult answer) throws Exception {
    return JSON.readTree(textOf(answer));
  }

  private static String textOf(final McpSchema.CallToolResult answer) {
    assertThat(answer.content()).hasSize(1).first().isInstanceOf(McpSchema.TextContent.class);
    return ((McpSchema.TextContent) answer.content().getFirst()).text();
  }
}
