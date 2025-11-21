-- DROPs para entorno dev
DROP TABLE IF EXISTS tracking_events CASCADE;
DROP TABLE IF EXISTS shipment_assignments CASCADE;
DROP TABLE IF EXISTS shipments CASCADE;
DROP TABLE IF EXISTS rates CASCADE;
DROP TABLE IF EXISTS geo_zones CASCADE;
DROP TABLE IF EXISTS vehicles CASCADE;
DROP TABLE IF EXISTS drivers CASCADE;

-- Drivers
CREATE TABLE drivers (
  id            BIGSERIAL PRIMARY KEY,
  name          VARCHAR(120) NOT NULL,
  phone         VARCHAR(40) NOT NULL,
  email         VARCHAR(140),
  active        BOOLEAN NOT NULL DEFAULT TRUE
);

-- Vehicles (opcional v1)
CREATE TABLE vehicles (
  id            BIGSERIAL PRIMARY KEY,
  plate         VARCHAR(32) NOT NULL UNIQUE,
  model         VARCHAR(120),
  capacity_kg   NUMERIC(10,2),
  active        BOOLEAN NOT NULL DEFAULT TRUE
);

-- Geo Zones
CREATE TABLE geo_zones (
  id            BIGSERIAL PRIMARY KEY,
  name          VARCHAR(120) NOT NULL,
  city          VARCHAR(120),
  state         VARCHAR(120),
  country       VARCHAR(120),
  zip_pattern   VARCHAR(120)
);

-- Rates
CREATE TABLE rates (
  id            BIGSERIAL PRIMARY KEY,
  zone_id       BIGINT NOT NULL REFERENCES geo_zones(id) ON DELETE CASCADE,
  base_price    NUMERIC(12,2) NOT NULL,
  price_per_km  NUMERIC(12,2),
  is_active     BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_rates_zone ON rates(zone_id);
CREATE INDEX idx_rates_active ON rates(is_active);

-- Shipments
CREATE TABLE shipments (
  id            BIGSERIAL PRIMARY KEY,
  order_id      BIGINT NOT NULL,
  user_id       BIGINT NOT NULL,
  address_line1 VARCHAR(200) NOT NULL,
  address_line2 VARCHAR(200),
  city          VARCHAR(120) NOT NULL,
  state         VARCHAR(120),
  zip           VARCHAR(40),
  country       VARCHAR(80) NOT NULL,
  zone_id       BIGINT REFERENCES geo_zones(id),
  status        VARCHAR(32) NOT NULL,
  total_price   NUMERIC(12,2) NOT NULL,
  created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_shipments_user ON shipments(user_id);
CREATE INDEX idx_shipments_order ON shipments(order_id);
CREATE INDEX idx_shipments_status ON shipments(status);

-- Shipment Assignments
CREATE TABLE shipment_assignments (
  id            BIGSERIAL PRIMARY KEY,
  shipment_id   BIGINT NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
  driver_id     BIGINT NOT NULL REFERENCES drivers(id),
  vehicle_id    BIGINT REFERENCES vehicles(id),
  assigned_at   TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_assignments_shipment ON shipment_assignments(shipment_id);
CREATE INDEX idx_assignments_driver ON shipment_assignments(driver_id);

-- Tracking Events
CREATE TABLE tracking_events (
  id            BIGSERIAL PRIMARY KEY,
  shipment_id   BIGINT NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
  status        VARCHAR(32),
  latitude      NUMERIC(10,6),
  longitude     NUMERIC(10,6),
  note          VARCHAR(250),
  created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_tracking_shipment ON tracking_events(shipment_id);
CREATE INDEX idx_tracking_created ON tracking_events(created_at);
