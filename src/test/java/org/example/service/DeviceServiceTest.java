package org.example.service;

import inet.ipaddr.mac.MACAddress;
import org.example.api.Device;
import org.example.api.DeviceType;
import org.example.api.Tree;
import org.example.exception.NodeNotFound;
import org.example.exception.UplinkNotFound;
import org.example.model.DeviceNode;
import org.example.repository.DeviceNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.util.StringToMacConverter.convertToMac;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceNodeRepository deviceNodeRepository;

    @InjectMocks
    private DeviceService deviceService;

    private MACAddress mac1;
    private MACAddress mac2;
    private Device device1;
    private Device device2;
    private DeviceNode deviceNode1;
    private DeviceNode deviceNode2;

    @BeforeEach
    void setUp() {
        mac1 = convertToMac("00:1A:2B:3C:4D:5E");
        mac2 = convertToMac("0A:1A:2B:3C:4D:5E");

        deviceNode1 = new DeviceNode(mac1, DeviceType.GATEWAY, null);
        deviceNode2 = new DeviceNode(mac2, DeviceType.SWITCH, deviceNode1);

        device1 = deviceNode1.toDevice();
        device2 = deviceNode2.toDevice();
    }

    @Test
    void shouldCreateDeviceWithoutUplink() {
        // when
        deviceService.createDevice(device1);

        // then
        verify(deviceNodeRepository, times(1)).save(eq(deviceNode1));
    }

    @Test
    void shouldCreateDeviceWithUplink() {
        // given
        when(deviceNodeRepository.findUniqueByMacAddress(mac1)).thenReturn(Optional.of(deviceNode1));

        // when
        deviceService.createDevice(device2);

        // then
        verify(deviceNodeRepository).findUniqueByMacAddress(mac1);
        verify(deviceNodeRepository, times(1)).save(eq(deviceNode2));
    }

    @Test
    void shouldThrowUplinkNotFound() {
        // given
        when(deviceNodeRepository.findUniqueByMacAddress(mac1)).thenReturn(Optional.empty());

        // when then
        Exception exception = assertThrows(UplinkNotFound.class, () -> {
            deviceService.createDevice(device2);
        });

        verify(deviceNodeRepository, never()).save(any(DeviceNode.class));
    }

    @Test
    void shouldReturnDevice() {
        // given
        when(deviceNodeRepository.findUniqueByMacAddress(mac1)).thenReturn(Optional.of(deviceNode1));

        // when
        Device result = deviceService.getDevice(mac1);

        // then
        assertEquals(result, device1);
    }

    @Test
    void shouldThrowNodeNotFound() {
        // given
        when(deviceNodeRepository.findUniqueByMacAddress(mac1)).thenReturn(Optional.empty());

        // when then
        Exception exception = assertThrows(NodeNotFound.class, () -> {
            deviceService.getDevice(mac1);
        });
    }

    @Test
    void shouldReturnDevices() {
        // given
        when(deviceNodeRepository.findAllOrderByDeviceTypeOrder()).thenReturn(List.of(deviceNode1, deviceNode2));

        // when
        List<Device> result = deviceService.getDevices();

        // then
        assertThat(result).containsExactly(device1, device2);
    }

    @Test
    void shouldReturnEmptyDevicesList() {
        // given
        when(deviceNodeRepository.findAllOrderByDeviceTypeOrder()).thenReturn(Collections.emptyList());

        // when
        List<Device> result = deviceService.getDevices();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTree() {
        // given
        when(deviceNodeRepository.findAllRoots()).thenReturn(List.of(deviceNode1));
        when(deviceNodeRepository.findById(any())).thenReturn(Optional.of(deviceNode1));

        // when
        Set<Tree> result = deviceService.getTree();

        // then
        assertNotNull(result);
        assertThat(result).contains(deviceNode1.toTree());
    }

    @Test
    void shouldReturnEmptyTreeSet() {
        // given
        when(deviceNodeRepository.findAllRoots()).thenReturn(Collections.emptyList());

        // when
        Set<Tree> result = deviceService.getTree();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTreeForGivenMac() {
        // given
        when(deviceNodeRepository.findUniqueByMacAddress(mac1)).thenReturn(Optional.of(deviceNode1));

        // when
        Tree result = deviceService.getTree(mac1);

        // then
        assertEquals(deviceNode1.toTree(), result);
    }

    @Test
    void shouldReturnEmptyTreeSetForGivenMac() {
        // given
        when(deviceNodeRepository.findUniqueByMacAddress(mac1)).thenReturn(Optional.empty());

        // when / then
        assertThrows(NodeNotFound.class, () -> {
            deviceService.getTree(mac1);
        });
    }
}