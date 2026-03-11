package com.bysx.datav.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDeviceClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public record DeviceView(String deviceId, String snCode, String deviceType, String brand, String name, int onlineStatus) {}

    public UserDeviceClient(@Value("${deps.userDeviceBaseUrl}") String baseUrl, ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
    }

    public List<DeviceView> listDevices(Long userId) {
        String url = "/api/device/list" + (userId == null ? "" : ("?userId=" + userId));
        String json = restClient.get().uri(url).retrieve().body(String.class);
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.path("data");
            if (!data.isArray()) return List.of();
            List<DeviceView> out = new ArrayList<>();
            for (JsonNode n : data) {
                out.add(new DeviceView(
                        n.path("deviceId").asText(),
                        n.path("snCode").asText(),
                        n.path("deviceType").asText(),
                        n.path("brand").asText(null),
                        n.path("name").asText(null),
                        n.path("onlineStatus").asInt(0)
                ));
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }
}

