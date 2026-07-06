CREATE TABLE trade_rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_candidate_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    completed_at TIMESTAMP(6),
    CONSTRAINT uk_trade_rooms_match UNIQUE (match_candidate_id),
    CONSTRAINT fk_trade_rooms_match FOREIGN KEY (match_candidate_id) REFERENCES match_candidates (id)
);

CREATE TABLE trade_room_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trade_room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    last_read_message_id BIGINT,
    completed_at TIMESTAMP(6),
    CONSTRAINT uk_trade_room_members_room_user UNIQUE (trade_room_id, user_id),
    CONSTRAINT fk_trade_room_members_room FOREIGN KEY (trade_room_id) REFERENCES trade_rooms (id),
    CONSTRAINT fk_trade_room_members_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_trade_room_members_user ON trade_room_members (user_id, trade_room_id);

CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trade_room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    client_message_id VARCHAR(36) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_chat_messages_client UNIQUE (trade_room_id, sender_id, client_message_id),
    CONSTRAINT fk_chat_messages_room FOREIGN KEY (trade_room_id) REFERENCES trade_rooms (id),
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id)
);

CREATE INDEX idx_chat_messages_room_id ON chat_messages (trade_room_id, id);
