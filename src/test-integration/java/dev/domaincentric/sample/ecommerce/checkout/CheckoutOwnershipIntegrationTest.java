package dev.domaincentric.sample.ecommerce.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionNotFoundException;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A session ID says which checkout, never whose.
 *
 * <p>The wizard steps take the session ID from the outside, and the web adapter that resolves the
 * visitor's <i>active</i> session is what keeps the browser flow safe — not the use cases. The rule
 * therefore lives in the use case: every step asks the repository a question scoped to the caller,
 * so a session that is not theirs is indistinguishable from one that does not exist.
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:checkout_ownership;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
class CheckoutOwnershipIntegrationTest {

  @Autowired private GetOrCreateActiveCartInputPort getOrCreateActiveCart;
  @Autowired private AddItemToCartInputPort addItemToCart;
  @Autowired private GetAllProductsInputPort getAllProducts;
  @Autowired private StartCheckoutInputPort startCheckout;
  @Autowired private GetCheckoutSessionInputPort getCheckoutSession;
  @Autowired private SubmitBuyerInfoInputPort submitBuyerInfo;
  @Autowired private SubmitPaymentInputPort submitPayment;
  @Autowired private ConfirmCheckoutInputPort confirmCheckout;

  @Test
  @DisplayName("Reading a session that is not the caller's is indistinguishable from none at all")
  void readingAStrangersSessionFindsNothing() {
    final String owner = "owner-" + System.nanoTime();
    final String stranger = "stranger-" + System.nanoTime();
    final String sessionId = checkoutOf(owner);

    assertThat(getCheckoutSession.execute(GetCheckoutSessionQuery.of(sessionId, owner)).found())
        .isTrue();
    assertThat(getCheckoutSession.execute(GetCheckoutSessionQuery.of(sessionId, stranger)).found())
        .isFalse();
  }

  @Test
  @DisplayName("A stranger cannot fill in the buyer information of somebody else's checkout")
  void submittingBuyerInfoForAStrangersSessionIsRefused() {
    final String owner = "owner-" + System.nanoTime();
    final String stranger = "stranger-" + System.nanoTime();
    final String sessionId = checkoutOf(owner);

    assertThatThrownBy(
            () ->
                submitBuyerInfo.execute(
                    new SubmitBuyerInfoCommand(
                        sessionId, stranger, "eve@example.com", "Eve", "Adams", "+1-555-0199")))
        .isInstanceOf(CheckoutSessionNotFoundException.class)
        .hasMessageContaining("Session not found");

    // The owner is unaffected
    assertThat(
            submitBuyerInfo
                .execute(
                    new SubmitBuyerInfoCommand(
                        sessionId, owner, "ada@example.com", "Ada", "Lovelace", "+1-555-0100"))
                .currentStep())
        .isEqualTo("DELIVERY");
  }

  @Test
  @DisplayName("A stranger cannot pay for or confirm somebody else's checkout")
  void payingForOrConfirmingAStrangersSessionIsRefused() {
    final String owner = "owner-" + System.nanoTime();
    final String stranger = "stranger-" + System.nanoTime();
    final String sessionId = checkoutOf(owner);

    assertThatThrownBy(
            () -> submitPayment.execute(new SubmitPaymentCommand(sessionId, stranger, "mock")))
        .isInstanceOf(CheckoutSessionNotFoundException.class)
        .hasMessageContaining("Session not found");

    assertThatThrownBy(
            () -> confirmCheckout.execute(new ConfirmCheckoutCommand(sessionId, stranger)))
        .isInstanceOf(CheckoutSessionNotFoundException.class)
        .hasMessageContaining("Session not found");
  }

  /** An active checkout session on a cart with one item, belonging to the given customer. */
  private String checkoutOf(final String customerId) {
    final String cartId =
        getOrCreateActiveCart.execute(new GetOrCreateActiveCartCommand(customerId)).cartId();
    addItemToCart.execute(new AddItemToCartCommand(cartId, customerId, anyProductId(), 1));
    return startCheckout.execute(new StartCheckoutCommand(cartId, customerId)).sessionId();
  }

  private String anyProductId() {
    return getAllProducts
        .execute(new GetAllProductsQuery())
        .products()
        .getFirst()
        .productId()
        .value()
        .toString();
  }
}
