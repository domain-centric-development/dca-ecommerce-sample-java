package dev.domaincentric.sample.ecommerce.product.application.getproductselection;

import dev.domaincentric.sample.ecommerce.product.application.shared.PricingDataPort;
import dev.domaincentric.sample.ecommerce.product.application.shared.PricingDataPort.PriceData;
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductStockDataPort;
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductStockDataPort.StockData;
import dev.domaincentric.sample.ecommerce.product.domain.model.EnrichedProduct;
import dev.domaincentric.sample.ecommerce.product.domain.model.Product;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductArticle;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductSelection;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Query use case that draws the product selection the homepage shows.
 *
 * <p>Only products Pricing holds a price for are candidates; a product without one is not offered.
 * The drawn products are enriched with their price and stock, in the order they were drawn.
 */
@Service
public class GetProductSelectionUseCase implements GetProductSelectionInputPort {

  private final ProductRepository productRepository;
  private final PricingDataPort pricingDataPort;
  private final ProductStockDataPort productStockDataPort;
  private final RandomGenerator random;

  public GetProductSelectionUseCase(
      final ProductRepository productRepository,
      final PricingDataPort pricingDataPort,
      final ProductStockDataPort productStockDataPort,
      final RandomGenerator random) {
    this.productRepository = productRepository;
    this.pricingDataPort = pricingDataPort;
    this.productStockDataPort = productStockDataPort;
    this.random = random;
  }

  @Override
  public GetProductSelectionResult execute(final GetProductSelectionQuery query) {
    final List<Product> products = productRepository.findAll();
    final List<ProductId> productIds = products.stream().map(Product::id).toList();
    final Map<ProductId, PriceData> prices = pricingDataPort.getPrices(productIds);

    final List<ProductId> pricedProducts = productIds.stream().filter(prices::containsKey).toList();
    final List<ProductId> drawn = ProductSelection.draw(pricedProducts, random).productIds();
    if (drawn.isEmpty()) {
      return new GetProductSelectionResult(List.of());
    }

    final Map<ProductId, Product> byId =
        products.stream()
            .collect(Collectors.toMap(Product::id, Function.identity(), (first, second) -> first));
    final Map<ProductId, StockData> stocks = productStockDataPort.getStockData(drawn);

    final List<EnrichedProduct> enriched =
        drawn.stream()
            .map(id -> EnrichedProduct.from(byId.get(id), article(id, prices, stocks)))
            .toList();
    return new GetProductSelectionResult(enriched);
  }

  private static ProductArticle article(
      final ProductId productId,
      final Map<ProductId, PriceData> prices,
      final Map<ProductId, StockData> stocks) {
    final StockData stockData = stocks.get(productId);
    final int stockQuantity = stockData != null ? stockData.availableStock() : 0;
    final boolean isAvailable = stockData != null && stockData.isAvailable();
    return new ProductArticle(prices.get(productId).currentPrice(), stockQuantity, isAvailable);
  }
}
