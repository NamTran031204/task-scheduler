# Task Scheduler API Documentation

## Base URL
```
http://localhost:8080/api/v1
```

## Database Schema Overview

### Main Tables:
- **`users`**: Store user accounts (id, username, email, password, full_name, avatar_url, is_active, created_at, updated_at)
- **`task_lists`**: Store task lists (id, name, description, color, is_shared, share_code, owner_id, created_at, updated_at)
- **`tasks`**: Store tasks (id, title, description, is_completed, priority, due_date, completed_at, task_list_id, created_by, assigned_to, created_at, updated_at)
- **`user_task_lists`**: Many-to-many relationship between users and task lists (id, user_id, task_list_id, role, joined_at)
- **`task_reminders`**: Store task reminders (id, task_id, remind_at, message, reminder_type, is_sent, sent_at, created_by, created_at)
- **`task_recurrences`**: Store task recurrence patterns (id, task_id, recurrence_type, recurrence_interval, recurrence_end_date, next_due_date, is_active, created_at, updated_at)
- **`attachments`**: Store file attachments (id, task_id, file_name, file_path, file_size, file_type, note_content, attachment_type, uploaded_by, uploaded_at)

### Key Relationships:
- Users can own multiple task lists (1:N)
- Users can be members of multiple task lists via user_task_lists (N:N)
- Task lists contain multiple tasks (1:N)
- Tasks can be assigned to users (N:1)
- Tasks can have multiple reminders (1:N)
- Tasks can have recurrence patterns (1:1)
- Tasks can have multiple attachments (1:N)

### Access Control:
- Người dùng chỉ có thể truy cập task lists mà họ là thành viên
- Các thao tác trên task yêu cầu quyền thành viên trong task list chứa task đó
- Chỉ chủ sở hữu task list mới có thể quản lý chia sẻ và quyền thành viên

## Error Response Structure
All error responses follow this format:
```json
{
  "status": integer,
  "message": "string",
  "timestamp": "date",
  "path": "string",
  "error": "string"
}
```

---

## 1. User Management APIs

### 1.1 Register User
- **Endpoint**: `/user/register`
- **HTTP Method**: `POST`
- **Function/Use Case**: Tạo tài khoản
- **Database Storage**: 
  - **Primary Table**: `users`
    - **Operation**: INSERT
    - **Fields affected**: `username`, `email`, `password`, `full_name`, `is_active` (default: true), `created_at`, `updated_at`
    - **Notes**: `created_at` và `updated_at` được set từ input nếu có; ngược lại mặc định là thời gian hiện tại.
    - **Auto-generated**: `id`
- **Request Body** (UserDTO):
```json
{
  "username": "string",        // required, cannot be blank
  "email": "string",          // required, cannot be blank, must be valid email format
  "password": "string",       // required, cannot be blank
  "fullname": "string"        // optional, user's full name
}
```
- **Response (200 OK)** (UserResponse):
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T10:30:00"
}
```
- **Exceptions**:
  - `400 BAD_REQUEST` - USER_EXISTED (1001): User already exists
  - `400 BAD_REQUEST` - USER_PASSWORD_FOUND (1004): Password already exists in system
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data
  - `400 BAD_REQUEST` - HTTP_MESSAGE_NOT_READABLE (12001): Malformed request body

### 1.2 User Login
- **Endpoint**: `/user/login`
- **HTTP Method**: `POST`
- **Function/Use Case**: Đăng nhập
- **Database Storage**: 
  - **Table**: `users`
  - **Operation**: SELECT
  - **Fields queried**: `id`, `username`, `email`, `password`, `full_name`, `is_active`
  - **Authentication**: So sánh password với encrypted password đã lưu
- **Request Body** (UserLoginDTO):
```json
{
  "email": "string",          // required, cannot be blank
  "password": "string"        // required, cannot be blank
}
```
- **Response (200 OK)**:
```json
"login success"
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found
  - `401 UNAUTHORIZED` - USER_PASSWORD_INCORRECT (1003): Password is incorrect
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data

### 1.3 Update User Avatar
- **Endpoint**: `/user/update_avatar/{id}`
- **HTTP Method**: `POST`
- **Function/Use Case**: Upload ảnh đại diện của người dùng, trong DB sẽ lưu url dẫn đến ảnh của người dùng
- **Database Storage**: 
  - **Table**: `users`
  - **Operation**: UPDATE
  - **Fields affected**: `avatar_url`, `updated_at`
  - **File storage**: File avatar được lưu vào file system, URL được lưu trong database
- **Path Parameters**: 
  - `id` (long): User ID
- **Request Body**: `MultipartFile` (form-data with key "file")
- **Response (200 OK)**:
```json
"Update User Avatar Successfully"
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found
  - `400 BAD_REQUEST` - FILE_TYPE_NOT_SUPPORTED (11004): File type not supported
  - `413 PROCESSING` - FILE_TOO_LARGE (11003): File size exceeds 10MB limit
  - `422 PROCESSING` - FILE_UPLOAD_FAILED (11005): Upload failed

### 1.4 Get User by ID
- **Endpoint**: `/user/{id}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Lấy thông tin user bằng userId
- **Database Storage**: 
  - **Table**: `users`
  - **Operation**: SELECT
  - **Fields queried**: `id`, `username`, `email`, `full_name`, `avatar_url`, `is_active`, `created_at`, `updated_at`
- **Path Parameters**: 
  - `id` (long): User ID
- **Response (200 OK)** (UserResponse):
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T10:30:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found

### 1.5 Get All Users (Paginated)
- **Endpoint**: `/user/get-all-user`
- **HTTP Method**: `GET`
- **Function/Use Case**: Lấy ra tất cả users (tính năng này chỉ dùng cho ADMIN hệ thống)
- **Database Storage**: 
  - **Table**: `users`
  - **Operation**: SELECT with pagination
  - **Fields queried**: `id`, `username`, `email`, `full_name`, `avatar_url`, `is_active`, `created_at`, `updated_at`
  - **Sorting**: ORDER BY `username` DESC
- **Query Parameters**:
  - `record` (int): Number of records per page
  - `page` (int): Page number (0-based)
- **Response (200 OK)** (UserListResponse):
```json
{
  "userResponses": [
    {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "avatarUrl": null,
      "createdAt": "2025-08-04T10:30:00",
      "updatedAt": "2025-08-04T10:30:00"
    }
  ],
  "totalPage": 5
}
```
- **Exceptions**:
  - `500 INTERNAL_SERVER_ERROR` - DATABASE_EXCEPTION (10000): Database error

