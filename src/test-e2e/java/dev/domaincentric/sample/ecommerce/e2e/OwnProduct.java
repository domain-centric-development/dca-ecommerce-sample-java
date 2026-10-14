package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import org.awaitility.Awaitility;

/**
 * A product a test creates for itself in the shop the suite drives, through the staff API ({@code
 * POST /api/products}). The seeded products are shared by every test: an order one test places
 * lowers a stock the next test reads. A test that changes a product's stock, or needs a stock value
 * to be exact, uses a product of its own and reads only that one.
 *
 * <p>Only the suite's own shop mints a staff token; against a shop started elsewhere ({@code
 * -De2e.baseUrl}) the test that asks for an own product is skipped.
 *
 * @param id the product's id
 * @param name the product's name, as its card and its page show it
 */
public record OwnProduct(String id, String name) {

  private static final ObjectMapper JSON = new ObjectMapper();

  /**
   * Creates a product with the given name and stock and waits until the shop shows that stock.
   *
   * @param name a name no other test gives its product
   * @param stock the stock it is created with
   * @return the created product
   */
  public static OwnProduct stocked(final String name, final int stock) throws Exception {
    final String staffToken = staffToken();
    final String sku = "E2E-" + Long.toHexString(System.nanoTime()).toUpperCase(Locale.ROOT);
    final HttpResponse<String> created =
        send(
            HttpRequest.newBuilder(URI.create(ShopUnderTest.baseUrl() + "/api/products"))
                .header("Authorization", "Bearer " + staffToken)
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        """
                        {"sku":"%s","name":"%s","price":19.99,"category":"Desk & Office",\
                        "stock":%d}"""
                            .formatted(sku, name, stock))));
    assertEquals(201, created.statusCode(), "creating " + name + ": " + created.body());
    final OwnProduct product =
        new OwnProduct(JSON.readTree(created.body()).path("id").asText(), name);
    Awaitility.await()
        .atMost(Duration.ofSeconds(15))
        .untilAsserted(() -> assertEquals(stock, product.stockShown(), name + " shows its stock"));
    return product;
  }

  /**
   * The path of the product's page.
   *
   * @return the path, without the base URL
   */
  public String path() {
    return "/products/" + id;
  }

  private int stockShown() throws Exception {
    final HttpResponse<String> response =
        send(HttpRequest.newBuilder(URI.create(ShopUnderTest.baseUrl() + "/api" + path())).GET());
    return response.statusCode() == 200
        ? JSON.readTree(response.body()).path("stockQuantity").asInt(-1)
        : -1;
  }

  /** A staff token for a fresh account; the test is skipped where the suite cannot mint one. */
  private static String staffToken() throws Exception {
    final String email =
        "staff+" + Long.toHexString(System.nanoTime()).toLowerCase(Locale.ROOT) + "@example.com";
    final HttpResponse<String> registered =
        send(
            HttpRequest.newBuilder(URI.create(ShopUnderTest.baseUrl() + "/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        """
                        {"email":"%s","password":"Secret123","firstName":"Ada",\
                        "lastName":"Lovelace","dateOfBirth":"1815-12-10"}"""
                            .formatted(email))));
    assertEquals(201, registered.statusCode(), "registration: " + registered.body());
    final String token = JSON.readTree(registered.body()).path("token").asText();
    final JsonNode claims = JSON.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
    final Optional<String> staff =
        ShopUnderTest.staffTokenFor(claims.path("sub").asText(), claims.path("email").asText());
    assumeTrue(staff.isPresent(), "a staff token is minted only by the suite's own shop");
    return staff.get();
  }

  private static HttpResponse<String> send(final HttpRequest.Builder request) throws Exception {
    return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
  }
}
