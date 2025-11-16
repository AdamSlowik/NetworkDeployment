package org.example.config;

import org.example.config.converters.MacAddressPropertyConverter;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.connectors.HttpConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.convert.Neo4jConversions;

import java.io.File;
import java.util.Set;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

@Configuration
public class Neo4JConfig {
    private static final File DATABASE_DIRECTORY = new File("data/NetworkDeployment");

    @Bean
    public Neo4jConversions neo4jConversions() {
        return new Neo4jConversions(
                Set.of(
                        new MacAddressPropertyConverter()
                )
        );
    }

    @Bean(destroyMethod = "shutdown")
    public DatabaseManagementService databaseManagementService() {
        return new DatabaseManagementServiceBuilder(DATABASE_DIRECTORY.toPath())
//                .setConfig(GraphDatabaseSettings.pagecache_memory, 536870912L)
                .setConfig(GraphDatabaseSettings.auth_enabled, false)
                .setConfig(BoltConnector.enabled, true)
                .setConfig(BoltConnector.listen_address, new SocketAddress("localhost", 7687))
                .setConfig(HttpConnector.enabled, true)
                .setConfig(HttpConnector.listen_address, new SocketAddress("localhost", 7474))

                .build();
    }

    @Bean
    public GraphDatabaseService graphDatabaseService(DatabaseManagementService managementService) {
        var db = managementService.database(DEFAULT_DATABASE_NAME);
        String createConstraint = "CREATE CONSTRAINT unique_mac IF NOT EXISTS FOR (n:DeviceNode) REQUIRE n.macAddress IS UNIQUE";
        String cleanDb = "MATCH (n) OPTIONAL MATCH (n)-[r]-() DETACH DELETE n, r";

        db.executeTransactionally(cleanDb);
        db.executeTransactionally(createConstraint);

        return managementService.database(DEFAULT_DATABASE_NAME);
    }
}
