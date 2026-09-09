package dev.domaincentric.sample.ecommerce.pricing.application.getpricesforproducts;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.sample.ecommerce.pricing.application.shared.ProductPriceRepository;
import dev.domaincentric.sample.ecommerce.pricing.domain.model.PriceId;
import dev.domaincentric.sample.ecommerce.pricing.domain.model.ProductPrice;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for GetPricesForProductsUseCase. */
@DisplayName("GetPricesForProductsUseCase")
class GetPricesForProductsUseCaseTest {

  private TestProductPriceRepository repository;
  private GetPricesForProductsUseCase useCase;

  @BeforeEach
  void setUp() {
    repository = new TestProductPriceRepository();
    useCase = new GetPricesForProductsUseCase(repository);
  }

  @Nested
  @DisplayName("execute")
  class Execute {

    @Test
    @DisplayName("returns prices for all requested products")
    void returnsPricesForAllRequestedProducts() {
      // Arrange
      ProductId product1 = ProductId.generate();
      ProductId product2 = ProductId.generate();
      ProductPrice price1 =
          ProductPrice.create(
              product1,
              dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(
                  Money.euro(10.00)));
      ProductPrice price2 =
          ProductPrice.create(
              product2,
              dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(
                  Money.euro(20.00)));
      repository.save(price1);
      repository.save(price2);

      GetPricesForProductsQuery query = new GetPricesForProductsQuery(List.of(product1, product2));

      // Act
      GetPricesForProductsResult result = useCase.execute(query);

      // Assert
      assertEquals(2, result.prices().size());
      assertTrue(result.prices().containsKey(product1));
      assertTrue(result.prices().containsKey(product2));
      assertEquals(product1, result.prices().get(product1).productId());
      assertEquals(product2, result.prices().get(product2).productId());
    }

    @Test
    @DisplayName("returns correct price data")
    void returnsCorrectPriceData() {
      // Arrange
      ProductId productId = ProductId.generate();
      Money price = Money.euro(15.50);
      ProductPrice productPrice =
          ProductPrice.create(
              productId,
              dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(price));
      repository.save(productPrice);

      GetPricesForProductsQuery query = new GetPricesForProductsQuery(List.of(productId));

      // Act
      GetPricesForProductsResult result = useCase.execute(query);

      // Assert
      assertEquals(1, result.prices().size());
      GetPricesForProductsResult.PriceData priceData = result.prices().get(productId);
      assertNotNull(priceData);
      assertEquals(productId, priceData.productId());
      assertEquals(price, priceData.currentPrice());
      assertNotNull(priceData.effectiveFrom());
    }

    @Test
    @DisplayName("returns empty map when no products requested")
    void returnsEmptyMapWhenNoProductsRequested() {
      // Arrange
      GetPricesForProductsQuery query = new GetPricesForProductsQuery(List.of());

      // Act
      GetPricesForProductsResult result = useCase.execute(query);

      // Assert
      assertTrue(result.prices().isEmpty());
    }

    @Test
    @DisplayName("returns only found prices when some products not in repository")
    void returnsOnlyFoundPricesWhenSomeProductsNotInRepository() {
      // Arrange
      ProductId existingProduct = ProductId.generate();
      ProductId missingProduct = ProductId.generate();
      ProductPrice price =
          ProductPrice.create(
              existingProduct,
              dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(
                  Money.euro(25.00)));
      repository.save(price);

      GetPricesForProductsQuery query =
          new GetPricesForProductsQuery(List.of(existingProduct, missingProduct));

      // Act
      GetPricesForProductsResult result = useCase.execute(query);

      // Assert
      assertEquals(1, result.prices().size());
      assertTrue(result.prices().containsKey(existingProduct));
      assertFalse(result.prices().containsKey(missingProduct));
    }
  }

  // Test double

  private static class TestProductPriceRepository implements ProductPriceRepository {

    private final Map<PriceId, ProductPrice> prices = new ConcurrentHashMap<>();

    @Override
    public Optional<ProductPrice> findById(PriceId id) {
      return Optional.ofNullable(prices.get(id));
    }

    @Override
    public ProductPrice save(ProductPrice productPrice) {
      prices.put(productPrice.id(), productPrice);
      return productPrice;
    }

    @Override
    public void deleteById(PriceId id) {
      prices.remove(id);
    }

    @Override
    public Optional<ProductPrice> findByProductId(ProductId productId) {
      return prices.values().stream().filter(p -> p.productId().equals(productId)).findFirst();
    }

    @Override
    public List<ProductPrice> findByProductIds(Collection<ProductId> productIds) {
      return prices.values().stream().filter(p -> productIds.contains(p.productId())).toList();
    }
  }
}
