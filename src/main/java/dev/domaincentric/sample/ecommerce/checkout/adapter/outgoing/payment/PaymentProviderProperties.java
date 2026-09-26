package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.payment;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Where the payment provider answers, bound from {@code checkout.payment-provider}.
 *
 * <p>The sample ships no address: without one the shop pays through {@link MockPaymentProvider},
 * with one through {@link RestPaymentProvider}.
 *
 * @param baseUrl the provider's base address, e.g. {@code https://psp.example.com}; blank or absent
 *     selects the stand-in
 */
@ConfigurationProperties(prefix = "checkout.payment-provider")
public record PaymentProviderProperties(@Nullable String baseUrl) {}
