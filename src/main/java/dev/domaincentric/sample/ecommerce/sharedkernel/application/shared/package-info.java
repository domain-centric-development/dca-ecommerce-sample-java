/**
 * Cross-context output ports of the shared kernel.
 *
 * <p>Unlike {@code dev.domaincentric.dca.buildingblocks.hexagonal.port.out}, which holds the
 * <em>generic</em> port markers ({@code OutputPort}, {@code Repository}, {@code
 * DomainEventPublisher}), this package contains <em>application-specific</em> ports that several
 * bounded contexts of this application share but that are not part of the reusable DCA building
 * blocks.
 *
 * <p><b>Contents:</b>
 *
 * <ul>
 *   <li>{@link dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider}
 *       - access to the current caller's identity (implemented by the account context's security
 *       adapter)
 * </ul>
 */
@org.jspecify.annotations.NullMarked
package dev.domaincentric.sample.ecommerce.sharedkernel.application.shared;
