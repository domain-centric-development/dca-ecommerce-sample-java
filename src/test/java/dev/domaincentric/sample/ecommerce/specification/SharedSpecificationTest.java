package dev.domaincentric.sample.ecommerce.specification;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.*;
import dev.domaincentric.sample.ecommerce.cart.domain.model.*;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;

/**
 * Thin adapters: each scenario drives production domain methods; no second domain implementation.
 * Runs only when the build was given the local specification checkout ({@code
 * -Pspecification.path=../dca-sample-specification}); otherwise the class is skipped.
 */
@org.junit.jupiter.api.condition.EnabledIfSystemProperty(
    named = "specification.path",
    matches = ".+",
    disabledReason = "shared specification not supplied (-Pspecification.path)")
class SharedSpecificationTest {
  static final ObjectMapper JSON = new ObjectMapper();
  static final Path ROOT = Path.of(System.getProperty("specification.path", "."));

  @Test
  void everyVectorFamilyHasAnAdapterAndExceptionsAreCurrent() throws Exception {
    try (var files = Files.list(ROOT.resolve("vectors"))) {
      assertEquals(
          Set.of(
              "money.json",
              "quantity.json",
              "product.json",
              "cart-reconciliation.json",
              "checkout.json",
              "delivery.json"),
          files
              .filter(p -> p.toString().endsWith(".json"))
              .map(p -> p.getFileName().toString())
              .collect(java.util.stream.Collectors.toSet()));
    }
    var ids = new HashSet<String>();
    try (var files = Files.list(ROOT.resolve("vectors"))) {
      for (Path path : files.filter(p -> p.toString().endsWith(".json")).toList())
        for (JsonNode node : JSON.readTree(path.toFile()))
          assertTrue(ids.add(node.path("id").asText()), "Duplicate vector id");
    }
    for (String line : Files.readAllLines(ROOT.resolve("exceptions.md"))) {
      if (!line.startsWith("|")) continue;
      String[] cells = line.split("\\|", -1);
      if (cells.length < 8 || cells[1].trim().equals("Id") || cells[1].contains("---")) continue;
      assertFalse(
          java.time.LocalDate.parse(cells[6].trim()).isBefore(java.time.LocalDate.now()),
          "Expired specification exception: " + cells[1]);
    }
  }

  @TestFactory
  Stream<DynamicTest> sharedVectors() throws Exception {
    var tests = new ArrayList<DynamicTest>();
    for (String file :
        List.of("money.json", "quantity.json", "product.json", "cart-reconciliation.json")) {
      for (JsonNode vector : JSON.readTree(ROOT.resolve("vectors/" + file).toFile())) {
        tests.add(DynamicTest.dynamicTest(vector.path("id").asText(), () -> run(file, vector)));
      }
    }
    return tests.stream();
  }

  static void run(String file, JsonNode v) throws Exception {
    switch (file) {
      case "money.json" -> {
        org.junit.jupiter.api.function.Executable action =
            () -> {
              Money result =
                  Money.of(
                      new BigDecimal(v.path("amount").asText()),
                      Currency.getInstance(v.path("currency").asText()));
              if (v.has("subtract"))
                result =
                    result.subtract(
                        Money.of(new BigDecimal(v.path("subtract").asText()), result.currency()));
              if (v.path("accept").asBoolean())
                assertEquals(v.path("normalized").asText(), result.amount().toPlainString());
            };
        if (v.path("accept").asBoolean()) assertDoesNotThrow(action);
        else assertThrows(IllegalArgumentException.class, action);
      }
      case "quantity.json" ->
          assertThrows(
              IllegalArgumentException.class, () -> Quantity.of(v.path("quantity").asInt()));
      case "cart-reconciliation.json" -> reconciliation(v);
      case "product.json" -> product(v);
      default -> fail("Vector has no adapter: " + v.path("id"));
    }
  }

  static void reconciliation(JsonNode v) {
    var cart = new ShoppingCart(CartId.generate(), CustomerId.of("specification"));
    var product = ProductId.generate();
    var price = Price.of(Money.euro(10));
    cart.addItem(product, Quantity.of(v.path("initial").asInt()), price);
    String snapshot = cart.items().get(0).positionSnapshot();
    for (JsonNode edit : v.path("edits")) {
      if (edit.has("add")) cart.addItem(product, Quantity.of(edit.path("add").asInt()), price);
      else if (edit.has("set"))
        cart.updateItemQuantity(cart.items().get(0).id(), Quantity.of(edit.path("set").asInt()));
      else if (edit.has("remove")) cart.removeItemByProductId(product);
      else if (edit.has("other"))
        cart.addItem(ProductId.generate(), Quantity.of(edit.path("other").asInt()), price);
      else fail("Unknown edit " + edit);
    }
    var later = cart.items().stream().map(CartItem::positionSnapshot).toList();
    // A repository round trip must preserve the allocation watermark and intervals.
    cart =
        ShoppingCart.reconstitute(
            cart.id(),
            cart.customerId(),
            cart.status(),
            cart.items().stream()
                .map(
                    i ->
                        new ShoppingCart.StoredItem(
                            i.id(),
                            i.productId(),
                            i.quantity(),
                            i.priceAtAddition(),
                            i.storedUnits()))
                .toList());
    cart.reconcileCheckout("session-1", List.of(snapshot));
    if (v.path("overlap").asBoolean()) cart.reconcileCheckout("session-2", later);
    assertEquals(v.path("remaining").asInt(), cart.totalQuantity());
    assertTrue(cart.isActive());
    cart.clearDomainEvents();
    cart.reconcileCheckout("session-1", List.of(snapshot));
    assertEquals(v.path("remaining").asInt(), cart.totalQuantity());
    assertTrue(cart.domainEvents().isEmpty());
  }

  static void product(JsonNode v) {
    org.junit.jupiter.api.function.Executable action =
        () -> {
          var product =
              new dev.domaincentric.sample.ecommerce.product.domain.model.ProductFactory()
                  .createBasicProduct(
                      dev.domaincentric.sample.ecommerce.product.domain.model.SKU.of("SPEC-1"),
                      dev.domaincentric.sample.ecommerce.product.domain.model.ProductName.of(
                          "Specification product"),
                      dev.domaincentric.sample.ecommerce.product.domain.model.Category.of("Test"),
                      Price.of(Money.euro(1)),
                      v.path("stock").asInt());
          assertEquals(1, product.domainEvents().size());
          if (v.has("fields")) {
            var captured = new ArrayList<Object>();
            new dev.domaincentric.sample.ecommerce.product.adapter.outgoing.event
                    .ProductCreatedEventPublisher(captured::add)
                .on(
                    (dev.domaincentric.sample.ecommerce.product.domain.event.ProductCreated)
                        product.domainEvents().get(0));
            var names =
                Arrays.stream(captured.get(0).getClass().getRecordComponents())
                    .map(java.lang.reflect.RecordComponent::getName)
                    .sorted()
                    .toList();
            var expected = new ArrayList<String>();
            v.path("fields").forEach(f -> expected.add(f.asText()));
            Collections.sort(expected);
            assertEquals(expected, names);
          }
        };
    if (v.path("accept").asBoolean()) assertDoesNotThrow(action);
    else assertThrows(IllegalArgumentException.class, action);
  }
}
