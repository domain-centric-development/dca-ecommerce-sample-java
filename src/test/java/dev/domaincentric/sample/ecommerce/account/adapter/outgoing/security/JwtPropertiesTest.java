package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The cookie policy refuses a combination a browser would silently drop. */
@DisplayName("JwtProperties")
class JwtPropertiesTest {

  private static final String SECRET = "a-test-secret-that-is-long-enough-for-hs256";

  @Test
  @DisplayName("SameSite=None without Secure cookies is refused rather than sent to the browser")
  void refusesNoneWithoutSecure() {
    assertThatThrownBy(() -> properties("None", false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("SameSite=None requires Secure cookies");
  }

  @Test
  @DisplayName("SameSite=None with Secure cookies is the embedded deployment and is accepted")
  void acceptsNoneWithSecure() {
    assertThat(properties("None", true).sameSite()).isEqualTo("None");
  }

  @Test
  @DisplayName("An unset policy falls back to Lax, the value a normal deployment runs with")
  void defaultsToLax() {
    assertThat(properties(null, false).sameSite()).isEqualTo(JwtProperties.DEFAULT_SAME_SITE);
    assertThat(properties("  ", false).sameSite()).isEqualTo("Lax");
  }

  private static JwtProperties properties(final String sameSite, final boolean secureCookies) {
    return new JwtProperties(
        SECRET,
        30,
        7,
        "test-issuer",
        JwtProperties.DEFAULT_COOKIE_NAME,
        JwtProperties.DEFAULT_SESSION_COOKIE_NAME,
        secureCookies,
        sameSite,
        false);
  }
}
