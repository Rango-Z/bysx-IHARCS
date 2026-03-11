package com.bysx.devicecontrol.web;

import com.bysx.common.api.ApiResponse;
import com.bysx.common.dto.ControlCommand;
import com.bysx.devicecontrol.entity.ControlRecordEntity;
import com.bysx.devicecontrol.repo.ControlRecordRepository;
import com.bysx.devicecontrol.service.DeviceLookupService;
import com.bysx.devicecontrol.service.MqttPublishService;
import com.bysx.devicecontrol.web.dto.ControlRecordView;
import com.bysx.devicecontrol.web.dto.SendControlRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/control")
public class ControlController {

    private final ControlRecordRepository repository;
    private final MqttPublishService mqttPublishService;
    private final DeviceLookupService deviceLookupService;
    private final ObjectMapper objectMapper;

    public ControlController(ControlRecordRepository repository,
                             MqttPublishService mqttPublishService,
                             DeviceLookupService deviceLookupService,
                             ObjectMapper objectMapper) {
        this.repository = repository;
        this.mqttPublishService = mqttPublishService;
        this.deviceLookupService = deviceLookupService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/send")
    public ApiResponse<ControlRecordView> send(@Valid @RequestBody SendControlRequest req,
                                               @RequestParam(required = false) Long userId) {
        long uid = userId == null ? 1L : userId;
        String commandId = "cr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        long devicePkId = deviceLookupService.resolveDevicePkIdByDeviceId(req.deviceId(), objectMapper);

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(req.payload());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid payload");
        }

        ControlRecordEntity record = new ControlRecordEntity();
        record.setCommandId(commandId);
        record.setUserId(uid);
        record.setDevicePkId(devicePkId);
        record.setCommandType(req.commandType());
        record.setCommandPayload(payloadJson);
        record.setStatus(0);
        record.setRequestTime(LocalDateTime.now());
        repository.save(record);

        try {
            ControlCommand cmd = new ControlCommand(commandId, req.commandType(), req.payload(), System.currentTimeMillis());
            String cmdJson = objectMapper.writeValueAsString(cmd);
            String topic = "home/device/" + req.deviceId() + "/control";
            mqttPublishService.publish(topic, cmdJson);

            record.setStatus(1);
            repository.save(record);
        } catch (Exception e) {
            record.setStatus(3);
            record.setResultMessage("publish failed");
            repository.save(record);
        }

        return ApiResponse.ok(toView(record, req.deviceId()));
    }

    @GetMapping("/result/{commandId}")
    public ApiResponse<ControlRecordView> result(@PathVariable String commandId) {
        ControlRecordEntity record = repository.findByCommandId(commandId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "command not found"));

        // 反查 deviceId：此 demo 用固定映射（与 DeviceLookupService 一致）
        String deviceId = switch (record.getDevicePkId().intValue()) {
            case 1 -> "ac001";
            case 2 -> "fr001";
            case 3 -> "wa001";
            case 4 -> "li001";
            default -> "unknown";
        };
        return ApiResponse.ok(toView(record, deviceId));
    }

    private static ControlRecordView toView(ControlRecordEntity r, String deviceId) {
        return new ControlRecordView(
                r.getCommandId(),
                deviceId,
                r.getCommandType(),
                r.getCommandPayload(),
                r.getStatus(),
                r.getRequestTime(),
                r.getExecuteTime(),
                r.getResultMessage()
        );
    }
}

