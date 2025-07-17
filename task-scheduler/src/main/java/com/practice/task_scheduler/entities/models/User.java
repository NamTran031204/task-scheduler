package com.practice.task_scheduler.entities.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
@Entity
@ToString(exclude = {"taskLists", "userTaskLists", "createdTasks", "assignedTasks", "taskReminders", "attachments", "taskHistories", "notifications"})
@EqualsAndHashCode(exclude = {"taskLists", "userTaskLists", "createdTasks", "assignedTasks", "taskReminders", "attachments", "taskHistories", "notifications", "createdAt", "updatedAt"})
public class User{

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 50, unique = true, nullable = false)
    private String username;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_expires")
    private LocalDateTime resetPasswordExpires;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    @OneToMany(mappedBy = "ownerUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskList> taskLists;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserTaskList> userTaskLists;

    //Tasks
    @OneToMany(mappedBy = "createdByUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> createdTasks;

    @OneToMany(mappedBy = "assignedToUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> assignedTasks;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskReminder> taskReminders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskHistory> taskHistories;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications;
}
