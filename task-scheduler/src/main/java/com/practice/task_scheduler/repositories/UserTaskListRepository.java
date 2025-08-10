package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.UserTaskList;
import com.practice.task_scheduler.entities.projections.UserTaskListProjection;
import org.hibernate.annotations.QueryCacheLayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTaskListRepository extends JpaRepository<UserTaskList, Long> {
    boolean existsByUserIdAndTaskListId(long userId, long taskListId);
    void deleteByUserIdAndTaskListId(long userId, long taskListId);
    Optional<UserTaskList> findByUserIdAndTaskListId(long userId, long taskListId);

    @Query(value = "SELECT * FROM user_task_lists ut WHERE task_list_id = :taskListId ORDER BY joined_at ASC LIMIT 1", nativeQuery = true)
    Optional<UserTaskList> findByTaskListIdIdOrderByJoinedAt(@Param("taskListId") long taskListId);

    @Query(value = """
            SELECT u.id,
                u.username,
                u.email,
                u.full_name,
                u.avatar_url,
                utl.role,
                u.is_active,
                utl.joined_at
            FROM user_task_lists utl
            JOIN users u ON u.id = utl.user_id
            WHERE utl.task_list_id = :taskListId AND u.is_active = 1
            """ , nativeQuery = true)
    List<UserTaskListProjection> findMemberByTaskListId(@Param("taskListId") long taskListId);
}