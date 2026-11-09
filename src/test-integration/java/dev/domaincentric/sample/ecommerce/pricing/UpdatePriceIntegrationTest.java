package dev.domaincentric.sample.ecommerce.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ChangePriceCommand;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ChangePriceInputPort;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ChangePriceResult;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * A product's price changed through the price change's input port in the wired application, with
 * the price store the shop runs, and read back from the product page a visitor opens.
 */
// A context of its own: the changed price must not reach the classes that read the seeded one.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties = "test.database=update_price_test")
@AutoConfigureMockMvc
@DisplayName("A product's price changes")
class UpdatePriceIntegrationTest {

  @Autowired private ChangePriceInputPort changePrice;
  @Autowired private MockMvc mockMvc;
  @Autowired private TokenService tokenService;

  @Test
  @DisplayName("A new price of one cent is accepted")
  void aNewPriceOfOneCentIsThePriceTheProductPageShows() throws Exception {
    final ShopRequests shop = new ShopRequests(mockMvc, tokenService);
    final String ddd = shop.productIdOf("Domain-Driven Design");

    assertThat(changePrice.execute(new ChangePriceCommand(ddd, new BigDecimal("0.01"))))
        .isInstanceOf(ChangePriceResult.Changed.class);

    assertThat(shop.shownPriceOf(ddd)).isEqualTo("0.01 EUR");
  }

  @Test
  @DisplayName("A product Pricing holds no price for is refused by name")
  void aProductWithoutAPriceIsRefusedAsNotFound() {
    assertThat(
            changePrice.execute(
                new ChangePriceCommand(
                    "00000000-0000-4000-8000-000000000001", new BigDecimal("49.99"))))
        .asInstanceOf(type(ChangePriceResult.ProductPriceNotFound.class))
        .extracting(refusal -> refusal.productId().value())
        .isEqualTo("00000000-0000-4000-8000-000000000001");
  }
}
