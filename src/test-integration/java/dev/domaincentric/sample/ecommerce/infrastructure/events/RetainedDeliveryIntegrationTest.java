package dev.domaincentric.sample.ecommerce.infrastructure.events;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEvent;
import dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence.JdbcShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.*;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@ActiveProfiles("dev")
@SpringBootTest(
    classes = {EcommerceSampleApplication.class, RetainedDeliveryIntegrationTest.Listeners.class},
    properties = {
      "dca.events.retry.enabled=false",
      "dca.events.retry.max-attempts=2",
      "dca.events.retry.base-delay=PT0.01S"
    })
class RetainedDeliveryIntegrationTest {
  @Autowired ApplicationEventPublisher publisher;
  @Autowired PlatformTransactionManager transactions;
  @Autowired JdbcTemplate jdbc;
  @Autowired IntegrationEventRecovery recovery;
  @Autowired JdbcShoppingCartRepository carts;
  @Autowired Observed observed;
  @Autowired org.springframework.modulith.events.core.EventPublicationRegistry registry;

  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(
      named = "specification.path",
      matches = ".+",
      disabledReason = "shared specification not supplied (-Pspecification.path)")
  @Test
  void productCreatedUsesTheSharedSixFieldWireSchema() throws Exception {
    var root = java.nio.file.Path.of(System.getProperty("specification.path"));
    var json = new com.fasterxml.jackson.databind.ObjectMapper();
    var schema = json.readTree(root.resolve("events/product-created-v1.schema.json").toFile());
    var expected = new java.util.TreeSet<String>();
    schema.path("required").forEach(n -> expected.add(n.asText()));
    var product =
        new dev.domaincentric.sample.ecommerce.product.domain.model.ProductFactory()
            .createProduct(
                dev.domaincentric.sample.ecommerce.product.domain.model.SKU.of("WIRE-SPEC"),
                dev.domaincentric.sample.ecommerce.product.domain.model.ProductName.of(
                    "Wire specification"),
                dev.domaincentric.sample.ecommerce.product.domain.model.ProductDescription.empty(),
                dev.domaincentric.sample.ecommerce.product.domain.model.Category.of("Test"),
                dev.domaincentric.sample.ecommerce.product.domain.model.ImageUrl.empty(),
                dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(
                    dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money.euro(1)),
                0);
    var event = product.domainEvents().get(0);
    new TransactionTemplate(transactions)
        .executeWithoutResult(status -> publisher.publishEvent(event));
    var payloads =
        jdbc.queryForList(
            "SELECT SERIALIZED_EVENT FROM EVENT_PUBLICATION WHERE EVENT_TYPE=? AND SERIALIZED_EVENT LIKE ?",
            String.class,
            dev.domaincentric.sample.ecommerce.product.events.ProductCreatedEvent.class.getName(),
            "%" + product.id().value() + "%");
    assertFalse(payloads.isEmpty());
    for (String payload : payloads) {
      var node = json.readTree(payload);
      var actual = new java.util.TreeSet<String>();
      node.fieldNames().forEachRemaining(actual::add);
      assertEquals(expected, actual);
      assertEquals("1.00", node.path("amount").asText());
      assertEquals("EUR", node.path("currency").asText());
      assertEquals(0, node.path("initialStock").asInt());
    }
  }

  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(
      named = "specification.path",
      matches = ".+",
      disabledReason = "shared specification not supplied (-Pspecification.path)")
  @TestFactory
  java.util.stream.Stream<DynamicTest> sharedDeliveryVectors() throws Exception {
    var root = java.nio.file.Path.of(System.getProperty("specification.path"));
    var nodes =
        new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(root.resolve("vectors/delivery.json").toFile());
    var tests = new ArrayList<DynamicTest>();
    for (var node : nodes) {
      String id = node.path("id").asText();
      tests.add(
          DynamicTest.dynamicTest(
              id,
              () -> {
                switch (id) {
                  case "delivery.rollback.no-publication" -> rollbackLeavesNoPublication();
                  case "delivery.startup.replays-committed-only" -> startupReplaysCommittedOnly();
                  case "delivery.multi-consumer.partial-failure" ->
                      partialFailureRetriesOnlyTheFailedConsumer();
                  case "delivery.retry.same-key" -> retryKeepsTheSameKey();
                  case "delivery.replay.snapshot" -> replayDeliversTheSnapshot();
                  case "delivery.ack.provider-accepted-not-delivered" ->
                      acceptanceWithoutAcknowledgementIsRetried();
                  case "delivery.retry.exhausted-inspectable" -> exhaustedDeliveryIsInspectable();
                  case "delivery.manual-replay.failed-consumer-only" ->
                      manualReplayTouchesOnlyTheFailedConsumer();
                  case "delivery.provider-idempotency.same-key" ->
                      idempotentProviderMakesOneEffect();
                  case "delivery.provider-no-idempotency.duplicate-possible" ->
                      providerWithoutIdempotencyMayMakeTwoEffects();
                  default -> fail("Vector has no adapter: " + id);
                }
              }));
    }
    return tests.stream();
  }

