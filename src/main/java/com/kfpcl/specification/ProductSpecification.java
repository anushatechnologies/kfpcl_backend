package com.kfpcl.specification;

import com.kfpcl.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filter(
            String search,
            String categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer moqMax,
            Boolean gstVerified,
            Boolean verifiedSupplier,
            Boolean featured,
            Product.Status status
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate descLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(titleLike, descLike));
            }

            if (StringUtils.hasText(categoryId)) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (moqMax != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("moq"), moqMax));
            }

            if (gstVerified != null) {
                predicates.add(criteriaBuilder.equal(root.get("supplier").get("gstVerified"), gstVerified));
            }

            if (verifiedSupplier != null) {
                predicates.add(criteriaBuilder.equal(root.get("supplier").get("isVerified"), verifiedSupplier));
            }

            if (featured != null) {
                predicates.add(criteriaBuilder.equal(root.get("featured"), featured));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
