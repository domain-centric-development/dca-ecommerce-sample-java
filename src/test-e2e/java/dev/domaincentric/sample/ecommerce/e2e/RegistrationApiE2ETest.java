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
 * A partner application registers an account through the running shop's JSON API and receives the
 * token it acts with. No browser: the actor is an application.
 */
@DisplayName("Registration API E2E Tests")
class RegistrationApiE2ETest {

  @Test
  @DisplayName("Registering hands out a token")
  void registeringAnAccountAnswersCreatedWithItsToken() throws Exception {
    // A shop started elsewhere may keep accounts from an earlier run: the address is new each time.
    final String email =
        "ada+" + Long.toHexString(System.nanoTime()).toLowerCase(Locale.ROOT) + "@example.com";

    final HttpResponse<String> response =
        HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder(URI.create(ShopUnderTest.baseUrl() + "/api/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            """
                            {"email":"%s","password":"Secret123","firstName":"Ada",\
                            "lastName":"Lovelace","dateOfBirth":"1815-12-10"}"""
                                .formatted(email)))
                    .build(),
                HttpResponse.BodyHandlers.ofString());

    assertEquals(201, response.statusCode(), response.body());
    final JsonNode body = new ObjectMapper().readTree(response.body());
    assertTrue(body.path("success").asBoolean(), "success is true");
    assertFalse(body.path("token").asText().isBlank(), "carries a token");
    assertEquals(email, body.path("email").asText());
  }
}
