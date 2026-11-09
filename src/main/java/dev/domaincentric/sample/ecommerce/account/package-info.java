/**
 * Account Bounded Context.
 *
 * <p>Responsible for user account management, authentication, and profile handling.
 */
@NullMarked
@BoundedContext(
    name = "Account",
    description = "Registered accounts, credentials, profile and authenticated sessions")
@ApplicationModule(allowedDependencies = {"sharedkernel", "infrastructure"})
package dev.domaincentric.sample.ecommerce.account;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.BoundedContext;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
