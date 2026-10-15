package dev.domaincentric.sample.ecommerce.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ChangePriceCommand;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ChangePriceInputPort;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ChangePriceResult;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ProductPriceNotFoundException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * How the staff API's price address translates a request into a price change and each refusal into
 * an answer, against a price change the test records: who may send it, what body and what address
 * it accepts, and what it answers when Pricing holds no price for the product. What the refusals
 * leave stored is {@link PriceChangeRefusalIntegrationTest}'s.
 */
// A context of its own: the recorded price change must not reach any other test class.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:price_resource_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@Import(PriceResourceIntegrationTest.PriceChangeRecordedByTheTest.class)
@DisplayName("Price address of the staff API")
class PriceResourceIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String UNKNOWN_PRODUCT = "00000000-0000-4000-8000-000000000001";

  @Autowired private MockMvc mockMvc;
  @Autowired private TokenService tokenService;
  @Autowired private RecordedPriceChanges priceChanges;

  private ShopRequests shop;
  private String ddd;

  @BeforeEach
  void findDomainDrivenDesign() throws Exception {
    priceChanges.clear();
    shop = new ShopRequests(mockMvc, tokenService);
    ddd = shop.productIdOf("Domain-Driven Design");
  }

  @Test
  @DisplayName("A stranger cannot change a price")
  void aClientWithoutATokenIsChallengedWithoutAskingForAChange() throws Exception {
    final MvcResult result = shop.changePrice(ddd, "{\"price\":49.99}", null);

    assertThat(result.getResponse().getStatus()).isEqualTo(401);
    assertThat(result.getResponse().getHeader(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");
    assertThat(priceChanges.commands()).as("price changes asked for").isEmpty();
  }

  @Test
  @DisplayName("A customer cannot change a price")
  void aClientWithACustomerTokenIsForbiddenWithoutAskingForAChange() throws Exception {
    final MvcResult result = shop.changePrice(ddd, "{\"price\":49.99}", shop.customerToken());

    assertThat(result.getResponse().getStatus()).isEqualTo(403);
    assertThat(priceChanges.commands()).as("price changes asked for").isEmpty();
  }

  @Test
  @DisplayName("A new price of zero is refused")
  void aNewPriceOfZeroIsAnUnacceptableValueNamingThePrice() throws Exception {
    final MvcResult result = shop.changePrice(ddd, "{\"price\":0}", shop.staffToken());

    assertThat(result.getResponse().getStatus()).isEqualTo(400);
    assertThat(result.getResponse().getContentType()).startsWith("application/problem+json");
    final JsonNode problem = JSON.readTree(result.getResponse().getContentAsString());
    assertThat(problem.path("title").asText()).isEqualTo("Unacceptable value");
    assertThat(problem.path("errors").path("price").asText())
        .as("the reason the problem gives for price: %s", problem)
        .isNotBlank();
    assertThat(priceChanges.commands()).as("price changes asked for").isEmpty();
  }

  @Test
  @DisplayName("Changing the price of an unknown product finds nothing")
  void aProductPricingHoldsNoPriceForIsNotFound() throws Exception {
    final MvcResult result =
        shop.changePrice(UNKNOWN_PRODUCT, "{\"price\":49.99}", shop.staffToken());

    assertThat(result.getResponse().getStatus()).isEqualTo(404);
    assertThat(priceChanges.commands())
        .extracting(ChangePriceCommand::productId)
        .containsExactly(UNKNOWN_PRODUCT);
  }

  @Test
  @DisplayName("A malformed product id in the price address is a bad request")
  void anIdThatIsNoUuidIsAnUnacceptableValueWithoutAskingForAChange() throws Exception {
    final MvcResult result =
        shop.changePrice("no-such-product", "{\"price\":49.99}", shop.staffToken());

    assertThat(result.getResponse().getStatus()).isEqualTo(400);
    assertThat(result.getResponse().getContentType()).startsWith("application/problem+json");
    final JsonNode problem = JSON.readTree(result.getResponse().getContentAsString());
    assertThat(problem.path("title").asText()).isEqualTo("Unacceptable value");
    assertThat(priceChanges.commands()).as("price changes asked for").isEmpty();
  }

  @Test
  @DisplayName("A staff client's new price is asked for and answered without a body")
  void aStaffClientsNewPriceBecomesThePriceChangeAndIsAnsweredNoContent() throws Exception {
    final MvcResult result = shop.changePrice(ddd, "{\"price\":49.99}", shop.staffToken());

    assertThat(result.getResponse().getStatus()).isEqualTo(204);
    assertThat(priceChanges.commands())
        .containsExactly(new ChangePriceCommand(ddd, new BigDecimal("49.99")));
  }

  @TestConfiguration
  static class PriceChangeRecordedByTheTest {

    @Bean
    @Primary
    RecordedPriceChanges recordedPriceChanges() {
      return new RecordedPriceChanges();
    }
  }

  /**
   * A price change that records what it was asked and changes nothing; Pricing holds no price for
   * the unknown product.
   */
  static final class RecordedPriceChanges implements ChangePriceInputPort {

    private final List<ChangePriceCommand> commands = new CopyOnWriteArrayList<>();

    @Override
    public ChangePriceResult execute(final ChangePriceCommand command) {
      commands.add(command);
      if (UNKNOWN_PRODUCT.equals(command.productId())) {
        throw new ProductPriceNotFoundException(ProductId.of(command.productId()));
      }
      return new ChangePriceResult(
          command.productId(), command.priceAmount(), "EUR", Instant.now());
    }

    List<ChangePriceCommand> commands() {
      return List.copyOf(commands);
    }

    void clear() {
      commands.clear();
    }
  }
}
