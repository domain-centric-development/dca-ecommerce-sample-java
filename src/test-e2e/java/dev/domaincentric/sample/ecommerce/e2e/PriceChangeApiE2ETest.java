package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
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
 * An operator changes a product's price: a plain HTTP client with a staff token sends {@code PUT
 * /api/products/<id>/price}, and a visitor's browser then opens the product page and the catalogue.
 * The shop is one of its own, so the changed price never reaches the catalogue the rest of the
 * suite reads.
 */
@DisplayName("Price Change API E2E Tests")
class PriceChangeApiE2ETest extends BaseE2ETest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Test
  @DisplayName("A changed price shows on the product page")
  void aPriceAStaffClientChangedIsThePriceOnTheProductPageAndTheCard() throws Exception {
    try (ConfigurableApplicationContext shop = ShopUnderTest.startedWith("price_change_api_e2e")) {
      final String shopUrl = ShopUnderTest.baseUrlOf(shop);
      final String staffToken = staffToken(shop);
      final String ddd = productIdOf(shopUrl, "Domain-Driven Design");
      assertEquals(
          "54.99 EUR",
          ProductDetailPage.navigateTo(page, shopUrl, "/products/" + ddd).price(),
          "the price before the change");

      HttpClient.newHttpClient()
          .send(
              HttpRequest.newBuilder(URI.create(shopUrl + "/api/products/" + ddd + "/price"))
                  .header("Authorization", "Bearer " + staffToken)
                  .header("Content-Type", "application/json")
                  .PUT(HttpRequest.BodyPublishers.ofString("{\"price\":49.99}"))
                  .build(),
              HttpResponse.BodyHandlers.ofString());

      assertEquals(
          "49.99 EUR",
          ProductDetailPage.navigateTo(page, shopUrl, "/products/" + ddd).price(),
          "the product page's price");
      assertEquals(
          "49.99 EUR",
          ProductCatalogPage.navigateTo(page, shopUrl).cardPriceOf("Domain-Driven Design"),
          "the card's price");
    }
  }

  /** The id of the catalogue's product with this name, as the API lists it. */
  private static String productIdOf(final String shopUrl, final String name) throws Exception {
    final HttpResponse<String> catalogue =
        HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder(URI.create(shopUrl + "/api/products")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    for (final JsonNode product : JSON.readTree(catalogue.body())) {
      if (name.equals(product.path("name").asText())) {
        return product.path("id").asText();
      }
    }
    throw new AssertionError("no product named " + name);
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
            Set.of(Role.CUSTOMER, Role.STAFF));
  }
}
