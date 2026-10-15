package dev.domaincentric.sample.ecommerce.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ChangePriceCommand;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ChangePriceInputPort;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ProductPriceNotFoundException;
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
    properties =
        "spring.datasource.url=jdbc:h2:mem:update_price_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
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

    changePrice.execute(new ChangePriceCommand(ddd, new BigDecimal("0.01")));

    assertThat(shop.shownPriceOf(ddd)).isEqualTo("0.01 EUR");
  }

  @Test
  @DisplayName("A product Pricing holds no price for is refused by name")
  void aProductWithoutAPriceIsRefusedAsNotFound() {
    assertThatThrownBy(
            () ->
                changePrice.execute(
                    new ChangePriceCommand(
                        "00000000-0000-4000-8000-000000000001", new BigDecimal("49.99"))))
        .isInstanceOf(ProductPriceNotFoundException.class);
  }
}
