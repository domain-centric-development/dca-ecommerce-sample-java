package dev.domaincentric.sample.ecommerce;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.product.CompositeArticleDataAdapter;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ArticleDataPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartUseCase;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdUseCase;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartUseCase;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartArticle;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartItem;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitdelivery.SubmitDeliveryCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitdelivery.SubmitDeliveryInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutResult;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.inventory.api.InventoryService;
import dev.domaincentric.sample.ecommerce.pricing.api.PricingService;
import dev.domaincentric.sample.ecommerce.pricing.application.setproductprice.SetProductPriceCommand;
import dev.domaincentric.sample.ecommerce.pricing.application.setproductprice.SetProductPriceInputPort;
import dev.domaincentric.sample.ecommerce.product.api.ProductCatalogService;
import dev.domaincentric.sample.ecommerce.product.api.ProductCatalogService.ProductInfo;
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import dev.domaincentric.sample.ecommerce.product.domain.model.Category;
import dev.domaincentric.sample.ecommerce.product.domain.model.ImageUrl;
import dev.domaincentric.sample.ecommerce.product.domain.model.Product;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductDescription;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductFactory;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductName;
import dev.domaincentric.sample.ecommerce.product.domain.model.SKU;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests verifying the article data flow across bounded contexts.
 *
 * <p>This test suite validates:
 *
 * <ul>
 *   <li>CompositeArticleDataAdapter aggregates data from ProductCatalogService, PricingService, and
 *       InventoryService (with fallback logic during transition)
 *   <li>StartCheckoutUseCase uses CheckoutArticleDataPort for pricing
 *   <li>ConfirmCheckoutUseCase uses resolver pattern for price validation
 *   <li>GetCartByIdUseCase enriches cart items with fresh pricing data
 *   <li>Price change detection works correctly
 * </ul>
 *
 * <p><b>US-87:</b> Integration Tests for Article Data Flow
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@Transactional
class ArticleDataFlowIntegrationTest {

  @Autowired private ProductRepository productRepository;

  @Autowired private ProductCatalogService productCatalogService;

  @Autowired private PricingService pricingService;

  @Autowired private InventoryService inventoryService;

  @Autowired private ArticleDataPort articleDataPort;

  @Autowired private GetOrCreateActiveCartUseCase getOrCreateActiveCartUseCase;

  @Autowired private AddItemToCartUseCase addItemToCartUseCase;

  @Autowired private GetCartByIdUseCase getCartByIdUseCase;

  @Autowired private StartCheckoutInputPort startCheckoutInputPort;

  @Autowired private GetCheckoutSessionInputPort getCheckoutSessionInputPort;

  @Autowired private SubmitBuyerInfoInputPort submitBuyerInfoInputPort;

  @Autowired private SubmitDeliveryInputPort submitDeliveryInputPort;

  @Autowired private SubmitPaymentInputPort submitPaymentInputPort;

  @Autowired private ConfirmCheckoutInputPort confirmCheckoutInputPort;

  @Autowired private SetProductPriceInputPort setProductPriceInputPort;

  private ProductId testProductId;
  private String testProductIdString;

  @BeforeEach
  void setUp() {
    // Get first available product from sample data
    List<Product> products = productRepository.findAll();
    assertFalse(products.isEmpty(), "Sample data should have products loaded");
    testProductId = products.get(0).id();
    testProductIdString = testProductId.value().toString();
  }

  @Nested
  @DisplayName("CompositeArticleDataAdapter Tests")
  class CompositeArticleDataAdapterTests {

