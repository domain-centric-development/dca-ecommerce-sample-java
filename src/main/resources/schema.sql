-- H2 in-memory database schema for Account
CREATE TABLE IF NOT EXISTS accounts (
  id VARCHAR(64) PRIMARY KEY,
  -- Unique because the email is the login credential; the use cases guard it as well, so this
  -- constraint is the backstop against a race, not the primary check.
  email VARCHAR(255) NOT NULL UNIQUE,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  date_of_birth DATE NOT NULL,
  -- An account is linked to exactly one UserId (ADR-011: the cross-context identity)
  linked_user_id VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  last_login_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS account_roles (
  account_id VARCHAR(64) NOT NULL,
  role VARCHAR(64) NOT NULL,
  PRIMARY KEY (account_id, role),
  CONSTRAINT fk_account_roles_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

-- H2 in-memory database schema for Shopping Cart
CREATE TABLE IF NOT EXISTS carts (
  id VARCHAR(64) PRIMARY KEY,
  customer_id VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  -- "At most one ACTIVE cart per customer" is uniqueness over a subset of the rows. H2 has no
  -- partial index, so the value the index guards is computed instead: the customer while the cart
  -- is active, NULL once it is completed or abandoned. A unique index counts NULLs as distinct, so
  -- a customer keeps any number of finished carts and at most one open one.
  active_customer_id VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN status = 'ACTIVE' THEN customer_id END),
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS cart_items (
  id VARCHAR(64) PRIMARY KEY,
  cart_id VARCHAR(64) NOT NULL,
  product_id VARCHAR(64) NOT NULL,
  quantity INT NOT NULL,
  position_units VARCHAR(16000) NOT NULL,
  price_amount DECIMAL(19,2) NOT NULL,
  price_currency VARCHAR(3) NOT NULL,
  CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_carts_active_customer ON carts(active_customer_id);
CREATE INDEX IF NOT EXISTS idx_carts_customer ON carts(customer_id);
CREATE INDEX IF NOT EXISTS idx_carts_status ON carts(status);
CREATE INDEX IF NOT EXISTS idx_items_cart ON cart_items(cart_id);
