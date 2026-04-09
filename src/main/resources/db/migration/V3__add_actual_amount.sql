-- Add actual_amount column for recording the actual amount paid by the user
-- (may differ from order amount due to discounts, coupons, etc.)
ALTER TABLE pay_payment_orders ADD COLUMN actual_amount BIGINT;
