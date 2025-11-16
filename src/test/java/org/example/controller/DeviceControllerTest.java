package org.example.controller;

import inet.ipaddr.mac.MACAddress;
import org.example.api.Device;
import org.example.api.DeviceType;
import org.example.model.DeviceNode;
import org.example.repository.DeviceNodeRepository;
import org.example.util.StringToMacConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DeviceControllerTest {

    private final TestRestTemplate restTemplate;
    private final DeviceNodeRepository repository;

    @Autowired
    DeviceControllerTest(TestRestTemplate restTemplate, DeviceNodeRepository repository) {
        this.restTemplate = restTemplate;
        this.repository = repository;
    }

    @LocalServerPort
    private int port;
    private String url;
    private MACAddress mac = StringToMacConverter.convertToMac("aa:bb:cc:dd:ee:ff");

    private MACAddress mac2 = StringToMacConverter.convertToMac("a0:bb:cc:dd:ee:ff");

    @BeforeEach
    public void setUp() {
        url = "http://localhost:" + port + "/api/device";
        repository.deleteAll();
    }


    @Test
    public void shouldCreateNodeWithoutParent() {
        // given
        var device = new Device(mac, DeviceType.GATEWAY, Optional.empty());

        // when
        var result = restTemplate.postForEntity(url, device, Void.class);
        var result2 = restTemplate.getForEntity(url + "/" + mac.toColonDelimitedString(), Device.class);

        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result2.getBody()).isEqualTo(device);
    }

    @Test
    public void shouldCreateNodeWithParent() {
        // given
        var parentDevice = new Device(mac, DeviceType.GATEWAY, Optional.empty());
        var childDevice = new Device(mac2, DeviceType.GATEWAY, Optional.of(mac));
        restTemplate.postForEntity(url, parentDevice, Void.class);

        // when
        var result = restTemplate.postForEntity(url, childDevice, Void.class);
        var result2 = restTemplate.getForEntity(url + "/" + mac2.toColonDelimitedString(), Device.class);

        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result2.getBody()).isEqualTo(childDevice);
    }

    @Test
    public void shouldReturnAllDevicesInOrderedFashion() {
        // given
        var createdDevices = createNetworkDeployment().stream().map(DeviceNode::toDevice).toList();
        var createdTypes = createdDevices.stream().map(Device::deviceType).toList();

        // when
        var result = restTemplate.getForEntity(url, Device[].class);

        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactlyInAnyOrderElementsOf(createdDevices);
        assertThat(Arrays.stream(result.getBody()).map(Device::deviceType)).containsExactlyElementsOf(createdTypes);
    }

    @Test
    public void shouldReturnConflictOnDuplicateCreation() {
        // given
        var device = new Device(mac, DeviceType.GATEWAY, Optional.empty());

        // when
        var result = restTemplate.postForEntity(url, device, Void.class);
        var result2 = restTemplate.postForEntity(url, device, Void.class);

        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void shouldReturnBadRequestWhenMacIsInvalid() {
        // given
        String rawJsonBody = "{\n" +
                "    \"macAddress\": \"BAD_MAC_aa:bb:cc:dd:ee:ff\",\n" +
                "    \"deviceType\": \"SWITCH\"" +
                "\n}";

        // when / then
        restTemplate.execute(
                url,
                HttpMethod.POST,
                request -> {
                    new StringHttpMessageConverter().write(rawJsonBody, MediaType.APPLICATION_JSON, request);
                },
                response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    return new StringHttpMessageConverter().read(String.class, response);
                }
        );
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenUplinkNotFound() {
        // given
        var device = new Device(mac, DeviceType.GATEWAY, Optional.of(mac2));

        // when
        var result = restTemplate.postForEntity(url, device, Void.class);

        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void shouldReturnNotFoundWhenDeviceIsMissing() {
        // when
        var result = restTemplate.getForEntity(url + "/" + mac.toColonDelimitedString(), Void.class);

        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private List<DeviceNode> createNetworkDeployment() {
        var root1 = new DeviceNode(new Device(mac, DeviceType.GATEWAY, Optional.empty()), null);
        var child1 = new DeviceNode(new Device(mac.increment(1), DeviceType.SWITCH, Optional.of(root1.getMacAddress())), root1);
        var child2 = new DeviceNode(new Device(mac.increment(2), DeviceType.ACCESS_POINT, Optional.of(child1.getMacAddress())), child1);
        var child3 = new DeviceNode(new Device(mac.increment(3), DeviceType.ACCESS_POINT, Optional.of(root1.getMacAddress())), root1);

        var root2 = new DeviceNode(new Device(mac2, DeviceType.GATEWAY, Optional.empty()), null);

        var allDevices = List.of(root1, root2, child1, child2, child3);

        repository.saveAll(allDevices);

        return allDevices;
    }
}