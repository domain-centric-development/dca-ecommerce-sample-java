package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.inventory;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.StockReservationPort;
import dev.domaincentric.sample.ecommerce.inventory.api.InventoryService;
import dev.domaincentric.sample.ecommerce.inventory.api.InventoryService.StockLine;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Outgoing adapter that reserves and releases an order's stock through Inventory's Open Host
 * Service.
 *
 * <p><b>Hexagonal Architecture:</b> implements Checkout's {@link StockReservationPort}; the only
 * place in Checkout that asks Inventory to hold stock. It translates Checkout's order lines into
 * Inventory's published stock lines and back.
 */
@Component
public class InventoryStockReservationAdapter implements StockReservationPort {

  private final InventoryService inventoryService;

  public InventoryStockReservationAdapter(final InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @Override
  public Optional<ProductId> reserve(final List<Line> lines) {
    return inventoryService.reserveStock(toStockLines(lines));
  }

  @Override
  public void release(final List<Line> lines) {
    inventoryService.releaseStock(toStockLines(lines));
  }

  private static List<StockLine> toStockLines(final List<Line> lines) {
    return lines.stream().map(line -> new StockLine(line.productId(), line.quantity())).toList();
  }
}
