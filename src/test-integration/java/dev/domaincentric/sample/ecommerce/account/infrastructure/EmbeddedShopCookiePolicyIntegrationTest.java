package dev.domaincentric.sample.ecommerce.account.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The embedded deployment: the shop runs inside an iframe on another origin. Every cookie the shop
 * needs there must be {@code SameSite=None; Secure} — the identity, the session and the CSRF token
 * alike, because a browser withholds the others in a foreign frame and the request would arrive
 * anonymous or without its form token. Framing is allowed in the same breath.
 *
 * <p>Counterpart: {@link CookiePolicyIntegrationTest} covers the normal deployment. Same two
 * scenarios as the .NET sample's {@code EmbeddedShopCookiePolicyTest}.
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.datasource.url=jdbc:h2:mem:cookie_policy_embedded;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
      "app.security.jwt.same-site=None",
      "app.security.jwt.secure-cookies=true"
    })
class EmbeddedShopCookiePolicyIntegrationTest extends CookiePolicyTestBase {

  @Test
  @DisplayName("Identity and CSRF cookies are SameSite=None and Secure")
  void everyCookieTravelsIntoAForeignFrame() {
    final List<String> cookies = setCookieHeadersOfAPageWithAForm();

    assertThat(CookiePolicy.named(cookies, "shop-identity"))
        .as("identity cookie")
        .isNotNull()
        .satisfies(
            cookie -> {
              assertThat(cookie.sameSite()).isEqualTo("None");
              assertThat(cookie.secure()).isTrue();
            });

    assertThat(CookiePolicy.named(cookies, "XSRF-TOKEN"))
        .as("CSRF cookie — withheld in a foreign frame unless it travels cross-site too")
        .isNotNull()
        .satisfies(
            cookie -> {
              assertThat(cookie.sameSite()).isEqualTo("None");
              assertThat(cookie.secure()).isTrue();
            });
  }

  @Test
  @DisplayName("A page may be framed by another origin")
  void framingIsAllowed() {
    assertThat(headerOfProductList("X-Frame-Options")).isNull();
  }
}
