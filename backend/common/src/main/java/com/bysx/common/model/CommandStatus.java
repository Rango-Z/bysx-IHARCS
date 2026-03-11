package com.bysx.common.model;

public enum CommandStatus {
    PENDING(0),
    SENT(1),
    SUCCESS(2),
    FAILED(3);

    private final int code;

    CommandStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}

