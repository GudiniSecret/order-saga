CREATE TABLE orders (
    -- orderId выходит за границы сервиса, он будет ключом Kafka-сообщения, полем в событиях и в URL
    -- Глобально уникальный ID не надо согласовывать с другими сервисами

    --UUID можно сгенерировать в приложении до вставки в БД - удобно, когда id Известен сразу без обращения к БД
    -- По UUID нельзя перебрать чужие заказы, а по /orders/1, /orders/2 - можно
    id UUID PRIMARY KEY,
    customer_id UUID NULL, -- Внешний id невозможен, так как customer лежит в другом сервисе
    status VARCHAR(32) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL, -- Обеспечивает абсолютную десятичную точностью, в отличие от FLOAT, REAL и DOUBLE
                                          -- которые хранят десятичные части с бесконечной погрешностью. 12, 2 по стандарту - 2 значения после запятой, 10 - до
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_orders_status
        CHECK (status IN ('NEW', 'CONFIRMED', 'CANCELLED')),
    CONSTRAINT chk_orders_total_amount_non_negative
        CHECK (total_amount >= 0)
);

CREATE INDEX idx_orders_customer_id_created_at
    ON orders (customer_id, created_at DESC);

CREATE TABLE order_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY , -- order_items никогда не покидает order-service, поэтому можно использовать просто автоинкремент + BIGINT занимает 8 байт вместо 16
    order_id UUID NOT NULL,
    product_id UUID NOT NULL, -- id будет лежать в inventory-service
    quantity INTEGER NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id)
            ON DELETE CASCADE,
    CONSTRAINT  uq_order_items_order_product
                         UNIQUE (order_id, product_id),
    CONSTRAINT  chk_order_items_quantity_positive
                         CHECK (quantity > 0),
    CONSTRAINT  chk_order_items_price_non_negative
        CHECK (price >= 0)
);