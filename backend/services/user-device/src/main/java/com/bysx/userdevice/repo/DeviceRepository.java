package com.bysx.userdevice.repo;

import com.bysx.userdevice.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
    Optional<DeviceEntity> findBySnCode(String snCode);
    Optional<DeviceEntity> findByDeviceId(String deviceId);
}

