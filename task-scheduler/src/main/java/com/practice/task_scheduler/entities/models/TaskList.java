package com.practice.task_scheduler.entities.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "task_lists")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"ownerUser", "userTaskLists", "tasks"})
@EqualsAndHashCode(exclude = {"ownerUser", "userTaskLists", "tasks", "createdAt", "updatedAt"})
public class TaskList{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String name;

    @Lob
    private String description;

    @Column(length = 7)
    private String color = "3b82f6";

    @Column(name = "is_shared")
    private Boolean isShared = false;

    @Column(name = "share_code", length = 20, unique = true)
    private String shareCode;

    @Column(name = "owner_id")
    private Long ownerId;

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

    // Relationship
    @JoinColumn(name = "owner_id", nullable = false, insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User ownerUser;

    @OneToMany(mappedBy = "taskList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserTaskList> userTaskLists;

    @OneToMany(mappedBy = "taskList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> tasks;
}
