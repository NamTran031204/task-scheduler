package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.UserTaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTaskListRepository extends JpaRepository<UserTaskList, Long> {
    boolean existsByUserIdAndTaskListId(long userId, long taskListId);
    void deleteByUserIdAndTaskListId(long userId, long taskListId);
    Optional<UserTaskList> findByUserIdAndTaskListId(long userId, long taskListId);

    @Query(value = "SELECT * FROM user_task_lists ut WHERE task_list_id = :taskListId ORDER BY joined_at ASC LIMIT 1", nativeQuery = true)
    Optional<UserTaskList> findByTaskListIdIdOrderByJoinedAt(@Param("taskListId") long taskListId);
}