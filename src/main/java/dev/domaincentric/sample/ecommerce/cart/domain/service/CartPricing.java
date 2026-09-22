package dev.domaincentric.sample.ecommerce.cart.domain.service;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainService;
import dev.domaincentric.sample.ecommerce.cart.domain.model.*;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.*;
import java.util.*;

public final class CartPricing implements DomainService {
  public Money calculateTotal(
      final ShoppingCart cart, final java.util.Map<ProductId, ArticlePrice> facts) {
    final var lines = cart.items();
    if (facts == null) {
      throw new IllegalArgumentException("Article facts cannot be null");
    }
    if (lines.isEmpty()) {
      return Money.euro(0.0);
    }

    Money total = Money.euro(0.0);
    for (final CartItem item : lines) {
      final ArticlePrice articlePrice = facts.get(item.productId());
      final Money itemTotal = articlePrice.price().multiply(item.quantity().value());
      total = total.add(itemTotal);
    }
    return total;
  }

  public CartValidationResult validateForCheckout(
      final ShoppingCart cart, final java.util.Map<ProductId, ArticlePrice> facts) {
    final var lines = cart.items();
    if (facts == null) {
      throw new IllegalArgumentException("Article facts cannot be null");
    }
    if (lines.isEmpty()) {
      return CartValidationResult.valid();
    }

    final List<CartValidationResult.ValidationError> errors = new ArrayList<>();
    for (final CartItem item : lines) {
      final ArticlePrice articlePrice = facts.get(item.productId());

      if (articlePrice == null || !articlePrice.isAvailable()) {
        errors.add(CartValidationResult.ValidationError.productUnavailable(item.productId()));
      } else if (articlePrice.availableStock() < item.quantity().value()) {
        errors.add(
            CartValidationResult.ValidationError.insufficientStock(
                item.productId(), item.quantity().value(), articlePrice.availableStock()));
      }
    }

    return errors.isEmpty()
        ? CartValidationResult.valid()
        : CartValidationResult.withErrors(errors);
  }
}
