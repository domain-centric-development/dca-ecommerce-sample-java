package dev.domaincentric.sample.ecommerce.checkout;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutChangedSinceReviewException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * How "Place Order" translates a refusal for an order that changed since its review — a change made
 * in another tab of the same browser — into the review with its message, against a confirmation and
 * a review the test arranges. That a change refuses the order is the domain's, tested on the
 * session and the review.
 */
// A context of its own: the arranged ports must not reach any other test class.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:order_changed_since_the_review_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@Import({
  ArrangedReview.Configuration.class,
  OrderChangedSinceTheReviewIntegrationTest.ConfirmationArrangedByTheTest.class
})
@DisplayName("An order changed since the review")
class OrderChangedSinceTheReviewIntegrationTest {

  private static final ShippingOption EXPRESS_SHIPPING =
      ShippingOption.of("express", "Express Shipping", "1-2 business days", Money.euro(9.99));

  @Autowired private MockMvc mockMvc;
  @Autowired private ArrangedReview review;
  @Autowired private RefusedConfirmation confirmation;

  @BeforeEach
  void nothingChangedYet() {
    review.reset();
    confirmation.reset();
  }

  @Test
  @DisplayName("Place order after a change in another tab is refused")
  void placeOrderAfterAChangeInAnotherTabIsRefused() throws Exception {
    final GuestCheckout guest = new GuestCheckout(mockMvc);
    final Element firstTabsForm =
        guest.toTheReview().selectFirst("[data-test=review-confirm-form]");
    final String shownFingerprint = review.fingerprint();
    review.shippingToday(EXPRESS_SHIPPING);

    final MvcResult placed = guest.submit(firstTabsForm);

    assertThat(confirmation.commands())
        .as("Place Order carries the fingerprint of the review it was sent from")
        .extracting(ConfirmCheckoutCommand::reviewFingerprint)
        .containsExactly(shownFingerprint);
    assertThat(placed.getResponse().getRedirectedUrl()).isEqualTo(GuestCheckout.REVIEW_STEP);
    final Document reviewAgain = guest.page(GuestCheckout.REVIEW_STEP);
    assertThat(reviewAgain.selectFirst("[data-test=review-error-message]").text())
        .isEqualTo(
            "Your order has changed since you reviewed it. Please check it and place it again.");
    assertThat(
            reviewAgain.selectFirst("[data-test=review-delivery-section]").select("p").eachText())
        .contains("Express Shipping");
    assertThat(reviewAgain.selectFirst("[data-test=confirmation-title]")).isNull();
  }

  @TestConfiguration
  static class ConfirmationArrangedByTheTest {

    @Bean
    @Primary
    RefusedConfirmation refusedConfirmation() {
      return new RefusedConfirmation();
    }
  }

  /** A confirmation that records what it was asked and refuses: the order changed since. */
  static final class RefusedConfirmation implements ConfirmCheckoutInputPort {

    private final List<ConfirmCheckoutCommand> commands = new CopyOnWriteArrayList<>();

    @Override
    public ConfirmCheckoutResult execute(final ConfirmCheckoutCommand command) {
      commands.add(command);
      throw CheckoutChangedSinceReviewException.orderChanged(
          CheckoutSessionId.of(command.sessionId()));
    }

    List<ConfirmCheckoutCommand> commands() {
      return List.copyOf(commands);
    }

    void reset() {
      commands.clear();
    }
  }
}
