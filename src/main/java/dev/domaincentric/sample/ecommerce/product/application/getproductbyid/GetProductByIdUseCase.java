package dev.domaincentric.sample.ecommerce.product.application.getproductbyid;

import dev.domaincentric.sample.ecommerce.product.application.shared.PricingDataPort;
import dev.domaincentric.sample.ecommerce.product.application.shared.PricingDataPort.PriceData;
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductStockDataPort;
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductStockDataPort.StockData;
import dev.domaincentric.sample.ecommerce.product.domain.model.EnrichedProduct;
import dev.domaincentric.sample.ecommerce.product.domain.model.Product;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductArticle;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Currency;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving a product by its ID.
 *
 * <p>This is a query use case that retrieves product details without modifying state. Creates an
 * enriched read model that combines aggregate state with external data from Pricing and Inventory
 * contexts.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetProductByIdInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
public class GetProductByIdUseCase implements GetProductByIdInputPort {

  private static final Currency DEFAULT_CURRENCY = Currency.getInstance("EUR");

  private final ProductRepository productRepository;
  private final PricingDataPort pricingDataPort;
  private final ProductStockDataPort productStockDataPort;

  public GetProductByIdUseCase(
      final ProductRepository productRepository,
      final PricingDataPort pricingDataPort,
      final ProductStockDataPort productStockDataPort) {
    this.productRepository = productRepository;
    this.pricingDataPort = pricingDataPort;
    this.productStockDataPort = productStockDataPort;
  }

  @Override
  public GetProductByIdResult execute(final GetProductByIdQuery input) {
    final ProductId productId = ProductId.of(input.productId());

    final Optional<Product> productOpt = productRepository.findById(productId);

    if (productOpt.isEmpty()) {
      return new GetProductByIdResult.ProductNotFound(productId);
    }

    final Product product = productOpt.get();

    // Build article data from external contexts
    final ProductArticle articleData = buildArticleData(productId);

    // Create enriched product using factory method
    final EnrichedProduct enrichedProduct = EnrichedProduct.from(product, articleData);

    return new GetProductByIdResult.Found(enrichedProduct);
  }

  private ProductArticle buildArticleData(final ProductId productId) {
    // Fetch price from Pricing context
    final Optional<PriceData> priceData = pricingDataPort.getPrice(productId);
    final Money currentPrice =
        priceData.map(PriceData::currentPrice).orElse(Money.zero(DEFAULT_CURRENCY));

    // Fetch stock from Inventory context
    final Optional<StockData> stockData = productStockDataPort.getStockData(productId);
    final int stockQuantity = stockData.map(StockData::availableStock).orElse(0);
    final boolean isAvailable = stockData.map(StockData::isAvailable).orElse(false);

    return new ProductArticle(currentPrice, stockQuantity, isAvailable);
  }
}
