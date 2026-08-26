package com.kfpcl.user.repository;

import com.kfpcl.user.entity.Role;
import com.kfpcl.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByPhone(String phone);

    Optional<User> findByPhoneAndRole(String phone, Role role);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndRole(String phone, Role role);

    Optional<User> findByIdAndIsActiveTrue(String id);
}
