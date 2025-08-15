package com.practice.task_scheduler.entities.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_task_assignments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"task", "user", "assignedByUser"})
@EqualsAndHashCode(exclude = {"task", "user", "assignedByUser", "assignedAt", "changedStatus"})
public class UserTaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "assigned_by", nullable = false)
    private Long assignedBy;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.IN_PROGRESS;

    @Column(name = "changed_status")
    private LocalDateTime changedStatus;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == Status.COMPLETED && changedStatus == null) {
            changedStatus = LocalDateTime.now();
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    @JsonIgnore
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", insertable = false, updatable = false)
    @JsonIgnore
    private User assignedByUser;

    public enum Status {
        IN_PROGRESS, COMPLETED
    }
}