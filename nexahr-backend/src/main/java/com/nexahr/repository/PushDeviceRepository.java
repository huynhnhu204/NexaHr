package com.nexahr.repository;

import com.nexahr.entity.PushDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushDeviceRepository extends JpaRepository<PushDevice, Long> {
    List<PushDevice> findByUserIdAndActiveTrue(Long userId);
    Optional<PushDevice> findByUserIdAndDeviceToken(Long userId, String deviceToken);
}
