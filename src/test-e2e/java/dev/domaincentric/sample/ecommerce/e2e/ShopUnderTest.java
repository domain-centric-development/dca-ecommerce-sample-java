package dev.domaincentric.sample.ecommerce.e2e;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Role;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
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
 * a stub on a free port that authorizes every payment until a test arranges another answer — and
 * the mail provider: a stub on a free port that accepts every mail.
 */
public final class ShopUnderTest {

  private static ConfigurableApplicationContext shop;
  private static WireMockServer paymentProvider;
  private static WireMockServer mailProvider;

  private ShopUnderTest() {}

  public static synchronized String baseUrl() {
    String given = System.getProperty("e2e.baseUrl", "");
    if (!given.isBlank()) {
      return given;
    }
    if (shop == null) {
      paymentProvider = authorizingPaymentProvider();
      mailProvider = acceptingMailProvider();
      shop =
          SpringApplication.run(
              EcommerceSampleApplication.class,
              "--server.port=0",
              "--checkout.payment-provider.base-url=" + paymentProvider.baseUrl(),
              "--checkout.mail-provider.base-url=" + mailProvider.baseUrl());
      Runtime.getRuntime().addShutdownHook(new Thread(shop::close));
      Runtime.getRuntime().addShutdownHook(new Thread(paymentProvider::stop));
      Runtime.getRuntime().addShutdownHook(new Thread(mailProvider::stop));
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
   * The mail provider the shop sends its mails through, for a test to read the mails it received.
   * Empty when the suite drives a shop started elsewhere: that shop's provider is not the suite's.
   *
   * @return the provider stub, or empty
   */
  public static synchronized Optional<WireMockServer> mailProvider() {
    baseUrl();
    return Optional.ofNullable(mailProvider);
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
   * The delivery held until released, for a test to publish an event whose delivery is still under
   * way while it looks at the log — a component of the suite's own shop, which scans this package.
   * Empty when the suite drives a shop started elsewhere: that shop carries no such listener.
   *
   * @return the held delivery, or empty
   */
  public static synchronized Optional<HeldDelivery> heldDelivery() {
    baseUrl();
    return Optional.ofNullable(shop).map(started -> started.getBean(HeldDelivery.class));
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

  /**
   * A staff token for a registered account, minted by a shop this suite started with {@link
   * #startedWith} — no registration or login hands the role out.
   *
   * @param started the shop
   * @param userId the account's user id
   * @param email the account's email address
   * @return the token
   */
  public static String staffTokenIn(
      final ConfigurableApplicationContext started, final String userId, final String email) {
    return started
        .getBean(TokenService.class)
        .generateRegisteredToken(UserId.of(userId), email, Set.of(Role.CUSTOMER, Role.STAFF));
  }

  /**
   * A staff token from a shop this suite started with {@link #startedWith}: an account registered
   * through its API, its token minted again with the staff role by that shop's own token service —
   * no registration or login hands the role out.
   *
   * @param started the shop
   * @return the token
   * @throws Exception when the registration cannot be sent or read
   */
  public static String staffTokenOf(final ConfigurableApplicationContext started) throws Exception {
    final HttpResponse<String> registered =
        HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder(URI.create(baseUrlOf(started) + "/api/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            """
                            {"email":"staff@example.com","password":"Secret123",\
                            "firstName":"Ada","lastName":"Lovelace","dateOfBirth":"1815-12-10"}"""))
                    .build(),
                HttpResponse.BodyHandlers.ofString());
    if (registered.statusCode() != 201) {
      throw new IllegalStateException("registration answered " + registered.body());
    }
    final ObjectMapper json = new ObjectMapper();
    final String token = json.readTree(registered.body()).path("token").asText();
    final JsonNode claims = json.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
    return started
        .getBean(TokenService.class)
        .generateRegisteredToken(
            UserId.of(claims.path("sub").asText()),
            claims.path("email").asText(),
            Set.of(Role.CUSTOMER, Role.STAFF));
  }

  /**
   * The id of the catalogue's product with this name in a shop this suite started with {@link
   * #startedWith}, as its API lists it.
   *
   * @param started the shop
   * @param name the product's name
   * @return the product id
   * @throws Exception when the catalogue cannot be read
   */
  public static String productIdIn(final ConfigurableApplicationContext started, final String name)
      throws Exception {
    return productIdOf(baseUrlOf(started), name);
  }

  /**
   * The id of a shop's catalogue product with this name, as its API lists it.
   *
   * @param shopUrl the shop's base URL
   * @param name the product's name
   * @return the product's id
   * @throws Exception when the catalogue cannot be read
   */
  public static String productIdOf(final String shopUrl, final String name) throws Exception {
    final HttpResponse<String> catalogue =
        HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder(URI.create(shopUrl + "/api/products")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    for (final JsonNode product : new ObjectMapper().readTree(catalogue.body())) {
      if (name.equals(product.path("name").asText())) {
        return product.path("id").asText();
      }
    }
    throw new AssertionError("no product named " + name);
  }

  /**
   * Sets a product's price through the staff API of a shop.
   *
   * @param shopUrl the shop's base URL
   * @param staffToken a token carrying the staff role
   * @param productId the product's id
   * @param price the new price, e.g. {@code "49.99"}
   * @return the status the API answered
   * @throws Exception when the request cannot be sent
   */
  public static int changePrice(
      final String shopUrl, final String staffToken, final String productId, final String price)
      throws Exception {
    return HttpClient.newHttpClient()
        .send(
            HttpRequest.newBuilder(URI.create(shopUrl + "/api/products/" + productId + "/price"))
                .header("Authorization", "Bearer " + staffToken)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("{\"price\":" + price + "}"))
                .build(),
            HttpResponse.BodyHandlers.ofString())
        .statusCode();
  }

  private static WireMockServer acceptingMailProvider() {
    WireMockServer provider = new WireMockServer(wireMockConfig().dynamicPort());
    provider.start();
    provider.stubFor(post("/messages").willReturn(aResponse().withStatus(202)));
    return provider;
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
