package com.user_service.repository;

import com.user_service.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.name LIKE %:name% ORDER BY u.id")
    List<UserEntity> findByNameContainingIgnoreCase(@Param("name") String name);

    boolean existsByEmail(String email);
}
