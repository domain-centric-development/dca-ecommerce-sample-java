package dev.domaincentric.sample.ecommerce.checkout.domain.service;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainService;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.*;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult.ValidationError;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.*;
import java.util.*;

public final class CheckoutPricing implements DomainService {
  public Money calculateOrderTotal(
      java.util.List<CheckoutLineItem> lines,
      java.util.Map<ProductId, CheckoutArticlePriceResolver.ArticlePrice> facts,
      java.util.Currency currency) {
    Money total = Money.zero(currency);
    for (final CheckoutLineItem item : lines) {
      final CheckoutArticlePriceResolver.ArticlePrice articlePrice = facts.get(item.productId());
      final Money itemTotal = articlePrice.price().multiply(item.quantity());
      total = total.add(itemTotal);
    }
    return total;
  }

  public CheckoutValidationResult validateItems(
      java.util.List<CheckoutLineItem> lines,
      java.util.Map<ProductId, CheckoutArticlePriceResolver.ArticlePrice> facts,
      java.util.Currency currency) {
    final List<ValidationError> errors = new ArrayList<>();
    for (final CheckoutLineItem item : lines) {
      final CheckoutArticlePriceResolver.ArticlePrice articlePrice = facts.get(item.productId());
      if (articlePrice == null || !articlePrice.isAvailable()) {
        errors.add(ValidationError.productUnavailable(item.productId()));
      } else if (!articlePrice.price().equals(item.unitPrice())) {
        errors.add(
            new ValidationError(
                item.productId(),
                "Price changed; start a fresh checkout to accept it",
                CheckoutValidationResult.ErrorType.PRICE_CHANGED));
      } else if (articlePrice.availableStock() < item.quantity()) {
        errors.add(
            ValidationError.insufficientStock(
                item.productId(), item.quantity(), articlePrice.availableStock()));
      }
    }
    return errors.isEmpty()
        ? CheckoutValidationResult.valid()
        : CheckoutValidationResult.withErrors(errors);
  }
}
