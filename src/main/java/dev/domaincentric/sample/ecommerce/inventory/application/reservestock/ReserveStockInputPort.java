package dev.domaincentric.sample.ecommerce.inventory.application.reservestock;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for reserving the stock of every line of an order that is being placed — all lines or
 * none.
 *
 * <p>Answers {@link ReserveStockResult.Reserved}, or {@link
 * ReserveStockResult.InsufficientUnreservedStock} for the first line the unreserved stock does not
 * cover.
 */
public interface ReserveStockInputPort extends UseCase<ReserveStockCommand, ReserveStockResult> {}
