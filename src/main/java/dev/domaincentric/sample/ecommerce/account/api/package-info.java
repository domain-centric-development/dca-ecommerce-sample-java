/**
 * Account API — published interface for cross-module access.
 *
 * <p>Exposes the identity of the current caller to the other contexts (Open Host Service pattern).
 * Their incoming adapters read it here and hand it to their use cases as a command or query
 * parameter; no use case depends on this package.
 */
@NamedInterface("api")
@NullMarked
package dev.domaincentric.sample.ecommerce.account.api;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.NamedInterface;
