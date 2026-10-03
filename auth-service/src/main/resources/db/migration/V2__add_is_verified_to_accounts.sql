-- Thêm cột is_verified vào bảng accounts
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS is_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Tài khoản đăng ký bằng GOOGLE tự động được đánh dấu đã xác thực
UPDATE accounts SET is_verified = TRUE WHERE type = 'GOOGLE';

-- Các tài khoản đã từng xác thực thành công qua mã OTP (used = TRUE) cũng được đánh dấu đã xác thực
UPDATE accounts SET is_verified = TRUE WHERE id IN (SELECT account_id FROM verification_tokens WHERE used = TRUE);
