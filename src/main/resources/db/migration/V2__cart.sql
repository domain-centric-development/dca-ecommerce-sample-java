-- Cart: shopping carts and their lines
CREATE TABLE carts (
  id VARCHAR(64) PRIMARY KEY,
  customer_id VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE cart_items (
  id VARCHAR(64) PRIMARY KEY,
  cart_id VARCHAR(64) NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
  product_id VARCHAR(64) NOT NULL,
  quantity INT NOT NULL,
  position_units TEXT NOT NULL,
  price_amount DECIMAL(19,2) NOT NULL,
  price_currency VARCHAR(3) NOT NULL
);

-- "At most one ACTIVE cart per customer" is uniqueness over a subset of the rows: a partial unique
-- index. A customer keeps any number of finished carts and at most one open one (ADR-045).
CREATE UNIQUE INDEX uq_carts_active_customer ON carts(customer_id) WHERE status = 'ACTIVE';
CREATE INDEX idx_carts_customer ON carts(customer_id);
CREATE INDEX idx_carts_status ON carts(status);
CREATE INDEX idx_items_cart ON cart_items(cart_id);
