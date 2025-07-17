package com.practice.task_scheduler.entities.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tasks")
@Entity
@ToString(exclude = {"taskList", "createdByUser", "assignedToUser", "taskRecurrences", "taskReminders", "attachments", "taskHistories", "notifications"})
@EqualsAndHashCode(exclude = {"taskList", "createdByUser", "assignedToUser", "taskRecurrences", "taskReminders", "attachments", "taskHistories", "notifications", "createdAt", "updatedAt", "completedAt"})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 300, nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "task_list_id", nullable = false)
    private Long taskListId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "assigned_to")
    private Long assignedTo;

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


    @JoinColumn(name = "task_list_id", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private TaskList taskList;

    @JoinColumn(name = "created_by", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User createdByUser;

    @JoinColumn(name = "assigned_to", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User assignedToUser;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TaskRecurrence> taskRecurrences;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TaskReminder> taskReminders;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TaskHistory> taskHistories;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Notification> notifications;

    public enum Priority{
        LOW, MEDIUM, HIGH, URGENT
    }

}


