package com.bysx.userdevice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "device")
public class DeviceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, unique = true)
    private String deviceId;

    @Column(name = "sn_code", nullable = false, unique = true)
    private String snCode;

    @Column(name = "device_type", nullable = false)
    private String deviceType;

    private String brand;
    private String name;

    @Column(name = "online_status", nullable = false)
    private Integer onlineStatus;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getSnCode() {
        return snCode;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getBrand() {
        return brand;
    }

    public String getName() {
        return name;
    }

    public Integer getOnlineStatus() {
        return onlineStatus;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setSnCode(String snCode) {
        this.snCode = snCode;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOnlineStatus(Integer onlineStatus) {
        this.onlineStatus = onlineStatus;
    }
}