  /** delivery.rollback.no-publication */
  @Test
  void rollbackLeavesNoPublication() {
    UUID eventId = UUID.randomUUID();
    CartId cartId = CartId.generate();
    assertThrows(
        IllegalStateException.class,
        () ->
            new TransactionTemplate(transactions)
                .execute(
                    status -> {
                      carts.save(ShoppingCart.create(cartId, CustomerId.of("rollback-probe")));
                      publisher.publishEvent(
                          new Probe(eventId, Instant.now(), "inside", false, false));
                      throw new IllegalStateException("rollback");
                    }));
    assertTrue(carts.findById(cartId).isEmpty());
    assertEquals(0, rows(eventId).size());
    UUID outside = UUID.randomUUID();
    new TransactionTemplate(transactions)
        .executeWithoutResult(
            status ->
                publisher.publishEvent(new Probe(outside, Instant.now(), "outside", false, false)));
    assertThrows(
        IllegalStateException.class,
        () ->
            new TransactionTemplate(transactions)
                .execute(
                    status -> {
                      carts.save(ShoppingCart.create(cartId, CustomerId.of("rollback-probe")));
                      throw new IllegalStateException("rollback");
                    }));
    assertTrue(carts.findById(cartId).isEmpty());
    assertEquals(2, rows(outside).size());
  }

  /** delivery.startup.replays-committed-only */
  @Test
  void startupReplaysCommittedOnly() throws Exception {
    UUID rolledBack = UUID.randomUUID();
    assertThrows(
        IllegalStateException.class,
        () ->
            new TransactionTemplate(transactions)
                .execute(
                    status -> {
                      publisher.publishEvent(
                          new Probe(rolledBack, Instant.now(), "rolled-back", false, false));
                      throw new IllegalStateException("rollback");
                    }));
    UUID id = UUID.randomUUID();
    observed.poison.add(id);
    publish(new Probe(id, Instant.now(), "retained-after-interruption", false, false));
    await(() -> failed(id));
    Row row = rows(id).stream().filter(r -> r.status().equals("FAILED")).findFirst().orElseThrow();
    // Model a retained row left PROCESSING by an interrupted worker; no callback or wakeup is
    // replayed.
    jdbc.update(
        "UPDATE EVENT_PUBLICATION SET STATUS='PROCESSING', PUBLICATION_DATE=?, LAST_RESUBMISSION_DATE=? WHERE ID=?",
        java.sql.Timestamp.from(Instant.now().minusSeconds(120)),
        java.sql.Timestamp.from(Instant.now().minusSeconds(120)),
        row.id());
    registry.markStalePublicationsFailed(status -> Duration.ofSeconds(60));
    assertTrue(failed(id));
    observed.poison.remove(id);
    recovery.retryDue();
    await(() -> rows(id).stream().allMatch(r -> r.status().equals("COMPLETED")));
    assertEquals(1, observed.first.get(id).get());
    assertEquals(2, observed.second.get(id).get());
    assertTrue(
        rows(id).stream().allMatch(r -> r.payload().contains("retained-after-interruption")));
    assertEquals(0, rows(rolledBack).size());
    assertNull(observed.first.get(rolledBack));
    assertNull(observed.second.get(rolledBack));
  }

