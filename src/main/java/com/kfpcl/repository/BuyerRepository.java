package com.kfpcl.repository;

import com.kfpcl.entity.Buyer;
import com.kfpcl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, String> {
    Optional<Buyer> findByUser(User user);
    Optional<Buyer> findByUserId(String userId);
}
