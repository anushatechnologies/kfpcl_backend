package com.kfpcl.repository;

import com.kfpcl.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, String> {

    List<UserRole> findByUserId(String userId);

    void deleteByUserId(String userId);

    boolean existsByUserIdAndRole(String userId, String role);
}