  /** delivery.multi-consumer.partial-failure */
  @Test
  void partialFailureRetriesOnlyTheFailedConsumer() throws Exception {
    UUID id = UUID.randomUUID();
    observed.poison.add(id);
    publish(new Probe(id, Instant.now(), "captured", false, false));
    await(() -> failed(id));
    await(() -> rows(id).stream().anyMatch(r -> r.status().equals("COMPLETED")));
    assertEquals(1, observed.first.get(id).get());
    assertEquals(1, observed.second.get(id).get());
    observed.poison.remove(id);
    Thread.sleep(20);
    recovery.retryDue();
    await(() -> rows(id).stream().allMatch(r -> r.status().equals("COMPLETED")));
    assertEquals(1, observed.first.get(id).get());
    assertEquals(2, observed.second.get(id).get());
  }

  /** delivery.retry.same-key */
  @Test
  void retryKeepsTheSameKey() throws Exception {
    UUID id = UUID.randomUUID();
    exhaust(id, "captured");
    var keys = observed.keys.get(id);
    assertEquals(2, keys.size());
    assertEquals(
        List.of(id + ":" + SecondListener.class.getName() + ":provider-effect"),
        keys.stream().distinct().toList());
  }

  /** delivery.replay.snapshot */
  @Test
  void replayDeliversTheSnapshot() throws Exception {
    UUID id = UUID.randomUUID();
    observed.poison.add(id);
    publish(new Probe(id, Instant.now(), "captured", false, false));
    await(() -> failed(id));
    observed.poison.remove(id);
    Thread.sleep(20);
    recovery.retryDue();
    await(() -> rows(id).stream().allMatch(r -> r.status().equals("COMPLETED")));
    assertEquals(List.of("captured", "captured"), observed.contents.get(id));
    assertTrue(rows(id).stream().allMatch(r -> r.payload().contains("captured")));
  }

  /** delivery.ack.provider-accepted-not-delivered */
  @Test
  void acceptanceWithoutAcknowledgementIsRetried() throws Exception {
    UUID id = UUID.randomUUID();
    publish(new Probe(id, Instant.now(), "captured", true, true));
    await(() -> failed(id));
    assertEquals(1, observed.effects.get(id).get());
    assertTrue(failed(id));
    Thread.sleep(20);
    recovery.retryDue();
    await(() -> rows(id).stream().allMatch(r -> r.status().equals("COMPLETED")));
    assertEquals(1, observed.first.get(id).get());
    assertEquals(2, observed.second.get(id).get());
    assertEquals(0, observed.inboxDeliveries.get());
  }

  /** delivery.retry.exhausted-inspectable */
  @Test
  void exhaustedDeliveryIsInspectable() throws Exception {
    UUID id = UUID.randomUUID();
    Row failed = exhaust(id, "captured");
    assertEquals("FAILED", failed.status());
    assertEquals(2, failed.attempts());
    assertTrue(failed.payload().contains("captured"));
    assertEquals(1, observed.first.get(id).get());
    recovery.retryDue();
    Thread.sleep(40);
    assertEquals(2, observed.second.get(id).get());
    assertTrue(rows(id).stream().anyMatch(r -> r.status().equals("FAILED") && r.attempts() == 2));
  }

  /** delivery.manual-replay.failed-consumer-only */
  @Test
  void manualReplayTouchesOnlyTheFailedConsumer() throws Exception {
    UUID id = UUID.randomUUID();
    Row failed = exhaust(id, "captured");
    observed.poison.remove(id);
    recovery.replayFailed(failed.id());
    await(() -> rows(id).stream().allMatch(r -> r.status().equals("COMPLETED")));
    assertEquals(1, observed.first.get(id).get());
    assertEquals(3, observed.second.get(id).get());
    assertEquals(1, observed.keys.get(id).stream().distinct().count());
    assertTrue(rows(id).stream().allMatch(r -> r.payload().contains("captured")));
  }

  /** delivery.provider-idempotency.same-key */
  @Test
  void idempotentProviderMakesOneEffect() throws Exception {
    UUID id = UUID.randomUUID();
    acceptThenRetry(id, true);
    assertEquals(1, observed.effects.get(id).get());
    assertEquals(1, observed.keys.get(id).stream().distinct().count());
  }

  /** delivery.provider-no-idempotency.duplicate-possible */
  @Test
  void providerWithoutIdempotencyMayMakeTwoEffects() throws Exception {
    UUID id = UUID.randomUUID();
    acceptThenRetry(id, false);
    assertEquals(2, observed.effects.get(id).get());
    assertEquals(1, observed.keys.get(id).stream().distinct().count());
  }

