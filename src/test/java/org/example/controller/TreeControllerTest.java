package org.example.controller;

import inet.ipaddr.mac.MACAddress;
import org.example.api.Device;
import org.example.api.DeviceType;
import org.example.api.Tree;
import org.example.model.DeviceNode;
import org.example.repository.DeviceNodeRepository;
import org.example.util.StringToMacConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.*;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TreeControllerTest {
    private final TestRestTemplate restTemplate;
    private final DeviceNodeRepository repository;

    @Autowired
    TreeControllerTest(TestRestTemplate restTemplate, DeviceNodeRepository repository) {
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
        url = "http://localhost:" + port + "/api/tree";
        repository.deleteAll();
    }

    @Test
    public void shouldReturnAllDevicesInTreeForm() {
        // given
        var trees = createNetworkDeployment();

        // when
        var result = restTemplate.getForEntity(url, Tree[].class);

        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactlyInAnyOrderElementsOf(trees);
    }

    @Test
    public void shouldReturnTreeForAGivenRoot() {
        // given
        var child1Mac = mac.increment(1);
        var child1Tree = createNetworkDeployment().stream()
                .filter(t->t.node().macAddress().equals(mac))
                .map(t->t.children().stream()
                        .filter(t2->t2.node().macAddress().equals(child1Mac))
                        .findAny())
                .findAny()
                .flatMap(it->it)
                .orElseThrow();

        // when
        var result = restTemplate.getForEntity(url + "/" + child1Mac.toColonDelimitedString(), Tree.class);

        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(child1Tree);
    }

    private Set<Tree> createNetworkDeployment() {
        //              (root1)                    (root2)
        //                  |
        //     _____________|______________
        //     |                           |
        //  (child1)                    (child3)
        //     |
        //  (child2)
        var root1 = new DeviceNode(new Device(mac, DeviceType.GATEWAY, Optional.empty()), null);
        var child1 = new DeviceNode(new Device(mac.increment(1), DeviceType.SWITCH, Optional.of(root1.getMacAddress())), root1);
        var child2 = new DeviceNode(new Device(mac.increment(2), DeviceType.ACCESS_POINT, Optional.of(child1.getMacAddress())), child1);
        var child3 = new DeviceNode(new Device(mac.increment(3), DeviceType.ACCESS_POINT, Optional.of(root1.getMacAddress())), root1);

        var root2 = new DeviceNode(new Device(mac2, DeviceType.GATEWAY, Optional.empty()), null);

        var allDevices = List.of(root1, root2, child1, child2, child3);

        repository.saveAll(allDevices);

        var subtree = new Tree(child1.toDevice(), Set.of(new Tree(child2.toDevice(), emptySet())));
        var tree1 = new Tree(root1.toDevice(), Set.of(subtree, new Tree(child3.toDevice(), emptySet())));
        var tree2 = new Tree(root2.toDevice(), emptySet());
        return Set.of(tree2, tree1);
    }
}