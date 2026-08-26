package com.kfpcl.specification;

import com.kfpcl.entity.Category;
import com.kfpcl.entity.Rfq;
import com.kfpcl.entity.enums.RFQStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RfqSpecification {

    private RfqSpecification() {}

    /**
     * Specification for filtering OPEN RFQs in the Seller Discovery Feed.
     */
    public static Specification<Rfq> filterOpenRfqs(Long categoryId, String keyword) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Mandatory status = OPEN
            predicates.add(criteriaBuilder.equal(root.get("status"), RFQStatus.OPEN));

            // 2. Optional Category filter
            if (categoryId != null) {
                Join<Rfq, Category> categoryJoin = root.join("category");
                predicates.add(criteriaBuilder.equal(categoryJoin.get("id"), categoryId));
            }

            // 3. Optional Keyword search across title, description, and delivery location
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern);
                Predicate descMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern);
                Predicate locationMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("deliveryLocation")), pattern);

                predicates.add(criteriaBuilder.or(titleMatch, descMatch, locationMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
