package dev.domaincentric.sample.ecommerce.account.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The deployment a real installation runs: framing refused, cookies marked {@code Secure}. The
 * sample leaves both open so it works embedded without configuration, and this is the switch back.
 *
 * <p>Same scenario as the .NET sample's {@code CookiePolicyTest.AHardenedShopRefusesToBeFramed}.
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.datasource.url=jdbc:h2:mem:cookie_policy_hardened;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
      "app.security.jwt.allow-framing=false",
      "app.security.jwt.secure-cookies=true"
    })
class HardenedShopCookiePolicyIntegrationTest extends CookiePolicyTestBase {

  @Test
  @DisplayName("Framing is refused and both cookies are marked Secure")
  void framingIsRefusedAndCookiesAreSecure() {
    assertThat(headerOfProductList("X-Frame-Options")).isEqualTo("SAMEORIGIN");

    final List<String> cookies = setCookieHeadersOfAPageWithAForm();
    assertThat(CookiePolicy.named(cookies, "shop-identity"))
        .isNotNull()
        .satisfies(
            cookie -> {
              assertThat(cookie.sameSite()).isEqualTo("Lax");
              assertThat(cookie.secure()).isTrue();
            });
    assertThat(CookiePolicy.named(cookies, "XSRF-TOKEN"))
        .isNotNull()
        .satisfies(cookie -> assertThat(cookie.secure()).isTrue());
  }
}
