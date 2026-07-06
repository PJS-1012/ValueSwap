ALTER TABLE provide_items MODIFY sub_category VARCHAR(100) NULL;
ALTER TABLE provide_items MODIFY estimated_value BIGINT NULL;
ALTER TABLE provide_items ADD COLUMN value_policy VARCHAR(30) NOT NULL DEFAULT 'DIRECT';

ALTER TABLE want_items MODIFY sub_category VARCHAR(100) NULL;
ALTER TABLE want_items MODIFY min_value BIGINT NULL;
ALTER TABLE want_items MODIFY max_value BIGINT NULL;
ALTER TABLE want_items ADD COLUMN value_policy VARCHAR(30) NOT NULL DEFAULT 'DIRECT';

ALTER TABLE notifications ADD COLUMN reference_id BIGINT NULL;
