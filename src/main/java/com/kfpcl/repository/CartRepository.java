package com.kfpcl.repository;

import com.kfpcl.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByBuyerId(String buyerId);
}
