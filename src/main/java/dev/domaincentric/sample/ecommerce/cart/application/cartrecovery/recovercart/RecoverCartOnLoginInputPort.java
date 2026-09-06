package dev.domaincentric.sample.ecommerce.cart.application.recovercart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input Port for recovering cart on login.
 *
 * <p>This port defines the contract for the "Recover Cart On Login" use case, which merges an
 * anonymous user's cart items into a registered user's cart when they log in.
 *
 * <p><b>Hexagonal Architecture:</b> This is a primary/driving port that adapters (e.g., web
 * controllers, event handlers) use to invoke the use case.
 */
public interface RecoverCartOnLoginInputPort
    extends UseCase<RecoverCartOnLoginCommand, RecoverCartOnLoginResult> {}
