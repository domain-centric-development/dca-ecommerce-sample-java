package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A customer's application adds to a cart that holds a product the catalogue no longer knows. No
 * browser: the actor is an application. The shop is one of its own, so the product it takes out of
 * the catalogue is missing nowhere else in the suite.
 */
@DisplayName("Cart With A Lost Product API E2E Tests")
class CartWithALostProductApiE2ETest {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final HttpClient HTTP = HttpClient.newHttpClient();

  @Test
  @DisplayName("A cart answers with a product the catalogue lost")
  void addingToACartWithAProductTheCatalogueLostAnswersWithTheLineUnavailableAndUnpriced()
      throws Exception {
    try (ConfigurableApplicationContext shop = ShopUnderTest.startedWith("cart_lost_product_e2e")) {
      final String shopUrl = ShopUnderTest.baseUrlOf(shop);
      final String cleanArchitecture = ShopUnderTest.productIdIn(shop, "Clean Architecture");
      final String domainDrivenDesign = ShopUnderTest.productIdIn(shop, "Domain-Driven Design");
      final String ada = registeredToken(shopUrl);
      final String cartId =
          JSON.readTree(send(authorized(ada, shopUrl + "/api/carts").POST(noBody())).body())
              .get("cartId")
              .asText();
      final HttpResponse<String> first = add(ada, shopUrl, cartId, cleanArchitecture);
      assertEquals(200, first.statusCode(), first.body());
      final BigDecimal unitPrice =
          lineOf(JSON.readTree(first.body()), cleanArchitecture).path("price").decimalValue();
      final HttpResponse<String> second = add(ada, shopUrl, cartId, domainDrivenDesign);
      assertEquals(200, second.statusCode(), second.body());
      shop.getBean(ProductRepository.class).deleteById(ProductId.of(domainDrivenDesign));

      final HttpResponse<String> response = add(ada, shopUrl, cartId, cleanArchitecture);

      assertEquals(200, response.statusCode(), response.body());
      final JsonNode cart = JSON.readTree(response.body());
      assertEquals(
          2, lineOf(cart, cleanArchitecture).path("quantity").asInt(), "Clean Architecture");
      final JsonNode lost = lineOf(cart, domainDrivenDesign);
      assertEquals(1, lost.path("quantity").asInt(), "the lost line's quantity");
      assertTrue(lost.path("available").isBoolean(), "the lost line says whether it is available");
      assertFalse(lost.path("available").asBoolean(), "the lost line is not available");
      assertTrue(lost.path("price").isMissingNode(), "the lost line has no price: " + lost);
      assertEquals(
          unitPrice.multiply(BigDecimal.valueOf(2)).toPlainString(),
          cart.path("total").decimalValue().toPlainString(),
          "the total of the two units of Clean Architecture alone");
    }
  }

  private static JsonNode lineOf(final JsonNode cart, final String productId) {
    JsonNode found = null;
    for (final JsonNode item : cart.path("items")) {
      if (productId.equals(item.path("productId").asText())) {
        found = item;
      }
    }
    assertNotNull(found, "the cart holds a line of " + productId + ": " + cart);
    return found;
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
