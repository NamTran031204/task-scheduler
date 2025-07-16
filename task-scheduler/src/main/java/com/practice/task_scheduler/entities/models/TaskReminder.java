package com.practice.task_scheduler.entities.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "remind_at", nullable = false)
    private LocalDateTime remindAt;

    private String message;

    @Column(name = "reminder_type")
    private RemindType remindType = RemindType.EMAIL;

    @Column(name = "is_sent")
    private Boolean isSent = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;

    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;


    public enum RemindType{
        EMAIL, PUSH, SMS
    }
}
