package dev.domaincentric.sample.ecommerce.product.application.getproductselection;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.product.adapter.outgoing.persistence.InMemoryProductRepository;
import dev.domaincentric.sample.ecommerce.product.application.shared.PricingDataPort;
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductStockDataPort;
import dev.domaincentric.sample.ecommerce.product.domain.model.Category;
import dev.domaincentric.sample.ecommerce.product.domain.model.EnrichedProduct;
import dev.domaincentric.sample.ecommerce.product.domain.model.Product;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductFactory;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductName;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductSelection;
import dev.domaincentric.sample.ecommerce.product.domain.model.SKU;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class GetProductSelectionUseCaseTest {

  private static final ProductFactory FACTORY = new ProductFactory();

  private final InMemoryProductRepository repository = new InMemoryProductRepository();
  private final FixedPricing pricing = new FixedPricing();
  private final FixedStock stock = new FixedStock();

  @Test
  void offersOnlyProductsThatHaveAPrice() {
    final List<Product> products = saveProducts(6);
    pricing.price(products.get(1), Money.euro(12.50));
    pricing.price(products.get(4), Money.euro(7));

    final GetProductSelectionResult result =
        useCase(new Random(3)).execute(new GetProductSelectionQuery());

    assertThat(result.products())
        .extracting(EnrichedProduct::productId)
        .containsExactlyInAnyOrder(products.get(1).id(), products.get(4).id());
    assertThat(
            result.products().stream()
                .filter(p -> p.productId().equals(products.get(1).id()))
                .findFirst()
                .orElseThrow()
                .currentPrice())
        .isEqualTo(Money.euro(12.50));
  }

  @Test
  void offersAtMostEightPricedProducts() {
    final List<Product> products = saveProducts(10);
    products.forEach(product -> pricing.price(product, Money.euro(5)));

    final GetProductSelectionResult result =
        useCase(new Random(3)).execute(new GetProductSelectionQuery());

    assertThat(result.products())
        .extracting(EnrichedProduct::productId)
        .hasSize(ProductSelection.MAX_SIZE)
        .doesNotHaveDuplicates();
  }

  @Test
  void offersAPricedProductThatIsOutOfStock() {
    final List<Product> products = saveProducts(1);
    pricing.price(products.get(0), Money.euro(9));
    stock.stock(products.get(0), 0);

    final GetProductSelectionResult result =
        useCase(new Random(3)).execute(new GetProductSelectionQuery());

    assertThat(result.products()).hasSize(1);
    assertThat(result.products().get(0).productId()).isEqualTo(products.get(0).id());
    assertThat(result.products().get(0).canPurchase()).isFalse();
  }

  @Test
  void keepsTheOrderTheProductsWereDrawnIn() {
    final List<Product> products = saveProducts(8);
    products.forEach(product -> pricing.price(product, Money.euro(5)));
    final List<ProductId> catalogueOrder = repository.findAll().stream().map(Product::id).toList();
    final List<ProductId> drawn =
        ProductSelection.draw(catalogueOrder, new Random(11)).productIds();

    final GetProductSelectionResult result =
        useCase(new Random(11)).execute(new GetProductSelectionQuery());

    assertThat(result.products()).extracting(EnrichedProduct::productId).isEqualTo(drawn);
  }

  @Test
  void offersNothingWhenNoProductHasAPrice() {
    saveProducts(5);

    final GetProductSelectionResult result =
        useCase(new Random(3)).execute(new GetProductSelectionQuery());

    assertThat(result.products()).isEmpty();
  }

  private GetProductSelectionUseCase useCase(final Random random) {
    return new GetProductSelectionUseCase(repository, pricing, stock, random);
  }

  private List<Product> saveProducts(final int count) {
    final List<Product> products = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      final Product product =
          FACTORY.createBasicProduct(
              SKU.of("SKU-" + i),
              ProductName.of(String.format("Product %02d", i)),
              Category.of("Books"),
              Price.of(Money.euro(1)),
              1);
      repository.save(product);
      products.add(product);
    }
    return products;
  }

  /** Pricing's answer holds only the products priced here, as with a product nobody priced yet. */
  private static final class FixedPricing implements PricingDataPort {

    private final Map<ProductId, PriceData> prices = new HashMap<>();

    void price(final Product product, final Money price) {
      prices.put(product.id(), new PriceData(product.id(), price));
    }

    @Override
    public Map<ProductId, PriceData> getPrices(final Collection<ProductId> productIds) {
      return productIds.stream()
          .filter(prices::containsKey)
          .collect(Collectors.toMap(id -> id, prices::get));
    }

    @Override
    public Optional<PriceData> getPrice(final ProductId productId) {
      return Optional.ofNullable(prices.get(productId));
    }
  }

  private static final class FixedStock implements ProductStockDataPort {

    private final Map<ProductId, StockData> stock = new HashMap<>();

    void stock(final Product product, final int available) {
      stock.put(product.id(), new StockData(product.id(), available, available > 0));
    }

    @Override
    public Optional<StockData> getStockData(final ProductId productId) {
      return Optional.ofNullable(stock.get(productId));
    }

    @Override
    public Map<ProductId, StockData> getStockData(final Collection<ProductId> productIds) {
      return productIds.stream()
          .filter(stock::containsKey)
          .collect(Collectors.toMap(id -> id, stock::get));
    }
  }
}
