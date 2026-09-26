package dev.domaincentric.sample.ecommerce.e2e;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The shop the browser suite drives: started once per test JVM, in this process, on a free port.
 *
 * <p>With {@code -De2e.baseUrl=<url>} the suite drives a shop started elsewhere instead (the
 * compose {@code e2e} service does that); without it, nothing has to run beforehand and every
 * dependency the shop needs in its default profile starts with it.
 */
public final class ShopUnderTest {

  private static ConfigurableApplicationContext shop;

  private ShopUnderTest() {}

  public static synchronized String baseUrl() {
    String given = System.getProperty("e2e.baseUrl", "");
    if (!given.isBlank()) {
      return given;
    }
    if (shop == null) {
      shop = SpringApplication.run(EcommerceSampleApplication.class, "--server.port=0");
      Runtime.getRuntime().addShutdownHook(new Thread(shop::close));
    }
    return "http://localhost:" + shop.getEnvironment().getProperty("local.server.port");
  }
}
