package dev.domaincentric.sample.ecommerce.inventory.adapter.outgoing.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.infrastructure.database.PostgresTestDatabase;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@DisplayName("JdbcStockLevelRepository")
class JdbcStockLevelRepositoryTest {

  private static final DataSource DATA_SOURCE =
      PostgresTestDatabase.migrated("stock-level-repository");

  private final JdbcStockLevelRepository stock = new JdbcStockLevelRepository(DATA_SOURCE);
  private final TransactionTemplate transactions =
      new TransactionTemplate(new DataSourceTransactionManager(DATA_SOURCE));

  @BeforeEach
  void emptyTable() {
    new JdbcTemplate(DATA_SOURCE).execute("TRUNCATE stock_levels");
  }

  @Test
  @DisplayName("A saved stock level reads back as it was saved, without events")
  void aSavedStockLevelReadsBack() {
    final StockLevel saved = StockLevel.create(ProductId.generate(), 10);
    saved.reserve(3);
    stock.save(saved);

    final StockLevel loaded = stock.findByProductId(saved.productId()).orElseThrow();

    assertThat(loaded.id()).isEqualTo(saved.id());
    assertThat(loaded.availableQuantity().value()).isEqualTo(10);
    assertThat(loaded.reservedQuantity().value()).isEqualTo(3);
    assertThat(loaded.domainEvents()).isEmpty();
  }

  @Test
  @DisplayName("A change survives only through save")
  void aChangeSurvivesOnlyThroughSave() {
    final StockLevel saved = stock.save(StockLevel.create(ProductId.generate(), 10));

    stock.findById(saved.id()).orElseThrow().reserve(4);

    assertThat(stock.findById(saved.id()).orElseThrow().reservedQuantity().value()).isZero();
  }

  @Test
  @DisplayName("Stock levels of several products are found together")
  void stockLevelsOfSeveralProducts() {
    final StockLevel first = stock.save(StockLevel.create(ProductId.generate(), 1));
    final StockLevel second = stock.save(StockLevel.create(ProductId.generate(), 2));

    assertThat(stock.findByProductIds(List.of(first.productId(), second.productId())))
        .extracting(StockLevel::id)
        .containsExactlyInAnyOrder(first.id(), second.id());
    assertThat(stock.findByProductIds(List.of())).isEmpty();
  }

  @Test
  @DisplayName("A stock level read in a transaction holds a second one back until it ends")
  void aTransactionalReadLocksTheStockLevel() throws Exception {
    final StockLevel saved = stock.save(StockLevel.create(ProductId.generate(), 5));
    final CountDownLatch firstHoldsTheRow = new CountDownLatch(1);
    final CountDownLatch releaseFirst = new CountDownLatch(1);

    try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
      final Future<?> first =
          pool.submit(
              () ->
                  transactions.executeWithoutResult(
                      status -> {
                        final StockLevel level =
                            stock.findByProductId(saved.productId()).orElseThrow();
                        firstHoldsTheRow.countDown();
                        await(releaseFirst);
                        level.reserve(5);
                        stock.save(level);
                      }));
      await(firstHoldsTheRow);

      final Future<Integer> second =
          pool.submit(
              () ->
                  transactions.execute(
                      status ->
                          stock
                              .findByProductId(saved.productId())
                              .orElseThrow()
                              .reservedQuantity()
                              .value()));

      Thread.sleep(300);
      assertThat(second.isDone()).as("the second read waits for the first transaction").isFalse();

      releaseFirst.countDown();
      first.get(10, TimeUnit.SECONDS);
      assertThat(second.get(10, TimeUnit.SECONDS))
          .as("the second read sees what the first committed")
          .isEqualTo(5);
    }
  }

  private static void await(final CountDownLatch latch) {
    try {
      if (!latch.await(10, TimeUnit.SECONDS)) {
        throw new IllegalStateException("timed out");
      }
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }
}
