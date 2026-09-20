package dev.domaincentric.sample.ecommerce.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security.JwtProperties;
import dev.domaincentric.sample.ecommerce.account.infrastructure.JwtDevelopmentDefaultsValidator;
import dev.domaincentric.sample.ecommerce.backoffice.infrastructure.BackofficeDevelopmentDefaultsValidator;
import dev.domaincentric.sample.ecommerce.backoffice.infrastructure.BackofficeSecurityProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

/**
 * The values this repository ships so the shop starts without configuration, and the guard that
 * keeps them where they belong.
 */
class DevelopmentDefaultsTest {

  private static final String OWN_SECRET = "a-secret-nobody-else-has-and-long-enough-for-hs256";

  @Test
  @DisplayName("both contexts mean the same thing by 'development'")
  void profileExpressionsAgree() {
    assertEquals(
        JwtDevelopmentDefaultsValidator.DEVELOPMENT_PROFILES,
        BackofficeDevelopmentDefaultsValidator.DEVELOPMENT_PROFILES,
        "Two contexts deciding differently what a development run is would let one of them accept"
            + " a committed default where the other refuses it");
  }

  @Test
  @DisplayName("the committed defaults in application.yml are the ones the guard knows")
  void configurationMatchesTheConstants() throws Exception {
    final String yaml = Files.readString(Path.of("src/main/resources/application.yml"));

    assertTrue(
        yaml.contains("${JWT_SECRET:" + JwtProperties.DEVELOPMENT_SECRET + "}"),
        "The fallback secret in application.yml must be JwtProperties.DEVELOPMENT_SECRET, or the"
            + " guard refuses a value nobody configured and passes the one that is actually used");
    assertTrue(
        yaml.contains(
            "${BACKOFFICE_USERNAME:" + BackofficeSecurityProperties.DEVELOPMENT_USERNAME + "}"),
        "The fallback operator name in application.yml must be the constant the guard compares to");
    assertTrue(
        yaml.contains(
            "${BACKOFFICE_PASSWORD:" + BackofficeSecurityProperties.DEVELOPMENT_PASSWORD + "}"),
        "The fallback operator password in application.yml must be the constant the guard compares"
            + " to");
  }

  @Nested
  @DisplayName("outside a development profile")
  class Hardened {

    private final MockEnvironment environment =
        new MockEnvironment().withProperty("server.servlet.session.cookie.secure", "true");

    @Test
    @DisplayName("the committed signing secret is refused")
    void refusesTheCommittedSecret() {
      final var failure =
          assertThrows(
              IllegalStateException.class,
              () -> validator(JwtProperties.DEVELOPMENT_SECRET, true).afterPropertiesSet());

      assertTrue(failure.getMessage().contains("JWT_SECRET"), failure.getMessage());
    }

    @Test
    @DisplayName("cookies that are not Secure are refused")
    void refusesInsecureCookies() {
      final var failure =
          assertThrows(
              IllegalStateException.class, () -> validator(OWN_SECRET, false).afterPropertiesSet());

      assertTrue(failure.getMessage().contains("JWT_SECURE_COOKIES"), failure.getMessage());
    }

    @Test
    @DisplayName("the committed operator credentials are refused")
    void refusesTheCommittedOperator() {
      final var failure =
          assertThrows(
              IllegalStateException.class,
              () ->
                  new BackofficeDevelopmentDefaultsValidator(
                          new BackofficeSecurityProperties(null, null), environment)
                      .afterPropertiesSet());

      assertTrue(failure.getMessage().contains("BACKOFFICE_PASSWORD"), failure.getMessage());
    }

    @Test
    @DisplayName("an operator session cookie that is not Secure is refused")
    void refusesInsecureOperatorSession() {
      final var failure =
          assertThrows(
              IllegalStateException.class,
              () ->
                  new BackofficeDevelopmentDefaultsValidator(
                          new BackofficeSecurityProperties("operator", "not-the-committed-one"),
                          new MockEnvironment())
                      .afterPropertiesSet());

      assertTrue(
          failure.getMessage().contains("SERVER_SERVLET_SESSION_COOKIE_SECURE"),
          failure.getMessage());
    }

    @Test
    @DisplayName("configured values start the shop")
    void acceptsConfiguredValues() {
      assertDoesNotThrow(() -> validator(OWN_SECRET, true).afterPropertiesSet());
      assertDoesNotThrow(
          () ->
              new BackofficeDevelopmentDefaultsValidator(
                      new BackofficeSecurityProperties("operator", "not-the-committed-one"),
                      environment)
                  .afterPropertiesSet());
    }
  }

  @Nested
  @DisplayName("in a development profile")
  class Development {

    @Test
    @DisplayName("the shipped values are what the sample is for")
    void acceptsTheShippedValues() {
      for (final String profile : new String[] {"dev", "inmemory"}) {
        final var environment = new MockEnvironment();
        environment.setActiveProfiles(profile);

        assertDoesNotThrow(
            () ->
                new JwtDevelopmentDefaultsValidator(
                        jwtProperties(JwtProperties.DEVELOPMENT_SECRET, false), environment)
                    .afterPropertiesSet(),
            profile);
        assertDoesNotThrow(
            () ->
                new BackofficeDevelopmentDefaultsValidator(
                        new BackofficeSecurityProperties(null, null), environment)
                    .afterPropertiesSet(),
            profile);
      }
    }
  }

  private JwtDevelopmentDefaultsValidator validator(final String secret, final boolean secure) {
    return new JwtDevelopmentDefaultsValidator(
        jwtProperties(secret, secure), new MockEnvironment());
  }

  private static JwtProperties jwtProperties(final String secret, final boolean secureCookies) {
    return new JwtProperties(
        secret,
        30,
        7,
        "dca-ecommerce-sample",
        "shop-identity",
        "shop-session",
        secureCookies,
        "Lax",
        true);
  }
}
