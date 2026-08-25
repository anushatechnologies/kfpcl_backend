package com.kfpcl.repository;

import com.kfpcl.entity.PlatformSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformSettingRepository extends JpaRepository<PlatformSetting, String> {

    Optional<PlatformSetting> findBySettingKey(String settingKey);
}
