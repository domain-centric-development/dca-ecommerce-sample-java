package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Role;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A member of staff adds a product to the running shop's catalogue through the API: a plain HTTP
 * client with a staff token sends {@code POST /api/products}. No browser: the actor is another
 * application. The shop is one of its own, so the product added here never reaches the catalogue
 * the rest of the suite counts.
 */
@DisplayName("Product Creation API E2E Tests")
class ProductCreationApiE2ETest {

  @Test
  @DisplayName("Staff create a product")
  void staffCreateAProductAndTheShopAnswersWithItInEuros() throws Exception {
    try (ConfigurableApplicationContext shop =
        ShopUnderTest.startedWith("product_creation_api_e2e")) {
      final String staffToken = staffToken(shop);

      final HttpResponse<String> response =
          createProduct(
              shop,
              staffToken,
              """
              {"sku":"MUG-002","name":"Aggregate Mug",\
              "description":"A mug with a consistency boundary.","imageUrl":"",\
              "price":14.99,"category":"Desk & Office","stock":12}""");

      assertEquals(201, response.statusCode(), "status");
      final JsonNode product = new ObjectMapper().readTree(response.body());
      assertFalse(product.path("id").asText().isBlank(), "the body has an id: " + product);
      assertEquals("MUG-002", product.path("sku").asText(), "sku");
      assertEquals("Aggregate Mug", product.path("name").asText(), "name");
      assertEquals(14.99, product.path("price").asDouble(), "price");
      assertEquals("EUR", product.path("currency").asText(), "currency");
      assertEquals("Desk & Office", product.path("category").asText(), "category");
    }
  }

  @Test
  @DisplayName("The answer carries the rounded price")
  void theAnswerCarriesThePriceRoundedHalfUpToCents() throws Exception {
    try (ConfigurableApplicationContext shop =
        ShopUnderTest.startedWith("product_creation_rounding_api_e2e")) {
      final String staffToken = staffToken(shop);

      final HttpResponse<String> response =
          createProduct(
              shop,
              staffToken,
              """
              {"sku":"ROUND-001","name":"Rounding Mug",\
              "price":14.995,"category":"Desk & Office","stock":5}""");

      assertEquals(201, response.statusCode(), "status: " + response.body());
      final JsonNode product = new ObjectMapper().readTree(response.body());
      assertEquals(15.00, product.path("price").asDouble(), "price: " + product);
    }
  }

  /** Sends {@code POST /api/products} with the staff token and the product as JSON. */
  private static HttpResponse<String> createProduct(
      final ConfigurableApplicationContext shop, final String staffToken, final String product)
      throws Exception {
    return HttpClient.newHttpClient()
        .send(
            HttpRequest.newBuilder(URI.create(ShopUnderTest.baseUrlOf(shop) + "/api/products"))
                .header("Authorization", "Bearer " + staffToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(product))
                .build(),
            HttpResponse.BodyHandlers.ofString());
  }

  /**
   * A staff token for an account registered through the API, from the shop's own token service. No
   * registration and no login hands out the staff role; staff tokens are issued outside the shop,
   * as this does.
   */
  private static String staffToken(final ConfigurableApplicationContext shop) throws Exception {
    final HttpResponse<String> registered =
        HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder(
                        URI.create(ShopUnderTest.baseUrlOf(shop) + "/api/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            """
                            {"email":"staff@example.com","password":"Secret123",\
                            "firstName":"Ada","lastName":"Lovelace","dateOfBirth":"1815-12-10"}"""))
                    .build(),
                HttpResponse.BodyHandlers.ofString());
    assertEquals(201, registered.statusCode(), "registration: " + registered.body());
    final ObjectMapper json = new ObjectMapper();
    final String token = json.readTree(registered.body()).path("token").asText();
    final JsonNode claims = json.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
    return shop.getBean(TokenService.class)
        .generateRegisteredToken(
            UserId.of(claims.path("sub").asText()),
            claims.path("email").asText(),
            Set.of(Role.CUSTOMER, Role.STAFF));
  }
}
