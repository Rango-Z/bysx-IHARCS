package com.bysx.userdevice.repo;

import com.bysx.userdevice.entity.UserDeviceBindEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDeviceBindRepository extends JpaRepository<UserDeviceBindEntity, Long> {
    List<UserDeviceBindEntity> findByUserId(Long userId);
    Optional<UserDeviceBindEntity> findByUserIdAndDeviceId(Long userId, Long deviceId);
}

