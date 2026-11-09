-- Cart: the events whose effect this context has applied
CREATE TABLE cart_processed_events (
  effect VARCHAR(100) NOT NULL,
  event_id UUID NOT NULL,
  PRIMARY KEY (effect, event_id)
);