    @Test
    @DisplayName("Should aggregate data from available OHS services")
    void shouldAggregateDataFromOhsServices() {
      // When: Fetching article data through the composite adapter
      Optional<CartArticle> result = articleDataPort.getArticleData(testProductId);

      // Then: Data should be present (with fallback logic for pricing/inventory if needed)
      assertTrue(result.isPresent(), "Article data should be found");
      CartArticle cartArticle = result.get();

      // Verify product name comes from ProductCatalogService
      Optional<ProductInfo> productInfo = productCatalogService.getProductInfo(testProductId);
      assertTrue(productInfo.isPresent(), "Product info should be available");
      assertEquals(
          productInfo.get().name(),
          cartArticle.name(),
          "Product name should come from ProductCatalogService");

      // Verify pricing is available (from PricingService or fallback to Product context)
      assertNotNull(cartArticle.currentPrice(), "Price should be resolved");
      assertTrue(
          cartArticle.currentPrice().amount().compareTo(BigDecimal.ZERO) > 0,
          "Price should be greater than zero");

      // Verify stock data is available
      assertTrue(cartArticle.availableStock() >= 0, "Stock should not be negative");
    }

    @Test
    @DisplayName("Should fetch bulk article data for multiple products")
    void shouldFetchBulkArticleData() {
      // Given: Multiple products from sample data
      List<Product> products = productRepository.findAll();
      assertTrue(products.size() >= 2, "Need at least 2 products for bulk test");

      Set<ProductId> productIds = Set.of(products.get(0).id(), products.get(1).id());

      // When: Fetching article data for multiple products
      Map<ProductId, CartArticle> result = articleDataPort.getArticleData(productIds);

      // Then: All requested products should have data
      assertEquals(2, result.size(), "Should return data for all requested products");

      for (ProductId productId : productIds) {
        assertTrue(
            result.containsKey(productId), "Should have data for product: " + productId.value());
        CartArticle cartArticle = result.get(productId);
        assertNotNull(cartArticle.name(), "Product should have name");
        assertNotNull(cartArticle.currentPrice(), "Product should have price");
      }
    }

    @Test
    @DisplayName("Should return empty for empty input")
    void shouldReturnEmptyForEmptyInput() {
      Map<ProductId, CartArticle> result = articleDataPort.getArticleData(Set.of());
      assertTrue(result.isEmpty(), "Should return empty map for empty input");
    }

    @Test
    @DisplayName("A product Pricing has not seen yet is offered as unavailable")
    void aProductPricingHasNotSeenYetIsOfferedAsUnavailable() {
      // Written straight into the catalog's repository, so no ProductCreatedEvent is ever
      // published:
      // this is the state of every new product until Pricing and Inventory have consumed that
      // event.
      final Product unpriced =
          new ProductFactory()
              .createProduct(
                  SKU.of("UNPRICED-1"),
                  ProductName.of("Unpriced Thing"),
                  ProductDescription.of("Nobody has priced this yet"),
                  Category.of("Home"),
                  ImageUrl.of("/images/products/ddd-book.webp"),
                  Money.euro(9.99),
                  5);
      productRepository.save(unpriced);
      try {
        final Optional<CartArticle> article = articleDataPort.getArticleData(unpriced.id());

        assertTrue(article.isPresent(), "The article is offered, not hidden");
        assertFalse(article.get().isAvailable(), "Without a price it cannot be sold");
        assertEquals(0, article.get().availableStock(), "Nothing is sellable either");
        assertFalse(article.get().hasStockFor(1), "Adding it to a cart is refused");
      } finally {
        // The catalog is shared with the other tests in this class, and an unsellable product would
        // fail whichever of them picks it up
        productRepository.deleteById(unpriced.id());
      }
    }

    @Test
    @DisplayName("Should return empty Optional for unknown product")
    void shouldReturnEmptyForUnknownProduct() {
      ProductId unknownId = ProductId.of("00000000-0000-0000-0000-000000000000");
      Optional<CartArticle> result = articleDataPort.getArticleData(unknownId);
      assertTrue(result.isEmpty(), "Should return empty for unknown product");
    }

    @Test
    @DisplayName("CompositeArticleDataAdapter is properly wired as ArticleDataPort")
    void compositeAdapterIsProperlyWired() {
      // Verify that the ArticleDataPort is implemented by CompositeArticleDataAdapter
      assertTrue(
          articleDataPort instanceof CompositeArticleDataAdapter,
          "ArticleDataPort should be implemented by CompositeArticleDataAdapter");
    }
  }

