/**
 * Shared Kernel.
 *
 * <p>Common value objects, DDD markers, and cross-cutting concerns shared across all bounded
 * contexts. Changes require coordination between all teams.
 *
 * <p><b>Package Structure:</b>
 *
 * <ul>
 *   <li>{@code marker/tactical/} - DDD tactical building blocks (Entity, Value, Aggregate, etc.)
 *   <li>{@code marker/strategic/} - DDD strategic patterns (BoundedContext, SharedKernel)
 *   <li>{@code marker/port/in/} - Input ports (UseCase, InputPort)
 *   <li>{@code marker/port/out/} - Output port markers (OutputPort, Repository,
 *       DomainEventPublisher)
 *   <li>{@code application/shared/} - Application-specific ports shared across contexts
 *       (IdentityProvider)
 *   <li>{@code domain/model/} - Universal value objects (Money, Price, ProductId, UserId)
 *   <li>{@code domain/specification/} - Composable specification pattern
 * </ul>
 */
@NullMarked
@SharedKernel(description = "Common value objects, DDD markers, and cross-cutting concerns")
@ApplicationModule(type = ApplicationModule.Type.OPEN)
package dev.domaincentric.sample.ecommerce.sharedkernel;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.SharedKernel;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
