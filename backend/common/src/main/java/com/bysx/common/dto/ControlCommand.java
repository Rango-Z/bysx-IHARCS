package com.bysx.common.dto;

import java.util.Map;

public record ControlCommand(
        String commandId,
        String commandType,
        Map<String, Object> payload,
        long timestamp
) {
}

