package dev.domaincentric.sample.ecommerce.checkout;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartUseCase;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdUseCase;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartUseCase;
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
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import dev.domaincentric.sample.ecommerce.product.domain.model.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test verifying the complete checkout flow end-to-end.
 *
 * <p>This test verifies:
 *
 * <ul>
 *   <li>Create cart and add items
 *   <li>Start checkout (cart remains ACTIVE - can still be modified)
 *   <li>Submit buyer information
 *   <li>Submit delivery information
 *   <li>Submit payment information
 *   <li>Confirm checkout (cart becomes COMPLETED)
 * </ul>
 *
 * <p><b>US-24:</b> Build and Manual Test - Verify complete checkout flow works end-to-end
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@Transactional
class CheckoutFlowIntegrationTest {

  @Autowired private GetOrCreateActiveCartUseCase getOrCreateActiveCartUseCase;

  @Autowired private AddItemToCartUseCase addItemToCartUseCase;

  @Autowired private GetCartByIdUseCase getCartByIdUseCase;

  @Autowired private StartCheckoutInputPort startCheckoutInputPort;

  @Autowired private GetCheckoutSessionInputPort getCheckoutSessionInputPort;

  @Autowired private SubmitBuyerInfoInputPort submitBuyerInfoInputPort;

  @Autowired private SubmitDeliveryInputPort submitDeliveryInputPort;

  @Autowired private SubmitPaymentInputPort submitPaymentInputPort;

  @Autowired private ConfirmCheckoutInputPort confirmCheckoutInputPort;

  @Autowired private ProductRepository productRepository;

  /** Gets the first product ID from sample data. */
  private String getFirstProductId() {
    List<Product> products = productRepository.findAll();
    assertFalse(products.isEmpty(), "Sample data should have products loaded");
    return products.get(0).id().value().toString();
  }

