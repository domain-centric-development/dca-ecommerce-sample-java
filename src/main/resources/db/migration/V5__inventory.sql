-- Inventory: one stock level per product, and the events whose effect this context has applied
CREATE TABLE stock_levels (
  id VARCHAR(64) PRIMARY KEY,
  product_id VARCHAR(64) NOT NULL CONSTRAINT uq_stock_levels_product UNIQUE,
  available_quantity INT NOT NULL CHECK (available_quantity >= 0),
  reserved_quantity INT NOT NULL CHECK (reserved_quantity >= 0)
);

CREATE TABLE inventory_processed_events (
  effect VARCHAR(100) NOT NULL,
  event_id UUID NOT NULL,
  PRIMARY KEY (effect, event_id)
);
