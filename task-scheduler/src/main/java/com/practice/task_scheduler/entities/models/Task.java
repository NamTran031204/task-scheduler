package com.practice.task_scheduler.entities.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@ToString(exclude = {"taskList", "taskRecurrences", "taskReminders", "attachments", "taskHistories", "notifications", "userTaskAssignments"})
@EqualsAndHashCode(exclude = {"taskList", "taskRecurrences", "taskReminders", "attachments", "taskHistories", "notifications", "userTaskAssignments", "createdAt", "updatedAt"})
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

    @Column(name = "task_list_id", nullable = false)
    private Long taskListId;

    @Column(name = "created_by")
    private Long createdBy;

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
    @JsonIgnore
    private TaskList taskList;

    @JoinColumn(name = "created_by", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User createdByUser;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TaskRecurrence> taskRecurrences;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TaskReminder> taskReminders;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TaskHistory> taskHistories;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Notification> notifications;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<UserTaskAssignment> userTaskAssignments;

    public enum Priority{
        LOW, MEDIUM, HIGH, URGENT
    }

}


