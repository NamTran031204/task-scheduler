package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.UserTaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTaskListRepository extends JpaRepository<UserTaskList, Long> {
    boolean existsByUserIdAndTaskListId(long userId, long taskListId);
    void deleteByUserIdAndTaskListId(long userId, long taskListId);
}