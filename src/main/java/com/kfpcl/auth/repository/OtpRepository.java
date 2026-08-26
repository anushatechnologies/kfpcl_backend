package com.kfpcl.auth.repository;

import com.kfpcl.auth.entity.Otp;
import com.kfpcl.auth.entity.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByPhoneAndPurposeOrderByCreatedAtDesc(String phone, OtpPurpose purpose);

    int countByPhoneAndCreatedAtAfter(String phone, LocalDateTime since);

    @Modifying
    @Query("DELETE FROM Otp o WHERE o.phone = :phone")
    void deleteByPhone(@Param("phone") String phone);
}
