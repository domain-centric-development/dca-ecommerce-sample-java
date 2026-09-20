package dev.domaincentric.sample.ecommerce.backoffice.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for backoffice admin authentication.
 *
 * <p>Configures the in-memory admin user for the backoffice. In production, replace with
 * OAuth2/Keycloak by swapping the {@link BackofficeSecurityConfiguration}.
 *
 * <p>The defaults below let the sample start without configuration. They are committed, so they are
 * known to everybody; {@link BackofficeDevelopmentDefaultsValidator} refuses them outside the
 * development profiles.
 *
 * @param username the admin username (default: {@value #DEVELOPMENT_USERNAME})
 * @param password the admin password (default: {@value #DEVELOPMENT_PASSWORD})
 */
@ConfigurationProperties(prefix = "app.security.backoffice")
public record BackofficeSecurityProperties(String username, String password) {

  /** The operator name the sample ships with. */
  public static final String DEVELOPMENT_USERNAME = "admin";

  /** The operator password the sample ships with. */
  public static final String DEVELOPMENT_PASSWORD = "admin";

  public BackofficeSecurityProperties {
    if (username == null || username.isBlank()) {
      username = DEVELOPMENT_USERNAME;
    }
    if (password == null || password.isBlank()) {
      password = DEVELOPMENT_PASSWORD;
    }
  }
}
