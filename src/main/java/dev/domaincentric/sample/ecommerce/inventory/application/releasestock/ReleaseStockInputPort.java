package dev.domaincentric.sample.ecommerce.inventory.application.releasestock;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/** Input port for giving back the stock an order reserved and was then not placed with. */
public interface ReleaseStockInputPort extends UseCase<ReleaseStockCommand, ReleaseStockResult> {}
