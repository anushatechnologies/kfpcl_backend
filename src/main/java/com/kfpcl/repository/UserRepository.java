package com.kfpcl.repository;

import com.kfpcl.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, String id);

    Page<User> findByRole(User.Role role, Pageable pageable);

    Page<User> findByStatus(User.Status status, Pageable pageable);

    long countByRole(User.Role role);

    long countByStatus(User.Status status);
}
