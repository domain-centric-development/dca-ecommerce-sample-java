package dev.domaincentric.sample.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.product.application.shared.PricingDataPort;
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import dev.domaincentric.sample.ecommerce.product.domain.model.Product;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The homepage's "Discover products" slider over a catalogue whose prices the test decides: the
 * seeded products, with Pricing's answer replaced by one that prices only the products a test names
 * — the state of a product nobody has priced yet. What the browser suite cannot arrange, because it
 * runs against the seeded shop.
 */
// A context of its own: the replaced Pricing answer must not reach any other test class.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:product_slider_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@Import(ProductSliderIntegrationTest.PricingChosenByTheTest.class)
@DisplayName("Product slider on the homepage")
class ProductSliderIntegrationTest {

  private static final Pattern CARD_LINK =
      Pattern.compile("<a\\b[^>]*\\bdata-test=\"product-slider-card-link\"[^>]*>");
  private static final Pattern HREF = Pattern.compile("\\bhref=\"/products/([0-9a-f-]{36})\"");

  @Autowired private MockMvc mockMvc;
  @Autowired private ProductRepository productRepository;
  @Autowired private ChosenPricing pricing;

  @Test
  @DisplayName("A product without a price is not among the slider's cards")
  void aProductWithoutAPriceIsNotAmongTheSlidersCards() throws Exception {
    final List<ProductId> catalogue = catalogue();
    final Set<ProductId> priced = Set.copyOf(catalogue.subList(0, 10));
    pricing.priceOnly(priced);

    for (int request = 0; request < 10; request++) {
      final List<ProductId> cards = cardProductIds(homePage());

      assertThat(cards).as("the slider's cards on request %d", request + 1).hasSize(8);
      assertThat(priced).as("the priced products").containsAll(cards);
    }
  }

  @Test
  @DisplayName("With two priced products the slider holds a card for each of them")
  void withTwoPricedProductsTheSliderHoldsACardForEachOfThem() throws Exception {
    final List<ProductId> catalogue = catalogue();
    final Set<ProductId> priced = Set.of(catalogue.get(3), catalogue.get(17));
    pricing.priceOnly(priced);

    final List<ProductId> cards = cardProductIds(homePage());

    assertThat(cards).containsExactlyInAnyOrderElementsOf(priced);
  }

  private List<ProductId> catalogue() {
    final List<ProductId> products = productRepository.findAll().stream().map(Product::id).toList();
    assertThat(products).as("the seeded catalogue").hasSizeGreaterThanOrEqualTo(18);
    return products;
  }

  private String homePage() throws Exception {
    return mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  /** The product of every slider card, read from the card's link to its product page. */
  private static List<ProductId> cardProductIds(final String html) {
    final List<ProductId> ids = new ArrayList<>();
    final Matcher tag = CARD_LINK.matcher(html);
    while (tag.find()) {
      final Matcher href = HREF.matcher(tag.group());
      if (href.find()) {
        ids.add(ProductId.of(href.group(1)));
      }
    }
    return ids;
  }

  @TestConfiguration
  static class PricingChosenByTheTest {

    @Bean
    @Primary
    ChosenPricing chosenPricing() {
      return new ChosenPricing();
    }
  }

  /** Pricing's answer holds only the products priced here; every other product has no price. */
  static final class ChosenPricing implements PricingDataPort {

    private volatile Set<ProductId> priced = Set.of();

    void priceOnly(final Set<ProductId> products) {
      priced = Set.copyOf(products);
    }

    @Override
    public Map<ProductId, PriceData> getPrices(final Collection<ProductId> productIds) {
      return productIds.stream()
          .filter(priced::contains)
          .distinct()
          .collect(Collectors.toMap(Function.identity(), id -> new PriceData(id, Money.euro(10))));
    }

    @Override
    public Optional<PriceData> getPrice(final ProductId productId) {
      return priced.contains(productId)
          ? Optional.of(new PriceData(productId, Money.euro(10)))
          : Optional.empty();
    }
  }
}
