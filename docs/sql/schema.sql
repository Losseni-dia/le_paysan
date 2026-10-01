-- Le Paysan - schéma PostgreSQL
-- Base cible : le_paysan
-- Voir docs/03-modele-donnees.md pour le dictionnaire de données complet.

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(30) NOT NULL UNIQUE,
    email           VARCHAR(255) UNIQUE,
    password_hash   VARCHAR(255),
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(30),
    security_selfie_url VARCHAR(500),
    auth_provider   VARCHAR(20) NOT NULL DEFAULT 'LOCAL' CHECK (auth_provider IN ('LOCAL','GOOGLE')),
    provider_id     VARCHAR(255),
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    cgu_accepted_at     TIMESTAMP,
    cgu_version         VARCHAR(10),
    cgv_accepted_at     TIMESTAMP,
    cgv_version         VARCHAR(10),
    privacy_accepted_at TIMESTAMP,
    privacy_version     VARCHAR(10),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE roles (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_VENDEUR'), ('ROLE_ADMIN');

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE shops (
    id              BIGSERIAL PRIMARY KEY,
    owner_user_id   BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    owner_type      VARCHAR(15) NOT NULL CHECK (owner_type IN ('PHYSIQUE','ENTREPRENANT','MORALE')),
    name            VARCHAR(150) NOT NULL UNIQUE,
    description     TEXT,
    category        VARCHAR(100),
    address_city    VARCHAR(100) NOT NULL,
    address_detail  VARCHAR(255) NOT NULL,
    latitude        NUMERIC(10,7) NOT NULL,
    longitude       NUMERIC(10,7) NOT NULL,
    phone           VARCHAR(30) NOT NULL,
    logo_url        VARCHAR(500),
    cover_url       VARCHAR(500),
    status          VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
                     CHECK (status IN ('EN_ATTENTE','VALIDEE','REJETEE','SUSPENDUE')),
    cgv_vendeur_accepted_at TIMESTAMP,
    cgv_vendeur_version     VARCHAR(10),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE shop_individual_profile (
    shop_id                 BIGINT PRIMARY KEY REFERENCES shops(id) ON DELETE CASCADE,
    first_name              VARCHAR(100) NOT NULL,
    last_name               VARCHAR(100) NOT NULL,
    birth_date              DATE NOT NULL,
    id_document_type        VARCHAR(20) NOT NULL CHECK (id_document_type IN ('CNI','ATTESTATION_IDENTITE','PASSEPORT')),
    id_document_number      VARCHAR(50) NOT NULL,
    id_document_front_url   VARCHAR(500) NOT NULL,
    id_document_back_url    VARCHAR(500),
    selfie_with_id_url      VARCHAR(500) NOT NULL
);

CREATE TABLE shop_entreprenant_profile (
    shop_id                 BIGINT PRIMARY KEY REFERENCES shops(id) ON DELETE CASCADE,
    first_name              VARCHAR(100) NOT NULL,
    last_name               VARCHAR(100) NOT NULL,
    birth_date              DATE NOT NULL,
    id_document_type        VARCHAR(20) NOT NULL CHECK (id_document_type IN ('CNI','ATTESTATION_IDENTITE','PASSEPORT')),
    id_document_number      VARCHAR(50) NOT NULL,
    id_document_front_url   VARCHAR(500) NOT NULL,
    id_document_back_url    VARCHAR(500),
    selfie_with_id_url      VARCHAR(500) NOT NULL,
    recepisse_number        VARCHAR(50) NOT NULL,
    recepisse_date          DATE NOT NULL,
    recepisse_document_url  VARCHAR(500) NOT NULL
);

CREATE TABLE shop_company_profile (
    shop_id                     BIGINT PRIMARY KEY REFERENCES shops(id) ON DELETE CASCADE,
    company_name                VARCHAR(200) NOT NULL,
    legal_form                  VARCHAR(20) NOT NULL CHECK (legal_form IN ('SARL','SA','SAS','SUARL','AUTRE')),
    rccm_number                 VARCHAR(30) NOT NULL,
    ncc_number                  VARCHAR(30) NOT NULL,
    legal_document_url              VARCHAR(500) NOT NULL,
    legal_rep_name                  VARCHAR(150) NOT NULL,
    legal_rep_role                  VARCHAR(100) NOT NULL,
    legal_rep_id_document_type      VARCHAR(20) NOT NULL CHECK (legal_rep_id_document_type IN ('CNI','ATTESTATION_IDENTITE','PASSEPORT')),
    legal_rep_id_document_number    VARCHAR(50) NOT NULL,
    legal_rep_id_document_front_url VARCHAR(500) NOT NULL,
    legal_rep_id_document_back_url  VARCHAR(500),
    legal_rep_selfie_with_id_url    VARCHAR(500) NOT NULL
);

CREATE TABLE shop_validations (
    id          BIGSERIAL PRIMARY KEY,
    shop_id     BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    admin_id    BIGINT NOT NULL REFERENCES users(id),
    decision    VARCHAR(20) NOT NULL CHECK (decision IN ('VALIDEE','REJETEE')),
    reason      TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE categories (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    shop_id         BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    category_id     BIGINT NOT NULL REFERENCES categories(id),
    name            VARCHAR(150) NOT NULL,
    description     TEXT,
    unit            VARCHAR(20) NOT NULL,
    unit_price      NUMERIC(12,2) NOT NULL CHECK (unit_price > 0),
    stock_quantity  INTEGER NOT NULL CHECK (stock_quantity >= 0),
    status          VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
                     CHECK (status IN ('EN_ATTENTE','VALIDE','REJETE','DEPUBLIE')),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE product_images (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url         VARCHAR(500) NOT NULL,
    position    INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE product_delivery_options (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    mode            VARCHAR(20) NOT NULL CHECK (mode IN ('RETRAIT_BOUTIQUE','LIVRAISON_DOMICILE')),
    pickup_location VARCHAR(255),
    delivery_price  NUMERIC(12,2),
    UNIQUE (product_id, mode)
);

CREATE TABLE product_validations (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    admin_id    BIGINT NOT NULL REFERENCES users(id),
    decision    VARCHAR(20) NOT NULL CHECK (decision IN ('VALIDE','REJETE')),
    reason      TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE carts (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE cart_items (
    id                BIGSERIAL PRIMARY KEY,
    cart_id           BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id        BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity          INTEGER NOT NULL CHECK (quantity > 0),
    delivery_mode     VARCHAR(20) NOT NULL CHECK (delivery_mode IN ('RETRAIT_BOUTIQUE','LIVRAISON_DOMICILE')),
    delivery_address  TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (cart_id, product_id)
);

CREATE TABLE payment_transactions (
    id                      BIGSERIAL PRIMARY KEY,
    buyer_user_id           BIGINT NOT NULL REFERENCES users(id),
    fedapay_transaction_id  VARCHAR(100) NOT NULL UNIQUE,
    amount                  NUMERIC(12,2) NOT NULL,
    status                  VARCHAR(20) NOT NULL CHECK (status IN ('PENDING','APPROVED','FAILED','CANCELED')),
    raw_payload             JSONB,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE orders (
    id                      BIGSERIAL PRIMARY KEY,
    order_number            VARCHAR(30) NOT NULL UNIQUE,
    buyer_user_id           BIGINT NOT NULL REFERENCES users(id),
    shop_id                 BIGINT NOT NULL REFERENCES shops(id),
    status                  VARCHAR(25) NOT NULL DEFAULT 'EN_ATTENTE_PAIEMENT'
                             CHECK (status IN ('EN_ATTENTE_PAIEMENT','PAYEE','EN_PREPARATION','PRETE','EXPEDIEE','LIVREE','ANNULEE')),
    subtotal_amount         NUMERIC(12,2) NOT NULL,
    delivery_amount         NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount            NUMERIC(12,2) NOT NULL,
    commission_rate         NUMERIC(5,2) NOT NULL,
    commission_amount       NUMERIC(12,2) NOT NULL,
    payment_transaction_id  BIGINT REFERENCES payment_transactions(id),
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE order_items (
    id                      BIGSERIAL PRIMARY KEY,
    order_id                BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id              BIGINT NOT NULL REFERENCES products(id),
    product_name_snapshot   VARCHAR(150) NOT NULL,
    unit_price_snapshot     NUMERIC(12,2) NOT NULL,
    quantity                INTEGER NOT NULL CHECK (quantity > 0),
    delivery_mode           VARCHAR(20) NOT NULL CHECK (delivery_mode IN ('RETRAIT_BOUTIQUE','LIVRAISON_DOMICILE')),
    delivery_price          NUMERIC(12,2) NOT NULL DEFAULT 0,
    delivery_address        TEXT
);

CREATE TABLE commission_settings (
    id          BIGSERIAL PRIMARY KEY,
    category_id BIGINT REFERENCES categories(id),
    rate        NUMERIC(5,2) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE product_reviews (
    id                  BIGSERIAL PRIMARY KEY,
    product_id          BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    buyer_user_id       BIGINT NOT NULL REFERENCES users(id),
    order_item_id       BIGINT NOT NULL REFERENCES order_items(id),
    rating              SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment             TEXT,
    status              VARCHAR(10) NOT NULL DEFAULT 'VISIBLE' CHECK (status IN ('VISIBLE','MASQUE')),
    moderation_reason   TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (product_id, buyer_user_id)
);

CREATE TABLE shop_reviews (
    id                  BIGSERIAL PRIMARY KEY,
    shop_id             BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    buyer_user_id       BIGINT NOT NULL REFERENCES users(id),
    order_id            BIGINT NOT NULL REFERENCES orders(id),
    rating              SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment             TEXT,
    status              VARCHAR(10) NOT NULL DEFAULT 'VISIBLE' CHECK (status IN ('VISIBLE','MASQUE')),
    moderation_reason   TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (shop_id, buyer_user_id)
);

CREATE TABLE contact_messages (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT REFERENCES users(id),
    full_name               VARCHAR(150) NOT NULL,
    email                   VARCHAR(255) NOT NULL,
    subject                 VARCHAR(200) NOT NULL,
    message                 TEXT NOT NULL,
    status                  VARCHAR(15) NOT NULL DEFAULT 'NOUVEAU' CHECK (status IN ('NOUVEAU','EN_COURS','TRAITE')),
    admin_response          TEXT,
    handled_by_admin_id     BIGINT REFERENCES users(id),
    handled_at              TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE account_recovery_requests (
    id                              BIGSERIAL PRIMARY KEY,
    user_id                         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    submitted_selfie_url            VARCHAR(500) NOT NULL,
    status                          VARCHAR(15) NOT NULL DEFAULT 'NOUVELLE'
                                     CHECK (status IN ('NOUVELLE','EN_COURS','APPROUVEE','REJETEE')),
    reviewed_by_admin_id            BIGINT REFERENCES users(id),
    review_reason                   TEXT,
    temporary_password_issued_at    TIMESTAMP,
    created_at                      TIMESTAMP NOT NULL DEFAULT now(),
    reviewed_at                     TIMESTAMP
);

CREATE TABLE notifications (
    id                  BIGSERIAL PRIMARY KEY,
    recipient_user_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type                VARCHAR(40) NOT NULL,
    title               VARCHAR(200) NOT NULL,
    message             TEXT NOT NULL,
    link_url            VARCHAR(500),
    read_at             TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE shop_payout_accounts (
    shop_id              BIGINT PRIMARY KEY REFERENCES shops(id) ON DELETE CASCADE,
    operator             VARCHAR(20) NOT NULL CHECK (operator IN ('ORANGE','MTN','MOOV','WAVE')),
    phone_number         VARCHAR(30) NOT NULL,
    account_holder_name  VARCHAR(150) NOT NULL,
    verified             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE vendor_payouts (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    shop_id             BIGINT NOT NULL REFERENCES shops(id),
    amount              NUMERIC(12,2) NOT NULL,
    status              VARCHAR(15) NOT NULL DEFAULT 'EN_ATTENTE' CHECK (status IN ('EN_ATTENTE','ENVOYE','ECHEC')),
    fedapay_payout_id   VARCHAR(100),
    failure_reason      TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    processed_at        TIMESTAMP
);

-- Index
CREATE INDEX idx_products_status_category ON products(status, category_id);
CREATE INDEX idx_products_shop ON products(shop_id);
CREATE INDEX idx_shops_status ON shops(status);
CREATE INDEX idx_orders_buyer ON orders(buyer_user_id);
CREATE INDEX idx_orders_shop ON orders(shop_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_product_reviews_product_status ON product_reviews(product_id, status);
CREATE INDEX idx_shop_reviews_shop_status ON shop_reviews(shop_id, status);
CREATE INDEX idx_contact_messages_status ON contact_messages(status);
CREATE INDEX idx_account_recovery_status ON account_recovery_requests(status);
CREATE INDEX idx_vendor_payouts_status ON vendor_payouts(status);
CREATE INDEX idx_notifications_recipient_read ON notifications(recipient_user_id, read_at);
