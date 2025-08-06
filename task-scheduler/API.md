# Task Scheduler API Documentation

## Base URL
```
http://localhost:8080/api/v1
```

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
- **Function/Use Case**: Register a new user account
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
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data
  - `400 BAD_REQUEST` - HTTP_MESSAGE_NOT_READABLE (12001): Malformed request body

### 1.2 User Login
- **Endpoint**: `/user/login`
- **HTTP Method**: `POST`
- **Function/Use Case**: Authenticate user login
- **Request Body** (UserLoginDTO):
```json
{
  "email": "string",          // required, cannot be blank
  "password": "string"        // required, cannot be blank
}
```
- **Response (200 OK)** (UserResponse + token):
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found
  - `401 UNAUTHORIZED` - USER_PASSWORD_INCORRECT (1003): Password is incorrect
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data

### 1.3 Update User Avatar
- **Endpoint**: `/user/update_avatar/{id}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Update user profile avatar
- **Path Parameters**: 
  - `id` (long): User ID
- **Request Body**: `MultipartFile` (form-data with key "avatar")
- **Response (200 OK)** (UserResponse):
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T11:15:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found
  - `400 BAD_REQUEST` - FILE_TYPE_NOT_SUPPORTED (11004): File type not supported
  - `413 PROCESSING` - FILE_TOO_LARGE (11003): File size exceeds 10MB limit
  - `422 PROCESSING` - FILE_UPLOAD_FAILED (11005): Upload failed

### 1.4 Get User by ID
- **Endpoint**: `/user/{id}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Retrieve user information by ID
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
- **Function/Use Case**: Retrieve all users with pagination
- **Query Parameters**:
  - `record` (int): Number of records per page
  - `page` (int): Page number (0-based)
- **Response (200 OK)** (Page<UserResponse>):
```json
{
  "content": [
    {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "createdAt": "2025-08-04T10:30:00",
      "updatedAt": "2025-08-04T10:30:00"
    }
  ],
  "totalElements": 50,
  "totalPages": 5,
  "size": 10,
  "number": 0
}
```
- **Exceptions**:
  - `500 INTERNAL_SERVER_ERROR` - DATABASE_EXCEPTION (10000): Database error

### 1.6 Update User
- **Endpoint**: `/user/update/{id}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Update user profile information
- **Path Parameters**: 
  - `id` (long): User ID
- **Request Body** (UserDTO - partial update):
```json
{
  "username": "string",        // optional
  "email": "string",          // optional, must be valid email format if provided
  "fullname": "string"        // optional
}
```
- **Response (200 OK)** (UserResponse):
```json
{
  "id": 1,
  "username": "john_doe_updated",
  "email": "john.updated@example.com",
  "fullName": "John Doe Updated",
  "createdAt": "2025-08-04T10:30:00",
  "updatedAt": "2025-08-04T12:15:00"
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data

### 1.7 Delete User
- **Endpoint**: `/user/delete/{id}`
- **HTTP Method**: `DELETE`
- **Function/Use Case**: Delete user account
- **Path Parameters**: 
  - `id` (long): User ID
- **Response (200 OK)**:
```json
"User deleted successfully"
```
- **Exceptions**:
  - `404 NOT_FOUND` - USER_NOT_FOUND (1002): User not found
  - `400 BAD_REQUEST` - DATABASE_EXCEPTION (10000): Cannot delete due to foreign key constraints

---

## 2. Task Management APIs

### 2.1 Create Task
- **Endpoint**: `/task/user/{userId}/create`
- **HTTP Method**: `POST`
- **Function/Use Case**: Create a new task (without file attachments)
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
- **Function/Use Case**: Upload file attachments to an existing task
- **Path Parameters**: 
  - `userId` (long): User ID
  - `taskId` (long): Task ID
- **Request Body**: `List<MultipartFile>` (form-data)
- **Response (200 OK)**:
```json
{
  "taskId": "long",
  "uploadedFiles": [
    {
      "fileName": "string",
      "fileUrl": "string",
      "fileSize": "long",
      "uploadedAt": "datetime"
    }
  ]
}
```
- **Exceptions**:
  - `404 NOT_FOUND` - TASK_NOT_FOUND (3001): Task not found
  - `403 FORBIDDEN` - TASK_ACCESS_DENIED (3002): No access to task
  - `400 BAD_REQUEST` - FILE_TYPE_NOT_SUPPORTED (11004): Unsupported file type
  - `413 PROCESSING` - FILE_TOO_LARGE (11003): File size exceeds limit
  - `422 PROCESSING` - FILE_UPLOAD_FAILED (11005): Upload failed

### 2.3 Get Task by ID
- **Endpoint**: `/task/user/{userId}/{taskId}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Retrieve detailed task information
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
  - `403 FORBIDDEN` - TASK_ACCESS_DENIED (3002): No access to task

### 2.4 Get Tasks by Task List ID
- **Endpoint**: `/task/user/{userId}/task-list/{taskListId}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Retrieve all tasks within a specific task list
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
- **Function/Use Case**: Retrieve all tasks assigned to or created by a user
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

### 2.6 Update Task
- **Endpoint**: `/task/user/{userId}/update/{id}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Update task information
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
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data

### 2.7 Delete Task
- **Endpoint**: `/task/user/{userId}/delete/{id}`
- **HTTP Method**: `DELETE`
- **Function/Use Case**: Delete a task
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
  - `403 FORBIDDEN` - TASK_ACCESS_DENIED (3002): No access to task
  - `400 BAD_REQUEST` - TASK_ASSIGNMENT_FAILED (3005): Cannot assign to non-member

### 2.10 Undo Task Completion
- **Endpoint**: `/task/user/{userId}/undo_complete/{taskId}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Revert a completed task back to pending status
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
- **Function/Use Case**: Retrieve all task lists owned by or shared with a user
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
  - `400 BAD_REQUEST` - VALIDATE_ERROR (12001): Invalid input data

### 3.5 Delete Task List
- **Endpoint**: `/task-list/delete/{id}/user/{userId}`
- **HTTP Method**: `DELETE`
- **Function/Use Case**: Delete a task list and all its tasks
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

### 3.6 Share Task List
- **Endpoint**: `/task-list/share/{id}/user/{userId}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Generate a share code for the task list
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

---

## 4. Task Reminder APIs

### 4.1 Get Reminder by ID
- **Endpoint**: `/reminder/user/{userId}/{reminderId}`
- **HTTP Method**: `GET`
- **Function/Use Case**: Retrieve reminder information by ID
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
- **Path Parameters**: 
  - `taskId` (long): Task ID
- **Request Body** (TaskRecurrenceDTO):
```json
{
  "recurrence_type": "string",      // required, enum: "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
  "recurrence_interval": 1,         // optional, default: 1 (repeat every N periods)
  "recurrence_end_date": "2025-12-31",  // optional, date format (when recurrence should stop)
  "is_active": true                 // optional, default: true
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
  - `400 BAD_REQUEST` - RECURRENCE_CANNOT_BE_BEFORE_NOW (4001): Recurrence cannot be before now
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
  - `404 NOT_FOUND` - TASK_NOT_FOUND (3001): Task not found
  - `404 NOT_FOUND` - RECURRENCE_NOT_FOUND (4002): Recurrence not found

### 5.3 Update Recurrence
- **Endpoint**: `/task_recurrence/update/{recurrenceId}`
- **HTTP Method**: `PUT`
- **Function/Use Case**: Update recurrence pattern
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
  - `400 BAD_REQUEST` - RECURRENCE_CANNOT_BE_BEFORE_NOW (4001): Recurrence cannot be before now
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
