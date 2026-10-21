package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A member of staff sends a product the shop refuses for one of its values: a plain HTTP client
 * with a staff token sends {@code POST /api/products}. No browser: the actor is another
 * application, and its one error handler reads the refusal as a problem document. The shop is one
 * of its own, so its catalogue holds exactly the seeded range.
 */
@DisplayName("Refused product value API E2E Tests")
class RefusedProductValueApiE2ETest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Test
  @DisplayName("A blank name is refused")
  void aBlankNameIsAnUnacceptableValueNamingTheNameAndNothingIsCreated() throws Exception {
    try (ConfigurableApplicationContext shop =
        ShopUnderTest.startedWith("refused_product_value_api_e2e")) {
      final String staffToken = ShopUnderTest.staffTokenOf(shop);

      final HttpResponse<String> response =
          HttpClient.newHttpClient()
              .send(
                  HttpRequest.newBuilder(
                          URI.create(ShopUnderTest.baseUrlOf(shop) + "/api/products"))
                      .header("Authorization", "Bearer " + staffToken)
                      .header("Content-Type", "application/json")
                      .POST(
                          HttpRequest.BodyPublishers.ofString(
                              """
                              {"sku":"BLANK-001","name":"   ","price":9.99,\
                              "category":"Books","stock":1}"""))
                      .build(),
                  HttpResponse.BodyHandlers.ofString());

      assertEquals(400, response.statusCode(), "status");
      assertTrue(
          response
              .headers()
              .firstValue("Content-Type")
              .orElse("")
              .startsWith("application/problem+json"),
          "content type: " + response.headers().firstValue("Content-Type"));
      final JsonNode problem = JSON.readTree(response.body());
      assertEquals("Unacceptable value", problem.path("title").asText(), "title");
      assertFalse(
          problem.path("errors").path("name").asText().isBlank(),
          "the reason the problem gives for name: " + problem);
      assertEquals(21, catalogue(shop).size(), "products in the catalogue");
    }
  }

  private static JsonNode catalogue(final ConfigurableApplicationContext shop) throws Exception {
    final HttpResponse<String> response =
        HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder(URI.create(ShopUnderTest.baseUrlOf(shop) + "/api/products"))
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode(), "catalogue: " + response.body());
    return JSON.readTree(response.body());
  }
}
