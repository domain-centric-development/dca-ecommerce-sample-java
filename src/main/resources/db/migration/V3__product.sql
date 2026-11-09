-- Product: the catalogue
CREATE TABLE products (
  id VARCHAR(64) PRIMARY KEY,
  -- A SKU names one product; the constraint holds the claim against a race between check and save
  sku VARCHAR(64) NOT NULL CONSTRAINT uq_products_sku UNIQUE,
  name VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  category VARCHAR(100) NOT NULL,
  image_url TEXT NOT NULL
);

CREATE INDEX idx_products_category ON products(category);
