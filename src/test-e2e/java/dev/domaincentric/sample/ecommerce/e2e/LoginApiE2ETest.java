package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * A partner application logs in to an account through the running shop's JSON API and receives the
 * token it acts with, or a problem document when the login is refused. No browser: the actor is an
 * application.
 */
@DisplayName("Login API E2E Tests")
class LoginApiE2ETest {

  /** The password every account of this class registers with. */
  private static final String PASSWORD = "Secret123";

  private final HttpClient client = HttpClient.newHttpClient();

  @Test
  @DisplayName("Logging in hands out a token")
  void loggingInWithTheRightPasswordAnswersOkWithAToken() throws Exception {
    final String email = freshAddress();
    final HttpResponse<String> registration = register(email, PASSWORD);
    assertEquals(201, registration.statusCode(), registration.body());

    final HttpResponse<String> response =
        post(
            "/api/auth/login",
            """
            {"email":"%s","password":"%s"}"""
                .formatted(email, PASSWORD));

    assertEquals(200, response.statusCode(), response.body());
    final JsonNode body = new ObjectMapper().readTree(response.body());
    assertTrue(body.path("success").asBoolean(), "success is true");
    assertFalse(body.path("token").asText().isBlank(), "carries a token");
    assertEquals(email, body.path("email").asText());
  }

  @Test
  @DisplayName("A wrong password is refused")
  void aWrongPasswordIsRefusedWithAProblemDocumentAndABearerChallenge() throws Exception {
    final String email = freshAddress();
    final HttpResponse<String> registration = register(email, PASSWORD);
    assertEquals(201, registration.statusCode(), registration.body());

    final HttpResponse<String> response =
        post(
            "/api/auth/login",
            """
            {"email":"%s","password":"Wrong1234"}"""
                .formatted(email));

    assertEquals(401, response.statusCode(), response.body());
    final String contentType = response.headers().firstValue("Content-Type").orElse("");
    assertTrue(
        contentType.startsWith("application/problem+json"),
        "the refusal is a problem document, not " + contentType);
    assertEquals("Bearer", response.headers().firstValue("WWW-Authenticate").orElse(""));
    final JsonNode problem = new ObjectMapper().readTree(response.body());
    assertEquals("Authentication failed", problem.path("title").asText());
    assertFalse(problem.has("detail"), "the problem carries no detail: " + response.body());
  }

  /**
   * An address no account holds yet: a shop started elsewhere may keep accounts from an earlier
   * run, so the address is new each time.
   */
  private static String freshAddress() {
    return "ada+" + Long.toHexString(System.nanoTime()).toLowerCase(Locale.ROOT) + "@example.com";
  }

  /**
   * Registers an account through the API.
   *
   * @param email the account's address
   * @param password the account's password
   * @return the registration's answer
   */
  private HttpResponse<String> register(final String email, final String password)
      throws Exception {
    return post(
        "/api/auth/register",
        """
        {"email":"%s","password":"%s","firstName":"Ada",\
        "lastName":"Lovelace","dateOfBirth":"1815-12-10"}"""
            .formatted(email, password));
  }

  private HttpResponse<String> post(final String path, final String json) throws Exception {
    return client.send(
        HttpRequest.newBuilder(URI.create(ShopUnderTest.baseUrl() + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build(),
        HttpResponse.BodyHandlers.ofString());
  }
}
