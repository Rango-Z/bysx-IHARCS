package com.bysx.devicecontrol.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeviceLookupService {

    private final RestClient restClient;

    public DeviceLookupService(@Value("${deps.userDeviceBaseUrl:http://localhost:8101}") String userDeviceBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(userDeviceBaseUrl).build();
    }

    public long resolveDevicePkIdByDeviceId(String deviceId, ObjectMapper objectMapper) {
        // user-device 暂时只暴露 deviceId->view；这里为了 demo，直接走 MySQL 里 control_record 的 device_id 字段需要 PK。
        // 简化：使用固定映射：ac001=1, fr001=2, wa001=3, li001=4（与初始化 SQL 插入顺序一致）。
        // 若你后续想严谨：在 user-device 服务增加 /api/device/pk/{deviceId} 返回主键即可。
        return switch (deviceId) {
            case "ac001" -> 1L;
            case "fr001" -> 2L;
            case "wa001" -> 3L;
            case "li001" -> 4L;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown deviceId: " + deviceId);
        };
    }
}

