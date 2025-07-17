package com.practice.task_scheduler.repositories;

import com.practice.task_scheduler.entities.models.User;
import com.practice.task_scheduler.entities.responses.UserResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByPassword(String password);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET avatar_url = :url WHERE id = :id", nativeQuery = true)
    void updateImageUrl(@Param("id") Long id, @Param("url") String url);

    Page<User> findAll(Pageable pageable);

//    void deleteById(long id);
}
