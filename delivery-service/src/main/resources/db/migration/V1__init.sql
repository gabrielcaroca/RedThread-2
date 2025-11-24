-- DROPs dev
DROP TABLE IF EXISTS tracking_events CASCADE;
DROP TABLE IF EXISTS shipment_assignments CASCADE;
DROP TABLE IF EXISTS shipments CASCADE;
DROP TABLE IF EXISTS delivery_routes CASCADE;

-- Routes
CREATE TABLE delivery_routes (
  id              BIGSERIAL PRIMARY KEY,
  nombre          VARCHAR(120) NOT NULL,
  descripcion     VARCHAR(500),
  total_pedidos   INTEGER NOT NULL,
  total_price     BIGINT NOT NULL,
  activa          BOOLEAN NOT NULL DEFAULT TRUE,
  assigned_user_id BIGINT,
  created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_routes_activa ON delivery_routes(activa);
CREATE INDEX idx_routes_assigned ON delivery_routes(assigned_user_id);

-- Shipments
CREATE TABLE shipments (
  id               BIGSERIAL PRIMARY KEY,
  order_id         BIGINT NOT NULL,
  user_id          BIGINT NOT NULL,
  address_line1    VARCHAR(200) NOT NULL,
  address_line2    VARCHAR(200),
  city             VARCHAR(120) NOT NULL,
  state            VARCHAR(120),
  zip              VARCHAR(40),
  country          VARCHAR(80) NOT NULL,
  status           VARCHAR(32) NOT NULL,
  total_price      NUMERIC(12,2) NOT NULL,

  -- NUEVO: asignación directa y ruta
  assigned_user_id BIGINT,
  route_id         BIGINT REFERENCES delivery_routes(id) ON DELETE SET NULL,

  -- NUEVO: evidencias
  evidence_url     VARCHAR(300),
  receiver_name    VARCHAR(120),
  note             VARCHAR(500),

  created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_shipments_user ON shipments(user_id);
CREATE INDEX idx_shipments_order ON shipments(order_id);
CREATE INDEX idx_shipments_status ON shipments(status);
CREATE INDEX idx_shipments_assigned ON shipments(assigned_user_id);
CREATE INDEX idx_shipments_route ON shipments(route_id);

-- Assignments (userId del despachador)
CREATE TABLE shipment_assignments (
  id               BIGSERIAL PRIMARY KEY,
  shipment_id      BIGINT NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
  assigned_user_id BIGINT NOT NULL,
  assigned_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_assignments_shipment ON shipment_assignments(shipment_id);
CREATE INDEX idx_assignments_user ON shipment_assignments(assigned_user_id);

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
