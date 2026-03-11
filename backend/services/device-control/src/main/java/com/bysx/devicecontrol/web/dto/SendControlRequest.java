package com.bysx.devicecontrol.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SendControlRequest(
        @NotBlank String deviceId,
        @NotBlank String commandType,
        @NotNull Map<String, Object> payload
) {
}

