package com.kfpcl.repository;

import com.kfpcl.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, String> {

    List<Banner> findAllByOrderByDisplayOrderAscCreatedAtDesc();

    List<Banner> findByStatusOrderByDisplayOrderAscCreatedAtDesc(Banner.Status status);

    List<Banner> findByStatusNotOrderByDisplayOrderAscCreatedAtDesc(Banner.Status status);
}
