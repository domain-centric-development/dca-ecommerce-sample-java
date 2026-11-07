package dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.mergecarts;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input Port for merging carts based on user's choice.
 *
 * <p>This port defines the contract for the "Merge Carts" use case, which executes the user's
 * chosen strategy for handling conflicting carts when logging in.
 *
 * <p><b>Hexagonal Architecture:</b> This is a primary/driving port that adapters (e.g., web
 * controllers) use to invoke the use case.
 *
 * <p>Answers {@link MergeCartsResult.Merged}, or {@link MergeCartsResult.GuestCartNotOwned} for a
 * guest other than the one the caller was before the login.
 */
public interface MergeCartsInputPort extends UseCase<MergeCartsCommand, MergeCartsResult> {}