  @Nested
  @DisplayName("StartCheckoutUseCase with Resolver Tests")
  class StartCheckoutUseCaseTests {

    @Test
    @DisplayName("Should use fresh pricing from CheckoutArticleDataPort when starting checkout")
    void shouldUseFreshPricingWhenStartingCheckout() {
      // Given: A cart with items
      String customerId = "test-customer-checkout-" + System.currentTimeMillis();
      GetOrCreateActiveCartResult cartResponse =
          getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
      String cartId = cartResponse.cartId();

      addItemToCartUseCase.execute(
          new AddItemToCartCommand(cartId, customerId, testProductIdString, 2));

      // When: Starting checkout
      StartCheckoutResult result =
          startCheckoutInputPort.execute(new StartCheckoutCommand(cartId, customerId));

      // Then: Checkout session should have line items with current prices
      assertNotNull(result.sessionId(), "Session should be created");
      assertFalse(result.lineItems().isEmpty(), "Should have line items");

      // Verify line item has pricing from CheckoutArticleDataPort
      StartCheckoutResult.LineItemData lineItem = result.lineItems().get(0);
      assertEquals(testProductIdString, lineItem.productId());
      assertNotNull(lineItem.unitPrice(), "Unit price should be set from pricing service");
      assertEquals(2, lineItem.quantity());
    }

    @Test
    @DisplayName("Should include product name from ProductCatalogService in line items")
    void shouldIncludeProductNameInLineItems() {
      // Given: A cart with items
      String customerId = "test-customer-name-" + System.currentTimeMillis();
      GetOrCreateActiveCartResult cartResponse =
          getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
      String cartId = cartResponse.cartId();

      addItemToCartUseCase.execute(
          new AddItemToCartCommand(cartId, customerId, testProductIdString, 1));

      // When: Starting checkout
      StartCheckoutResult result =
          startCheckoutInputPort.execute(new StartCheckoutCommand(cartId, customerId));

      // Then: Line item should have product name
      StartCheckoutResult.LineItemData lineItem = result.lineItems().get(0);
      Optional<ProductInfo> productInfo = productCatalogService.getProductInfo(testProductId);
      assertTrue(productInfo.isPresent());
      assertEquals(
          productInfo.get().name(),
          lineItem.productName(),
          "Line item should have product name from ProductCatalogService");
    }
  }

  @Nested
  @DisplayName("ConfirmCheckoutUseCase with Resolver Tests")
  class ConfirmCheckoutUseCaseTests {

    @Test
    @DisplayName("Should validate pricing with resolver during confirmation")
    void shouldValidatePricingDuringConfirmation() {
      // Given: A complete checkout session ready to confirm
      String customerId = "test-customer-confirm-" + System.currentTimeMillis();
      GetOrCreateActiveCartResult cartResponse =
          getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
      String cartId = cartResponse.cartId();

      addItemToCartUseCase.execute(
          new AddItemToCartCommand(cartId, customerId, testProductIdString, 1));

      StartCheckoutResult startResult =
          startCheckoutInputPort.execute(new StartCheckoutCommand(cartId, customerId));
      String sessionId = startResult.sessionId();

      // Complete all checkout steps
      completeCheckoutSteps(sessionId);

      // When: Confirming checkout
      ConfirmCheckoutResult result =
          confirmCheckoutInputPort.execute(new ConfirmCheckoutCommand(sessionId));

      // Then: Checkout should be confirmed successfully
      assertEquals("CONFIRMED", result.status(), "Checkout should be confirmed");
      assertNotNull(result.totalAmount(), "Total should be calculated");
    }

