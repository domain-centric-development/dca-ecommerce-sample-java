package dev.domaincentric.sample.ecommerce.checkout;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview.GetCheckoutReviewQuery;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * How the review page shows what the review at today's prices answers, against a review the test
 * arranges: today's price is whatever the test says it is. That the review takes the price from
 * Pricing is {@link OrderAtTodaysPriceAfterReopeningTheReviewIntegrationTest}'s.
 */
// A context of its own: the arranged review must not reach any other test class.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:review_takes_todays_prices_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@Import(ArrangedReview.Configuration.class)
@DisplayName("The review takes today's prices")
class ReviewTakesTodaysPricesIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ArrangedReview review;

  @BeforeEach
  void todayTheBookCostsWhatItCostsInTheCatalogue() {
    review.reset();
  }

  @Test
  @DisplayName("The review opened again shows today's price")
  void theReviewOpenedAgainShowsTodaysPrice() throws Exception {
    final GuestCheckout guest = new GuestCheckout(mockMvc);
    final Document firstOpened = guest.toTheReview();
    assertThat(GuestCheckout.itemsOf(firstOpened.selectFirst(".order-summary")))
        .as("the review as first opened")
        .containsExactly("Domain-Driven Design x1 54.99 EUR");
    review.priceToday(Money.euro(49.99));

    final Document reopened = guest.page(GuestCheckout.REVIEW_STEP);

    assertThat(GuestCheckout.itemsOf(reopened.selectFirst(".order-summary")))
        .containsExactly("Domain-Driven Design x1 49.99 EUR");
    assertThat(GuestCheckout.totalsOf(reopened.selectFirst(".order-summary")))
        .contains("Total 54.98 EUR");
    final List<GetCheckoutReviewQuery> asked = review.queries();
    assertThat(asked).as("reviews asked for, one per opening").hasSize(2);
    assertThat(asked.get(1)).isEqualTo(asked.get(0));
  }
}
