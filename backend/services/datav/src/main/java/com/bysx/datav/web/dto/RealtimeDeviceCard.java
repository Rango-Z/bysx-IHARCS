package com.bysx.datav.web.dto;

import java.util.Date;
import java.util.Map;

public record RealtimeDeviceCard(
        String deviceId,
        String deviceType,
        String name,
        Date timestamp,
        Map<String, Object> status,
        Map<String, Object> energy
) {
}

