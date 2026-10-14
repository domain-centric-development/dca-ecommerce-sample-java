/**
 * Infrastructure support components.
 *
 * <p>This package contains framework-specific support components that are not pure configuration
 * classes but provide cross-cutting infrastructure behavior.
 *
 * <p><b>Contents:</b>
 *
 * <ul>
 *   <li>{@link
 *       dev.domaincentric.sample.ecommerce.infrastructure.support.AsyncInitializationProcessor} -
 *       Spring BeanPostProcessor for async initialization
 *   <li>{@link dev.domaincentric.sample.ecommerce.infrastructure.support.CookieFlashMapManager} -
 *       Spring MVC flash maps in a sealed cookie instead of the server session
 * </ul>
 *
 * <p><b>Distinction from config package:</b>
 *
 * <ul>
 *   <li>{@code config/} - Pure @Configuration classes with @Bean definitions
 *   <li>{@code support/} - Framework components (processors, listeners, handlers)
 * </ul>
 */
@org.jspecify.annotations.NullMarked
package dev.domaincentric.sample.ecommerce.infrastructure.support;
