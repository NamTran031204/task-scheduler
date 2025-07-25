package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.Task;
import com.practice.task_scheduler.entities.responses.TaskResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.taskListId = :taskListId ORDER BY t.createdAt DESC")
    Page<Task> findByTaskListIdOrderByCreatedAtDesc(@Param("taskListId") Long taskListId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.createdBy = :userId ORDER BY t.createdAt DESC")
    Page<Task> findByCreatedByOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :userId ORDER BY t.createdAt DESC")
    Page<Task> findByAssignedToOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentTime AND t.isCompleted = false")
    List<Task> findOverdueTasks(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT t FROM Task t WHERE t.taskListId = :taskListId AND t.isCompleted = true ORDER BY t.completedAt DESC")
    Page<Task> findCompletedTasksByTaskListId(@Param("taskListId") Long taskListId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.taskListId = :taskListId AND t.isCompleted = false ORDER BY t.dueDate ASC")
    Page<Task> findPendingTasksByTaskListId(@Param("taskListId") Long taskListId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.dueDate = :dueDate WHERE t.id = :id")
    void updateNextDueDateById(@Param("id") long id, @Param("dueDate") LocalDateTime dueDate);
}