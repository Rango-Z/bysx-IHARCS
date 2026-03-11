package com.bysx.datav.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Document("device_realtime")
public class DeviceRealtimeDoc {
    @Id
    private String deviceId;

    private String deviceType;
    private Date timestamp;
    private Map<String, Object> status;
    private Map<String, Object> energy;

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

