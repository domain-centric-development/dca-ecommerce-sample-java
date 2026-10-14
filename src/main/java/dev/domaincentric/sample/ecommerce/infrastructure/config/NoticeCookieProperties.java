package dev.domaincentric.sample.ecommerce.infrastructure.config;

import java.util.Base64;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The settings of the cookie that carries a redirect's notice to the next page ({@code
 * app.web.notice-cookie}).
 *
 * @param key the AES key every instance seals the cookie with, Base64 of 32 bytes; {@code
 *     NOTICE_COOKIE_KEY} in the environment, {@link #DEVELOPMENT_KEY} on a laptop
 * @param secure whether the cookie is flagged {@code Secure}
 * @param sameSite the cookie's {@code SameSite} attribute
 */
@ConfigurationProperties("app.web.notice-cookie")
public record NoticeCookieProperties(
    @Nullable String key, boolean secure, @Nullable String sameSite) {

  /**
   * The key {@code application.yml} falls back to. It is committed, therefore public: anyone can
   * read and forge a cookie sealed with it. {@link NoticeCookieDevelopmentDefaultsValidator}
   * refuses it outside the development profiles.
   */
  public static final String DEVELOPMENT_KEY = "hQ6wT6HZ3Kr+ilEKaTUAaF/3Be+MqOdGyTlUYcnPImQ=";

  public NoticeCookieProperties {
    key = key == null || key.isBlank() ? DEVELOPMENT_KEY : key;
    sameSite = sameSite == null || sameSite.isBlank() ? "Lax" : sameSite;
  }

  /**
   * The key's bytes.
   *
   * @return the decoded key
   * @throws IllegalStateException if the key is not Base64 of 32 bytes
   */
  public byte[] keyBytes() {
    final byte[] bytes;
    try {
      bytes = Base64.getDecoder().decode(key);
    } catch (final IllegalArgumentException e) {
      throw new IllegalStateException(
          "NOTICE_COOKIE_KEY is not Base64. Generate one with `openssl rand -base64 32`.", e);
    }
    if (bytes.length != 32) {
      throw new IllegalStateException(
          "NOTICE_COOKIE_KEY must be Base64 of 32 bytes, it has "
              + bytes.length
              + ". Generate one with `openssl rand -base64 32`.");
    }
    return bytes;
  }
}