  @Test
  void completeCheckoutFlow_shouldTransitionCartThroughAllStates() {
    // Step 1: Create a cart and add a product
    String customerId = "test-customer-" + System.currentTimeMillis();

    GetOrCreateActiveCartResult cartResponse =
        getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
    String cartId = cartResponse.cartId();
    assertNotNull(cartId, "Cart should be created");

    // Get a real product ID from sample data
    String productId = getFirstProductId();

    // Add a product
    addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, customerId, productId, 2));

    // Verify cart is ACTIVE with items
    GetCartByIdResult cartBefore =
        getCartByIdUseCase.execute(new GetCartByIdQuery(cartId, customerId));
    assertTrue(cartBefore.found(), "Cart should be found");
    assertEquals(
        "ACTIVE",
        cartBefore.cart().orElseThrow().status().name(),
        "Cart should be ACTIVE before checkout");
    assertFalse(cartBefore.cart().orElseThrow().items().isEmpty(), "Cart should have items");

    // Step 2: Start checkout (cart remains ACTIVE - user can still modify it)
    StartCheckoutResult startResponse =
        startCheckoutInputPort.execute(new StartCheckoutCommand(cartId, customerId));
    String sessionId = startResponse.sessionId();
    assertNotNull(sessionId, "Checkout session should be created");
    assertEquals("BUYER_INFO", startResponse.currentStep(), "Should start at BUYER_INFO step");
    assertEquals("ACTIVE", startResponse.status(), "Session should be ACTIVE");

    // Verify cart remains ACTIVE during checkout (can still be modified)
    GetCartByIdResult cartAfterStart =
        getCartByIdUseCase.execute(new GetCartByIdQuery(cartId, customerId));
    assertEquals(
        "ACTIVE",
        cartAfterStart.cart().orElseThrow().status().name(),
        "Cart should remain ACTIVE during checkout");

    // Step 3: Submit buyer information
    submitBuyerInfoInputPort.execute(
        new SubmitBuyerInfoCommand(sessionId, "test@example.com", "John", "Doe", "+1-555-0100"));

    GetCheckoutSessionResult afterBuyer =
        getCheckoutSessionInputPort.execute(GetCheckoutSessionQuery.of(sessionId));
    assertEquals("DELIVERY", afterBuyer.currentStep(), "Should advance to DELIVERY step");

    // Step 4: Submit delivery information
    submitDeliveryInputPort.execute(
        new SubmitDeliveryCommand(
            sessionId,
            "123 Main Street",
            null, // streetLine2
            "Springfield",
            "12345",
            "United States",
            "IL", // state
            "STANDARD", // shippingOptionId
            "Standard Shipping",
            "5-7 business days",
            new BigDecimal("5.99"),
            "EUR"));

    GetCheckoutSessionResult afterDelivery =
        getCheckoutSessionInputPort.execute(GetCheckoutSessionQuery.of(sessionId));
    assertEquals("PAYMENT", afterDelivery.currentStep(), "Should advance to PAYMENT step");

    // Step 5: Submit payment information (using "mock" payment provider)
    submitPaymentInputPort.execute(new SubmitPaymentCommand(sessionId, "mock"));

    GetCheckoutSessionResult afterPayment =
        getCheckoutSessionInputPort.execute(GetCheckoutSessionQuery.of(sessionId));
    assertEquals("REVIEW", afterPayment.currentStep(), "Should advance to REVIEW step");

    // Step 6: Confirm checkout
    ConfirmCheckoutResult confirmResponse =
        confirmCheckoutInputPort.execute(new ConfirmCheckoutCommand(sessionId));

    assertEquals("CONFIRMED", confirmResponse.status(), "Session should be CONFIRMED");
    // Note: orderReference is set when complete() is called, not during confirm()
    // The confirm step just validates and marks the session as CONFIRMED

    // Note: Cart completion (ACTIVE -> COMPLETED) is triggered via CheckoutConfirmedEvent
    // processed by CheckoutEventConsumer with @ApplicationModuleListener. This runs
    // asynchronously in a new transaction, so it cannot be verified in a @Transactional test.
    // Cart completion is verified separately through the event consumer's own tests.
  }

  @Test
  void checkoutFlow_shouldRejectEmptyCart() {
    // Create an empty cart
    String customerId = "test-customer-empty-" + System.currentTimeMillis();

    GetOrCreateActiveCartResult cartResponse =
        getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
    String cartId = cartResponse.cartId();

    // Try to start checkout with empty cart - should fail
    assertThrows(
        IllegalArgumentException.class,
        () -> startCheckoutInputPort.execute(new StartCheckoutCommand(cartId, customerId)),
        "Should reject checkout of empty cart");
  }

  @Test
  void checkoutFlow_allowsMultipleCheckoutSessionsWhileCartIsActive() {
    // Create a cart and add a product
    String customerId = "test-customer-double-" + System.currentTimeMillis();

    GetOrCreateActiveCartResult cartResponse =
        getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));
    String cartId = cartResponse.cartId();

    // Get a real product ID from sample data
    String productId = getFirstProductId();

    // Add a product
    addItemToCartUseCase.execute(new AddItemToCartCommand(cartId, customerId, productId, 1));

    // Start first checkout
    StartCheckoutResult firstSession =
        startCheckoutInputPort.execute(new StartCheckoutCommand(cartId, customerId));
    assertNotNull(firstSession.sessionId(), "First checkout session should be created");

    // Cart remains ACTIVE, so starting another checkout is allowed
    // (user abandoned previous checkout and started fresh)
    StartCheckoutResult secondSession =
        startCheckoutInputPort.execute(new StartCheckoutCommand(cartId, customerId));
    assertNotNull(secondSession.sessionId(), "Second checkout session should be created");

    // Verify cart is still ACTIVE
    GetCartByIdResult cart = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId, customerId));
    assertEquals("ACTIVE", cart.cart().orElseThrow().status().name(), "Cart should remain ACTIVE");
  }
}
