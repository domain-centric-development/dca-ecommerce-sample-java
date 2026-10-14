package dev.domaincentric.sample.ecommerce.infrastructure.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Refuses to start the shop with the notice cookie key the sample ships for convenience (ADR-043).
 *
 * <p>A committed key is a published key: anyone reading this repository could read the cookie and
 * seal one that makes a page show a notice it was never sent. On a laptop that costs nothing; in
 * any other run the key comes from {@code NOTICE_COOKIE_KEY}, the same on every instance.
 */
@Component
public class NoticeCookieDevelopmentDefaultsValidator implements InitializingBean {

  /** The profiles that mean "this is a development run" — the same expression as the others. */
  public static final String DEVELOPMENT_PROFILES = "dev | inmemory";

  private final NoticeCookieProperties properties;
  private final Environment environment;

  public NoticeCookieDevelopmentDefaultsValidator(
      final NoticeCookieProperties properties, final Environment environment) {
    this.properties = properties;
    this.environment = environment;
  }

  @Override
  public void afterPropertiesSet() {
    if (environment.matchesProfiles(DEVELOPMENT_PROFILES)) {
      return;
    }
    if (NoticeCookieProperties.DEVELOPMENT_KEY.equals(properties.key())) {
      throw new IllegalStateException(
          "The notice cookie key is the one committed to this repository, so it is public and "
              + "anyone can forge the notice a page shows. Set NOTICE_COOKIE_KEY to Base64 of 32 "
              + "random bytes (`openssl rand -base64 32`), the same on every instance, or run with "
              + "the dev profile (SPRING_PROFILES_ACTIVE=dev).");
    }
  }
}
