-- Pricing: one price per product, and the events whose effect this context has applied
CREATE TABLE product_prices (
  id VARCHAR(64) PRIMARY KEY,
  product_id VARCHAR(64) NOT NULL CONSTRAINT uq_product_prices_product UNIQUE,
  amount DECIMAL(19,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  effective_from TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Kept in the same store and transaction as the effect it guards, so an effect and its record
-- commit or roll back together
CREATE TABLE pricing_processed_events (
  effect VARCHAR(100) NOT NULL,
  event_id UUID NOT NULL,
  PRIMARY KEY (effect, event_id)
);
