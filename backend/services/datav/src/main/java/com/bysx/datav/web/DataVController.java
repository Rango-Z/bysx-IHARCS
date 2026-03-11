package com.bysx.datav.web;

import com.bysx.common.api.ApiResponse;
import com.bysx.datav.mongo.DeviceRealtimeDoc;
import com.bysx.datav.mongo.DeviceRealtimeRepository;
import com.bysx.datav.service.StatsService;
import com.bysx.datav.service.UserDeviceClient;
import com.bysx.datav.web.dto.EnergyPieResponse;
import com.bysx.datav.web.dto.RealtimeDeviceCard;
import com.bysx.datav.web.dto.UsageStatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/datav")
public class DataVController {

    private final UserDeviceClient userDeviceClient;
    private final DeviceRealtimeRepository realtimeRepository;
    private final StatsService statsService;

    public DataVController(UserDeviceClient userDeviceClient,
                           DeviceRealtimeRepository realtimeRepository,
                           StatsService statsService) {
        this.userDeviceClient = userDeviceClient;
        this.realtimeRepository = realtimeRepository;
        this.statsService = statsService;
    }

    @GetMapping("/realtime/devices")
    public ApiResponse<List<RealtimeDeviceCard>> realtimeDevices(@RequestParam(required = false) Long userId) {
        List<UserDeviceClient.DeviceView> devices = userDeviceClient.listDevices(userId);
        List<RealtimeDeviceCard> cards = new ArrayList<>();
        for (UserDeviceClient.DeviceView d : devices) {
            Optional<DeviceRealtimeDoc> rt = realtimeRepository.findById(d.deviceId());
            if (rt.isPresent()) {
                DeviceRealtimeDoc doc = rt.get();
                cards.add(new RealtimeDeviceCard(d.deviceId(), d.deviceType(), d.name(), doc.getTimestamp(), doc.getStatus(), doc.getEnergy()));
            } else {
                cards.add(new RealtimeDeviceCard(d.deviceId(), d.deviceType(), d.name(), null, Map.of(), Map.of()));
            }
        }
        return ApiResponse.ok(cards);
    }

    @GetMapping("/stat/usage")
    public ApiResponse<UsageStatResponse> usage(@RequestParam(defaultValue = "7") int days,
                                                @RequestParam(defaultValue = "10") int intervalSeconds,
                                                @RequestParam(required = false) Long userId) {
        List<UserDeviceClient.DeviceView> devices = userDeviceClient.listDevices(userId);
        List<String> ids = devices.stream().map(UserDeviceClient.DeviceView::deviceId).toList();
        StatsService.UsageResult r = statsService.usageHoursByDeviceLastDays(ids, days, intervalSeconds);
        return ApiResponse.ok(new UsageStatResponse(r.days(), r.deviceHoursByDay()));
    }

    @GetMapping("/stat/energy")
    public ApiResponse<EnergyPieResponse> energy(@RequestParam(defaultValue = "7") int days,
                                                 @RequestParam(required = false) Long userId) {
        List<UserDeviceClient.DeviceView> devices = userDeviceClient.listDevices(userId);
        List<String> ids = devices.stream().map(UserDeviceClient.DeviceView::deviceId).toList();
        Map<String, String> idToName = new HashMap<>();
        for (UserDeviceClient.DeviceView d : devices) {
            idToName.put(d.deviceId(), d.name() == null ? d.deviceId() : d.name());
        }

        List<StatsService.EnergyItem> items = statsService.energyKwhByDeviceLastDays(ids, days);
        List<EnergyPieResponse.Item> out = items.stream()
                .map(i -> new EnergyPieResponse.Item(i.deviceId(), idToName.getOrDefault(i.deviceId(), i.deviceId()), i.kwh()))
                .toList();
        return ApiResponse.ok(new EnergyPieResponse(out));
    }

    @GetMapping("/realtime/washer-progress")
    public ApiResponse<Map<String, Object>> washerProgress() {
        // 简化：直接从 wa001 的 realtime.status.progress 取值
        Optional<DeviceRealtimeDoc> rt = realtimeRepository.findById("wa001");
        double progress = 0.0;
        if (rt.isPresent() && rt.get().getStatus() != null) {
            Object p = rt.get().getStatus().get("progress");
            if (p != null) {
                try {
                    progress = Double.parseDouble(String.valueOf(p));
                } catch (Exception ignored) {
                }
            }
        }
        return ApiResponse.ok(Map.of("deviceId", "wa001", "progress", progress));
    }
}

