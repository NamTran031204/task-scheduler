### Tóm tắt dự án

Đây là một hệ thống backend cho ứng dụng Lập lịch công việc (Task Scheduler), được xây dựng từ đầu. Hệ thống được thiết kế như một API server độc lập, quản lý người dùng, công việc, danh sách công việc, nhắc nhở và các file đính kèm. Toàn bộ dự án được đóng gói bằng Docker để dễ dàng triển khai và mở rộng, giúp đội ngũ frontend có thể tích hợp một cách đơn giản.

### Core Technologies and Skills

-   **Backend Framework**: Spring Boot (phiên bản 3.4.7)
-   **Ngôn ngữ**: Java 21
-   **Database**: MySQL (phiên bản 8.0) với schema được thiết kế riêng.
-   **Data Access**: Spring Data JPA / Hibernate cho việc ánh xạ đối tượng-quan hệ (ORM).
-   **API**: Thiết kế và triển khai RESTful API.
-   **Build Tool**: Maven
-   **Containerization**: Docker & Docker Compose

### Key Features & Implementation

-   **API Development**: Thiết kế và xây dựng API sử dụng `RESTful` cho tất cả các tính năng của ứng dụng, bao gồm các hoạt động CRUD cho công việc, danh sách và người dùng.
-   **Database Design**: Tự thiết kế database với các schemas. Schema bao gồm các bảng cho người dùng, công việc, công việc lặp lại, nhắc nhở, file đính kèm, và lịch sử thay đổi (history), sử dụng các indexs để tối ưu hiệu suất.
-   **Scheduled Tasks**: Triển khai các tác vụ chạy nền bằng cơ chế scheduling của Spring để xử lý các nhắc nhở và thông báo tự động.
-   **Exception Handling**: Xây dựng một hệ thống xử lý ngoại lệ (exception handling) toàn diện. Tất cả các lỗi nghiệp vụ (business logic errors) và lỗi hệ thống đều được bắt và trả về dưới dạng một cấu trúc JSON nhất quán, đi kèm với các mã lỗi (error codes) và thông điệp rõ ràng, giúp phía frontend dễ dàng xử lý và hiển thị thông báo cho người dùng.
-   **File Handling**: Phát triển chức năng tải lên và phục vụ các file đính kèm cho công việc, được quản lý thông qua một resource handler riêng.
-   **Custom Algorithm**: Tự triển khai một thuật toán riêng cùng với áp dụng `FNV-1a hash` để tạo share code độc nhất cho các task lists.

### DevOps & Deployment

-   **Dockerization**: Viết multi-stage `Dockerfile` để tạo ra các image production được tối ưu và gọn nhẹ.
-   **Orchestration**: Sử dụng `Docker Compose` để định nghĩa và chạy ứng dụng (gồm ứng dụng Spring Boot và database MySQL), quản lý network, volume, và các biến môi trường thông qua `docker-compose.yml`.
-   **Developer Documentation**: Soạn thảo documents hướng dẫn rõ ràng trong `docker-run.md` để giúp đội frontend có thể cài đặt và chạy toàn bộ môi trường backend chỉ bằng một câu lệnh.