-- Address
CREATE TABLE addresses (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(128) NOT NULL,
  line1 VARCHAR(255) NOT NULL,
  line2 VARCHAR(255),
  city  VARCHAR(120) NOT NULL,
  state VARCHAR(120) NOT NULL,
  zip   VARCHAR(40)  NOT NULL,
  country VARCHAR(80) NOT NULL,
  is_default BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_addresses_user ON addresses(user_id);

-- Cart
CREATE TABLE carts (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(128) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_carts_user ON carts(user_id);

-- CartItem
CREATE TABLE cart_items (
  id BIGSERIAL PRIMARY KEY,
  cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
  variant_id BIGINT NOT NULL,
  quantity INTEGER NOT NULL CHECK (quantity >= 1),
  unit_price NUMERIC(12,2) NOT NULL,
  UNIQUE (cart_id, variant_id)
);
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);

-- Orders
CREATE TYPE order_status AS ENUM ('CREATED','PAID','CANCELLED','SHIPPED');

CREATE TABLE orders (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(128) NOT NULL,
  address_id BIGINT NOT NULL REFERENCES addresses(id),
  status order_status NOT NULL,
  total_amount NUMERIC(14,2) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_user ON orders(user_id);

-- OrderItem
CREATE TABLE order_items (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  variant_id BIGINT NOT NULL,
  quantity INTEGER NOT NULL CHECK (quantity >= 1),
  unit_price NUMERIC(12,2) NOT NULL,
  line_total NUMERIC(14,2) NOT NULL
);

CREATE INDEX idx_order_items_order ON order_items(order_id);

-- PaymentAttempt
CREATE TYPE payment_status AS ENUM ('PENDING','APPROVED','REJECTED');


CREATE TABLE payment_attempts (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  provider VARCHAR(60),
  status payment_status NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payment_attempts_order ON payment_attempts(order_id);
