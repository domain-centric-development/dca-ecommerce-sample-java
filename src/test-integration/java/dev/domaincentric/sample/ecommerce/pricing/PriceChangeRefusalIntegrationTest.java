package dev.domaincentric.sample.ecommerce.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * A refused price change in the wired application, with the price change and the price store the
 * shop runs: the refusal is answered, and the product page still shows the price as it was.
 */
// A context of its own: a price change that slipped through must not reach any other test class.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:price_change_refusal_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("A refused price change leaves the price")
class PriceChangeRefusalIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private TokenService tokenService;

  private ShopRequests shop;
  private String ddd;

  @BeforeEach
  void findDomainDrivenDesignAtItsSeededPrice() throws Exception {
    shop = new ShopRequests(mockMvc, tokenService);
    ddd = shop.productIdOf("Domain-Driven Design");
    assertThat(shop.shownPriceOf(ddd)).as("the seeded price").isEqualTo("54.99 EUR");
  }

  @Test
  @DisplayName("A stranger cannot change a price")
  void aStrangersPriceChangeLeavesThePrice() throws Exception {
    final MvcResult result = shop.changePrice(ddd, "{\"price\":49.99}", null);

    assertThat(result.getResponse().getStatus()).isEqualTo(401);
    assertThat(shop.shownPriceOf(ddd)).isEqualTo("54.99 EUR");
  }

  @Test
  @DisplayName("A customer cannot change a price")
  void aCustomersPriceChangeLeavesThePrice() throws Exception {
    final MvcResult result = shop.changePrice(ddd, "{\"price\":49.99}", shop.customerToken());

    assertThat(result.getResponse().getStatus()).isEqualTo(403);
    assertThat(shop.shownPriceOf(ddd)).isEqualTo("54.99 EUR");
  }

  @Test
  @DisplayName("A new price of zero is refused")
  void aNewPriceOfZeroLeavesThePrice() throws Exception {
    final MvcResult result = shop.changePrice(ddd, "{\"price\":0}", shop.staffToken());

    assertThat(result.getResponse().getStatus()).isEqualTo(400);
    assertThat(shop.shownPriceOf(ddd)).isEqualTo("54.99 EUR");
  }
}
