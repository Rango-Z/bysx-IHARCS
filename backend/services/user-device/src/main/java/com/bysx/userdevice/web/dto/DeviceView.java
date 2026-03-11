package com.bysx.userdevice.web.dto;

public record DeviceView(
        String deviceId,
        String snCode,
        String deviceType,
        String brand,
        String name,
        int onlineStatus
) {
}