  /** Lets the second listener fail until its bounded attempts are spent; returns the row. */
  private Row exhaust(UUID id, String content) throws InterruptedException {
    observed.poison.add(id);
    publish(new Probe(id, Instant.now(), content, false, false));
    await(() -> failed(id));
    Thread.sleep(20);
    recovery.retryDue();
    await(() -> rows(id).stream().anyMatch(r -> r.attempts() == 2 && r.status().equals("FAILED")));
    return rows(id).stream().filter(r -> r.status().equals("FAILED")).findFirst().orElseThrow();
  }

  /** The provider accepts, the listener crashes before acknowledging, one retry completes it. */
  private void acceptThenRetry(UUID id, boolean providerIdempotency) throws InterruptedException {
    publish(new Probe(id, Instant.now(), "captured", true, providerIdempotency));
    await(() -> failed(id));
    Thread.sleep(20);
    recovery.retryDue();
    await(() -> rows(id).stream().allMatch(r -> r.status().equals("COMPLETED")));
  }

  private void publish(Probe event) {
    new TransactionTemplate(transactions).executeWithoutResult(s -> publisher.publishEvent(event));
  }

  private boolean failed(UUID id) {
    return rows(id).stream().anyMatch(r -> r.status().equals("FAILED"));
  }

  private List<Row> rows(UUID id) {
    return jdbc.query(
        "SELECT ID,STATUS,COMPLETION_ATTEMPTS,SERIALIZED_EVENT FROM EVENT_PUBLICATION WHERE SERIALIZED_EVENT LIKE ?",
        (rs, n) ->
            new Row(rs.getObject(1, UUID.class), rs.getString(2), rs.getInt(3), rs.getString(4)),
        "%" + id + "%");
  }

  private static void await(java.util.function.BooleanSupplier test) throws InterruptedException {
    for (int i = 0; i < 200 && !test.getAsBoolean(); i++) Thread.sleep(10);
    assertTrue(test.getAsBoolean());
  }

  private record Row(UUID id, String status, int attempts, String payload) {}

  public record Probe(
      UUID eventId,
      Instant occurredOn,
      String content,
      boolean crashAfterAcceptance,
      boolean providerIdempotency)
      implements IntegrationEvent {}

  static class Observed {
    final ConcurrentMap<UUID, AtomicInteger> first = new ConcurrentHashMap<>(),
        second = new ConcurrentHashMap<>(),
        effects = new ConcurrentHashMap<>();
    final ConcurrentMap<UUID, List<String>> keys = new ConcurrentHashMap<>(),
        contents = new ConcurrentHashMap<>();
    final Set<UUID> poison = ConcurrentHashMap.newKeySet();
    final Set<String> accepted = ConcurrentHashMap.newKeySet();
    final AtomicInteger inboxDeliveries = new AtomicInteger();
  }

  static class FirstListener {
    final Observed state;

    FirstListener(Observed state) {
      this.state = state;
    }

    @ApplicationModuleListener
    public void first(Probe event) {
      state.first.computeIfAbsent(event.eventId(), k -> new AtomicInteger()).incrementAndGet();
    }
  }

  static class SecondListener {
    final Observed state;

    SecondListener(Observed state) {
      this.state = state;
    }

    @ApplicationModuleListener
    public void second(Probe event) {
      int attempt =
          state.second.computeIfAbsent(event.eventId(), k -> new AtomicInteger()).incrementAndGet();
      String key = event.eventId() + ":" + SecondListener.class.getName() + ":provider-effect";
      state.keys.computeIfAbsent(event.eventId(), k -> new CopyOnWriteArrayList<>()).add(key);
      state
          .contents
          .computeIfAbsent(event.eventId(), k -> new CopyOnWriteArrayList<>())
          .add(event.content());
      if (state.poison.contains(event.eventId()))
        throw new IllegalStateException("provider unavailable");
      if (!event.providerIdempotency() || state.accepted.add(key))
        state.effects.computeIfAbsent(event.eventId(), k -> new AtomicInteger()).incrementAndGet();
      if (event.crashAfterAcceptance() && attempt == 1)
        throw new IllegalStateException("crash before local acknowledgement");
    }
  }

  @TestConfiguration
  static class Listeners {
    @Bean
    Observed observed() {
      return new Observed();
    }

    @Bean
    FirstListener firstListener(Observed state) {
      return new FirstListener(state);
    }

    @Bean
    SecondListener secondListener(Observed state) {
      return new SecondListener(state);
    }
  }
}
