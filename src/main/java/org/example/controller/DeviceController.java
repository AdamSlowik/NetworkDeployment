package org.example.controller;

import inet.ipaddr.mac.MACAddress;
import jakarta.validation.Valid;
import org.example.api.Device;
import org.example.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("api/device")
public class DeviceController {
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createDevice(@RequestBody @Valid Device device) {
        deviceService.createDevice(device);
    }

    @GetMapping()
    public List<Device> getDevices() {
        return deviceService.getDevices();
    }

    @GetMapping("{macAddress}")
    public Device getDevice(@PathVariable MACAddress macAddress) {
        return deviceService.getDevice(macAddress);
    }
}
