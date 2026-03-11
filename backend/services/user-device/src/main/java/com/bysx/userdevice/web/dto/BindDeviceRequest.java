package com.bysx.userdevice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BindDeviceRequest(
        @NotBlank String snCode,
        @NotNull String deviceType,
        String brand,
        @NotBlank String name
) {
}

