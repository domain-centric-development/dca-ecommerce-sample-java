package dev.domaincentric.sample.ecommerce.checkout.domain.service;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainService;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.*;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult.ValidationError;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.*;
import java.util.*;

/**
 * Prices and checks a checkout's lines against today's article facts. A price changed since the
 * checkout started is no refusal here: the review shows today's prices, and "Place Order" orders
 * them only while they are what the review showed. An unavailable line or one short of stock is.
 */
public final class CheckoutPricing implements DomainService {

  /**
   * Today's unit price of every line whose article is available.
   *
   * @param session the checkout whose lines are priced
   * @param facts today's article facts per product
   * @return today's price per product; an unavailable or unknown article has none
   */
  public Map<ProductId, Money> todaysPrices(
      final CheckoutSession session,
      final Map<ProductId, CheckoutArticlePriceResolver.ArticlePrice> facts) {
    final Map<ProductId, Money> prices = new HashMap<>();
    for (final CheckoutLineItem item : session.lineItems()) {
      final CheckoutArticlePriceResolver.ArticlePrice articlePrice = facts.get(item.productId());
      if (articlePrice != null && articlePrice.isAvailable()) {
        prices.put(item.productId(), articlePrice.price());
      }
    }
    return Map.copyOf(prices);
  }

  public CheckoutValidationResult validateItems(
      final CheckoutSession session,
      final Map<ProductId, CheckoutArticlePriceResolver.ArticlePrice> facts) {
    final var lines = session.lineItems();
    final List<ValidationError> errors = new ArrayList<>();
    for (final CheckoutLineItem item : lines) {
      final CheckoutArticlePriceResolver.ArticlePrice articlePrice = facts.get(item.productId());
      if (articlePrice == null || !articlePrice.isAvailable()) {
        errors.add(ValidationError.productUnavailable(item.productId(), item.productName()));
      } else if (articlePrice.availableStock() < item.quantity()) {
        errors.add(
            ValidationError.insufficientStock(
                item.productId(),
                item.productName(),
                item.quantity(),
                articlePrice.availableStock()));
      }
    }
    return errors.isEmpty()
        ? CheckoutValidationResult.valid()
        : CheckoutValidationResult.withErrors(errors);
  }
}
