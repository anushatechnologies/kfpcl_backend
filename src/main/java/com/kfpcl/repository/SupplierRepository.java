package com.kfpcl.repository;

import com.kfpcl.entity.Supplier;
import com.kfpcl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {
    Optional<Supplier> findByUser(User user);
    Optional<Supplier> findByUserId(String userId);
}
