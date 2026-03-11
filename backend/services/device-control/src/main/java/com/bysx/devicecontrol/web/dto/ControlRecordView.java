package com.bysx.devicecontrol.web.dto;

import java.time.LocalDateTime;

public record ControlRecordView(
        String commandId,
        String deviceId,
        String commandType,
        String commandPayload,
        int status,
        LocalDateTime requestTime,
        LocalDateTime executeTime,
        String resultMessage
) {
}

