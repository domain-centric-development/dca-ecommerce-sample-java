package dev.domaincentric.sample.ecommerce.inventory.application.reservestock;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for reserving the stock of every line of an order that is being placed — all lines or
 * none.
 */
public interface ReserveStockInputPort extends UseCase<ReserveStockCommand, ReserveStockResult> {}
