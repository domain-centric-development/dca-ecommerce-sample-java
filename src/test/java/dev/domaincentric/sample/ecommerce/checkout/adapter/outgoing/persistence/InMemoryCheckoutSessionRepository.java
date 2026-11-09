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

/**
 * In-memory fake of {@link CheckoutSessionRepository} for tests without a database: sessions stored
 * and handed out as copies, so a change survives only through {@code save}.
 */
public class InMemoryCheckoutSessionRepository implements CheckoutSessionRepository {

  private final ConcurrentHashMap<CheckoutSessionId, CheckoutSession> sessions =
      new ConcurrentHashMap<>();

  @Override
  public Optional<CheckoutSession> findById(final CheckoutSessionId id) {
    return Optional.ofNullable(sessions.get(id)).map(InMemoryCheckoutSessionRepository::copyOf);
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
        .findFirst()
        .map(InMemoryCheckoutSessionRepository::copyOf);
  }

  @Override
  public Optional<CheckoutSession> findActiveByCartId(final CartId cartId) {
    return sessions.values().stream()
        .filter(session -> session.cartId().equals(cartId))
        .filter(session -> session.status() == CheckoutSessionStatus.ACTIVE)
        .findFirst()
        .map(InMemoryCheckoutSessionRepository::copyOf);
  }

  @Override
  public Optional<CheckoutSession> findActiveByCustomerId(final CustomerId customerId) {
    return sessions.values().stream()
        .filter(session -> session.customerId().equals(customerId))
        .filter(session -> session.status() == CheckoutSessionStatus.ACTIVE)
        .findFirst()
        .map(InMemoryCheckoutSessionRepository::copyOf);
  }

  @Override
  public List<CheckoutSession> findExpiredSessions() {
    return sessions.values().stream()
        .filter(session -> session.status() == CheckoutSessionStatus.EXPIRED)
        .map(InMemoryCheckoutSessionRepository::copyOf)
        .toList();
  }

  @Override
  public List<CheckoutSession> findAll() {
    return sessions.values().stream().map(InMemoryCheckoutSessionRepository::copyOf).toList();
  }

  @Override
  public CheckoutSession save(final CheckoutSession session) {
    sessions.put(session.id(), copyOf(session));
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
        .findFirst()
        .map(InMemoryCheckoutSessionRepository::copyOf);
  }

  private static CheckoutSession copyOf(final CheckoutSession session) {
    return CheckoutSession.reconstitute(
        session.id(),
        session.cartId(),
        session.customerId(),
        session.lineItems(),
        session.totals(),
        session.currentStep(),
        session.status(),
        session.buyerInfo(),
        session.deliveryAddress(),
        session.shippingOption(),
        session.paymentSelection(),
        session.orderReference());
  }
}
