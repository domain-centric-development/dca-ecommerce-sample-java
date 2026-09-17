package dev.domaincentric.sample.ecommerce.account.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The cookie and framing policy a normal deployment runs with: cookies are {@code SameSite=Lax} and
 * the shop refuses to be framed by another origin.
 *
 * <p>Counterpart: {@link EmbeddedShopCookiePolicyIntegrationTest} covers the embedded deployment.
 * Same two scenarios as the .NET sample's {@code CookiePolicyTest}.
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties =
        "spring.datasource.url=jdbc:h2:mem:cookie_policy_default;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
class CookiePolicyIntegrationTest extends CookiePolicyTestBase {

  @Test
  @DisplayName("Identity and CSRF cookies are SameSite=Lax and carry no Secure flag over HTTP")
  void cookiesAreLaxByDefault() {
    final List<String> cookies = setCookieHeadersOfAPageWithAForm();

    assertThat(CookiePolicy.named(cookies, "shop-identity"))
        .as("identity cookie")
        .isNotNull()
        .satisfies(
            cookie -> {
              assertThat(cookie.sameSite()).isEqualTo("Lax");
              assertThat(cookie.secure()).isFalse();
              assertThat(cookie.httpOnly()).isTrue();
            });

    assertThat(CookiePolicy.named(cookies, "XSRF-TOKEN"))
        .as("CSRF cookie — the form token travels with the request that carries it")
        .isNotNull()
        .satisfies(
            cookie -> {
              assertThat(cookie.sameSite()).isEqualTo("Lax");
              assertThat(cookie.secure()).isFalse();
            });
  }

  @Test
  @DisplayName("A page refuses to be framed by another origin")
  void framingIsRefused() {
    assertThat(headerOfProductList("X-Frame-Options")).isEqualTo("SAMEORIGIN");
  }
}
