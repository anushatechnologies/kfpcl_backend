package com.kfpcl.specification;

import com.kfpcl.dto.request.ProductFilterRequest;
import com.kfpcl.entity.Category;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.Seller;
import com.kfpcl.entity.enums.ProductStatus;
import com.kfpcl.entity.enums.VerificationStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    private ProductSpecification() {}

    /**
     * Builds dynamic JPA Specification for public product catalog search & filtering.
     * Enforces public visibility rules: ACTIVE, APPROVED, and VERIFIED SELLER.
     */
    public static Specification<Product> filterPublicProducts(ProductFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Mandatory Public Catalog Rules
            predicates.add(criteriaBuilder.equal(root.get("status"), ProductStatus.ACTIVE));
            predicates.add(criteriaBuilder.isTrue(root.get("isApproved")));

            Join<Product, Seller> sellerJoin = root.join("seller");
            predicates.add(criteriaBuilder.isTrue(sellerJoin.get("isVerified")));
            predicates.add(criteriaBuilder.equal(sellerJoin.get("verificationStatus"), VerificationStatus.VERIFIED));

            if (filter == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            // 2. Keyword Search (Product name, description, tags, sku)
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String searchPattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate descMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern);
                Predicate tagsMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("tags")), searchPattern);
                Predicate skuMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), searchPattern);

                predicates.add(criteriaBuilder.or(nameMatch, descMatch, tagsMatch, skuMatch));
            }

            // 3. Category Filter
            if (filter.getCategoryId() != null) {
                Join<Product, Category> categoryJoin = root.join("category");
                predicates.add(criteriaBuilder.equal(categoryJoin.get("id"), filter.getCategoryId()));
            }

            // 4. Price Band Filtering
            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("basePrice"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("basePrice"), filter.getMaxPrice()));
            }

            // 5. MOQ Filter (Products with MOQ <= maxMoq)
            if (filter.getMaxMoq() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("moq"), filter.getMaxMoq()));
            }

            // 6. Unit Filter
            if (filter.getUnit() != null && !filter.getUnit().isBlank()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("unit")), filter.getUnit().trim().toUpperCase()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
