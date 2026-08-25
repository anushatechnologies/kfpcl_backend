package com.kfpcl.repository;

import com.kfpcl.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, String> {

    Optional<Brand> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

    Page<Brand> findByStatus(Brand.Status status, Pageable pageable);

    Page<Brand> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Brand> findByNameContainingIgnoreCaseAndStatus(String name, Brand.Status status, Pageable pageable);
}
