package dev.domaincentric.sample.ecommerce.checkout.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutArticlePriceResolver.ArticlePrice;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItemId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckoutPricing: today's prices of a checkout's lines")
class CheckoutPricingTodaysPricesTest {

  private static final ProductId DDD = ProductId.generate();
  private static final ProductId IDDD = ProductId.generate();
  private static final ProductId DESK_MODEL = ProductId.generate();

  private final CheckoutPricing pricing = new CheckoutPricing();

  @Test
  @DisplayName("a line whose price changed since it was captured is not refused")
  void aLineWhosePriceChangedSinceItWasCapturedIsNotRefused() {
    final CheckoutSession session = sessionOf(line(DDD, "Domain-Driven Design", Money.euro(10), 2));

    assertTrue(
        pricing
            .validateItems(session, Map.of(DDD, new ArticlePrice(Money.euro(11), true, 100)))
            .isValid());
  }

  @Test
  @DisplayName("today's price is each available article's price")
  void todaysPriceIsEachAvailableArticlesPrice() {
    final CheckoutSession session =
        sessionOf(
            line(DDD, "Domain-Driven Design", Money.euro(54.99), 1),
            line(IDDD, "Implementing Domain-Driven Design", Money.euro(45), 2));

    assertEquals(
        Map.of(DDD, Money.euro(49.99), IDDD, Money.euro(45)),
        pricing.todaysPrices(
            session,
            Map.of(
                DDD, new ArticlePrice(Money.euro(49.99), true, 10),
                IDDD, new ArticlePrice(Money.euro(45), true, 10))));
  }

  @Test
  @DisplayName("an unavailable or unknown article has no price today")
  void anUnavailableOrUnknownArticleHasNoPriceToday() {
    final CheckoutSession session =
        sessionOf(
            line(DDD, "Domain-Driven Design", Money.euro(54.99), 1),
            line(DESK_MODEL, "Wooden Hexagon Desk Model", Money.euro(10), 1));

    assertEquals(
        Map.of(),
        pricing.todaysPrices(session, Map.of(DDD, new ArticlePrice(Money.euro(49.99), false, 10))));
  }

  private static CheckoutSession sessionOf(final CheckoutLineItem... lines) {
    final Money subtotal =
        List.of(lines).stream().map(CheckoutLineItem::lineTotal).reduce(Money.euro(0), Money::add);
    return CheckoutSession.start(
        CartId.generate(), CustomerId.of("customer-1"), List.of(lines), subtotal);
  }

  private static CheckoutLineItem line(
      final ProductId product, final String name, final Money price, final int quantity) {
    return CheckoutLineItem.of(CheckoutLineItemId.generate(), product, name, price, quantity, null);
  }
}
