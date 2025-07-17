package com.practice.task_scheduler.entities.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_task_lists")
@Entity
@ToString(exclude = {"owner", "taskList"})
@EqualsAndHashCode(exclude = {"owner", "taskList", "joinedAt"})
public class UserTaskList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "task_list_id")
    private Long taskListId;

    @Enumerated(EnumType.STRING)
    private Role role = Role.MEMBER; // must check

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;


    @JoinColumn(name = "user_id", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    @JoinColumn(name = "task_list_id", updatable = false, insertable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private TaskList taskList;

    public enum Role {
        HOST,MEMBER
    }
}
