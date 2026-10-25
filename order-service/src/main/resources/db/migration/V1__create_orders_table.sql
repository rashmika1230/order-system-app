CREATE TABLE orders (
                        id          BIGINT         NOT NULL AUTO_INCREMENT,
                        product_id  BIGINT         NOT NULL,
                        quantity    INT            NOT NULL,
                        unit_price  DECIMAL(10, 2) NOT NULL,
                        total_price DECIMAL(12, 2) NOT NULL,
                        status      VARCHAR(20)    NOT NULL,
                        created_at  TIMESTAMP(6)   NOT NULL,
                        PRIMARY KEY (id),
                        CONSTRAINT chk_orders_quantity CHECK (quantity > 0),
                        INDEX idx_orders_product_id (product_id)
);
