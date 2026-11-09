-- Checkout: sessions with the data of each completed step, and their line items
CREATE TABLE checkout_sessions (
  id VARCHAR(64) PRIMARY KEY,
  cart_id VARCHAR(64) NOT NULL,
  customer_id VARCHAR(64) NOT NULL,
  current_step VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  subtotal_amount DECIMAL(19,2) NOT NULL,
  shipping_amount DECIMAL(19,2) NOT NULL,
  tax_amount DECIMAL(19,2) NOT NULL,
  total_amount DECIMAL(19,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  -- buyer step
  buyer_email VARCHAR(255),
  buyer_first_name VARCHAR(100),
  buyer_last_name VARCHAR(100),
  buyer_phone VARCHAR(64),
  -- delivery step
  delivery_street VARCHAR(255),
  delivery_street_line2 VARCHAR(255),
  delivery_city VARCHAR(100),
  delivery_postal_code VARCHAR(32),
  delivery_country VARCHAR(100),
  delivery_state VARCHAR(100),
  shipping_option_id VARCHAR(64),
  shipping_option_name VARCHAR(255),
  shipping_option_estimated_delivery VARCHAR(255),
  shipping_option_cost_amount DECIMAL(19,2),
  shipping_option_cost_currency VARCHAR(3),
  -- payment step
  payment_provider_id VARCHAR(64),
  payment_provider_reference VARCHAR(255),
  payment_authorized_amount DECIMAL(19,2),
  payment_authorized_currency VARCHAR(3),
  order_reference VARCHAR(255),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_checkout_sessions_cart ON checkout_sessions(cart_id);
CREATE INDEX idx_checkout_sessions_customer ON checkout_sessions(customer_id);

CREATE TABLE checkout_line_items (
  session_id VARCHAR(64) NOT NULL REFERENCES checkout_sessions(id) ON DELETE CASCADE,
  position INT NOT NULL,
  id VARCHAR(64) NOT NULL,
  product_id VARCHAR(64) NOT NULL,
  product_name VARCHAR(255) NOT NULL,
  unit_price_amount DECIMAL(19,2) NOT NULL,
  unit_price_currency VARCHAR(3) NOT NULL,
  quantity INT NOT NULL,
  image_url TEXT,
  position_snapshot TEXT NOT NULL,
  PRIMARY KEY (session_id, position)
);
