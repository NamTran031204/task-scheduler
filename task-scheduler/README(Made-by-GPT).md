# Task Scheduler - Deployment Guide

## 🚀 Quick Start với Docker Compose

### Prerequisites
- Docker và Docker Compose đã được cài đặt
- Port 8088 và 3306 chưa được sử dụng

### Bước 1: Clone repository này
```bash
git clone <repository-url>
cd task-scheduler
```

### Bước 2: Chạy ứng dụng
```bash
# Chạy tất cả services
docker-compose up -d

# Xem logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Bước 3: Truy cập ứng dụng
- **API Base URL:** http://localhost:8088
- **API Documentation:** http://localhost:8088/api/v1
- **Database:** localhost:3306 (user: root, password: root)

## 🔧 Manual Docker Commands (Alternative)

### Nếu không muốn dùng Docker Compose:

```bash
# 1. Tạo network
docker network create taskscheduler-network

# 2. Chạy MySQL
docker run --network taskscheduler-network --name mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=task_scheduler \
  -v $(pwd)/new-task-scheduler.sql:/docker-entrypoint-initdb.d/init.sql \
  -d mysql:8.0.43-debian

# 3. Đợi MySQL ready (30-60 giây)
docker logs -f mysql

# 4. Chạy ứng dụng
docker run --network taskscheduler-network --name task-scheduler \
  -p 8088:8088 \
  -e "DBMS_CONNECTION=jdbc:mysql://mysql:3306/task_scheduler?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh" \
  -e DBMS_USERNAME=root \
  -e DBMS_PASSWORD=root \
  namchan/task-scheduler:0.0.1
```

## 📋 API Endpoints

### Authentication
- `POST /api/v1/auth/login` - Đăng nhập
- `POST /api/v1/auth/register` - Đăng ký

### Tasks
- `GET /api/v1/task/user/{userId}` - Lấy danh sách task
- `POST /api/v1/task/user/{userId}/create` - Tạo task mới
- `PUT /api/v1/task/user/{userId}/{taskId}` - Cập nhật task
- `DELETE /api/v1/task/user/{userId}/{taskId}` - Xóa task

### File Upload
- `PUT /api/v1/task/user/{userId}/create/task/{taskId}` - Upload file attachment

## 🐛 Troubleshooting

### Lỗi kết nối database:
```bash
# Kiểm tra MySQL container
docker logs mysql

# Kiểm tra network connectivity
docker exec -it task-scheduler ping mysql
```

### Lỗi port đã được sử dụng:
```bash
# Kiểm tra process sử dụng port
netstat -tulpn | grep :8088
netstat -tulpn | grep :3306

# Dừng containers
docker-compose down
```