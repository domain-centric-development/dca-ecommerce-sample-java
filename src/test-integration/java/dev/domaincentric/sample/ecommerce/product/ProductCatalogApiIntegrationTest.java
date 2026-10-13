package dev.domaincentric.sample.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * The catalogue a partner application reads over the REST API, from the wired application with its
 * seeded range, by a client without a token.
 */
// A context of its own: another test class that adds a product to a shared context would change
// the seeded range this class reads.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:catalogue_api_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Product catalogue API")
class ProductCatalogApiIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("A listed product carries price and stock")
  void aListedProductCarriesItsMasterDataPriceAndStock() throws Exception {
    final JsonNode ddd = listedProductNamed("Domain-Driven Design");

    assertThat(ddd.path("id").asText()).isNotBlank();
    assertThat(ddd.path("sku").asText()).isEqualTo("BOOK-001");
    assertThat(ddd.path("name").asText()).isEqualTo("Domain-Driven Design");
    assertThat(ddd.path("description").asText()).isNotBlank();
    assertThat(ddd.path("imageUrl").asText()).isNotBlank();
    assertThat(ddd.path("price").decimalValue()).isEqualByComparingTo(new BigDecimal("54.99"));
    assertThat(ddd.path("currency").asText()).isEqualTo("EUR");
    assertThat(ddd.path("category").asText()).isEqualTo("Books");
    assertThat(ddd.path("stockQuantity").asInt()).isEqualTo(20);
    assertThat(ddd.path("isAvailable").isBoolean()).isTrue();
    assertThat(ddd.path("isAvailable").asBoolean()).isTrue();
  }

  @Test
  @DisplayName("One product by its id")
  void oneProductIsReadByTheIdTheListReturns() throws Exception {
    final String id = listedProductNamed("Hexagonal Architecture Explained").path("id").asText();

    final MvcResult result =
        mockMvc.perform(get("/api/products/" + id)).andExpect(status().isOk()).andReturn();

    final JsonNode product = JSON.readTree(result.getResponse().getContentAsString());
    assertThat(product.path("id").asText()).isEqualTo(id);
    assertThat(product.path("sku").asText()).isEqualTo("BOOK-005");
    assertThat(product.path("price").decimalValue()).isEqualByComparingTo(new BigDecimal("34.99"));
    assertThat(product.path("currency").asText()).isEqualTo("EUR");
    assertThat(product.path("category").asText()).isEqualTo("Books");
    assertThat(product.path("stockQuantity").asInt()).isEqualTo(26);
    assertThat(product.path("isAvailable").isBoolean()).isTrue();
    assertThat(product.path("isAvailable").asBoolean()).isTrue();
  }

  @Test
  @DisplayName("An unknown product is not found")
  void anUnknownProductIsAnsweredWithNotFoundAndNoBody() throws Exception {
    final MvcResult result = mockMvc.perform(get("/api/products/no-such-product")).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(404);
    assertThat(result.getResponse().getContentAsString()).isEmpty();
  }

  private JsonNode listedProductNamed(final String name) throws Exception {
    final MvcResult result =
        mockMvc.perform(get("/api/products")).andExpect(status().isOk()).andReturn();
    for (final JsonNode listed : JSON.readTree(result.getResponse().getContentAsString())) {
      if (name.equals(listed.path("name").asText())) {
        return listed;
      }
    }
    throw new AssertionError("GET /api/products lists no product named " + name);
  }
}
