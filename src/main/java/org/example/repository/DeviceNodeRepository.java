package org.example.repository;

import inet.ipaddr.mac.MACAddress;
import org.example.model.DeviceNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface DeviceNodeRepository extends Neo4jRepository<DeviceNode, Long> {
    List<DeviceNode> findByMacAddress(MACAddress macAddress);

    @Query("MATCH (n:DeviceNode) \n" +
            "OPTIONAL MATCH (n)-[r:UPLINK]->(u:DeviceNode) \n" +
            "WITH n, u, r \n" +
            "ORDER BY CASE n.deviceType \n" +
            "WHEN 'GATEWAY' THEN 1 \n" +
            "WHEN 'SWITCH' THEN 2 \n" +
            "WHEN 'ACCESS_POINT' THEN 3 \n" +
            "ELSE 99 END \n" +
            "RETURN n, u, r")
    List<DeviceNode> findAllOrderByDeviceTypeOrder();

    @Query("match (n)\n" +
            "where not ((n)-[:UPLINK]->())\n" +
            "return n")
    List<DeviceNode> findAllRoots();

    default Optional<DeviceNode> findUniqueByMacAddress(MACAddress macAddress) {
        var nodes = findByMacAddress(macAddress);
        if (nodes.size() == 1) {
            return Optional.of(nodes.get(0));
        }
        if (nodes.isEmpty()) {
            return Optional.empty();
        } else {
            // db constraint is violated. Somebody tampered with db?
            throw new RuntimeException("MAC is not unique");
        }
    }
}
