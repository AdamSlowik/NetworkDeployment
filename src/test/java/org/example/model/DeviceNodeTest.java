package org.example.model;

import inet.ipaddr.mac.MACAddress;
import org.example.api.Device;
import org.example.api.DeviceType;
import org.example.api.Tree;
import org.example.util.StringToMacConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.util.StringToMacConverter.convertToMac;
import static org.junit.jupiter.api.Assertions.*;

public class DeviceNodeTest {

    private MACAddress macAddress1;
    private MACAddress macAddress2;
    private DeviceType deviceType;
    private Device device;

    @BeforeEach
    void setUp() {
        macAddress1 = convertToMac("00:1A:2B:3C:4D:5E");
        macAddress2 = convertToMac("FF:EE:DD:CC:BB:AA");

        deviceType = DeviceType.GATEWAY;
        device = new Device(macAddress1, deviceType, Optional.empty());
    }

    @Test
    void testConstructorWithFields() {
        // given
        DeviceNode uplinkNode = new DeviceNode();

        // when
        DeviceNode node = new DeviceNode(macAddress1, deviceType, uplinkNode);

        // then
        assertEquals(macAddress1, node.getMacAddress());
        assertEquals(deviceType, node.getDeviceType());
        assertEquals(uplinkNode, node.getUplinkDeviceNode());
        assertNotNull(node.getChildNodes());
        assertTrue(node.getChildNodes().isEmpty());
    }

    @Test
    void testConstructorWithDeviceDTO() {
        // given
        DeviceNode uplinkNode = new DeviceNode();

        // when
        DeviceNode node = new DeviceNode(device, uplinkNode);

        // then
        assertEquals(macAddress1, node.getMacAddress());
        assertEquals(deviceType, node.getDeviceType());
        assertEquals(uplinkNode, node.getUplinkDeviceNode());

    }

    @Test
    void shouldCreateDeviceWithUplink() {
        // given
        DeviceNode uplinkNode = new DeviceNode();
        uplinkNode.setMacAddress(macAddress2);
        DeviceNode node = new DeviceNode(macAddress1, deviceType, uplinkNode);

        // when
        Device device = node.toDevice();

        assertEquals(macAddress1, device.macAddress());
        assertEquals(deviceType, device.deviceType());
        assertTrue(device.uplinkMacAddress().isPresent());
        assertEquals(macAddress2, device.uplinkMacAddress().get());
    }

    @Test
    void shouldCreateDeviceWithoutUplink() {
        DeviceNode node = new DeviceNode(macAddress1, deviceType, null);

        Device device = node.toDevice();

        assertEquals(macAddress1, device.macAddress());
        assertEquals(deviceType, device.deviceType());
        assertFalse(device.uplinkMacAddress().isPresent());
        assertEquals(Optional.empty(), device.uplinkMacAddress());
    }

    @Test
    void shouldCreateTreeWithSingleNode() {
        // given
        DeviceNode node = new DeviceNode(macAddress1, deviceType, null);

        // when
        Tree tree = node.toTree();

        // then
        assertNotNull(tree);
        assertNotNull(tree.children());
        assertTrue(tree.children().isEmpty());
    }

    @Test
    void shouldCreateTreeWithMultipleNodes() {
        // given
        DeviceNode root = new DeviceNode(macAddress1, deviceType, null);
        DeviceNode child1 = new DeviceNode(macAddress2, DeviceType.GATEWAY, root);
        DeviceNode child2 = new DeviceNode(macAddress1.increment(1), DeviceType.GATEWAY, root);

        root.setChildNodes(List.of(child1, child2));

        // when
        Tree tree = root.toTree();

        assertNotNull(tree);
        assertFalse(tree.children().isEmpty());
        assertEquals(2, tree.children().size());
        assertThat(tree.children()).containsExactlyInAnyOrder(new Tree(child1.toDevice(), Collections.emptySet() ), new Tree(child2.toDevice(), Collections.emptySet() ));
    }
}