    @Test
    @DisplayName("Should use resolver to fetch fresh pricing during confirmation")
    void shouldUseFreshPricingDuringConfirmation() {
      // Given: A checkout session
      String customerId = "test-customer-pricing-" + System.currentTimeMillis();
      GetOrCreateActiveCartResult cartResponse =
          getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
      String cartId = cartResponse.cartId();

      addItemToCartUseCase.execute(
          new AddItemToCartCommand(cartId, customerId, testProductIdString, 1));

      StartCheckoutResult startResult =
          startCheckoutInputPort.execute(new StartCheckoutCommand(cartId, customerId));
      String sessionId = startResult.sessionId();

      // Complete all checkout steps
      completeCheckoutSteps(sessionId);

      // Get session before confirmation
      GetCheckoutSessionResult beforeConfirm =
          getCheckoutSessionInputPort.execute(GetCheckoutSessionQuery.of(sessionId));
      assertEquals("REVIEW", beforeConfirm.currentStep(), "Should be at review step");

      // When: Confirming - the resolver will fetch fresh prices from CheckoutArticleDataPort
      ConfirmCheckoutResult result =
          confirmCheckoutInputPort.execute(new ConfirmCheckoutCommand(sessionId));

      // Then: Confirmation should use prices from the resolver
      assertEquals("CONFIRMED", result.status());
    }
  }

  @Nested
  @DisplayName("GetCartByIdUseCase Enrichment Tests")
  class GetCartByIdUseCaseEnrichmentTests {

    @Test
    @DisplayName("Should enrich cart items with fresh pricing data")
    void shouldEnrichCartItemsWithFreshPricing() {
      // Given: A cart with items
      String customerId = "test-customer-enrich-" + System.currentTimeMillis();
      GetOrCreateActiveCartResult cartResponse =
          getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
      String cartId = cartResponse.cartId();

      addItemToCartUseCase.execute(
          new AddItemToCartCommand(cartId, customerId, testProductIdString, 2));

      // When: Getting cart by ID
      GetCartByIdResult result =
          getCartByIdUseCase.execute(new GetCartByIdQuery(cartId, customerId));

      // Then: Cart items should have current pricing from ArticleDataPort
      assertTrue(result.found(), "Cart should be found");
      EnrichedCart cart = result.cart().orElseThrow();
      assertFalse(cart.items().isEmpty(), "Cart should have items");

      EnrichedCartItem item = cart.items().get(0);

      // Verify current price is fetched from pricing service
      assertNotNull(item.currentArticle().currentPrice(), "Current price should be set");
      assertNotNull(
          item.currentArticle().currentPrice().currency(), "Current price currency should be set");
    }

    @Test
    @DisplayName("Should include both price at addition and current price")
    void shouldIncludeBothPriceAtAdditionAndCurrentPrice() {
      // Given: A cart with items
      String customerId = "test-customer-prices-" + System.currentTimeMillis();
      GetOrCreateActiveCartResult cartResponse =
          getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
      String cartId = cartResponse.cartId();

      addItemToCartUseCase.execute(
          new AddItemToCartCommand(cartId, customerId, testProductIdString, 1));

      // When: Getting cart
      GetCartByIdResult result =
          getCartByIdUseCase.execute(new GetCartByIdQuery(cartId, customerId));

      // Then: Item should have both price at addition and current price
      EnrichedCartItem item = result.cart().orElseThrow().items().get(0);

      assertNotNull(item.priceAtAddition(), "Price at addition should be set");
      assertNotNull(
          item.priceAtAddition().value().currency(), "Price at addition currency should be set");
      assertNotNull(item.currentArticle().currentPrice(), "Current price should be set");
      assertNotNull(
          item.currentArticle().currentPrice().currency(), "Current price currency should be set");
    }
  }

  @Nested
  @DisplayName("Price Change Detection Tests")
  class PriceChangeDetectionTests {

    @Test
    @DisplayName("Should detect when price has not changed")
    void shouldDetectNoPriceChange() {
      // Given: A cart with items (price at addition equals current price)
      String customerId = "test-customer-nochange-" + System.currentTimeMillis();
      GetOrCreateActiveCartResult cartResponse =
          getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
      String cartId = cartResponse.cartId();

      addItemToCartUseCase.execute(
          new AddItemToCartCommand(cartId, customerId, testProductIdString, 1));

      // When: Getting cart immediately (price hasn't changed)
      GetCartByIdResult result =
          getCartByIdUseCase.execute(new GetCartByIdQuery(cartId, customerId));

      // Then: Price should not be marked as changed
      EnrichedCartItem item = result.cart().orElseThrow().items().get(0);
      assertFalse(
          item.hasPriceChanged(),
          "Price should not be marked as changed when current equals original");

      // Verify prices are equal
      assertEquals(
          0,
          item.priceAtAddition()
              .value()
              .amount()
              .compareTo(item.currentArticle().currentPrice().amount()),
          "Prices should be equal");
    }

