-- Drop incorrect uniqueness constraint
ALTER TABLE carts
    DROP CONSTRAINT uq_carts_user;

-- Enforce only one ACTIVE cart per user
CREATE UNIQUE INDEX uq_active_cart_per_user
    ON carts(user_id)
    WHERE status = 'ACTIVE';
