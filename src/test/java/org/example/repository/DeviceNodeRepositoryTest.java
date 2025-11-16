package org.example.repository;

import org.example.api.DeviceType;
import org.example.model.DeviceNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.util.StringToMacConverter.convertToMac;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DeviceNodeRepositoryTest {

    @Autowired
    private DeviceNodeRepository repository;

    private DeviceNode gateway;
    private DeviceNode switch1;
    private DeviceNode ap1;
    private DeviceNode ap2;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        gateway = new DeviceNode(convertToMac("00:00:00:00:00:01"), DeviceType.GATEWAY, null);
        repository.save(gateway);

        switch1 = new DeviceNode(convertToMac("00:00:00:00:00:02"), DeviceType.SWITCH, gateway);
        repository.save(switch1);

        ap1 = new DeviceNode(convertToMac("00:00:00:00:00:03"), DeviceType.ACCESS_POINT, switch1);
        ap2 = new DeviceNode(convertToMac("00:00:00:00:00:04"), DeviceType.ACCESS_POINT, switch1);
        repository.saveAll(List.of(ap1, ap2));

        gateway = repository.findByMacAddress(gateway.getMacAddress()).get(0);
        switch1 = repository.findByMacAddress(switch1.getMacAddress()).get(0);
        ap1 = repository.findByMacAddress(ap1.getMacAddress()).get(0);
        ap2 = repository.findByMacAddress(ap2.getMacAddress()).get(0);
    }

    @Test
    void shouldFindAll() {
        // When
        List<DeviceNode> nodes = repository.findAll();

        // Then
        assertThat(nodes).hasSize(4);
        assertThat(nodes).containsExactlyInAnyOrder(gateway, switch1, ap1, ap2);
    }

    @Test
    void shouldFindByMacAddress() {
        // Given
        var uplinkMac = convertToMac("00:00:00:00:00:01");
        var nodeMac = convertToMac("00:00:00:00:00:02");

        // When
        List<DeviceNode> nodes = repository.findByMacAddress(nodeMac);

        // Then
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0).getMacAddress().toColonDelimitedString()).isEqualTo(nodeMac.toColonDelimitedString());
        assertThat(nodes.get(0).getDeviceType()).isEqualTo(DeviceType.SWITCH);
        assertThat(nodes.get(0).getUplinkDeviceNode().getMacAddress().toColonDelimitedString()).isEqualTo(uplinkMac.toColonDelimitedString());
    }

    @Test
    void shouldReturnEmptyListForNonExistentMacAddress() {
        // When
        List<DeviceNode> nodes = repository.findByMacAddress(convertToMac("AA:AA:AA:AA:AA:AA"));

        // Then
        assertThat(nodes).isEmpty();
    }

    @Test
    void shouldFindAllOrderByDeviceTypeOrder() {
        // Given
        var expectedNodes = List.of(gateway, switch1, ap1, ap2);
        var expectedTypes = expectedNodes.stream().map(DeviceNode::getDeviceType).toList();

        // When
        List<DeviceNode> orderedNodes = repository.findAllOrderByDeviceTypeOrder();

        // Then
        assertThat(orderedNodes).hasSize(4);
        assertThat(orderedNodes).containsExactlyElementsOf(expectedNodes);

        List<DeviceType> orderedTypes = orderedNodes.stream()
                .map(DeviceNode::getDeviceType)
                .toList();
        assertThat(orderedTypes).containsExactlyElementsOf(expectedTypes);
    }

    @Test
    void shouldFindAllRoots() {
        // When
        List<DeviceNode> roots = repository.findAllRoots();

        // Then
        assertThat(roots).hasSize(1);
        assertThat(roots.get(0).getMacAddress().toColonDelimitedString()).isEqualTo(gateway.getMacAddress().toColonDelimitedString());
    }

    @Test
    void shouldFindUniqueByMacAddressWhenUnique() {
        // When
        Optional<DeviceNode> foundNode = repository.findUniqueByMacAddress(convertToMac("00:00:00:00:00:04"));

        // Then
        assertThat(foundNode).isPresent();
        assertThat(foundNode.get()).isEqualTo(ap2);
    }

    @Test
    void shouldReturnEmptyForNonExistentMacInUniqueFinder() {
        // When
        Optional<DeviceNode> foundNode = repository.findUniqueByMacAddress(convertToMac("BB:BB:BB:BB:BB:BB"));

        // Then
        assertThat(foundNode).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenDuplicateIsAdded() {
        // Given
        DeviceNode duplicate = new DeviceNode(convertToMac("00:00:00:00:00:01"), DeviceType.ACCESS_POINT, null);

        // When / Then
        assertThatThrownBy(() -> repository.save(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}