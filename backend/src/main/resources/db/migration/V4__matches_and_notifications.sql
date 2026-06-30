CREATE TABLE match_candidates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    score INT NOT NULL,
    cycle_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_match_candidates_cycle_key UNIQUE (cycle_key)
);

CREATE INDEX idx_match_candidates_score_created ON match_candidates (score, created_at);

CREATE TABLE match_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_candidate_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    exchange_post_id BIGINT NOT NULL,
    order_index INT NOT NULL,
    accept_status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_match_participants_candidate FOREIGN KEY (match_candidate_id) REFERENCES match_candidates (id),
    CONSTRAINT fk_match_participants_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_match_participants_post FOREIGN KEY (exchange_post_id) REFERENCES exchange_posts (id),
    CONSTRAINT uk_match_participants_candidate_post UNIQUE (match_candidate_id, exchange_post_id)
);

CREATE INDEX idx_match_participants_user ON match_participants (user_id);

CREATE TABLE match_edges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_candidate_id BIGINT NOT NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    from_post_id BIGINT NOT NULL,
    to_post_id BIGINT NOT NULL,
    provide_item_id BIGINT NOT NULL,
    want_item_id BIGINT NOT NULL,
    edge_order INT NOT NULL,
    score INT NOT NULL,
    CONSTRAINT fk_match_edges_candidate FOREIGN KEY (match_candidate_id) REFERENCES match_candidates (id),
    CONSTRAINT fk_match_edges_from_user FOREIGN KEY (from_user_id) REFERENCES users (id),
    CONSTRAINT fk_match_edges_to_user FOREIGN KEY (to_user_id) REFERENCES users (id),
    CONSTRAINT fk_match_edges_from_post FOREIGN KEY (from_post_id) REFERENCES exchange_posts (id),
    CONSTRAINT fk_match_edges_to_post FOREIGN KEY (to_post_id) REFERENCES exchange_posts (id),
    CONSTRAINT fk_match_edges_provide_item FOREIGN KEY (provide_item_id) REFERENCES provide_items (id),
    CONSTRAINT fk_match_edges_want_item FOREIGN KEY (want_item_id) REFERENCES want_items (id)
);

CREATE INDEX idx_match_edges_candidate_order ON match_edges (match_candidate_id, edge_order);

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    type VARCHAR(40) NOT NULL,
    is_read BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_notifications_user_created ON notifications (user_id, created_at);
