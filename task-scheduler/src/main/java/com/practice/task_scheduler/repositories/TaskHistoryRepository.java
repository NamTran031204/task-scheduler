package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {

    @Query(value = "SELECT t.new_value FROM task_history t WHERE t.user_id = :userId AND t.task_id = :taskId ORDER BY t.created_at DESC LIMIT 1", nativeQuery = true)
    String findByTaskIdAndUserId (@Param("taskId") long taskId,@Param("userId") long userId);
}
