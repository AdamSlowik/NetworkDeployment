package org.example.controller;

import inet.ipaddr.mac.MACAddress;
import org.example.api.Tree;
import org.example.service.DeviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController()
@RequestMapping("api/tree")
public class TreeController {
    private final DeviceService deviceService;

    public TreeController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping()
    public Set<Tree> getDevices() {
        return deviceService.getTree();
    }

    @GetMapping("{macAddress}")
    public Tree getDevice(@PathVariable MACAddress macAddress) {
        return deviceService.getTree(macAddress);
    }
}
