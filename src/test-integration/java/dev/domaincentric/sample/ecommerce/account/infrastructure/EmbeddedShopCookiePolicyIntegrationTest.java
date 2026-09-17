package dev.domaincentric.sample.ecommerce.account.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The shop framed by another <em>site</em> — a different domain, not merely another port. Only then
 * does the cookie policy have to be relaxed: every cookie the shop needs must be {@code
 * SameSite=None; Secure}, the identity, the session and the CSRF token alike, because a browser
 * withholds the others there and the request would arrive anonymous or without its form token.
 *
 * <p>Counterparts: {@link CookiePolicyIntegrationTest} is the unconfigured sample, {@link
 * HardenedShopCookiePolicyIntegrationTest} the hardened deployment. Same scenarios as the .NET
 * sample's {@code CookiePolicyTest}.
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
}
