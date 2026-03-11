package com.bysx.datav.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceRealtimeRepository extends MongoRepository<DeviceRealtimeDoc, String> {
}

