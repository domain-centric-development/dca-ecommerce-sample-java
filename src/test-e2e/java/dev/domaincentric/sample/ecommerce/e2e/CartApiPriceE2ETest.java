package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
 * A partner application adds to a cart after a price changed: the cart holds a product added at its
 * old price, a staff client sets the new price through the API, and the customer's client adds a
 * second product. No browser: the actors are applications. The shop is one of its own, so the
 * changed price never reaches the catalogue the rest of the suite reads.
 */
@DisplayName("Cart API Price E2E Tests")
class CartApiPriceE2ETest {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final HttpClient HTTP = HttpClient.newHttpClient();

  @Test
  @DisplayName("Adding answers at todays prices")
  void addingToTheCartAnswersEveryItemAtTodaysPrice() throws Exception {
    try (ConfigurableApplicationContext shop = ShopUnderTest.startedWith("cart_api_price_e2e")) {
      final String shopUrl = ShopUnderTest.baseUrlOf(shop);
      final String ddd = ShopUnderTest.productIdIn(shop, "Domain-Driven Design");
      final String cleanArchitecture = ShopUnderTest.productIdIn(shop, "Clean Architecture");
      final String ada = registeredToken(shopUrl);
      final String cartId =
          JSON.readTree(send(authorized(ada, shopUrl + "/api/carts").POST(noBody())).body())
              .get("cartId")
              .asText();
      final HttpResponse<String> first = add(ada, shopUrl, cartId, ddd);
      assertEquals(200, first.statusCode(), first.body());
      assertEquals(
          "54.99",
          JSON.readTree(first.body()).path("total").decimalValue().toPlainString(),
          "added at 54.99 EUR");
      final HttpResponse<String> priced =
          send(
              authorized(
                      ShopUnderTest.staffTokenOf(shop), shopUrl + "/api/products/" + ddd + "/price")
                  .header("Content-Type", "application/json")
                  .PUT(HttpRequest.BodyPublishers.ofString("{\"price\":49.99}")));
      assertEquals(204, priced.statusCode(), priced.body());

      final HttpResponse<String> response = add(ada, shopUrl, cartId, cleanArchitecture);

      assertEquals(200, response.statusCode(), response.body());
      final JsonNode cart = JSON.readTree(response.body());
      JsonNode dddItem = null;
      for (final JsonNode item : cart.path("items")) {
        if (ddd.equals(item.path("productId").asText())) {
          dddItem = item;
        }
      }
      assertNotNull(dddItem, "the Domain-Driven Design item: " + response.body());
      assertEquals("49.99", dddItem.path("price").decimalValue().toPlainString(), "its price");
      assertEquals("89.98", cart.path("total").decimalValue().toPlainString(), "the total");
      assertEquals(2, cart.path("items").size(), "the items: " + response.body());
      for (final JsonNode item : cart.path("items")) {
        assertFalse(item.has("priceAtAddition"), "an item with priceAtAddition: " + item);
      }
    }
  }

  private static HttpResponse<String> add(
      final String token, final String shopUrl, final String cartId, final String productId)
      throws Exception {
    return send(
        authorized(token, shopUrl + "/api/carts/" + cartId + "/items")
            .header("Content-Type", "application/json")
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    """
                    {"productId":"%s","quantity":1}"""
                        .formatted(productId))));
  }

  private static String registeredToken(final String shopUrl) throws Exception {
    final HttpResponse<String> response =
        send(
            HttpRequest.newBuilder(URI.create(shopUrl + "/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        """
                        {"email":"ada@example.com","password":"Secret123","firstName":"Ada",\
                        "lastName":"Lovelace","dateOfBirth":"1815-12-10"}""")));
    assertEquals(201, response.statusCode(), response.body());
    return JSON.readTree(response.body()).get("token").asText();
  }

  private static HttpRequest.Builder authorized(final String token, final String url) {
    return HttpRequest.newBuilder(URI.create(url)).header("Authorization", "Bearer " + token);
  }

  private static HttpRequest.BodyPublisher noBody() {
    return HttpRequest.BodyPublishers.noBody();
  }

  private static HttpResponse<String> send(final HttpRequest.Builder request) throws Exception {
    return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
  }
}
