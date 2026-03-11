package com.bysx.devicedata.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceRealtimeRepository extends MongoRepository<DeviceRealtimeDoc, String> {
}

