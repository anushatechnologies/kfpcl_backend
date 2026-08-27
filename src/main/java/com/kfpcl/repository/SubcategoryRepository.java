package com.kfpcl.repository;

import com.kfpcl.entity.Subcategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubcategoryRepository extends JpaRepository<Subcategory, String> {

    List<Subcategory> findByCategoryId(String categoryId);

    List<Subcategory> findByCategoryIdAndStatus(String categoryId, Subcategory.Status status);

    Page<Subcategory> findByCategoryId(String categoryId, Pageable pageable);

    Page<Subcategory> findByCategoryIdAndStatus(String categoryId, Subcategory.Status status, Pageable pageable);

    Page<Subcategory> findByStatus(Subcategory.Status status, Pageable pageable);

    Page<Subcategory> findByStatusNot(Subcategory.Status status, Pageable pageable);

    Page<Subcategory> findByCategoryIdAndStatusNot(String categoryId, Subcategory.Status status, Pageable pageable);

    boolean existsByCategoryIdAndNameIgnoreCase(String categoryId, String name);

    boolean existsByCategoryIdAndNameIgnoreCaseAndIdNot(String categoryId, String name, String id);

    Optional<Subcategory> findByIdAndCategoryId(String id, String categoryId);
}
