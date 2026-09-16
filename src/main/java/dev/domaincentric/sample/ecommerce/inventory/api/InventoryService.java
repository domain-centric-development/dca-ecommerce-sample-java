package dev.domaincentric.sample.ecommerce.inventory.api;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.OpenHostService;
import dev.domaincentric.sample.ecommerce.inventory.application.getstockforproducts.GetStockForProductsInputPort;
import dev.domaincentric.sample.ecommerce.inventory.application.getstockforproducts.GetStockForProductsQuery;
import dev.domaincentric.sample.ecommerce.inventory.application.getstockforproducts.GetStockForProductsResult;
import dev.domaincentric.sample.ecommerce.inventory.application.reducestock.ReduceStockCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.reducestock.ReduceStockInputPort;
import dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel.SetStockLevelCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel.SetStockLevelInputPort;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Open Host Service for Inventory.
 *
 * <p>An Open Host Service in {@code api/}: the in-process published contract of the Inventory
 * context. It delegates to use cases (input ports) and translates responses to its own DTOs.
 *
 * <p>Consuming contexts should NOT use this service directly in their use cases - they should
 * define their own output ports and implement adapters that delegate to this service.
 *
 * <p><b>Hexagonal Architecture:</b> Like an incoming adapter, this service calls input ports (use
 * cases), never output ports (repositories) directly.
 */
@OpenHostService(
    context = "Inventory",
    description = "Provides stock information for other bounded contexts")
@Service("inventoryContextOhs")
public class InventoryService {

  private final GetStockForProductsInputPort getStockForProductsInputPort;
  private final SetStockLevelInputPort setStockLevelInputPort;
  private final ReduceStockInputPort reduceStockInputPort;

  public InventoryService(
      GetStockForProductsInputPort getStockForProductsInputPort,
      SetStockLevelInputPort setStockLevelInputPort,
      ReduceStockInputPort reduceStockInputPort) {
    this.getStockForProductsInputPort = getStockForProductsInputPort;
    this.setStockLevelInputPort = setStockLevelInputPort;
    this.reduceStockInputPort = reduceStockInputPort;
  }

  /**
   * Stock information DTO for cross-context communication.
   *
   * @param productId the product ID
   * @param availableStock the quantity of stock available (unreserved)
   * @param isAvailable whether any stock is available for purchase
   */
  public record StockInfo(ProductId productId, int availableStock, boolean isAvailable) {

    public StockInfo {
      if (productId == null) {
        throw new IllegalArgumentException("ProductId cannot be null");
      }
      if (availableStock < 0) {
        throw new IllegalArgumentException("Available stock cannot be negative");
      }
    }
  }

  /**
   * Retrieves stock information for multiple products.
   *
   * @param productIds the collection of product IDs to get stock for
   * @return map of product IDs to their stock info
   */
  public Map<ProductId, StockInfo> getStock(Collection<ProductId> productIds) {
    if (productIds.isEmpty()) {
      return Collections.emptyMap();
    }

    GetStockForProductsResult result =
        getStockForProductsInputPort.execute(new GetStockForProductsQuery(productIds));

    return result.stocks().entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry ->
                    new StockInfo(
                        entry.getValue().productId(),
                        entry.getValue().availableStock(),
                        entry.getValue().isAvailable())));
  }

  /**
   * Retrieves stock information for a single product.
   *
   * @param productId the product ID
   * @return stock info if found
   */
  public Optional<StockInfo> getStock(ProductId productId) {
    Map<ProductId, StockInfo> stocks = getStock(Collections.singletonList(productId));
    return Optional.ofNullable(stocks.get(productId));
  }

  /**
   * Checks if sufficient stock is available for a product.
   *
   * @param productId the product ID
   * @param quantity the required quantity
   * @return true if the product has at least the specified quantity available
   */
  public boolean hasStock(ProductId productId, int quantity) {
    if (quantity <= 0) {
      return true;
    }

    return getStock(productId)
        .map(stockInfo -> stockInfo.availableStock() >= quantity)
        .orElse(false);
  }

  /**
   * Sets the initial stock level for a product.
   *
   * <p>Delegates to the {@link SetStockLevelInputPort} use case. If no stock level exists for the
   * product, a new stock level record is created. If a stock level already exists, it is updated.
   *
   * @param productId the product ID
   * @param quantity the stock quantity to set (must be non-negative)
   */
  public void setInitialStock(ProductId productId, int quantity) {
    setStockLevelInputPort.execute(new SetStockLevelCommand(productId.value(), quantity));
  }

  /**
   * Reduces the stock level for a product by the specified quantity.
   *
   * <p>Delegates to the {@link ReduceStockInputPort} use case. Typically invoked when an order is
   * confirmed to reduce the available stock for the ordered products.
   *
   * @param productId the product ID
   * @param quantity the quantity to reduce (must be positive)
   */
  public void reduceStock(ProductId productId, int quantity) {
    reduceStockInputPort.execute(new ReduceStockCommand(productId.value(), quantity));
  }
}
