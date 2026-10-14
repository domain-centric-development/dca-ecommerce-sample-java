package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A partner application opens a cart for its signed-in customer and puts a product into it through
 * the running shop's JSON API; staff see every cart in the shop. No browser: the actor is an
 * application.
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

  @Test
  @DisplayName("More than in stock is refused")
  void addingMoreThanIsInStockIsAConflictAndLeavesTheCartEmpty() throws Exception {
    final String token = registeredToken();
    final String cartId =
        JSON.readTree(send(authorized(token, "/api/carts").POST(noBody())).body())
            .get("cartId")
            .asText();
    final String productId = productIdOf("Wooden Hexagon Desk Model");

    final HttpResponse<String> response =
        send(
            authorized(token, "/api/carts/" + cartId + "/items")
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        """
                        {"productId":"%s","quantity":9}"""
                            .formatted(productId))));

    assertEquals(409, response.statusCode(), response.body());
    assertEquals(
        "application/problem+json",
        response.headers().firstValue("Content-Type").orElse("").split(";")[0]);
    final JsonNode problem = JSON.readTree(response.body());
    assertEquals("Not enough stock", problem.path("title").asText());
    assertEquals(
        "Requested 9 of product " + productId + ", more than is in stock.",
        problem.path("detail").asText());
    final JsonNode cart =
        JSON.readTree(send(authorized(token, "/api/carts/" + cartId).GET()).body());
    assertTrue(cart.path("items").isArray(), cart.toString());
    assertEquals(0, cart.path("items").size(), "the cart is still empty");
  }

  @Test
  @DisplayName("A malformed product id is a bad request")
  void addingAProductIdThatIsNoUuidIsABadRequestAndLeavesTheCartEmpty() throws Exception {
    final String token = registeredToken();
    final String cartId =
        JSON.readTree(send(authorized(token, "/api/carts").POST(noBody())).body())
            .get("cartId")
            .asText();

    final HttpResponse<String> response =
        send(
            authorized(token, "/api/carts/" + cartId + "/items")
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        """
                        {"productId":"no-such-product","quantity":1}""")));

    assertEquals(400, response.statusCode(), response.body());
    assertEquals(
        "application/problem+json",
        response.headers().firstValue("Content-Type").orElse("").split(";")[0]);
    final JsonNode problem = JSON.readTree(response.body());
    assertEquals("Unacceptable value", problem.path("title").asText());
    assertFalse(problem.has("detail"), "the problem carries a detail: " + problem);
    final JsonNode cart =
        JSON.readTree(send(authorized(token, "/api/carts/" + cartId).GET()).body());
    assertTrue(cart.path("items").isArray(), cart.toString());
    assertEquals(0, cart.path("items").size(), "the cart is still empty");
  }

  @Test
  @DisplayName("No active cart is not found")
  void theActiveCartOfACustomerWithoutOneIsNotFound() throws Exception {
    final String token = registeredToken();
    final String customerId = claimsOf(token).get("sub").asText();

    final HttpResponse<String> response =
        send(authorized(token, "/api/carts/customer/" + customerId + "/active").GET());

    assertEquals(404, response.statusCode(), response.body());
  }

  @Test
  @DisplayName("Staff list every cart")
  void staffSeeEveryCartWithItsOwnerStatusAndTotal() throws Exception {
    final String ada = registeredToken();
    final JsonNode adasCart =
        JSON.readTree(send(authorized(ada, "/api/carts").POST(noBody())).body());
    final String cartId = adasCart.get("cartId").asText();
    final HttpResponse<String> added =
        send(
            authorized(ada, "/api/carts/" + cartId + "/items")
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        """
                        {"productId":"%s","quantity":2}"""
                            .formatted(productIdOf("Domain-Driven Design")))));
    assertEquals(200, added.statusCode(), added.body());
    final JsonNode operator = claimsOf(registeredToken());
    final Optional<String> staff =
        ShopUnderTest.staffTokenFor(operator.get("sub").asText(), operator.get("email").asText());
    assumeTrue(staff.isPresent(), "a staff token is minted only by the suite's own shop");

    final HttpResponse<String> response = send(authorized(staff.get(), "/api/carts").GET());

    assertEquals(200, response.statusCode(), response.body());
    JsonNode listed = null;
    for (final JsonNode cart : JSON.readTree(response.body()).path("carts")) {
      if (cartId.equals(cart.path("cartId").asText())) {
        listed = cart;
      }
    }
    assertNotNull(listed, "Ada's cart is among the carts");
    assertEquals(adasCart.get("customerId").asText(), listed.path("customerId").asText());
    assertEquals("ACTIVE", listed.path("status").asText());
    assertEquals(1, listed.path("itemCount").asInt());
    assertEquals("109.98", listed.path("total").decimalValue().toPlainString());
    assertEquals("EUR", listed.path("currency").asText());
  }

  private static JsonNode claimsOf(final String jwt) throws Exception {
    return JSON.readTree(Base64.getUrlDecoder().decode(jwt.split("\\.")[1]));
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
