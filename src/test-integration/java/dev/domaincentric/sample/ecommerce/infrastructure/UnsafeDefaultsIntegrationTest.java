package dev.domaincentric.sample.ecommerce.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The guards of {@code DevelopmentDefaultsTest} seen from outside: a shop that is not told it is a
 * development run does not start on the values this repository ships.
 *
 * <p>The unit test pins what the rule says; this one pins that the rule is wired — a validator
 * nobody registers refuses nothing, and that difference is invisible in the class itself.
 */
class UnsafeDefaultsIntegrationTest {

  private static final String HARDENED = "--spring.profiles.active=production-like";
  private static final String OWN_SECRET =
      "--app.security.jwt.secret=a-secret-nobody-else-has-and-long-enough";
  private static final String SECURE_COOKIES = "--app.security.jwt.secure-cookies=true";

  @Test
  @DisplayName("the committed signing secret does not start a shop that is not in development")
  void refusesTheCommittedSecret() {
    final var failure = assertThrows(Exception.class, () -> start(HARDENED));

    assertTrue(
        message(failure).contains("JWT_SECRET"),
        "Expected the startup failure to name the variable that fixes it, but got: "
            + message(failure));
  }

  @Test
  @DisplayName("cookies that are not Secure do not start it either")
  void refusesInsecureCookies() {
    final var failure = assertThrows(Exception.class, () -> start(HARDENED, OWN_SECRET));

    assertTrue(message(failure).contains("JWT_SECURE_COOKIES"), message(failure));
  }

  @Test
  @DisplayName("nor do the committed operator credentials")
  void refusesTheCommittedOperator() {
    final var failure =
        assertThrows(Exception.class, () -> start(HARDENED, OWN_SECRET, SECURE_COOKIES));

    assertTrue(message(failure).contains("BACKOFFICE_PASSWORD"), message(failure));
  }

  @Test
  @DisplayName("a configured shop starts")
  void startsWhenEverythingIsConfigured() {
    try (var context =
        start(
            HARDENED,
            OWN_SECRET,
            SECURE_COOKIES,
            "--app.security.backoffice.username=operator",
            "--app.security.backoffice.password=not-the-committed-one",
            "--server.servlet.session.cookie.secure=true")) {
      assertTrue(context.isRunning());
    }
  }

  @Test
  @DisplayName("a development run starts on the values the sample ships")
  void startsInDevelopmentOnTheShippedValues() {
    try (var context = start("--spring.profiles.active=dev")) {
      assertEquals(JwtProperties.DEVELOPMENT_SECRET, context.getBean(JwtProperties.class).secret());
    }
  }

  private static ConfigurableApplicationContext start(final String... arguments) {
    final var application = new SpringApplication(EcommerceSampleApplication.class);
    final var all = new String[arguments.length + 1];
    all[0] = "--server.port=0";
    System.arraycopy(arguments, 0, all, 1, arguments.length);
    return application.run(all);
  }

  private static String message(final Throwable failure) {
    Throwable cause = failure;
    final var text = new StringBuilder();
    while (cause != null) {
      text.append(cause.getMessage()).append('\n');
      cause = cause.getCause();
    }
    return text.toString();
  }
}
