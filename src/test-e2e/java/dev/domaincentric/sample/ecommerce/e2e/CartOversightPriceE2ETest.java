package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Staff list every cart after a price changed: a customer's cart holds a product added at its old
 * price, a staff client sets the new price through the API and then lists the carts. No browser:
 * the actors are applications. The shop is one of its own, so the changed price never reaches the
 * catalogue the rest of the suite reads.
 */
@DisplayName("Cart Oversight Price E2E Tests")
class CartOversightPriceE2ETest {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final HttpClient HTTP = HttpClient.newHttpClient();

  @Test
  @DisplayName("The staff list totals at todays prices")
  void theStaffListTotalsEachCartAtTheProductsCurrentPrice() throws Exception {
    try (ConfigurableApplicationContext shop =
        ShopUnderTest.startedWith("cart_oversight_price_e2e")) {
      final String shopUrl = ShopUnderTest.baseUrlOf(shop);
      final String ddd = productIdOf(shopUrl, "Domain-Driven Design");
      final String ada = registeredToken(shopUrl, "ada@example.com");
      final String cartId =
          JSON.readTree(send(authorized(ada, shopUrl + "/api/carts").POST(noBody())).body())
              .get("cartId")
              .asText();
      final HttpResponse<String> added =
          send(
              authorized(ada, shopUrl + "/api/carts/" + cartId + "/items")
                  .header("Content-Type", "application/json")
                  .POST(
                      HttpRequest.BodyPublishers.ofString(
                          """
                          {"productId":"%s","quantity":1}"""
                              .formatted(ddd))));
      assertEquals(200, added.statusCode(), added.body());
      assertEquals(
          "54.99",
          JSON.readTree(added.body()).path("total").decimalValue().toPlainString(),
          "added at 54.99 EUR");
      final JsonNode operator = claimsOf(registeredToken(shopUrl, "staff@example.com"));
      final String staff =
          ShopUnderTest.staffTokenIn(
              shop, operator.get("sub").asText(), operator.get("email").asText());
      final HttpResponse<String> priced =
          send(
              authorized(staff, shopUrl + "/api/products/" + ddd + "/price")
                  .header("Content-Type", "application/json")
                  .PUT(HttpRequest.BodyPublishers.ofString("{\"price\":49.99}")));
      assertEquals(204, priced.statusCode(), priced.body());

      final HttpResponse<String> response = send(authorized(staff, shopUrl + "/api/carts").GET());

      assertEquals(200, response.statusCode(), response.body());
      JsonNode listed = null;
      for (final JsonNode cart : JSON.readTree(response.body()).path("carts")) {
        if (cartId.equals(cart.path("cartId").asText())) {
          listed = cart;
        }
      }
      assertNotNull(listed, "Ada's cart is among the carts");
      assertEquals("49.99", listed.path("total").decimalValue().toPlainString(), "the total");
    }
  }

  private static JsonNode claimsOf(final String jwt) throws Exception {
    return JSON.readTree(Base64.getUrlDecoder().decode(jwt.split("\\.")[1]));
  }

  private static String registeredToken(final String shopUrl, final String email) throws Exception {
    final HttpResponse<String> response =
        send(
            HttpRequest.newBuilder(URI.create(shopUrl + "/api/auth/register"))
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

  private static String productIdOf(final String shopUrl, final String name) throws Exception {
    final HttpResponse<String> response =
        send(HttpRequest.newBuilder(URI.create(shopUrl + "/api/products")).GET());
    for (final JsonNode product : JSON.readTree(response.body())) {
      if (name.equals(product.get("name").asText())) {
        return product.get("id").asText();
      }
    }
    throw new AssertionError("no product named " + name);
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
