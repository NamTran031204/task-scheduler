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


CREATE TABLE user_task_lists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_list_id BIGINT NOT NULL,
    role ENUM('HOST', 'MEMBER') DEFAULT 'MEMBER',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (task_list_id) REFERENCES task_lists(id) ON DELETE CASCADE,
    UNIQUE KEY unique_member (user_id, task_list_id),
    INDEX idx_user (user_id),
    INDEX idx_task_list (task_list_id),
    INDEX idx_role (role)
);


CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    is_completed BOOLEAN DEFAULT FALSE,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    due_date DATETIME,
    task_list_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (task_list_id) REFERENCES task_lists(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_task_list (task_list_id),
    INDEX idx_created_by (created_by),
    INDEX idx_due_date (due_date),
    INDEX idx_completed (is_completed),
    INDEX idx_priority (priority),
    INDEX idx_created_by_completed (created_by, is_completed)
);

CREATE TABLE task_recurrences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    recurrence_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') NOT NULL,
    recurrence_interval INT DEFAULT 1, -- Lặp lại mỗi X đơn vị
    recurrence_end_date DATE, -- Ngày kết thúc lặp
    next_due_date DATETIME, -- Ngày đến hạn tiếp theo
    is_active BOOLEAN DEFAULT TRUE, -- Có thể tạm dừng recurring
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    INDEX idx_task (task_id),
    INDEX idx_next_due (next_due_date),
    INDEX idx_active (is_active),
    INDEX idx_type (recurrence_type)
);

-- TÁCH RIÊNG: Bảng task_reminders - Nhắc nhở cho mỗi task
CREATE TABLE task_reminders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    remind_at DATETIME NOT NULL,
    message VARCHAR(500),
    reminder_type ENUM('EMAIL', 'PUSH', 'SMS') DEFAULT 'EMAIL',
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at DATETIME,
    created_by BIGINT NOT NULL, -- Ai tạo reminder này
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_task (task_id),
    INDEX idx_remind_time (remind_at),
    INDEX idx_pending (is_sent, remind_at),
    INDEX idx_creator (created_by)
);

-- ĐƠN GIẢN HÓA: Bảng attachments - Gộp file và note vào một bảng
CREATE TABLE attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,

    -- File attachment fields (nullable nếu chỉ là note)
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    file_size BIGINT, -- Size in bytes
    file_type VARCHAR(100), -- MIME type

    -- Note fields (nullable nếu chỉ là file)
    note_content TEXT,

    -- Common fields
    attachment_type ENUM('FILE', 'NOTE', 'BOTH') NOT NULL,
    uploaded_by BIGINT NOT NULL,
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_task (task_id),
    INDEX idx_uploader (uploaded_by),
    INDEX idx_type (attachment_type),

    -- Constraint: Nếu là FILE thì phải có file_name và file_path
    CONSTRAINT chk_file_required
        CHECK (attachment_type != 'FILE' OR (file_name IS NOT NULL AND file_path IS NOT NULL)),
    -- Constraint: Nếu là NOTE thì phải có note_content
    CONSTRAINT chk_note_required
        CHECK (attachment_type != 'NOTE' OR note_content IS NOT NULL)
);

-- GIỮ NGUYÊN: Bảng task_history - Lịch sử thay đổi (audit trail)
CREATE TABLE task_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action ENUM('CREATED', 'UPDATED', 'COMPLETED', 'DELETED', 'ASSIGNED', 'RECURRING_CREATED') NOT NULL,
    old_value TEXT,
    new_value TEXT,
    description VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_task (task_id),
    INDEX idx_user (user_id),
    INDEX idx_action (action),
    INDEX idx_created (created_at)
);

-- THÊM: Bảng notifications - Thông báo hệ thống
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_id BIGINT, -- Nullable cho notification tổng quát
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type ENUM('REMINDER', 'TASK_ASSIGNED', 'TASK_COMPLETED', 'SHARED_LIST', 'SYSTEM') NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    read_at DATETIME,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_task (task_id),
    INDEX idx_unread (user_id, is_read),
    INDEX idx_type (notification_type),
    INDEX idx_sent (sent_at)
);

CREATE TABLE user_task_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    assigned_by BIGINT NOT NULL,
    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('IN_PROGRESS', 'COMPLETED') DEFAULT 'IN_PROGRESS',
    changed_status DATETIME NULL,

    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE CASCADE,

    UNIQUE KEY unique_task_user (task_id, user_id),
    INDEX idx_task (task_id),
    INDEX idx_user (user_id),
    INDEX idx_assigned_by (assigned_by),
    INDEX idx_status (status),
    INDEX idx_changed_status (changed_status)
);