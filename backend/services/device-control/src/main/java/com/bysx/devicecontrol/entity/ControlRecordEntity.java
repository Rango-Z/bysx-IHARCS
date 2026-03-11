package com.bysx.devicecontrol.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "control_record")
public class ControlRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "command_id", nullable = false, unique = true)
    private String commandId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_id", nullable = false)
    private Long devicePkId;

    @Column(name = "command_type", nullable = false)
    private String commandType;

    @Column(name = "command_payload", columnDefinition = "json")
    private String commandPayload;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "request_time", nullable = false)
    private LocalDateTime requestTime;

    @Column(name = "execute_time")
    private LocalDateTime executeTime;

    @Column(name = "result_message")
    private String resultMessage;

    public Long getId() {
        return id;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDevicePkId() {
        return devicePkId;
    }

    public void setDevicePkId(Long devicePkId) {
        this.devicePkId = devicePkId;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandPayload() {
        return commandPayload;
    }

    public void setCommandPayload(String commandPayload) {
        this.commandPayload = commandPayload;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public LocalDateTime getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(LocalDateTime executeTime) {
        this.executeTime = executeTime;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
}

