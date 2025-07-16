-- Bảng users - Quản lý người dùng
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    avatar_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    reset_password_token VARCHAR(255),
    reset_password_expires DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_username (username),
    INDEX idx_reset_token (reset_password_token)
);

-- Bảng task_lists - Danh sách công việc
CREATE TABLE task_lists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    color VARCHAR(7) DEFAULT '#3b82f6', -- Hex color code
    is_shared BOOLEAN DEFAULT FALSE,
    share_code VARCHAR(20) UNIQUE, -- Mã chia sẻ
    owner_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_owner (owner_id),
    INDEX idx_share_code (share_code),
    INDEX idx_shared (is_shared)
);

-- Bảng task_list_members - Thành viên trong danh sách chia sẻ
CREATE TABLE task_list_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_list_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('HOST', 'MEMBER') DEFAULT 'MEMBER',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (task_list_id) REFERENCES task_lists(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_member (task_list_id, user_id),
    INDEX idx_task_list (task_list_id),
    INDEX idx_user (user_id)
);

-- Bảng tasks - Công việc cụ thể
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    is_completed BOOLEAN DEFAULT FALSE,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    deadline DATETIME,
    completed_at DATETIME,
    
    -- Recurring fields
    is_recurring BOOLEAN DEFAULT FALSE,
    recurring_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'),
    recurring_interval INT DEFAULT 1, -- Lặp lại mỗi X đơn vị
    recurring_end_date DATE, -- Ngày kết thúc lặp
    next_due_date DATETIME, -- Ngày đến hạn tiếp theo
    
    -- Relations
    task_list_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    assigned_to BIGINT, -- Người được giao việc (trong shared list)
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (task_list_id) REFERENCES task_lists(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_task_list (task_list_id),
    INDEX idx_deadline (deadline),
    INDEX idx_completed (is_completed),
    INDEX idx_priority (priority),
    INDEX idx_recurring (is_recurring),
    INDEX idx_next_due (next_due_date),
    INDEX idx_created_by (created_by),
    INDEX idx_assigned_to (assigned_to)
);

-- Bảng reminders - Nhắc nhở
CREATE TABLE reminders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    remind_at DATETIME NOT NULL,
    message VARCHAR(500),
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at DATETIME,
    reminder_type ENUM('EMAIL', 'PUSH', 'SMS') DEFAULT 'EMAIL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    INDEX idx_task (task_id),
    INDEX idx_remind_time (remind_at),
    INDEX idx_pending (is_sent, remind_at)
);

-- Bảng task_attachments - File đính kèm
CREATE TABLE task_attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT, -- Size in bytes
    file_type VARCHAR(100), -- MIME type
    uploaded_by BIGINT NOT NULL,
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_task (task_id),
    INDEX idx_uploader (uploaded_by)
);

-- Bảng task_history - Lịch sử thay đổi (optional, cho audit trail)
CREATE TABLE task_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action ENUM('CREATED', 'UPDATED', 'COMPLETED', 'DELETED', 'ASSIGNED') NOT NULL,
    old_value JSON,
    new_value JSON,
    description VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_task (task_id),
    INDEX idx_user (user_id),
    INDEX idx_action (action),
    INDEX idx_created (created_at)
);