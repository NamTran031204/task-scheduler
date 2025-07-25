package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.TaskReminder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskReminderRepository extends JpaRepository<TaskReminder, Long> {

    @Query("SELECT tr FROM TaskReminder tr WHERE tr.taskId = :taskId ORDER BY tr.remindAt ASC")
    List<TaskReminder> findByTaskIdOrderByRemindAtAsc(@Param("taskId") Long taskId);

    @Query("SELECT tr FROM TaskReminder tr WHERE tr.createdBy = :userId ORDER BY tr.remindAt ASC")
    Page<TaskReminder> findByCreatedByOrderByRemindAtAsc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT tr FROM TaskReminder tr WHERE tr.createdBy = :userId AND tr.isSent = false ORDER BY tr.remindAt ASC")
    Page<TaskReminder> findPendingByCreatedByOrderByRemindAtAsc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT tr FROM TaskReminder tr WHERE tr.remindAt <= :currentTime AND tr.isSent = false ORDER BY tr.remindAt ASC")
    List<TaskReminder> findDueReminders(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT tr FROM TaskReminder tr WHERE tr.taskId = :taskId AND tr.createdBy = :userId")
    Optional<TaskReminder> findByTaskIdAndCreatedBy(@Param("taskId") Long taskId, @Param("userId") Long userId);
}