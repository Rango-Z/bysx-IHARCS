package com.bysx.devicecontrol.service;

import com.bysx.common.dto.ControlResult;
import com.bysx.devicecontrol.entity.ControlRecordEntity;
import com.bysx.devicecontrol.repo.ControlRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ControlResultConsumeService {

    private final ControlRecordRepository repository;

    public ControlResultConsumeService(ControlRecordRepository repository) {
        this.repository = repository;
    }

    public void consume(String json, ObjectMapper objectMapper) {
        try {
            ControlResult result = objectMapper.readValue(json, ControlResult.class);
            repository.findByCommandId(result.commandId()).ifPresent(record -> {
                String r = result.result() == null ? "" : result.result().trim().toUpperCase();
                if ("SUCCESS".equals(r) || "OK".equals(r)) {
                    record.setStatus(2);
                } else {
                    record.setStatus(3);
                }
                record.setExecuteTime(LocalDateTime.now());
                record.setResultMessage(result.message());
                repository.save(record);
            });
        } catch (Exception ignored) {
        }
    }
}

