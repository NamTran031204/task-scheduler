package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.Task;
import com.practice.task_scheduler.entities.responses.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Get tasks by task list
    @Query("SELECT t FROM Task t WHERE t.taskListId = :taskListId ORDER BY t.createdAt DESC")
    Page<Task> findByTaskListIdOrderByCreatedAtDesc(@Param("taskListId") Long taskListId, Pageable pageable);

    // Get tasks by created by user
    @Query("SELECT t FROM Task t WHERE t.createdBy = :userId ORDER BY t.createdAt DESC")
    Page<Task> findByCreatedByOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // Get tasks assigned to user
    @Query("SELECT t FROM Task t WHERE t.assignedTo = :userId ORDER BY t.createdAt DESC")
    Page<Task> findByAssignedToOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // Get overdue tasks
    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentTime AND t.isCompleted = false")
    List<Task> findOverdueTasks(@Param("currentTime") LocalDateTime currentTime);

    // Get completed tasks by task list
    @Query("SELECT t FROM Task t WHERE t.taskListId = :taskListId AND t.isCompleted = true ORDER BY t.completedAt DESC")
    Page<Task> findCompletedTasksByTaskListId(@Param("taskListId") Long taskListId, Pageable pageable);

    // Get pending tasks by task list
    @Query("SELECT t FROM Task t WHERE t.taskListId = :taskListId AND t.isCompleted = false ORDER BY t.dueDate ASC")
    Page<Task> findPendingTasksByTaskListId(@Param("taskListId") Long taskListId, Pageable pageable);

    // Count tasks by task list
    long countByTaskListId(Long taskListId);

    // Count completed tasks by task list
    long countByTaskListIdAndIsCompleted(Long taskListId, Boolean isCompleted);
}