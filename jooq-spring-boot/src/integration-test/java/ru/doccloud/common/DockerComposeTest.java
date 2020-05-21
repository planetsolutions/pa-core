package ru.doccloud.common;

import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;

public abstract class DockerComposeTest {
    public static DockerComposeContainer compose ;

    static {
        compose = new DockerComposeContainer(
                new File("src/integration-test/resources/docker-compose-test.yml"))
                .withExposedService("hazelcast-test", 5701)
                .withExposedService("docclouddb-test", 5432,
                        Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30))
                );
        compose.start();
    }

}
