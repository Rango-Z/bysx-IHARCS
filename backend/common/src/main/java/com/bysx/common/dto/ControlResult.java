package com.bysx.common.dto;

public record ControlResult(
        String commandId,
        String result,
        String message,
        long timestamp
) {
}

