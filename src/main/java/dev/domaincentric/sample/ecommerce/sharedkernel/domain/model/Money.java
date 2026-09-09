package dev.domaincentric.sample.ecommerce.sharedkernel.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Value Object representing money with amount and currency.
 *
 * <p>This is part of the Shared Kernel and can be used across multiple bounded contexts.
 */
public record Money(BigDecimal amount, Currency currency) implements Value {

  public Money {
    if (amount == null) {
      throw new IllegalArgumentException("Amount cannot be null");
    }
    if (currency == null) {
      throw new IllegalArgumentException("Currency cannot be null");
    }
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Amount cannot be negative");
    }
    if (amount.compareTo(new BigDecimal("999999999999.99")) > 0) {
      throw new IllegalArgumentException("Amount exceeds maximum monetary value");
    }
    // Normalize scale to 2 decimal places
    amount = amount.setScale(2, RoundingMode.HALF_UP);
  }

  public static Money of(final BigDecimal amount, final Currency currency) {
    return new Money(amount, currency);
  }

  public static Money euro(final BigDecimal amount) {
    return new Money(amount, Currency.getInstance("EUR"));
  }

  public static Money euro(final double amount) {
    return euro(BigDecimal.valueOf(amount));
  }

  public static Money zero(final Currency currency) {
    return new Money(BigDecimal.ZERO, currency);
  }

  public Money add(final Money other) {
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException("Cannot add money with different currencies");
    }
    return new Money(this.amount.add(other.amount), this.currency);
  }

  public Money subtract(final Money other) {
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException("Cannot subtract money with different currencies");
    }
    return new Money(this.amount.subtract(other.amount), this.currency);
  }

  public Money multiply(final int factor) {
    return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
  }

  public Money multiply(final BigDecimal factor) {
    return new Money(this.amount.multiply(factor), this.currency);
  }

  public boolean isZero() {
    return amount.compareTo(BigDecimal.ZERO) == 0;
  }

  public boolean isGreaterThan(final Money other) {
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException("Cannot compare money with different currencies");
    }
    return this.amount.compareTo(other.amount) > 0;
  }
}