### 1.6 Update User
- **Endpoint**: `/user/update/{id}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Chỉnh sửa thông tin của user, bao gồm các usecase: chỉnh sửa thông tin (username or fullname), chỉnh sửa avatar (upload avatar mới), hoặc tổng hợp cả 2 usecase trên (chỉnh sửa thông tin user)
- **Lời khuyên từ backend**: chỗ này làm một cái form như kiểu Linkedin khi cập nhật profile.
- **Database Storage**: 
  - **Table**: `users`
  - **Operation**: UPDATE
  - **Fields affected**: `username`, `full_name`, `updated_at`
  - **Notes**: Nếu file `avatar` được cung cấp, nó sẽ được validate và file avatar cũ sẽ bị xóa;
- **Path Parameters**: 
  - `id` (long): User ID
- **Request Input**: form-data body (key - type)
  + key: username - type: text,        
  + key: fullname - type: text,
  + key: avatar - type: file,
- **Response (200 OK)**:
```json
"update complete"
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data

### 1.7 Delete User
- **Endpoint**: `/user/delete/{id}`
- **HTTP Method**: `DELETE`
- **Function/Use Case**: Xóa tài khoản user
- **Database Storage**: 
  - **Primary Table**: `users`
    - **Operation**: DELETE (CASCADE)
  - **Related tables affected** (via CASCADE DELETE or UPDATE):
    - **`user_task_lists`**: Tất cả các record thành viên của user này
    - **`task_lists`**: Task lists thuộc sở hữu của user này (nếu không có thành viên khác thì xóa; nếu có thành viên thì chuyển quyền sở hữu)
    - **`tasks`**: 
      - WHERE `created_by` = user_id: Set về NULL hoặc xóa
      - WHERE `assigned_to` = user_id: Set về NULL
    - **`task_reminders`**: Tất cả reminders được tạo bởi user này
    - **`attachments`**: Tất cả files được upload bởi user này
    - **`task_histories`**: Tất cả history records được tạo bởi user này
    - **`notifications`**: Tất cả notifications cho user này
  - **Complex logic**: Xử lý chuyển quyền sở hữu cho task lists
- **Path Parameters**: 
  - `id` (long): User ID
- **Response (200 OK)**:
```json
"Delete Complete"
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found
  - `400 BAD_REQUEST` - DATABASE_EXCEPTION (10000): Cannot delete due to foreign key constraints

### 1.8 Get Calendar Tasks
- **Endpoint**: `/user/calendar-tasks/{userId}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Lấy danh sách task cho view Calendar theo khoảng ngày, nhóm theo từng ngày
- **Database Storage**: 
  - **Tables**: `tasks`, `task_lists`, `user_task_lists`
  - **Operation**: SELECT with JOINs và date filtering
  - **Fields queried**: Các tasks có `due_date` nằm trong khoảng [startDate, endDate], bao gồm cả task list color/name để render calendar
  - **Joins**: tasks → task_lists (listName/color), user_task_lists (cho access control)
- **Path Parameters**: 
  - `userId` (long): User ID
- **Query Parameters**:
  - `start-date` (LocalDate): Start date (format: YYYY-MM-DD)
  - `end-date` (LocalDate): End date (format: YYYY-MM-DD)
