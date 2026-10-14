package dev.domaincentric.sample.ecommerce.infrastructure.config;

import dev.domaincentric.sample.ecommerce.infrastructure.support.CookieFlashMapManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FlashMapManager;

/**
 * Flash attributes travel in a sealed cookie, not in the server session: the shop runs as several
 * instances without sticky sessions, and the page a redirect leads to may be answered by another
 * one. The bean replaces Spring's session-based manager by its name.
 */
@Configuration
@EnableConfigurationProperties(NoticeCookieProperties.class)
public class NoticeCookieConfiguration {

  @Bean(name = DispatcherServlet.FLASH_MAP_MANAGER_BEAN_NAME)
  public FlashMapManager flashMapManager(final NoticeCookieProperties properties) {
    return new CookieFlashMapManager(
        properties.keyBytes(), properties.secure(), properties.sameSite());
  }
}
