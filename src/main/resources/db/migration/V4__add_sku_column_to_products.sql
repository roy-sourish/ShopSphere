ALTER TABLE products
    ADD sku VARCHAR(255);

ALTER TABLE products
    ALTER COLUMN sku SET NOT NULL;

ALTER TABLE products
    ADD CONSTRAINT uc_products_sku UNIQUE (sku);