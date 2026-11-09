package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionStatus;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;

/**
 * The stored spelling of a session's status and step, written out per value: a renamed constant
 * must not change what every stored session carries.
 */
final class CheckoutColumns {

  private CheckoutColumns() {}

  static String valueOf(final CheckoutSessionStatus status) {
    return switch (status) {
      case ACTIVE -> "ACTIVE";
      case CONFIRMED -> "CONFIRMED";
      case COMPLETED -> "COMPLETED";
      case ABANDONED -> "ABANDONED";
      case EXPIRED -> "EXPIRED";
      case SUPERSEDED -> "SUPERSEDED";
    };
  }

  static CheckoutSessionStatus statusOf(final String value) {
    return switch (value) {
      case "ACTIVE" -> CheckoutSessionStatus.ACTIVE;
      case "CONFIRMED" -> CheckoutSessionStatus.CONFIRMED;
      case "COMPLETED" -> CheckoutSessionStatus.COMPLETED;
      case "ABANDONED" -> CheckoutSessionStatus.ABANDONED;
      case "EXPIRED" -> CheckoutSessionStatus.EXPIRED;
      case "SUPERSEDED" -> CheckoutSessionStatus.SUPERSEDED;
      default -> throw new IllegalStateException("Unknown stored checkout status: " + value);
    };
  }

  static String valueOf(final CheckoutStep step) {
    return switch (step) {
      case BUYER_INFO -> "BUYER_INFO";
      case DELIVERY -> "DELIVERY";
      case PAYMENT -> "PAYMENT";
      case REVIEW -> "REVIEW";
      case CONFIRMATION -> "CONFIRMATION";
    };
  }

  static CheckoutStep stepOf(final String value) {
    return switch (value) {
      case "BUYER_INFO" -> CheckoutStep.BUYER_INFO;
      case "DELIVERY" -> CheckoutStep.DELIVERY;
      case "PAYMENT" -> CheckoutStep.PAYMENT;
      case "REVIEW" -> CheckoutStep.REVIEW;
      case "CONFIRMATION" -> CheckoutStep.CONFIRMATION;
      default -> throw new IllegalStateException("Unknown stored checkout step: " + value);
    };
  }
}
