package com.kfpcl.repository;

import com.kfpcl.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

    Page<Category> findByStatus(Category.Status status, Pageable pageable);

    List<Category> findByStatus(Category.Status status);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Category> findByNameContainingIgnoreCaseAndStatus(String name, Category.Status status, Pageable pageable);

    Page<Category> findByStatusNot(Category.Status status, Pageable pageable);

    Page<Category> findByNameContainingIgnoreCaseAndStatusNot(String name, Category.Status status, Pageable pageable);
}