    @Test
    @DisplayName("Should detect when price has changed")
    void shouldDetectPriceChange() {
      // Given: A cart with items
      String customerId = "test-customer-change-" + System.currentTimeMillis();
      GetOrCreateActiveCartResult cartResponse =
          getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
      String cartId = cartResponse.cartId();

      addItemToCartUseCase.execute(
          new AddItemToCartCommand(cartId, customerId, testProductIdString, 1));

      // Get initial cart state
      GetCartByIdResult initialResult =
          getCartByIdUseCase.execute(new GetCartByIdQuery(cartId, customerId));
      EnrichedCartItem initialItem = initialResult.cart().orElseThrow().items().get(0);
      BigDecimal originalPrice = initialItem.priceAtAddition().value().amount();

      // When: Price changes (update to a different price)
      BigDecimal newPrice = originalPrice.add(BigDecimal.valueOf(10.00));
      setProductPriceInputPort.execute(
          new SetProductPriceCommand(testProductIdString, newPrice, "EUR"));

      // Then: Getting cart should show price changed
      GetCartByIdResult updatedResult =
          getCartByIdUseCase.execute(new GetCartByIdQuery(cartId, customerId));
      EnrichedCartItem updatedItem = updatedResult.cart().orElseThrow().items().get(0);

      assertTrue(
          updatedItem.hasPriceChanged(), "Price should be marked as changed after price update");
      assertEquals(
          0,
          originalPrice.compareTo(updatedItem.priceAtAddition().value().amount()),
          "Price at addition should remain the original price");
      assertEquals(
          0,
          newPrice.compareTo(updatedItem.currentArticle().currentPrice().amount()),
          "Current price should be the new price");
    }

    @Test
    @DisplayName("Price change detection should work for multiple items")
    void shouldDetectPriceChangeForMultipleItems() {
      // Given: Multiple products
      List<Product> products = productRepository.findAll();
      assertTrue(products.size() >= 2, "Need at least 2 products");

      ProductId product1Id = products.get(0).id();
      ProductId product2Id = products.get(1).id();

      // Create cart with both products
      String customerId = "test-customer-multi-" + System.currentTimeMillis();
      GetOrCreateActiveCartResult cartResponse =
          getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
      String cartId = cartResponse.cartId();

      addItemToCartUseCase.execute(
          new AddItemToCartCommand(cartId, customerId, product1Id.value().toString(), 1));
      addItemToCartUseCase.execute(
          new AddItemToCartCommand(cartId, customerId, product2Id.value().toString(), 1));

      // Get initial prices
      GetCartByIdResult initialResult =
          getCartByIdUseCase.execute(new GetCartByIdQuery(cartId, customerId));
      assertEquals(
          2, initialResult.cart().orElseThrow().items().size(), "Cart should have 2 items");

      // Change price for only first product
      EnrichedCartItem item1Initial =
          initialResult.cart().orElseThrow().items().stream()
              .filter(i -> i.productId().equals(product1Id))
              .findFirst()
              .orElseThrow();

      BigDecimal newPrice =
          item1Initial.priceAtAddition().value().amount().add(BigDecimal.valueOf(5.00));
      setProductPriceInputPort.execute(
          new SetProductPriceCommand(product1Id.value().toString(), newPrice, "EUR"));

      // When: Getting cart
      GetCartByIdResult result =
          getCartByIdUseCase.execute(new GetCartByIdQuery(cartId, customerId));

      // Then: Only first product should show price change
      EnrichedCartItem item1 =
          result.cart().orElseThrow().items().stream()
              .filter(i -> i.productId().equals(product1Id))
              .findFirst()
              .orElseThrow();

      EnrichedCartItem item2 =
          result.cart().orElseThrow().items().stream()
              .filter(i -> i.productId().equals(product2Id))
              .findFirst()
              .orElseThrow();

      assertTrue(item1.hasPriceChanged(), "First product should show price changed");
      assertFalse(item2.hasPriceChanged(), "Second product should not show price changed");
    }
  }

