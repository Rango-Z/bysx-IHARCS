package com.bysx.userdevice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_device_bind")
public class UserDeviceBindEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "bind_time", nullable = false)
    private LocalDateTime bindTime;

    @Column(name = "is_owner", nullable = false)
    private Integer isOwner;

    public Long getUserId() {
        return userId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public void setIsOwner(Integer isOwner) {
        this.isOwner = isOwner;
    }
}

