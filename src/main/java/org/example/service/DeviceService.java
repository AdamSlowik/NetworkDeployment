package org.example.service;

import inet.ipaddr.mac.MACAddress;
import org.example.api.Device;
import org.example.api.Tree;
import org.example.exception.NodeNotFound;
import org.example.exception.UplinkNotFound;
import org.example.model.DeviceNode;
import org.example.repository.DeviceNodeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private final DeviceNodeRepository deviceNodeRepository;

    public DeviceService(DeviceNodeRepository deviceNodeRepository) {
        this.deviceNodeRepository = deviceNodeRepository;
    }

    public void createDevice(Device device) {
        var uplinkNode = device.uplinkMacAddress()
                .map(mac -> deviceNodeRepository
                        .findUniqueByMacAddress(mac)
                        .orElseThrow(
                                () -> new UplinkNotFound("Uplink with mac %s not found.".formatted(mac.toColonDelimitedString())))
                );
        var nodeDevice = new DeviceNode(device, uplinkNode.orElse(null));
        deviceNodeRepository.save(nodeDevice);
    }

    public Device getDevice(MACAddress mac) {
        return deviceNodeRepository.findUniqueByMacAddress(mac)
                .orElseThrow(
                        () -> new NodeNotFound("Device with mac %s not found".formatted(mac.toColonDelimitedString())))
                .toDevice();
    }

    public List<Device> getDevices() {
        return deviceNodeRepository.findAllOrderByDeviceTypeOrder().stream().map(DeviceNode::toDevice).toList();
    }

    public Set<Tree> getTree() {
        return deviceNodeRepository.findAllRoots().stream()
                .map(root -> deviceNodeRepository.findById(root.getId()).orElseThrow().toTree())
                .collect(Collectors.toSet());
    }

    public Tree getTree(MACAddress mac) {
        return deviceNodeRepository.findUniqueByMacAddress(mac)
                .orElseThrow(
                        () -> new NodeNotFound("Device with mac %s not found".formatted(mac.toColonDelimitedString())))
                .toTree();
    }
}
