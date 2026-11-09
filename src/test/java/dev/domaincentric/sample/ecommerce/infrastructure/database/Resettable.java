package dev.domaincentric.sample.ecommerce.infrastructure.database;

/**
 * A bean that keeps state of its own across tests — a recording stub, a switch a test flips — and
 * goes back to how it started before each test, as the database does.
 */
public interface Resettable {

  /** Back to the state after the start. */
  void reset();
}
