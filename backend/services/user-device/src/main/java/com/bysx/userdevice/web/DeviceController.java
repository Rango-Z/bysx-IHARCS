package com.bysx.userdevice.web;

import com.bysx.common.api.ApiResponse;
import com.bysx.userdevice.entity.DeviceEntity;
import com.bysx.userdevice.entity.UserDeviceBindEntity;
import com.bysx.userdevice.repo.DeviceRepository;
import com.bysx.userdevice.repo.UserDeviceBindRepository;
import com.bysx.userdevice.web.dto.BindDeviceRequest;
import com.bysx.userdevice.web.dto.DeviceView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final UserDeviceBindRepository bindRepository;

    public DeviceController(DeviceRepository deviceRepository, UserDeviceBindRepository bindRepository) {
        this.deviceRepository = deviceRepository;
        this.bindRepository = bindRepository;
    }

    @GetMapping("/list")
    public ApiResponse<List<DeviceView>> list(@RequestParam(required = false) Long userId) {
        long uid = userId == null ? 1L : userId;
        List<UserDeviceBindEntity> binds = bindRepository.findByUserId(uid);
        if (binds.isEmpty()) {
            return ApiResponse.ok(List.of());
        }
        Map<Long, DeviceEntity> devices = deviceRepository.findAllById(
                        binds.stream().map(UserDeviceBindEntity::getDeviceId).toList()
                ).stream()
                .collect(Collectors.toMap(DeviceEntity::getId, d -> d));

        List<DeviceView> result = binds.stream()
                .map(b -> devices.get(b.getDeviceId()))
                .filter(Objects::nonNull)
                .map(d -> new DeviceView(d.getDeviceId(), d.getSnCode(), d.getDeviceType(), d.getBrand(), d.getName(), d.getOnlineStatus()))
                .toList();
        return ApiResponse.ok(result);
    }

    @PostMapping("/bind")
    public ApiResponse<DeviceView> bind(@Valid @RequestBody BindDeviceRequest req,
                                        @RequestParam(required = false) Long userId) {
        long uid = userId == null ? 1L : userId;

        DeviceEntity device = deviceRepository.findBySnCode(req.snCode())
                .orElseGet(() -> {
                    DeviceEntity d = new DeviceEntity();
                    d.setSnCode(req.snCode());
                    d.setDeviceType(req.deviceType());
                    d.setBrand(req.brand());
                    d.setName(req.name());
                    d.setOnlineStatus(0);
                    d.setDeviceId("dev_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
                    return deviceRepository.save(d);
                });

        // 更新用户自定义名称/品牌（按需）
        device.setName(req.name());
        if (req.brand() != null) {
            device.setBrand(req.brand());
        }
        deviceRepository.save(device);

        bindRepository.findByUserIdAndDeviceId(uid, device.getId())
                .orElseGet(() -> {
                    UserDeviceBindEntity bind = new UserDeviceBindEntity();
                    bind.setUserId(uid);
                    bind.setDeviceId(device.getId());
                    bind.setIsOwner(1);
                    return bindRepository.save(bind);
                });

        return ApiResponse.ok(new DeviceView(device.getDeviceId(), device.getSnCode(), device.getDeviceType(), device.getBrand(), device.getName(), device.getOnlineStatus()));
    }

    @GetMapping("/by-deviceId/{deviceId}")
    public ApiResponse<DeviceView> getByDeviceId(@PathVariable String deviceId) {
        DeviceEntity d = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "device not found"));
        return ApiResponse.ok(new DeviceView(d.getDeviceId(), d.getSnCode(), d.getDeviceType(), d.getBrand(), d.getName(), d.getOnlineStatus()));
    }
}

