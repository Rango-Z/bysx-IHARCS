package com.bysx.datav.service;

import com.bysx.datav.mongo.DeviceLogDoc;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatsService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("MM-dd");

    private final MongoTemplate mongoTemplate;

    public StatsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public record UsageResult(List<String> days, Map<String, List<Double>> deviceHoursByDay) {}
    public record EnergyItem(String deviceId, double kwh) {}

    public UsageResult usageHoursByDeviceLastDays(List<String> deviceIds, int days, int intervalSeconds) {
        if (days <= 0) days = 7;
        if (intervalSeconds <= 0) intervalSeconds = 10;

        LocalDate today = LocalDate.now(ZONE);
        List<LocalDate> dayList = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            dayList.add(today.minusDays(i));
        }
        List<String> dayLabels = dayList.stream().map(DAY_FMT::format).toList();

        Map<String, Map<LocalDate, Integer>> counts = new HashMap<>();
        for (String deviceId : deviceIds) {
            counts.put(deviceId, new HashMap<>());
        }

        Date from = Date.from(today.minusDays(days - 1).atStartOfDay(ZONE).toInstant());
        Query q = new Query();
        q.addCriteria(Criteria.where("timestamp").gte(from));
        if (!deviceIds.isEmpty()) {
            q.addCriteria(Criteria.where("deviceId").in(deviceIds));
        }
        q.fields().include("deviceId").include("timestamp").include("status");

        List<DeviceLogDoc> logs = mongoTemplate.find(q, DeviceLogDoc.class, "device_log");
        for (DeviceLogDoc log : logs) {
            if (log.getDeviceId() == null || log.getTimestamp() == null) continue;
            Object power = log.getStatus() == null ? null : log.getStatus().get("power");
            if (power == null) continue;
            String p = String.valueOf(power).trim().toUpperCase();
            if (!"ON".equals(p) && !"1".equals(p) && !"TRUE".equals(p)) continue;

            LocalDate d = Instant.ofEpochMilli(log.getTimestamp().getTime()).atZone(ZONE).toLocalDate();
            counts.computeIfAbsent(log.getDeviceId(), k -> new HashMap<>())
                    .merge(d, 1, Integer::sum);
        }

        Map<String, List<Double>> hours = new LinkedHashMap<>();
        for (String deviceId : counts.keySet()) {
            Map<LocalDate, Integer> perDay = counts.get(deviceId);
            List<Double> series = new ArrayList<>();
            for (LocalDate d : dayList) {
                int c = perDay == null ? 0 : perDay.getOrDefault(d, 0);
                double h = (c * intervalSeconds) / 3600.0;
                series.add(round2(h));
            }
            hours.put(deviceId, series);
        }
        return new UsageResult(dayLabels, hours);
    }

    public List<EnergyItem> energyKwhByDeviceLastDays(List<String> deviceIds, int days) {
        if (days <= 0) days = 7;
        LocalDate today = LocalDate.now(ZONE);
        Date from = Date.from(today.minusDays(days - 1).atStartOfDay(ZONE).toInstant());

        List<EnergyItem> out = new ArrayList<>();
        for (String deviceId : deviceIds) {
            Query q = new Query();
            q.addCriteria(Criteria.where("deviceId").is(deviceId));
            q.addCriteria(Criteria.where("timestamp").gte(from));
            q.with(Sort.by(Sort.Direction.ASC, "timestamp"));
            q.fields().include("timestamp").include("energy");

            List<DeviceLogDoc> logs = mongoTemplate.find(q, DeviceLogDoc.class, "device_log");
            Double first = null, last = null;
            for (DeviceLogDoc log : logs) {
                Object kwh = log.getEnergy() == null ? null : log.getEnergy().get("kwhTotal");
                if (kwh == null) continue;
                try {
                    double v = Double.parseDouble(String.valueOf(kwh));
                    if (first == null) first = v;
                    last = v;
                } catch (Exception ignored) {
                }
            }
            double delta = (first == null || last == null) ? 0.0 : Math.max(0.0, last - first);
            out.add(new EnergyItem(deviceId, round2(delta)));
        }
        return out;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}

