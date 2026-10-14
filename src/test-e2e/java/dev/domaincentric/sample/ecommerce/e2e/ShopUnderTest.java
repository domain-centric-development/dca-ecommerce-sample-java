package dev.domaincentric.sample.ecommerce.e2e;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Role;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.Optional;
import java.util.Set;
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
   * A second shop, started in this process on a free port with settings of its own — for a scenario
   * about how the shop was started rather than what it does. Its own in-memory database, so it
   * shares no rows with the shop the suite drives; the caller closes it.
   *
   * @param database the name of its in-memory database
   * @param settings the settings it starts with, as {@code key=value}
   * @return the started shop
   */
  public static ConfigurableApplicationContext startedWith(
      final String database, final String... settings) {
    final String[] arguments = new String[settings.length + 2];
    arguments[0] = "--server.port=0";
    arguments[1] =
        "--spring.datasource.url=jdbc:h2:mem:"
            + database
            + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    for (int i = 0; i < settings.length; i++) {
      arguments[i + 2] = "--" + settings[i];
    }
    return SpringApplication.run(EcommerceSampleApplication.class, arguments);
  }

  /**
   * The address of a shop this suite started, reached as {@code localhost}.
   *
   * @param started the shop
   * @return its base URL
   */
  public static String baseUrlOf(final ConfigurableApplicationContext started) {
    return "http://localhost:" + started.getEnvironment().getProperty("local.server.port");
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

  /**
   * The delivery that fails once, for a test to publish an event whose first delivery fails — a
   * component of the suite's own shop, which scans this package. Empty when the suite drives a shop
   * started elsewhere: that shop carries no such listener.
   *
   * @return the failing-once delivery, or empty
   */
  public static synchronized Optional<FailingOnceDelivery> failingOnceDelivery() {
    baseUrl();
    return Optional.ofNullable(shop).map(started -> started.getBean(FailingOnceDelivery.class));
  }

  /**
   * The delivery that fails until it is fixed, for a test to publish an event whose deliveries fail
   * until the test fixes their cause — a component of the suite's own shop, which scans this
   * package. Empty when the suite drives a shop started elsewhere: that shop carries no such
   * listener.
   *
   * @return the failing-until-fixed delivery, or empty
   */
  public static synchronized Optional<FailingUntilFixedDelivery> failingUntilFixedDelivery() {
    baseUrl();
    return Optional.ofNullable(shop)
        .map(started -> started.getBean(FailingUntilFixedDelivery.class));
  }

  /**
   * The delivery to two listeners that fail twice and once, for a test to publish an event whose
   * listeners need a different number of attempts — a component of the suite's own shop, which
   * scans this package. Empty when the suite drives a shop started elsewhere: that shop carries no
   * such listeners.
   *
   * @return the two-listener delivery, or empty
   */
  public static synchronized Optional<TwoListenerDelivery> twoListenerDelivery() {
    baseUrl();
    return Optional.ofNullable(shop).map(started -> started.getBean(TwoListenerDelivery.class));
  }

  /**
   * A staff token for a registered account, minted by the suite's own shop the way an operator
   * provisioning tool would — no registration or login hands the role out. Empty when the suite
   * drives a shop started elsewhere: that shop's signing key is not the suite's.
   *
   * @param userId the account's user id
   * @param email the account's email address
   * @return the token, or empty
   */
  public static synchronized Optional<String> staffTokenFor(
      final String userId, final String email) {
    baseUrl();
    return Optional.ofNullable(shop)
        .map(
            started ->
                started
                    .getBean(TokenService.class)
                    .generateRegisteredToken(
                        UserId.of(userId), email, Set.of(Role.CUSTOMER, Role.STAFF)));
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
