package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {

    @Query(value = "SELECT t.new_value FROM task_history t WHERE t.user_id = :userId AND t.task_id = :taskId ORDER BY t.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<String> findByTaskIdAndUserId (@Param("taskId") long taskId, @Param("userId") long userId);

    @Query("SELECT th FROM TaskHistory th WHERE th.taskId = :taskId ORDER BY th.createdAt DESC")
    List<TaskHistory> findByTaskIdOrderByCreatedAtDesc(@Param("taskId") Long taskId);

    @Query("SELECT th FROM TaskHistory th WHERE th.taskId = :taskId ORDER BY th.createdAt DESC")
    Optional<TaskHistory> findLatestByTaskId(@Param("taskId") Long taskId);
}
