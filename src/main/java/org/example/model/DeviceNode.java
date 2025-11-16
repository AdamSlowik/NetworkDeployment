package org.example.model;

import inet.ipaddr.mac.MACAddress;
import org.example.api.Device;
import org.example.api.DeviceType;
import org.example.api.Tree;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Node
public class DeviceNode {

    @Id
    @GeneratedValue
    Long id;

    MACAddress macAddress;

    DeviceType deviceType;

    @Relationship(type = "UPLINK", direction = Relationship.Direction.OUTGOING)
    private DeviceNode uplinkDeviceNode;

    @Relationship(type = "UPLINK", direction = Relationship.Direction.INCOMING)
    private List<DeviceNode> childNodes = new ArrayList<>();

    public DeviceNode() {
    }

    public DeviceNode(MACAddress macAddress, DeviceType deviceType, DeviceNode uplinkDeviceNode) {
        this.macAddress = macAddress;
        this.deviceType = deviceType;
        this.uplinkDeviceNode = uplinkDeviceNode;
    }

    public DeviceNode(Device device, DeviceNode uplinkDeviceNode) {
        this.macAddress = device.macAddress();
        this.deviceType = device.deviceType();
        this.uplinkDeviceNode = uplinkDeviceNode;
    }

    public Device toDevice() {
        return new Device(this.macAddress, this.deviceType, Optional.ofNullable(this.uplinkDeviceNode).map(it -> it.macAddress));
    }

    public Tree toTree() {
        return new Tree(this.toDevice(), this.childNodes.stream().map(DeviceNode::toTree).collect(Collectors.toSet()));
    }

    public Long getId() {
        return id;
    }

    public List<DeviceNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<DeviceNode> childNodes) {
        this.childNodes = childNodes;
    }

    public MACAddress getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(MACAddress macAddress) {
        this.macAddress = macAddress;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public DeviceNode getUplinkDeviceNode() {
        return uplinkDeviceNode;
    }

    public void setUplinkDeviceNode(DeviceNode uplinkDeviceNode) {
        this.uplinkDeviceNode = uplinkDeviceNode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DeviceNode that = (DeviceNode) o;
        return Objects.equals(id, that.id) && Objects.equals(macAddress, that.macAddress) && deviceType == that.deviceType && Objects.equals(uplinkDeviceNode, that.uplinkDeviceNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, macAddress, deviceType, uplinkDeviceNode, childNodes);
    }
}
