package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The catalogue a partner application reads from the running shop: a plain HTTP client without a
 * token asks {@code GET /api/products}. No browser: the actor is another application.
 */
@DisplayName("Catalogue API E2E Tests")
class CatalogueApiE2ETest {

  @Test
  @DisplayName("Listing returns the range in name order")
  void listingReturnsTheSeededRangeInOrdinalNameOrder() throws Exception {
    final HttpResponse<String> response =
        HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder(URI.create(ShopUnderTest.baseUrl() + "/api/products"))
                    .header("Accept", "application/json")
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode(), "status");
    final JsonNode products = new ObjectMapper().readTree(response.body());
    assertTrue(products.isArray(), "the answer is a JSON array");
    final List<String> names = new ArrayList<>();
    products.forEach(product -> names.add(product.get("name").asText()));
    assertEquals(21, names.size(), "The seed creates 21 products: " + names);
    assertEquals(names.stream().sorted().toList(), names, "Products are in ordinal name order");
    assertEquals("\"Bounded Context\" Enamel Pin", names.get(0), "The quoted names sort first");
  }
}
