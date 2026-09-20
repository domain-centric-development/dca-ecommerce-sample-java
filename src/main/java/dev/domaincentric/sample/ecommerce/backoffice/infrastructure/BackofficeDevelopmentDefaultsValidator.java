package dev.domaincentric.sample.ecommerce.backoffice.infrastructure;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Refuses to start the shop with the operator credentials the sample ships for convenience.
 *
 * <p>The backoffice replays failed event publications, so its login is the most privileged door in
 * the shop, and {@code admin}/{@code admin} is written down in this repository. The check runs at
 * startup for the same reason as its twin on the token settings: a default that is merely
 * documented as dev-only is a default that ships.
 */
@Component
public class BackofficeDevelopmentDefaultsValidator implements InitializingBean {

  /**
   * The profiles that mean "this is a development run". The Account context states the same
   * expression for its own validator; the two are not shared, because a context does not reach into
   * another context's infrastructure to learn what development means. {@code
   * DevelopmentDefaultsTest} holds them to one answer.
   */
  public static final String DEVELOPMENT_PROFILES = "dev | inmemory";

  private static final String SESSION_COOKIE_SECURE = "server.servlet.session.cookie.secure";

  private final BackofficeSecurityProperties properties;
  private final Environment environment;

  public BackofficeDevelopmentDefaultsValidator(
      final BackofficeSecurityProperties properties, final Environment environment) {
    this.properties = properties;
    this.environment = environment;
  }

  @Override
  public void afterPropertiesSet() {
    if (environment.matchesProfiles(DEVELOPMENT_PROFILES)) {
      return;
    }

    if (BackofficeSecurityProperties.DEVELOPMENT_USERNAME.equals(properties.username())
        && BackofficeSecurityProperties.DEVELOPMENT_PASSWORD.equals(properties.password())) {
      throw new IllegalStateException(
          "The backoffice still uses the operator credentials committed to this repository. Set "
              + "BACKOFFICE_USERNAME and BACKOFFICE_PASSWORD, or run with the dev profile "
              + "(SPRING_PROFILES_ACTIVE=dev).");
    }

    // The operator session is the servlet container's, so its Secure flag is a container property
    // rather than one of ours — and its default is false. The .NET twin reads its own
    // Backoffice:SecureCookies here; the rule both state is the same one.
    if (!environment.getProperty(SESSION_COOKIE_SECURE, Boolean.class, false)) {
      throw new IllegalStateException(
          "The operator session cookie is not flagged Secure, so a browser sends it over plain "
              + "HTTP. Set "
              + SESSION_COOKIE_SECURE
              + "=true (SERVER_SERVLET_SESSION_COOKIE_SECURE=true), or run with the dev profile "
              + "(SPRING_PROFILES_ACTIVE=dev).");
    }
  }
}
