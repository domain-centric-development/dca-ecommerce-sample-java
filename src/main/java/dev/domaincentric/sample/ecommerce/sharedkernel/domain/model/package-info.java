/**
 * Universal Value Objects.
 *
 * <p>This package contains value objects that are shared across all bounded contexts. These are
 * fundamental domain concepts that have the same meaning everywhere.
 *
 * <p><b>Contents:</b>
 *
 * <ul>
 *   <li>{@link Money} - Monetary value with currency
 *   <li>{@link Price} - Product price (wraps Money with business rules)
 *   <li>{@link ProductId} - Product identifier (cross-context reference)
 *   <li>{@link Role} - What an account may do beyond shopping (granted by account, guarded on by
 *       other contexts)
 *   <li>{@link UserId} - User identifier (cross-context reference)
 * </ul>
 *
 * <p><b>Guidelines:</b>
 *
 * <ul>
 *   <li>All value objects must be immutable (use records)
 *   <li>Include validation in constructors
 *   <li>Keep this package minimal - only truly universal concepts
 * </ul>
 */
package dev.domaincentric.sample.ecommerce.sharedkernel.domain.model;