- **Response (200 OK)** (CalendarResponse):
```json
{
  "tasksByDate": {
    "2025-01-15": [
      {
        "id": 1,
        "title": "Complete project documentation",
        "description": "Write comprehensive API documentation",
        "dueDate": "2025-01-15T14:30:00",
        "priority": "HIGH",
        "isCompleted": false,
        "color": "#4285F4",
        "listName": "Work Tasks",
        "recurringInstanceId": null,
        "isRecurring": false
      }
    ],
    "2025-01-16": []
  },
  "startDate": "2025-01-15",
  "endDate": "2025-01-21",
  "totalTasks": 1,
  "taskCountsByPriority": {
    "LOW": 0,
    "MEDIUM": 0,
    "HIGH": 1,
    "URGENT": 0
  }
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid date parameters
  - `500 INTERNAL_SERVER_ERROR` - DATABASE_EXCEPTION (10000): Database error

---

## 2. Task Management APIs

### 2.1 Create Task
- **Endpoint**: `/task/user/{userId}/create`
- **HTTP Method**: `POST`
- **Function/Use Case**: Tạo một task mới (không bao gồm file attachments)
- **Database Storage**: 
  - **Table**: `tasks`
  - **Operation**: INSERT
  - **Fields affected**: `title`, `description`, `priority` (enum: LOW/MEDIUM/HIGH/URGENT), `due_date`, `task_list_id`, `created_by`, `assigned_to`, `is_completed` (default: false), `created_at`, `updated_at`
  - **Auto-generated**: `id`, `created_at`, `updated_at`
  - **Validation**: task_list_id phải tồn tại và user phải có quyền truy cập
- **Path Parameters**: 
  - `userId` (long): User ID
- **Request Body** (TaskDTO):
```json
{
  "title": "string",           // required, cannot be blank
  "description": "string",     // optional
  "priority": "string",        // optional, enum: "LOW", "MEDIUM", "HIGH", "URGENT"
  "due_date": "2025-08-10T14:30:00",  // optional, ISO datetime format
  "task_list_id": 1,          // required, must be valid task list ID
  "assigned_to": 2            // optional, user ID to assign task to
}
```
- **Response (200 OK)** (TaskResponse):
```json
{
  "id": 1,
  "title": "Complete project documentation",
  "description": "Write comprehensive API documentation",
  "isCompleted": false,
  "priority": "HIGH",
  "dueDate": "2025-08-10T14:30:00",
  "completedAt": null,
  "taskListId": 1,
  "createdBy": 1,
  "assignedTo": 2,
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T10:30:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found
  - `404 NOT_FOUND` - TASKLIST_NOT_FOUND (2002): Task list not found
  - `403 FORBIDDEN` - TASKLIST_ACCESS_DENIED (2003): No access to task list
  - `400 BAD_REQUEST` - TASK_INVALID_DUE_DATE (3004): Due date cannot be in the past
  - `400 BAD_REQUEST` - TASK_ASSIGNMENT_FAILED (3005): Cannot assign to non-member
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data

### 2.2 Upload Task Attachments
- **Endpoint**: `/task/user/{userId}/create/task/{taskId}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Upload file attachments vào một task đã tồn tại
- **Database Storage**: 
  - **Table**: `attachments`
  - **Operation**: INSERT (multiple records)
  - **Fields affected**: `task_id`, `file_name`, `file_path`, `file_size`, `file_type`, `attachment_type` (FILE), `uploaded_by`, `uploaded_at`
  - **File storage**: Files được lưu vào file system, metadata được lưu trong database
  - **Auto-generated**: `id`, `uploaded_at`
- **Path Parameters**: 
  - `userId` (long): User ID
  - `taskId` (long): Task ID
- **Request Body**: `List<MultipartFile>` (form-data, key: "files")
- **Response (200 OK)**:
```json
"upload files completed"
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found
  - `404 NOT_FOUND` - TASK_NOT_FOUND (3001): Task not found
  - `404 NOT_FOUND` - TASKLIST_NOT_FOUND (2002): Task list not found
  - `403 FORBIDDEN` - TASKLIST_ACCESS_DENIED (2003): User is not owner or member of the task list
  - `400 BAD_REQUEST` - FILE_TYPE_NOT_SUPPORTED (11004): Unsupported file type
  - `413 PROCESSING` - FILE_TOO_LARGE (11003): File size exceeds limit
  - `422 PROCESSING` - FILE_UPLOAD_FAILED (11005): Upload failed

### 2.3 Get Task by ID
- **Endpoint**: `/task/user/{userId}/{taskId}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Lấy thông tin chi tiết của task
- **Database Storage**: 
  - **Tables**: `tasks`, `task_lists`
  - **Operation**: SELECT with JOIN (task -> task_list)
  - **Fields queried**: Chỉ các fields của Task (TaskResponse)
  - **Access control**: User phải là owner hoặc member của task list chứa task này
- **Path Parameters**: 
  - `userId` (long): User ID
  - `taskId` (long): Task ID
- **Response (200 OK)** (TaskResponse):
```json
{
  "id": 1,
  "title": "Complete project documentation",
  "description": "Write comprehensive API documentation",
  "isCompleted": false,
  "priority": "HIGH",
  "dueDate": "2025-08-10T14:30:00",
  "completedAt": null,
  "taskListId": 1,
  "createdBy": 1,
  "assignedTo": 2,
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T10:30:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASK_NOT_FOUND (3001): Task not found
  - `404 NOT_FOUND` - TASKLIST_NOT_FOUND (2002): Task list not found
  - `403 FORBIDDEN` - TASK_ACCESS_DENIED (3002): No access to task

### 2.4 Get Tasks by Task List ID
- **Endpoint**: `/task/user/{userId}/task-list/{taskListId}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Lấy tất cả tasks trong một task list cụ thể
- **Database Storage**: 
  - **Tables**: `tasks`, `task_lists`, `user_task_lists` (cho access control)
  - **Operation**: SELECT with JOINs và pagination
  - **Fields queried**: Tất cả task fields + task list validation
  - **Sorting**: ORDER BY `created_at` DESC
  - **Access control**: Verify user có quyền truy cập task list thông qua user_task_lists
- **Path Parameters**: 
  - `userId` (long): User ID
  - `taskListId` (long): Task List ID
- **Query Parameters**:
  - `record` (int): Number of records per page
  - `page` (int): Page number (0-based)
- **Response (200 OK)** (Page<TaskResponse>):
```json
{
  "content": [
    {
      "id": 1,
      "title": "Complete project documentation",
      "description": "Write comprehensive API documentation",
      "isCompleted": false,
      "priority": "HIGH",
      "dueDate": "2025-08-10T14:30:00",
      "completedAt": null,
      "taskListId": 1,
      "createdBy": 1,
      "assignedTo": 2,
      "createdAt": "2025-08-04T10:30:00",
      "updatedAt": "2025-08-04T10:30:00"
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "size": 10,
  "number": 0
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASKLIST_NOT_FOUND (2002): Task list not found
  - `403 FORBIDDEN` - TASKLIST_ACCESS_DENIED (2003): No access to task list

### 2.5 Get Tasks by User ID
- **Endpoint**: `/task/user/{userId}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Retrieve all tasks created by a user
- **Database Storage**: 
  - **Table**: `tasks`
  - **Operation**: SELECT with pagination, WHERE `created_by` = userId
  - **Sorting**: ORDER BY `created_at` DESC
  - **Access control**: N/A (returns tasks created by the user)
- **Path Parameters**: 
  - `userId` (long): User ID
- **Query Parameters**:
  - `record` (int): Number of records per page
  - `page` (int): Page number (0-based)
- **Response (200 OK)** (Page<TaskResponse>):
```json
{
  "content": [
    {
      "id": 1,
      "title": "Complete project documentation",
      "description": "Write comprehensive API documentation",
      "isCompleted": false,
      "priority": "HIGH",
      "dueDate": "2025-08-10T14:30:00",
      "completedAt": null,
      "taskListId": 1,
      "createdBy": 1,
      "assignedTo": 2,
      "createdAt": "2025-08-04T10:30:00",
      "updatedAt": "2025-08-04T10:30:00"
    }
  ],
  "totalElements": 15,
  "totalPages": 2,
  "size": 10,
  "number": 0
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found

### 2.6 Update Task
- **Endpoint**: `/task/user/{userId}/update/{id}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Update thông tin task: có thể chia nhỏ thành các usecase:
  + cập nhật title của task (nội dung task)
  + cập nhật mô tả của task
  + Cập nhật mức độ ưu tiên của task (cái này có thể làm màn hình riêng)
  + Thêm hoặc cập nhật ngày đến hạn của task (vì task ban đầu được khởi tạo chưa chắc có ngày đến hạn)
- **Database Storage**: 
  - **Table**: `tasks`
  - **Operation**: UPDATE
  - **Fields affected**: `title`, `description`, `priority`, `due_date`, `assigned_to`, `updated_at`
  - **Access control**: Only task creator or assigned user can update
- **Path Parameters**: 
  - `userId` (long): User ID
  - `id` (long): Task ID
- **Request Body** (TaskDTO - partial update):
```json
{
  "title": "string",           // optional
  "description": "string",     // optional
  "priority": "string",        // optional, enum: "LOW", "MEDIUM", "HIGH", "URGENT"
  "due_date": "2025-08-15T16:00:00"  // optional, ISO datetime format
}
```
- **Response (200 OK)** (TaskResponse):
```json
{
  "id": 1,
  "title": "Updated project documentation",
  "description": "Write comprehensive API documentation with examples",
  "isCompleted": false,
  "priority": "URGENT",
  "dueDate": "2025-08-15T16:00:00",
  "completedAt": null,
  "taskListId": 1,
  "createdBy": 1,
  "assignedTo": 2,
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T14:20:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASK_NOT_FOUND (3001): Task not found
  - `403 FORBIDDEN` - TASK_ACCESS_DENIED (3002): No access to task
  - `400 BAD_REQUEST` - TASK_INVALID_DUE_DATE (3004): Due date cannot be in the past
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): Assigned user not found (when changing assignee)
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data

### 2.7 Delete Task
- **Endpoint**: `/task/user/{userId}/delete/{id}`
- **HTTP Method**: `DELETE`
- **Function/Use Case**: Delete a task
- **Database Storage**: 
  - **Table**: `tasks` (primary)
  - **Operation**: DELETE CASCADE
  - **Related tables affected**: `task_reminders`, `task_recurrences`, `attachments`, `task_histories`, `notifications` (all related to this task)
  - **Access control**: Only task creator or task list HOST (owner) can delete
- **Path Parameters**: 
  - `userId` (long): User ID
  - `id` (long): Task ID
- **Response (200 OK)**:
```json
"Task deleted successfully"
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASK_NOT_FOUND (3001): Task not found
  - `403 FORBIDDEN` - TASK_ACCESS_DENIED (3002): No access to task

### 2.8 Complete Task
- **Endpoint**: `/task/user/{userId}/complete/{id}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Mark a task as completed
- **Database Storage**: 
  - **Table**: `tasks`
  - **Operation**: UPDATE
  - **Fields affected**: `is_completed` (set to true), `completed_at` (set to current timestamp), `updated_at`
  - **Access control**: Only memeber user (HOST hoặc MEMBER) can complete the task
- **Path Parameters**: 
  - `userId` (long): User ID
  - `id` (long): Task ID
- **Response (200 OK)** (TaskResponse):
```json
{
  "id": 1,
  "title": "Complete project documentation",
  "description": "Write comprehensive API documentation",
  "isCompleted": true,
  "priority": "HIGH",
  "dueDate": "2025-08-10T14:30:00",
  "completedAt": "2025-08-04T15:45:00",
  "taskListId": 1,
  "createdBy": 1,
  "assignedTo": 2,
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T15:45:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASK_NOT_FOUND (3001): Task not found
  - `403 FORBIDDEN` - TASK_ACCESS_DENIED (3002): No access to task
  - `400 BAD_REQUEST` - TASK_ALREADY_COMPLETED (3003): Task is already completed

### 2.9 Assign Task
- **Endpoint**: `/task/user/{userId}/assign/{id}/assign-to/{assignedToUserId}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Assign a task to another user
- **Database Storage**: 
  - **Table**: `tasks`
  - **Operation**: UPDATE
  - **Fields affected**: `assigned_to` (set to assignedToUserId), `updated_at`
  - **Validation**: For shared task lists, `assignedToUserId` must be the owner or a member of the task list (ý là task dành cho nhiều người)
- **Path Parameters**: 
  - `userId` (long): User ID (assigner)
  - `id` (long): Task ID
  - `assignedToUserId` (long): User ID to assign task to
- **Response (200 OK)** (TaskResponse):
```json
{
  "id": 1,
  "title": "Complete project documentation",
  "description": "Write comprehensive API documentation",
  "isCompleted": false,
  "priority": "HIGH",
  "dueDate": "2025-08-10T14:30:00",
  "completedAt": null,
  "taskListId": 1,
  "createdBy": 1,
  "assignedTo": 3,
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T16:20:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASK_NOT_FOUND (3001): Task not found
  - `403 FORBIDDEN` - TASK_ACCESS_DENIED (3002): No access to assign
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): Assigned user not found
  - `403 FORBIDDEN` - TASK_ACCESS_DENIED (3002): Cannot assign to non-member (for shared lists)

### 2.10 Undo Task Completion
- **Endpoint**: `/task/user/{userId}/undo_complete/{taskId}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Revert a completed task back to pending status
- **Database Storage**: 
  - **Table**: `tasks`
  - **Operation**: UPDATE
  - **Fields affected**: `is_completed` (set to false), `completed_at` (set to current timestamp), `updated_at`
  - **Access control**: Only task creator or assigned user can undo completion
- **Path Parameters**: 
  - `userId` (long): User ID
  - `taskId` (long): Task ID
- **Response (200 OK)** (TaskResponse):
```json
{
  "id": 1,
  "title": "Complete project documentation",
  "description": "Write comprehensive API documentation",
  "isCompleted": false,
  "priority": "HIGH",
  "dueDate": "2025-08-10T14:30:00",
  "completedAt": null,
  "taskListId": 1,
  "createdBy": 1,
  "assignedTo": 2,
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T17:10:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASK_NOT_FOUND (3001): Task not found
  - `403 FORBIDDEN` - TASK_ACCESS_DENIED (3002): No access to task
  - `400 BAD_REQUEST` - TASK_IS_NOT_COMPLETED (3007): Task is not completed

---

## 3. Task List Management APIs

### 3.1 Create Task List
- **Endpoint**: `/task-list/create/{id}`
- **HTTP Method**: `POST`
- **Function/Use Case**: Create a new task list
- **Database Storage**: 
  - **Primary Table**: `task_lists`
    - **Operation**: INSERT
  - **Fields affected**: `name`, `description`, `color` (default: "#3b82f6"), `owner_id`, `is_shared` (default: false)
  - **Auto-generated**: `id`, `created_at`, `updated_at`
  - **Secondary Table**: `user_task_lists`
    - **Operation**: INSERT
    - **Fields affected**: `user_id` (= owner_id), `task_list_id` (= new task list id), `role` (= 'HOST'), `joined_at`
    - **Purpose**: Automatically add owner as HOST member
- **Path Parameters**: 
  - `id` (long): User ID
- **Request Body** (TaskListDTO):
```json
{
  "name": "string",          // required, cannot be blank
  "description": "string",   // optional
  "color": "string"         // optional, hex color code like "#FF5733"
}
```
- **Response (200 OK)** (TaskListResponse):
```json
{
  "id": 1,
  "name": "Work Projects",
  "description": "All work-related tasks and projects",
  "color": "#FF5733",
  "isShared": false,
  "shareCode": null,
  "ownerId": 1,
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T10:30:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found
  - `400 BAD_REQUEST` - TASKLIST_ALREADY_EXIST (2001): Task list already exists
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data

### 3.2 Get Task List by ID
- **Endpoint**: `/task-list/{id}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Retrieve task list information by ID
- **Database Storage**: 
  - **Table**: `task_lists`
  - **Operation**: SELECT
  - **Fields queried**: Task list fields only (TaskListResponse)
- **Path Parameters**: 
  - `id` (long): Task List ID
- **Response (200 OK)** (TaskListResponse):
```json
{
  "id": 1,
  "name": "Work Projects",
  "description": "All work-related tasks and projects",
  "color": "#FF5733",
  "isShared": true,
  "shareCode": "ABC123XYZ",
  "ownerId": 1,
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T12:15:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASKLIST_NOT_FOUND (2002): Task list not found

### 3.3 Get All Task Lists by User ID
- **Endpoint**: `/task-list/user/{userId}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Retrieve all task lists owned by a user
- **Database Storage**: 
  - **Table**: `task_lists`
  - **Operation**: SELECT with pagination, WHERE `owner_id` = userId
  - **Sorting**: ORDER BY `created_at` DESC
- **Path Parameters**: 
  - `userId` (long): User ID
- **Query Parameters**:
  - `record` (int): Number of records per page
  - `page` (int): Page number (0-based)
- **Response (200 OK)** (Page<TaskListResponse>):
```json
{
  "content": [
    {
      "id": 1,
      "name": "Work Projects",
      "description": "All work-related tasks and projects",
      "color": "#FF5733",
      "isShared": true,
      "shareCode": "ABC123XYZ",
      "ownerId": 1,
      "createdAt": "2025-08-04T10:30:00",
      "updatedAt": "2025-08-04T12:15:00"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found

### 3.4 Update Task List
- **Endpoint**: `/task-list/update/{id}/user/{userId}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Update task list information
- **Access control**: Only task list owner can update
- **Path Parameters**: 
  - `id` (long): Task List ID
  - `userId` (long): User ID
- **Request Body** (TaskListDTO - partial update):
```json
{
  "name": "string",          // optional
  "description": "string",   // optional
  "color": "string"         // optional, hex color code
}
```
- **Response (200 OK)** (TaskListResponse):
```json
{
  "id": 1,
  "name": "Updated Work Projects",
  "description": "Updated description for work-related tasks",
  "color": "#33A1FF",
  "isShared": true,
  "shareCode": "ABC123XYZ",
  "ownerId": 1,
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T14:20:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASKLIST_NOT_FOUND (2002): Task list not found
  - `403 FORBIDDEN` - TASKLIST_ACCESS_DENIED (2003): Access denied
  - `400 BAD_REQUEST` - TASKLIST_ALREADY_EXIST (2001): Duplicate name for same owner
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data

### 3.5 Delete Task List
- **Endpoint**: `/task-list/delete/{id}/user/{userId}`
- **HTTP Method**: `DELETE`
- **Function/Use Case**: Delete a task list and all its tasks
- **Database Storage**: 
  - **Primary Table**: `task_lists`
    - **Operation**: DELETE (CASCADE)
  - **Related tables affected** (via CASCADE DELETE):
    - `user_task_lists`, `tasks`, `task_reminders`, `task_recurrences`, `attachments`, `task_histories`, `notifications`
  - **Access control**: Only task list owner (HOST) can delete
  - **Business rule in current implementation**: If list is shared and has members, deletion is blocked; remove members first.
- **Path Parameters**: 
  - `id` (long): Task List ID
  - `userId` (long): User ID
- **Response (200 OK)**:
```json
"TaskList deleted successfully"
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASKLIST_NOT_FOUND (2002): Task list not found
  - `403 FORBIDDEN` - TASKLIST_ACCESS_DENIED (2003): Access denied
  - `400 BAD_REQUEST` - TASKLIST_EXCEPTION (2007): Cannot delete shared list with members

### 3.6 Share Task List
- **Endpoint**: `/task-list/share/{id}/user/{userId}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Generate a share code for the task list
- **Database Storage**: 
  - **Table**: `task_lists`
  - **Operation**: UPDATE
  - **Fields affected**: toggle `is_shared` (true/false), `share_code` (generate once if absent), `updated_at`
  - **Access control**: Only task list owner can share/unshare
- **Path Parameters**: 
  - `id` (long): Task List ID
  - `userId` (long): User ID
- **Response (200 OK)** (TaskListResponse):
```json
{
  "id": 1,
  "name": "Work Projects",
  "description": "All work-related tasks and projects",
  "color": "#FF5733",
  "isShared": true,
  "shareCode": "ABC123XYZ",
  "ownerId": 1,
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T15:45:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASKLIST_NOT_FOUND (2002): Task list not found
  - `403 FORBIDDEN` - TASKLIST_ACCESS_DENIED (2003): Access denied

### 3.7 Join Task List by Share Code
- **Endpoint**: `/task-list/join/{shareCode}/user/{userId}`
- **HTTP Method**: `POST`
- **Function/Use Case**: Join a shared task list using share code
- **Database Storage**: 
  - **Query Table**: `task_lists`
    - **Operation**: SELECT
    - **Purpose**: Validate `share_code` exists and `is_shared` = true
  - **Fields queried**: `id`, `owner_id`, `is_shared`, `share_code`
  - **Insert Table**: `user_task_lists`
    - **Operation**: INSERT
    - **Fields affected**: `user_id`, `task_list_id` (from found task list), `role` (= 'MEMBER'), `joined_at`
    - **Validation**: Check user is not already a member
- **Path Parameters**: 
  - `shareCode` (string): Share Code
  - `userId` (long): User ID
- **Response (200 OK)** (TaskListResponse):
```json
{
  "id": 1,
  "name": "Work Projects",
  "description": "All work-related tasks and projects",
  "color": "#FF5733",
  "isShared": true,
  "shareCode": "ABC123XYZ",
  "ownerId": 2,
  "createdAt": "2025-08-03T08:15:00",
  "updatedAt": "2025-08-03T08:15:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found
  - `403 FORBIDDEN` - TASKLIST_INVALID_SHARECODE (2004): Invalid share code
  - `403 FORBIDDEN` - TASKLIST_NOT_SHARED (2005): Task list is not shared
  - `400 BAD_REQUEST` - TASKLIST_CANNOT_JOIN (2006): Cannot join task list

### 3.8 Leave Task List
- **Endpoint**: `/task-list/user/{userId}/leave/taskList/{taskListId}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: User leaves a task list
- **Database Storage**: 
  - **Primary Table**: `user_task_lists`
    - **Operation**: DELETE
    - **Condition**: WHERE `user_id` = userId AND `task_list_id` = taskListId
  - **Conditional Operations**: If leaving user is HOST:
    - **Query Table**: `user_task_lists` 
      - **Operation**: SELECT (find next oldest member by `joined_at`)
      - **Purpose**: Find new HOST
    - **Update Table**: `user_task_lists` (new host record)
      - **Operation**: UPDATE
      - **Fields affected**: `role` (set to 'HOST')
    - **Update Table**: `task_lists`
      - **Operation**: UPDATE  
      - **Fields affected**: `owner_id` (set to new host's user_id)
    - **Alternative**: If no other members exist, DELETE the entire task list
- **Path Parameters**: 
  - `userId` (long): User ID
  - `taskListId` (long): Task List ID
- **Response (200 OK)**:
```json
"Delete successfully"
```
or
```json
"Authority role HOST to new User"
```
or
```json
"Cannot find authority, so delete task list"
```
- **Exceptions**:
  - `404 NOT_FOUND` - USERTASKLIST_NOT_FOUND: User is not a member of this task list
  - `404 NOT_FOUND` - TASKLIST_NOT_FOUND (2002): Task list not found

### 3.9 Get Members in Task List
- **Endpoint**: `/task-list/get-member/taskList/{id}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Get all members of a task list
- **Database Storage**: 
  - **Tables**: `user_task_lists`, `users`
  - **Operation**: SELECT with JOIN
  - **Fields queried**: User info + role + joined_at for all task list members
  - **Join**: user_task_lists.user_id = users.id WHERE task_list_id = id
- **Path Parameters**: 
  - `id` (long): Task List ID
- **Response (200 OK)** (UserTaskListResponse):
```json
{
  "userByRoleAndJoinedAt": {
    "HOST": [
      [ { "id": 1, 
          "username": "john_doe", 
          "email": "john@example.com", 
          "fullName": "John Doe", 
          "avatarUrl": null 
        }, 
        "2025-08-04T10:30:00" 
      ]
    ],
    "MEMBER": [
      [ { "id": 2, "username": "jane_smith", "email": "jane@example.com", "fullName": "Jane Smith", "avatarUrl": null }, "2025-08-05T14:20:00" ]
    ]
  }
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASKLIST_NOT_FOUND (2002): Task list not found

## 3.10 Authority Member Role

- **Endpoint**: `/task-list/authority/user/{assignedUserId}/inTaskList/{taskListId}/byUser/{userId}/role`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Change role of a member in a task list (promote MEMBER to HOST or demote HOST to MEMBER)
- **Database Storage**: 
  - **Primary Table**: `user_task_lists`
    - **Operation**: UPDATE
    - **Fields affected**: `role` (set to new role value), `updated_at` (implicit)
    - **Condition**: WHERE `user_id` = assignedUserId AND `task_list_id` = taskListId
  - **Access control**: Only HOST can change member roles
  - **Business logic**: 
    - If promoting MEMBER to HOST, demote current HOST to MEMBER
    - If demoting HOST to MEMBER, promote oldest MEMBER to HOST
    - Cannot demote the only HOST without other members
  - **Secondary Table**: `task_lists` (if ownership transfer occurs)
    - **Operation**: UPDATE
    - **Fields affected**: `owner_id` (set to new HOST's user_id)
- **Path Parameters**: 
  - `taskListId` (long): Task List ID
  - `userId` (long): User ID (must be current HOST)
  - `assignedUserId` (long): Target user ID whose role will be changed
- **Query Parameters**:
  - `role` (UserTaskList.Role): New role to assign, enum: "HOST", "MEMBER"
- **Response (200 OK)**:
```json
"Authority Done!"
```
- **Exceptions**:
  - `404 NOT_FOUND` - USERTASKLIST_NOT_FOUND: Requesting user is not a member of this task list
  - `403 FORBIDDEN` - TASKLIST_ACCESS_DENIED (2003): Only HOST can change member roles
  - `404 NOT_FOUND` - USERTASKLIST_NOT_FOUND: Target user is not a member of this task list
  - `404 NOT_FOUND` - TASKLIST_NOT_FOUND (2002): Task list not found
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid role parameter

**Notes**:
- This operation may trigger ownership transfer if promoting a MEMBER to HOST
- The endpoint follows role-based access control where only HOST users can modify member roles
- If target user already has the requested role, the operation completes successfully without changes
- Role changes affect task assignment permissions within the task list
---

## 4. Task Reminder APIs

### 4.1 Get Reminder by ID
- **Endpoint**: `/reminder/user/{userId}/{reminderId}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Retrieve reminder information by ID
- **Database Storage**: 
  - **Tables**: `task_reminders`, `tasks`, `task_lists`, `user_task_lists`
  - **Operation**: SELECT with JOINs
  - **Fields queried**: All reminder fields + task info
  - **Access control**: User must have access to the related task list
- **Path Parameters**: 
  - `userId` (long): User ID
  - `reminderId` (long): Reminder ID
- **Response (200 OK)** (TaskReminderResponse):
```json
{
  "id": 1,
  "taskId": 5,
  "taskTitle": "Complete project documentation",
  "remindAt": "2025-08-09T14:00:00",
  "message": "Don't forget to complete the documentation!",
  "remindType": "EMAIL",
  "isSent": false,
  "sentAt": null,
  "createdBy": 1,
  "createdAt": "2025-08-04T10:30:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - Reminder not found
  - `403 FORBIDDEN` - Access denied to reminder

### 4.2 Get Reminders by Task ID
- **Endpoint**: `/reminder/user/{userId}/task/{taskId}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Retrieve all reminders for a specific task
- **Path Parameters**: 
  - `userId` (long): User ID
  - `taskId` (long): Task ID
- **Response (200 OK)** (List<TaskReminderResponse>):
```json
[
  {
    "id": 1,
    "taskId": 5,
    "taskTitle": "Complete project documentation",
    "remindAt": "2025-08-09T14:00:00",
    "message": "Don't forget to complete the documentation!",
    "remindType": "EMAIL",
    "isSent": false,
    "sentAt": null,
    "createdBy": 1,
    "createdAt": "2025-08-04T10:30:00"
  }
]
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASK_NOT_FOUND (3001): Task not found
  - `403 FORBIDDEN` - TASK_ACCESS_DENIED (3002): No access to task

### 4.3 Get Reminders by User ID
- **Endpoint**: `/reminder/user/{userId}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Retrieve all reminders for a user (paginated)
- **Path Parameters**: 
  - `userId` (long): User ID
- **Query Parameters**:
  - `record` (int): Number of records per page
  - `page` (int): Page number (0-based)
- **Response (200 OK)** (Page<TaskReminderResponse>):
```json
{
  "content": [
    {
      "id": 1,
      "taskId": 5,
      "taskTitle": "Complete project documentation",
      "remindAt": "2025-08-09T14:00:00",
      "message": "Don't forget to complete the documentation!",
      "remindType": "EMAIL",
      "isSent": false,
      "sentAt": null,
      "createdBy": 1,
      "createdAt": "2025-08-04T10:30:00"
    }
  ],
  "totalElements": 8,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found

### 4.4 Get Pending Reminders by User ID
- **Endpoint**: `/reminder/user/{userId}/pending`
- **HTTP Method**: `GET`
- **Function/Use Case**: Retrieve only pending (not sent) reminders for a user
- **Path Parameters**: 
  - `userId` (long): User ID
- **Query Parameters**:
  - `record` (int): Number of records per page
  - `page` (int): Page number (0-based)
- **Response (200 OK)** (Page<TaskReminderResponse>):
```json
{
  "content": [
    {
      "id": 1,
      "taskId": 5,
      "taskTitle": "Complete project documentation",
      "remindAt": "2025-08-09T14:00:00",
      "message": "Don't forget to complete the documentation!",
      "remindType": "EMAIL",
      "isSent": false,
      "sentAt": null,
      "createdBy": 1,
      "createdAt": "2025-08-04T10:30:00"
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found

### 4.5 Update Reminder
- **Endpoint**: `/reminder/user/{userId}/update/{reminderId}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Update reminder information
- **Path Parameters**: 
  - `userId` (long): User ID
  - `reminderId` (long): Reminder ID
- **Request Body** (TaskReminderDTO):
```json
{
  "remind_at": "2025-08-09T16:00:00",        // optional, ISO datetime format
  "message": "string",                       // optional
  "reminder_type": "string",                 // optional, enum: "EMAIL", "PUSH", "SMS"
  "minutes_before_due": 30                   // optional, minutes before due date
}
```
- **Response (200 OK)** (TaskReminderResponse):
```json
{
  "id": 1,
  "taskId": 5,
  "taskTitle": "Complete project documentation",
  "remindAt": "2025-08-09T16:00:00",
  "message": "Updated reminder message",
  "remindType": "PUSH",
  "isSent": false,
  "sentAt": null,
  "createdBy": 1,
  "createdAt": "2025-08-04T10:30:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - Reminder not found
  - `403 FORBIDDEN` - Access denied to reminder
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data

### 4.6 Delete Reminder
- **Endpoint**: `/reminder/delete/{reminderId}/user/{userId}`
- **HTTP Method**: `DELETE`
- **Function/Use Case**: Delete a reminder
- **Path Parameters**: 
  - `reminderId` (long): Reminder ID
  - `userId` (long): User ID
- **Response (200 OK)**:
```json
"Reminder deleted successfully"
```
- **Exceptions**:
  - `404 NOT_FOUND` - Reminder not found
  - `403 FORBIDDEN` - Access denied to reminder

### 4.7 Send Due Reminders (System)
- **Endpoint**: `/reminder/system/send-due-reminders`
- **HTTP Method**: `POST`
- **Function/Use Case**: System endpoint to send all due reminders (scheduled task)
- **Database Storage**: 
  - **Query Table**: `task_reminders`
    - **Operation**: SELECT
    - **Condition**: WHERE `remind_at` <= NOW() AND `is_sent` = false
    - **Purpose**: Find all unsent reminders that are due
  - **Update Table**: `task_reminders`
    - **Operation**: UPDATE (for each processed reminder)
    - **Fields affected**: `is_sent` (set to true), `sent_at` (set to current timestamp)
    - **Condition**: WHERE `id` IN (processed reminder IDs)
  - **External Action**: Send actual notifications (email/push/SMS) based on `reminder_type`
- **Response (200 OK)**:
```json
"Sent {count} reminders"
```
- **Exceptions**:
  - `500 INTERNAL_SERVER_ERROR` - System error during reminder processing

---

## 5. Task Recurrence APIs

### 5.1 Create Recurrence
- **Endpoint**: `/task_recurrence/task/{taskId}`
- **HTTP Method**: `POST`
- **Function/Use Case**: Create a recurring pattern for a task
- **Database Storage**: 
  - **Table**: `task_recurrences`
  - **Operation**: INSERT
  - **Fields affected**: `task_id`, `recurrence_type`, `recurrence_interval` (default: 1), `recurrence_end_date`, `next_due_date` (calculated), `is_active`, `created_at`, `updated_at`
  - **Auto-generated**: `id`
  - **Notes**: If `recurrence_interval` < 1, mark as inactive. `next_due_date` is computed from task's `due_date` if present; otherwise from current time.
- **Path Parameters**: 
  - `taskId` (long): Task ID
- **Request Body** (TaskRecurrenceDTO):
```json
{
  "recurrence_type": "string",      // required, enum: "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
  "recurrence_interval": 1,         // optional, default: 1 (repeat every N periods)
  "recurrence_end_date": "2025-12-31"  // optional, YYYY-MM-DD
}
```
- **Response (200 OK)** (TaskRecurrenceResponse):
```json
{
  "id": 1,
  "taskId": 5,
  "recurrenceType": "WEEKLY",
  "recurrenceInterval": 1,
  "recurrenceEndDate": "2025-12-31",
  "nextDueDate": "2025-08-11T14:30:00",
  "isActive": true,
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T10:30:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASK_NOT_FOUND (3001): Task not found
  - `400 BAD_REQUEST` - RECURRENCE_ALREADY_EXIST (4000): Task recurrence already exists
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data

### 5.2 Get Recurrence by Task ID
- **Endpoint**: `/task_recurrence/task/{taskId}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Retrieve recurrence pattern for a task
- **Path Parameters**: 
  - `taskId` (long): Task ID
- **Response (200 OK)** (TaskRecurrenceResponse):
```json
{
  "id": 1,
  "taskId": 5,
  "recurrenceType": "WEEKLY",
  "recurrenceInterval": 1,
  "recurrenceEndDate": "2025-12-31",
  "nextDueDate": "2025-08-11T14:30:00",
  "isActive": true,
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T10:30:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - RECURRENCE_NOT_FOUND (4002): Recurrence not found

### 5.3 Update Recurrence
- **Endpoint**: `/task_recurrence/update/{recurrenceId}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Update recurrence pattern
 - **Database Storage**:
   - **Table**: `task_recurrences`
   - **Operation**: UPDATE by id (custom repository update)
   - **Fields affected**: `recurrence_type`, `recurrence_interval`, `recurrence_end_date`, `is_active`, `updated_at`
- **Path Parameters**: 
  - `recurrenceId` (long): Recurrence ID
- **Request Body** (TaskRecurrenceDTO - partial update):
```json
{
  "recurrence_type": "string",      // optional, enum: "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
  "recurrence_interval": 2,         // optional
  "recurrence_end_date": "2026-06-30",  // optional, date format
  "is_active": false                // optional
}
```
- **Response (200 OK)** (TaskRecurrenceResponse):
```json
{
  "id": 1,
  "taskId": 5,
  "recurrenceType": "MONTHLY",
  "recurrenceInterval": 2,
  "recurrenceEndDate": "2026-06-30",
  "nextDueDate": "2025-10-04T14:30:00",
  "isActive": false,
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T16:15:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - RECURRENCE_NOT_FOUND (4002): Recurrence not found
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data

### 5.4 Delete Recurrence
- **Endpoint**: `/task_recurrence/delete/{recurrenceId}`
- **HTTP Method**: `DELETE`
- **Function/Use Case**: Delete a recurrence pattern
- **Path Parameters**: 
  - `recurrenceId` (long): Recurrence ID
- **Response (200 OK)**:
```json
"delete complete"
```
- **Exceptions**:
  - `404 NOT_FOUND` - RECURRENCE_NOT_FOUND (4002): Recurrence not found

---

## 6. Common Error Codes Reference

### Database Storage Notes for Frontend Developers:

**Understanding Database Operations:**
- **INSERT**: Creates new records (POST operations)
- **SELECT**: Retrieves data (GET operations) 
- **UPDATE**: Modifies existing records (PUT operations)
- **DELETE**: Removes records (DELETE operations)

**Multi-Table Operations:**
- **PRIMARY TABLE**: The main table being affected by the operation
- **SECONDARY TABLE**: Additional tables that need INSERT/UPDATE in the same transaction
- **QUERY TABLE**: Tables used for validation or data lookup
- **RELATED TABLES AFFECTED**: Tables modified via CASCADE or business logic

**Key Patterns:**
1. **Access Control**: Most operations verify user has access to resources through `user_task_lists` table
2. **Timestamps**: `created_at` and `updated_at` are automatically managed
3. **Cascading Operations**: 
   - Some deletes trigger cascading deletes across related tables
   - Some operations require complex business logic (like ownership transfer)
4. **File Storage**: File uploads save to filesystem, store metadata in `attachments` table
5. **Relationships**: Data is connected through foreign keys (task_list_id, user_id, etc.)
6. **Transactional Operations**: Multi-table operations are wrapped in database transactions

**Complex Operations Examples:**
- **Create Task List**: INSERT into `task_lists` + INSERT into `user_task_lists` (owner as HOST)
- **Join Task List**: SELECT from `task_lists` (validate) + INSERT into `user_task_lists`
- **Leave Task List**: DELETE from `user_task_lists` + possible HOST role transfer + possible task list deletion
- **Delete User**: CASCADE deletes/updates across 7+ tables with ownership transfers

**Performance Considerations:**
- GET operations use JOINs to reduce multiple queries
- Pagination is used for large result sets
- Indexes exist on frequently queried fields (user_id, task_list_id)
- Complex multi-table operations use database transactions for consistency

### User Related Errors (1000-1999)
- `1000` - USER_ERROR: General user error
- `1001` - USER_EXISTED: User already exists  
- `1002` - USER_NOT_FOUND: User not found
- `1003` - USER_PASSWORD_INCORRECT: Password is incorrect
- `1013` - USER_PASSWORD_FOUND: Password already used

### Task List Related Errors (2000-2999)
- `2000` - TASKLIST_EXCEPTION: General task list error
- `2001` - TASKLIST_ALREADY_EXIST: Task list already exists
- `2002` - TASKLIST_NOT_FOUND: Task list not found
- `2003` - TASKLIST_ACCESS_DENIED: Access denied to task list
- `2004` - TASKLIST_INVALID_SHARECODE: Invalid share code
- `2005` - TASKLIST_NOT_SHARED: Task list is not shared
- `2006` - TASKLIST_CANNOT_JOIN: Cannot join task list

### Task Related Errors (3000-3999)
- `3001` - TASK_NOT_FOUND: Task not found
- `3002` - TASK_ACCESS_DENIED: Access denied to task
- `3003` - TASK_ALREADY_COMPLETED: Task is already completed
- `3004` - TASK_INVALID_DUE_DATE: Due date cannot be in the past
- `3005` - TASK_ASSIGNMENT_FAILED: Cannot assign task to non-member
- `3007` - TASK_IS_NOT_COMPLETED: Task is not completed

### Recurrence Related Errors (4000-4999)
- `4000` - RECURRENCE_ALREADY_EXIST: Task recurrence already exists
- `4001` - RECURRENCE_CANNOT_BE_BEFORE_NOW: Recurrence cannot be before now
- `4002` - RECURRENCE_NOT_FOUND: Recurrence not found

### Database Related Errors (10000-10999)
- `10000` - DATABASE_EXCEPTION: General database error
- `10001` - SQL_EXCEPTION: Invalid SQL operator
- `10002` - NULL_POINTER_EXCEPTION: Null pointer error

### File Related Errors (11000-11999)
- `11000` - FILE_EXCEPTION: General file error
- `11001` - FILE_PROCESS: Error in processing file
- `11002` - FILE_NOT_EXIST: File upload does not exist
- `11003` - FILE_TOO_LARGE: File must be < 10MB
- `11004` - FILE_TYPE_NOT_SUPPORTED: File type not supported
- `11005` - FILE_UPLOAD_FAILED: Upload failed

### General System Errors (12000-12999)
- `12000` - INTERNAL_SERVER_ERROR: Internal server error
- `12001` - VALIDATE_ERROR: Method argument not validated
- `12001` - HTTP_MESSAGE_NOT_READABLE: Cannot read HTTP message

---

## Notes

1. **Authentication**: All endpoints require proper user authentication (implementation details not shown in this documentation)

2. **Pagination**: All paginated endpoints use 0-based page numbering

3. **File Upload**: 
   - Maximum file size: 10MB
   - Supported file types: (defined in application configuration)
   - Files are stored with UUID-based naming to prevent conflicts

4. **Date/Time Format**: All datetime fields use ISO 8601 format (e.g., "2025-08-04T14:30:00") with timezone information

5. **Task Priorities**: LOW, MEDIUM, HIGH, URGENT

6. **Task Status**: Determined by `isCompleted` boolean field (true = completed, false = pending)

7. **Recurrence Types**: DAILY, WEEKLY, MONTHLY, YEARLY

8. **Reminder Types**: EMAIL, PUSH, SMS

9. **Timezone Handling**: All times are stored and returned in the system's configured timezone

10. **Validation**: All request bodies are validated according to the DTO validation rules defined in the application

11. **JSON Field Names**: Request bodies use snake_case for field names (e.g., "due_date", "task_list_id") as defined in the DTOs, while responses use camelCase

12. **Required vs Optional Fields**: 
    - Required fields are marked with validation annotations and will return validation errors if missing
    - Optional fields can be omitted from request bodies
    - Partial updates are supported where only provided fields will be updated
