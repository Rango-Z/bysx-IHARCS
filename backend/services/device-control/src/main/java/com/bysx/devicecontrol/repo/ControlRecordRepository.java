package com.bysx.devicecontrol.repo;

import com.bysx.devicecontrol.entity.ControlRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ControlRecordRepository extends JpaRepository<ControlRecordEntity, Long> {
    Optional<ControlRecordEntity> findByCommandId(String commandId);
}

