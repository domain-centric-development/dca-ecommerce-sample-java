package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A partner application opens a cart for its signed-in customer and puts a product into it through
 * the running shop's JSON API. No browser: the actor is an application.
 */
@DisplayName("Shopping Cart API E2E Tests")
class ShoppingCartApiE2ETest {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final HttpClient HTTP = HttpClient.newHttpClient();

  @Test
  @DisplayName("Adding a product returns the cart")
  void addingAProductToTheOpenCartAnswersTheCartWithItsLine() throws Exception {
    final String token = registeredToken();
    final String cartId =
        JSON.readTree(send(authorized(token, "/api/carts").POST(noBody())).body())
            .get("cartId")
            .asText();
    final String productId = productIdOf("Domain-Driven Design");

    final HttpResponse<String> response =
        send(
            authorized(token, "/api/carts/" + cartId + "/items")
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        """
                        {"productId":"%s","quantity":2}"""
                            .formatted(productId))));

    assertEquals(200, response.statusCode(), response.body());
    final JsonNode cart = JSON.readTree(response.body());
    assertEquals(1, cart.path("items").size(), "one item");
    assertEquals(productId, cart.path("items").path(0).path("productId").asText());
    assertEquals(2, cart.path("items").path(0).path("quantity").asInt());
    assertEquals("109.98", cart.path("total").decimalValue().toPlainString());
    assertEquals("EUR", cart.path("currency").asText());
    assertEquals(1, cart.path("itemCount").asInt());
  }

  /** A fresh customer: a shop started elsewhere may keep accounts and carts from an earlier run. */
  private static String registeredToken() throws Exception {
    final String email =
        "ada+" + Long.toHexString(System.nanoTime()).toLowerCase(Locale.ROOT) + "@example.com";
    final HttpResponse<String> response =
        send(
            HttpRequest.newBuilder(URI.create(ShopUnderTest.baseUrl() + "/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        """
                        {"email":"%s","password":"Secret123","firstName":"Ada",\
                        "lastName":"Lovelace","dateOfBirth":"1815-12-10"}"""
                            .formatted(email))));
    assertEquals(201, response.statusCode(), response.body());
    return JSON.readTree(response.body()).get("token").asText();
  }

  private static String productIdOf(final String name) throws Exception {
    final HttpResponse<String> response =
        send(HttpRequest.newBuilder(URI.create(ShopUnderTest.baseUrl() + "/api/products")).GET());
    for (final JsonNode product : JSON.readTree(response.body())) {
      if (name.equals(product.get("name").asText())) {
        return product.get("id").asText();
      }
    }
    throw new AssertionError("no product named " + name);
  }

  private static HttpRequest.Builder authorized(final String token, final String path) {
    return HttpRequest.newBuilder(URI.create(ShopUnderTest.baseUrl() + path))
        .header("Authorization", "Bearer " + token);
  }

  private static HttpRequest.BodyPublisher noBody() {
    return HttpRequest.BodyPublishers.noBody();
  }

  private static HttpResponse<String> send(final HttpRequest.Builder request) throws Exception {
    return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
  }
}
