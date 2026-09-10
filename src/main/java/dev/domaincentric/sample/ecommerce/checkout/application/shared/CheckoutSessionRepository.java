package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.Repository;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CheckoutSession aggregate.
 *
 * <p>Provides collection-like access to CheckoutSession aggregates using domain language.
 * Implementation resides in the secondary adapter layer.
 *
 * <p>Extends the base {@link Repository} interface which provides common methods:
 *
 * <ul>
 *   <li>{@code findById(CheckoutSessionId)} - inherited from base interface
 *   <li>{@code save(CheckoutSession)} - inherited from base interface
 *   <li>{@code deleteById(CheckoutSessionId)} - inherited from base interface
 * </ul>
 */
public interface CheckoutSessionRepository extends Repository<CheckoutSession, CheckoutSessionId> {

  /** Serializes confirmation versus replacement, including the caller's transaction completion. */
  default <T> T inCartSession(CartId cartId, java.util.function.Supplier<T> operation) {
    synchronized (this) {
      return operation.get();
    }
  }

  /**
   * Finds a checkout session by the cart it was created from.
   *
   * <p>Returns any session for this cart, regardless of status.
   *
   * @param cartId the cart ID
   * @return the checkout session if found, empty otherwise
   */
  Optional<CheckoutSession> findByCartId(CartId cartId);

  /**
   * Finds an active checkout session by the cart it was created from.
   *
   * <p>Only returns sessions with ACTIVE status that can still be modified.
   *
   * @param cartId the cart ID
   * @return the active checkout session if found, empty otherwise
   */
  Optional<CheckoutSession> findActiveByCartId(CartId cartId);

  /**
   * Finds an active checkout session for a customer.
   *
   * <p>A customer can have at most one active checkout session at a time. Active means the session
   * status is ACTIVE (not confirmed, completed, abandoned, or expired).
   *
   * @param customerId the customer ID
   * @return the active checkout session if found, empty otherwise
   */
  Optional<CheckoutSession> findActiveByCustomerId(CustomerId customerId);

  /**
   * Finds all checkout sessions that have expired.
   *
   * <p>A session is considered expired when its status is EXPIRED. This method is typically used by
   * cleanup processes or for reporting purposes.
   *
   * @return list of expired checkout sessions
   */
  List<CheckoutSession> findExpiredSessions();

  /**
   * Retrieves all checkout sessions.
   *
   * @return list of all checkout sessions
   */
  List<CheckoutSession> findAll();

  /**
   * Finds a confirmed or completed checkout session for a customer.
   *
   * <p>Used for displaying the confirmation/thank you page after order confirmation.
   *
   * @param customerId the customer ID
   * @return the confirmed or completed checkout session if found, empty otherwise
   */
  Optional<CheckoutSession> findConfirmedOrCompletedByCustomerId(CustomerId customerId);
}
