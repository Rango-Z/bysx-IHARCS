package com.bysx.datav.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Document("device_log")
public class DeviceLogDoc {
    @Id
    private String id;

    private String deviceId;
    private String deviceType;
    private Date timestamp;
    private Map<String, Object> status;
    private Map<String, Object> energy;

    public String getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getStatus() {
        return status;
    }

    public Map<String, Object> getEnergy() {
        return energy;
    }
}

