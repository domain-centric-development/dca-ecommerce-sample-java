package dev.domaincentric.sample.ecommerce.e2e;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The shop the browser suite drives: started once per test JVM, in this process, on a free port.
 *
 * <p>With {@code -De2e.baseUrl=<url>} the suite drives a shop started elsewhere instead (the
 * compose {@code e2e} service does that); without it, nothing has to run beforehand and every
 * dependency the shop needs in its default profile starts with it — the payment provider included:
 * a stub on a free port that authorizes every payment until a test arranges another answer.
 */
public final class ShopUnderTest {

  private static ConfigurableApplicationContext shop;
  private static WireMockServer paymentProvider;

  private ShopUnderTest() {}

  public static synchronized String baseUrl() {
    String given = System.getProperty("e2e.baseUrl", "");
    if (!given.isBlank()) {
      return given;
    }
    if (shop == null) {
      paymentProvider = authorizingPaymentProvider();
      shop =
          SpringApplication.run(
              EcommerceSampleApplication.class,
              "--server.port=0",
              "--checkout.payment-provider.base-url=" + paymentProvider.baseUrl());
      Runtime.getRuntime().addShutdownHook(new Thread(shop::close));
      Runtime.getRuntime().addShutdownHook(new Thread(paymentProvider::stop));
    }
    return "http://localhost:" + shop.getEnvironment().getProperty("local.server.port");
  }

  /**
   * The payment provider the shop pays through, for a test to arrange its answers and count its
   * requests. Empty when the suite drives a shop started elsewhere: that shop's provider is not the
   * suite's to control.
   *
   * @return the provider stub, or empty
   */
  public static synchronized Optional<WireMockServer> paymentProvider() {
    baseUrl();
    return Optional.ofNullable(paymentProvider);
  }

  private static WireMockServer authorizingPaymentProvider() {
    WireMockServer provider = new WireMockServer(wireMockConfig().dynamicPort());
    provider.start();
    provider.stubFor(post("/payments").willReturn(authorized()));
    return provider;
  }

  /**
   * The provider's answer to a payment it authorizes: {@code 201} with a payment reference.
   *
   * @return the answer, for a test to arrange again
   */
  public static ResponseDefinitionBuilder authorized() {
    return aResponse()
        .withStatus(201)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"reference\": \"pay-e2e\"}");
  }
}
