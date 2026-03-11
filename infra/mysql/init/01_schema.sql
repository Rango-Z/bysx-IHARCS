CREATE DATABASE IF NOT EXISTS smart_home DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_home;

DROP TABLE IF EXISTS user;
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  phone VARCHAR(32),
  email VARCHAR(128),
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS device;
CREATE TABLE device (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  device_id VARCHAR(64) NOT NULL UNIQUE,
  sn_code VARCHAR(64) NOT NULL UNIQUE,
  device_type VARCHAR(32) NOT NULL,
  brand VARCHAR(64),
  name VARCHAR(128),
  online_status TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS user_device_bind;
CREATE TABLE user_device_bind (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  device_id BIGINT NOT NULL,
  bind_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_owner TINYINT NOT NULL DEFAULT 1,
  UNIQUE KEY uk_user_device (user_id, device_id),
  CONSTRAINT fk_bind_user FOREIGN KEY (user_id) REFERENCES user(id),
  CONSTRAINT fk_bind_device FOREIGN KEY (device_id) REFERENCES device(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS control_record;
CREATE TABLE control_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  command_id VARCHAR(64) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  device_id BIGINT NOT NULL,
  command_type VARCHAR(64) NOT NULL,
  command_payload JSON,
  status TINYINT NOT NULL DEFAULT 0,
  request_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  execute_time DATETIME NULL,
  result_message VARCHAR(255),
  CONSTRAINT fk_ctrl_user FOREIGN KEY (user_id) REFERENCES user(id),
  CONSTRAINT fk_ctrl_device FOREIGN KEY (device_id) REFERENCES device(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化演示用户
INSERT INTO user (username, password_hash, phone, email)
VALUES ('admin', '$2a$10$1mTnZq1yBqkVfKkUqQ9TtO9Xx6m.7f0l.0l5oK1eJ0QJ9m3S3kY3K', '13800000000', 'admin@example.com')
ON DUPLICATE KEY UPDATE username=username;

-- 初始化 4 台模拟设备（device_id 固定为题目要求）
INSERT INTO device (device_id, sn_code, device_type, brand, name, online_status)
VALUES
  ('ac001', 'SN-AC-001', 'AC', 'Demo', '客厅空调', 0),
  ('fr001', 'SN-FR-001', 'FRIDGE', 'Demo', '厨房冰箱', 0),
  ('wa001', 'SN-WA-001', 'WASHER', 'Demo', '阳台洗衣机', 0),
  ('li001', 'SN-LI-001', 'LIGHT', 'Demo', '客厅灯光', 0)
ON DUPLICATE KEY UPDATE device_id=device_id;

-- 绑定 admin 与四台设备（user_id=1 的前提下）
INSERT INTO user_device_bind (user_id, device_id, is_owner)
SELECT 1, d.id, 1 FROM device d
ON DUPLICATE KEY UPDATE is_owner=VALUES(is_owner);

