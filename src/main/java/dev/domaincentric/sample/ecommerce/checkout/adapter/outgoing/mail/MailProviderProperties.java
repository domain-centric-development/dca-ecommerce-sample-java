package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.mail;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Where the mail provider answers, bound from {@code checkout.mail-provider}.
 *
 * <p>The sample ships no address: without one the shop's mails go to {@link StandInMailProvider},
 * with one through {@link RestMailProvider}.
 *
 * @param baseUrl the provider's base address, e.g. {@code https://mail.example.com}; blank or
 *     absent selects the stand-in
 */
@ConfigurationProperties(prefix = "checkout.mail-provider")
public record MailProviderProperties(@Nullable String baseUrl) {}