  @Nested
  @DisplayName("End-to-End Article Data Flow Tests")
  class EndToEndFlowTests {

    @Test
    @DisplayName("Complete flow: Cart -> Checkout -> Confirm with fresh pricing at each step")
    void completeFlowWithFreshPricingAtEachStep() {
      // Given: A cart with items
      String customerId = "test-e2e-" + System.currentTimeMillis();
      GetOrCreateActiveCartResult cartResponse =
          getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
      String cartId = cartResponse.cartId();

      addItemToCartUseCase.execute(
          new AddItemToCartCommand(cartId, customerId, testProductIdString, 2));

      // Step 1: Verify cart enrichment
      GetCartByIdResult cartResult =
          getCartByIdUseCase.execute(new GetCartByIdQuery(cartId, customerId));
      assertTrue(cartResult.found());
      assertNotNull(
          cartResult.cart().orElseThrow().items().get(0).currentArticle().currentPrice(),
          "Cart should have fresh pricing from ArticleDataPort");

      // Step 2: Start checkout (uses CheckoutArticleDataPort for pricing)
      StartCheckoutResult startResult =
          startCheckoutInputPort.execute(new StartCheckoutCommand(cartId, customerId));
      assertNotNull(
          startResult.lineItems().get(0).unitPrice(),
          "Checkout should have pricing from CheckoutArticleDataPort");

      // Step 3: Complete checkout steps
      String sessionId = startResult.sessionId();
      completeCheckoutSteps(sessionId);

      // Step 4: Confirm (uses resolver with fresh pricing)
      ConfirmCheckoutResult confirmResult =
          confirmCheckoutInputPort.execute(new ConfirmCheckoutCommand(sessionId));
      assertEquals(
          "CONFIRMED",
          confirmResult.status(),
          "Checkout should be confirmed with resolver validation");
    }

    @Test
    @DisplayName("All three OHS services are accessible")
    void ohsServicesAreAccessible() {
      // Verify ProductCatalogService returns data
      Optional<ProductInfo> productInfo = productCatalogService.getProductInfo(testProductId);
      assertTrue(productInfo.isPresent(), "ProductCatalogService should return product info");

      // Verify PricingService and InventoryService can be called (may or may not have data
      // depending on async initialization, but calls should not fail)
      assertDoesNotThrow(
          () -> pricingService.getPrice(testProductId), "PricingService should be callable");
      assertDoesNotThrow(
          () -> inventoryService.getStock(testProductId), "InventoryService should be callable");

      // Verify composite adapter returns aggregated data (with fallback for pricing if needed)
      Optional<CartArticle> cartArticle = articleDataPort.getArticleData(testProductId);
      assertTrue(cartArticle.isPresent(), "CompositeAdapter should return aggregated data");
      assertEquals(productInfo.get().name(), cartArticle.get().name(), "Product name should match");
    }
  }

  /** Helper method to complete all checkout steps before confirmation. */
  private void completeCheckoutSteps(String sessionId) {
    // Submit buyer info
    submitBuyerInfoInputPort.execute(
        new SubmitBuyerInfoCommand(sessionId, "test@example.com", "John", "Doe", "+1-555-0100"));

    // Submit delivery
    submitDeliveryInputPort.execute(
        new SubmitDeliveryCommand(
            sessionId,
            "123 Main Street",
            null,
            "Springfield",
            "12345",
            "United States",
            "IL",
            "STANDARD",
            "Standard Shipping",
            "5-7 business days",
            new BigDecimal("5.99"),
            "EUR"));

    // Submit payment
    submitPaymentInputPort.execute(new SubmitPaymentCommand(sessionId, "mock"));
  }
}
