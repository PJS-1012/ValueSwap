CREATE TABLE exchange_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    status VARCHAR(30) NOT NULL,
    region VARCHAR(100) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_exchange_posts_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_exchange_posts_status_created ON exchange_posts (status, created_at);
CREATE INDEX idx_exchange_posts_user_created ON exchange_posts (user_id, created_at);

CREATE TABLE provide_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exchange_post_id BIGINT NOT NULL,
    category VARCHAR(40) NOT NULL,
    sub_category VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    quantity INT NOT NULL,
    estimated_value BIGINT NOT NULL,
    CONSTRAINT fk_provide_items_post FOREIGN KEY (exchange_post_id) REFERENCES exchange_posts (id)
);

CREATE INDEX idx_provide_items_post ON provide_items (exchange_post_id);
CREATE INDEX idx_provide_items_category ON provide_items (category, sub_category);

CREATE TABLE provide_item_tags (
    provide_item_id BIGINT NOT NULL,
    tag_order INT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (provide_item_id, tag_order),
    CONSTRAINT fk_provide_item_tags_item FOREIGN KEY (provide_item_id) REFERENCES provide_items (id)
);

CREATE TABLE want_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exchange_post_id BIGINT NOT NULL,
    category VARCHAR(40) NOT NULL,
    sub_category VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    quantity INT NOT NULL,
    min_value BIGINT NOT NULL,
    max_value BIGINT NOT NULL,
    CONSTRAINT fk_want_items_post FOREIGN KEY (exchange_post_id) REFERENCES exchange_posts (id)
);

CREATE INDEX idx_want_items_post ON want_items (exchange_post_id);
CREATE INDEX idx_want_items_category ON want_items (category, sub_category);

CREATE TABLE want_item_tags (
    want_item_id BIGINT NOT NULL,
    tag_order INT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (want_item_id, tag_order),
    CONSTRAINT fk_want_item_tags_item FOREIGN KEY (want_item_id) REFERENCES want_items (id)
);
