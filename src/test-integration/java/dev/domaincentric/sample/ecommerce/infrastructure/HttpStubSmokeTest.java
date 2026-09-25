package dev.domaincentric.sample.ecommerce.infrastructure;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * The stub an integration test puts in place of an external system: a real HTTP server on a free
 * port that answers what the test arranges.
 */
class HttpStubSmokeTest {

  @RegisterExtension
  static WireMockExtension externalSystem =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @Test
  void answersWhatTheTestArranged() throws Exception {
    externalSystem.stubFor(
        get("/status").willReturn(aResponse().withStatus(200).withBody("available")));

    HttpResponse<String> response =
        HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder(URI.create(externalSystem.baseUrl() + "/status")).build(),
                HttpResponse.BodyHandlers.ofString());

    assertEquals("available", response.body());
  }
}
