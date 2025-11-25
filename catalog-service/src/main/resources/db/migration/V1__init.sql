-- Limpieza defensiva
DROP TABLE IF EXISTS product_images CASCADE; 
DROP TABLE IF EXISTS inventory CASCADE;
DROP TABLE IF EXISTS variants CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS brands CASCADE;

-- Catálogo base
CREATE TABLE brands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    brand_id BIGINT REFERENCES brands(id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    base_price NUMERIC(12,2) NOT NULL CHECK (base_price >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,

    -- ✅ NUEVO: destacado para home
    featured BOOLEAN NOT NULL DEFAULT FALSE,

    -- ✅ NUEVO: género para tabs
    gender VARCHAR(10) NOT NULL DEFAULT 'HOMBRE',
    CONSTRAINT products_gender_chk CHECK (gender IN ('HOMBRE', 'MUJER')),

    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Tipos de talla
-- size_type: 'EU' o 'LETTER'
-- size_value: para EU (39-46) validado en app + constraint suavecita
CREATE TABLE variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    size_type VARCHAR(16) NOT NULL,             -- 'EU' | 'LETTER'
    size_value VARCHAR(16) NOT NULL,            -- '39'..'46' o 'XS'..'XXL'
    color VARCHAR(40) NOT NULL,
    sku VARCHAR(64) NOT NULL UNIQUE,
    price_override NUMERIC(12,2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_variant UNIQUE(product_id, size_type, size_value, color),
    CONSTRAINT ck_size_type CHECK (size_type IN ('EU','LETTER')),
    CONSTRAINT ck_letter CHECK (
        (size_type = 'LETTER' AND size_value IN ('XXS','XS','S','M','L','XL','XXL'))
        OR (size_type = 'EU')
    )
);

CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    variant_id BIGINT NOT NULL UNIQUE REFERENCES variants(id) ON DELETE CASCADE,
    stock_available INT NOT NULL DEFAULT 0 CHECK (stock_available >= 0),
    stock_reserved INT NOT NULL DEFAULT 0 CHECK (stock_reserved >= 0),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    file_path TEXT NOT NULL,              -- ruta en disco
    public_url TEXT NOT NULL,             -- URL pública /media/...
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índices
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_featured ON products(featured);
CREATE INDEX idx_products_gender ON products(gender);

CREATE INDEX idx_variants_product ON variants(product_id);
CREATE INDEX idx_inventory_variant ON inventory(variant_id);
CREATE INDEX idx_images_product ON product_images(product_id);
