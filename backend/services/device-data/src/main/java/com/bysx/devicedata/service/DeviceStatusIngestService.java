package com.bysx.devicedata.service;

import com.bysx.devicedata.mongo.DeviceLogDoc;
import com.bysx.devicedata.mongo.DeviceLogRepository;
import com.bysx.devicedata.mongo.DeviceRealtimeDoc;
import com.bysx.devicedata.mongo.DeviceRealtimeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class DeviceStatusIngestService {

    private final DeviceLogRepository logRepository;
    private final DeviceRealtimeRepository realtimeRepository;

    public DeviceStatusIngestService(DeviceLogRepository logRepository, DeviceRealtimeRepository realtimeRepository) {
        this.logRepository = logRepository;
        this.realtimeRepository = realtimeRepository;
    }

    public void ingest(String json, ObjectMapper objectMapper) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String deviceId = root.path("deviceId").asText(null);
            if (deviceId == null || deviceId.isBlank()) {
                return;
            }
            String deviceType = root.path("deviceType").asText(null);
            long ts = root.path("timestamp").asLong(System.currentTimeMillis());
            Date time = new Date(ts);

            Map<String, Object> status = null;
            Map<String, Object> energy = null;
            if (root.hasNonNull("status")) {
                status = objectMapper.convertValue(root.get("status"), new TypeReference<>() {});
            }
            if (root.hasNonNull("energy")) {
                energy = objectMapper.convertValue(root.get("energy"), new TypeReference<>() {});
            }

            logRepository.save(new DeviceLogDoc(deviceId, deviceType, time, status, energy));
            realtimeRepository.save(new DeviceRealtimeDoc(deviceId, deviceType, time, status, energy));
        } catch (Exception ignored) {
            // 忽略坏数据，保证采集链路稳定
        }
    }
}

