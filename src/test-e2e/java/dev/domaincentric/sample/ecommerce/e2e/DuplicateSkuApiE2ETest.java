package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
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
 * A member of staff tries to add a product under a stock keeping unit the catalogue already holds:
 * a plain HTTP client with a staff token sends {@code POST /api/products}. No browser: the actor is
 * another application. The shop is one of its own, so its catalogue holds exactly the seeded range.
 */
@DisplayName("Duplicate SKU API E2E Tests")
class DuplicateSkuApiE2ETest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Test
  @DisplayName("A duplicate sku is a conflict")
  void aSecondProductUnderAnSkuInUseIsAConflictAndTheCatalogueStaysAsItWas() throws Exception {
    try (ConfigurableApplicationContext shop = ShopUnderTest.startedWith("duplicate_sku_api_e2e")) {
      final String staffToken = staffToken(shop);

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
                              {"sku":"BOOK-001","name":"Another Book",\
                              "price":19.99,"category":"Books","stock":5}"""))
                      .build(),
                  HttpResponse.BodyHandlers.ofString());

      assertEquals(409, response.statusCode(), "status");
      assertTrue(
          response
              .headers()
              .firstValue("Content-Type")
              .orElse("")
              .startsWith("application/problem+json"),
          "content type: " + response.headers().firstValue("Content-Type"));
      final JsonNode problem = JSON.readTree(response.body());
      assertEquals("Stock keeping unit already in use", problem.path("title").asText(), "title");
      assertEquals(
          "A product with SKU BOOK-001 already exists.", problem.path("detail").asText(), "detail");
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
    final String token = JSON.readTree(registered.body()).path("token").asText();
    final JsonNode claims = JSON.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
    return shop.getBean(TokenService.class)
        .generateRegisteredToken(
            UserId.of(claims.path("sub").asText()),
            claims.path("email").asText(),
            Set.of(IdentityProvider.Identity.ROLE_CUSTOMER, IdentityProvider.Identity.ROLE_STAFF));
  }
}
