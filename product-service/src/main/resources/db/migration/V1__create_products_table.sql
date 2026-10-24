CREATE TABLE products (
                          id          BIGINT         NOT NULL AUTO_INCREMENT,
                          name        VARCHAR(100)   NOT NULL,
                          description VARCHAR(500),
                          price       DECIMAL(10, 2) NOT NULL,
                          stock       INT            NOT NULL,
                          created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (id),
                          CONSTRAINT chk_products_price CHECK (price >= 0),
                          CONSTRAINT chk_products_stock CHECK (stock >= 0)
);
