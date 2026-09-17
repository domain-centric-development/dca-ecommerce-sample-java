package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionStatus;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of CheckoutSessionRepository.
 *
 * <p>This secondary adapter provides a thread-safe in-memory storage for checkout sessions using
 * ConcurrentHashMap. In a production system, this would be replaced with a database implementation.
 *
 * <p>Note: No profile restriction - this is the default implementation until JPA/JDBC
 * implementations are added for the checkout bounded context.
 */
@Repository
public class InMemoryCheckoutSessionRepository implements CheckoutSessionRepository {

  private final ConcurrentHashMap<CheckoutSessionId, CheckoutSession> sessions =
      new ConcurrentHashMap<>();

  @Override
  public Optional<CheckoutSession> findById(final CheckoutSessionId id) {
    return Optional.ofNullable(sessions.get(id));
  }

  @Override
  public Optional<CheckoutSession> findByIdForCustomer(
      final CheckoutSessionId id, final CustomerId customerId) {
    return findById(id).filter(session -> session.customerId().equals(customerId));
  }

  @Override
  public Optional<CheckoutSession> findByCartId(final CartId cartId) {
    return sessions.values().stream()
        .filter(session -> session.cartId().equals(cartId))
        .findFirst();
  }

  @Override
  public Optional<CheckoutSession> findActiveByCartId(final CartId cartId) {
    return sessions.values().stream()
        .filter(session -> session.cartId().equals(cartId))
        .filter(session -> session.status() == CheckoutSessionStatus.ACTIVE)
        .findFirst();
  }

  @Override
  public Optional<CheckoutSession> findActiveByCustomerId(final CustomerId customerId) {
    return sessions.values().stream()
        .filter(session -> session.customerId().equals(customerId))
        .filter(session -> session.status() == CheckoutSessionStatus.ACTIVE)
        .findFirst();
  }

  @Override
  public List<CheckoutSession> findExpiredSessions() {
    return sessions.values().stream()
        .filter(session -> session.status() == CheckoutSessionStatus.EXPIRED)
        .toList();
  }

  @Override
  public List<CheckoutSession> findAll() {
    return List.copyOf(sessions.values());
  }

  @Override
  public CheckoutSession save(final CheckoutSession session) {
    sessions.put(session.id(), session);
    return session;
  }

  @Override
  public void deleteById(final CheckoutSessionId id) {
    sessions.remove(id);
  }

  @Override
  public Optional<CheckoutSession> findConfirmedOrCompletedByCustomerId(
      final CustomerId customerId) {
    return sessions.values().stream()
        .filter(session -> session.customerId().equals(customerId))
        .filter(
            session ->
                session.status() == CheckoutSessionStatus.CONFIRMED
                    || session.status() == CheckoutSessionStatus.COMPLETED)
        .findFirst();
  }
}
