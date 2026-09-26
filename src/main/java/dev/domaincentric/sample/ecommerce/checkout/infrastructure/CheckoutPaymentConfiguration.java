package dev.domaincentric.sample.ecommerce.checkout.infrastructure;

import dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.payment.PaymentProviderProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the payment provider's address, which decides between the REST adapter and the stand-in.
 */
@Configuration
@EnableConfigurationProperties(PaymentProviderProperties.class)
public class CheckoutPaymentConfiguration {}
