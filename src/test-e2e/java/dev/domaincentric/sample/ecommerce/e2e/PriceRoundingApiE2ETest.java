package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A member of staff creates a product at a price with a fraction of a cent: a plain HTTP client
 * with a staff token sends {@code POST /api/products}, and a visitor's browser then opens the
 * catalogue. The shop is one of its own, so the product added here never reaches the catalogue the
 * rest of the suite counts. The price reaches the catalogue by event, so the card is awaited.
 */
@DisplayName("Price Rounding API E2E Tests")
class PriceRoundingApiE2ETest extends BaseE2ETest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Test
  @DisplayName("A price is rounded half up to cents")
  void aPriceWithAFractionOfACentIsRoundedHalfUpInTheAnswerAndOnTheCard() throws Exception {
    try (ConfigurableApplicationContext shop =
        ShopUnderTest.startedWith("price_rounding_api_e2e")) {
      final String shopUrl = ShopUnderTest.baseUrlOf(shop);
      final String staffToken = staffToken(shop);

      final HttpResponse<String> response =
          HttpClient.newHttpClient()
              .send(
                  HttpRequest.newBuilder(URI.create(shopUrl + "/api/products"))
                      .header("Authorization", "Bearer " + staffToken)
                      .header("Content-Type", "application/json")
                      .POST(
                          HttpRequest.BodyPublishers.ofString(
                              """
                              {"sku":"ROUND-001","name":"Rounding Mug",\
                              "price":14.995,"category":"Desk & Office","stock":5}"""))
                      .build(),
                  HttpResponse.BodyHandlers.ofString());

      assertEquals(201, response.statusCode(), "status: " + response.body());
      final JsonNode product = JSON.readTree(response.body());
      assertEquals(15.00, product.path("price").asDouble(), "price: " + product);
      Awaitility.await()
          .atMost(Duration.ofSeconds(15))
          .untilAsserted(
              () ->
                  assertEquals(
                      "15.00 EUR",
                      ProductCatalogPage.navigateTo(page, shopUrl).cardPriceOf("Rounding Mug"),
                      "the card's price"));
    }
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
