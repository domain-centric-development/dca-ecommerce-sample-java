package dev.domaincentric.sample.ecommerce.account.infrastructure;

import dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security.JwtProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Refuses to start the shop with the token and cookie settings the sample ships for convenience.
 *
 * <p>A committed secret is a published secret: anyone reading this repository can mint a token this
 * shop accepts. Cookies without the {@code Secure} flag travel over plain HTTP, where the token is
 * readable in transit. Both are fine while the shop runs on a laptop and unacceptable anywhere
 * else, and neither announces itself — the application starts and behaves normally.
 *
 * <p>So the check is made at startup, against the profile rather than against a hostname: a
 * deployment that names its own profile says it is not the laptop, and from that moment the
 * development values are refused rather than merely discouraged. The message names the environment
 * variable that supplies a real value, because a fail-fast the operator cannot act on is only an
 * outage.
 */
@Component
public class JwtDevelopmentDefaultsValidator implements InitializingBean {

  /**
   * The profiles that mean "this is a development run". Both samples word this rule the same way:
   * the shop is production unless it says otherwise (the .NET twin reads {@code
   * ASPNETCORE_ENVIRONMENT} for it).
   */
  public static final String DEVELOPMENT_PROFILES = "dev | inmemory";

  private final JwtProperties properties;
  private final Environment environment;

  public JwtDevelopmentDefaultsValidator(
      final JwtProperties properties, final Environment environment) {
    this.properties = properties;
    this.environment = environment;
  }

  @Override
  public void afterPropertiesSet() {
    if (environment.matchesProfiles(DEVELOPMENT_PROFILES)) {
      return;
    }

    if (JwtProperties.DEVELOPMENT_SECRET.equals(properties.secret())) {
      throw new IllegalStateException(
          "The JWT signing secret is the one committed to this repository, so it is public and "
              + "anyone can mint a token this shop accepts. Set JWT_SECRET to a secret of at "
              + "least 32 characters, or run with the dev profile (SPRING_PROFILES_ACTIVE=dev).");
    }

    if (!properties.secureCookies()) {
      throw new IllegalStateException(
          "Identity and session cookies are not flagged Secure, so a browser sends them over "
              + "plain HTTP. Set JWT_SECURE_COOKIES=true, or run with the dev profile "
              + "(SPRING_PROFILES_ACTIVE=dev).");
    }
  }
}
