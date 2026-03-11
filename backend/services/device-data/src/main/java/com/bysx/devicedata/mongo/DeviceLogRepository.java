package com.bysx.devicedata.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceLogRepository extends MongoRepository<DeviceLogDoc, String> {
}

