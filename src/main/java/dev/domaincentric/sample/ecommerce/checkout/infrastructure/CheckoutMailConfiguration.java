package dev.domaincentric.sample.ecommerce.checkout.infrastructure;

import dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.mail.MailProviderProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Binds the mail provider's address, which decides between the REST adapter and the stand-in. */
@Configuration
@EnableConfigurationProperties(MailProviderProperties.class)
public class CheckoutMailConfiguration {}
