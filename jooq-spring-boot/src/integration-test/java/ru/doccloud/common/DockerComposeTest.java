package ru.doccloud.common;

import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

public abstract class DockerComposeTest {
    public static DockerComposeContainer compose ;

    static {
        compose = new DockerComposeContainer(
                new File("src/integration-test/resources/docker-compose-test.yml"))
                .withExposedService("hazelcast-test", 5701)
                .withExposedService("hazelcast-man-test", 8080)
                .withExposedService("docclouddb-test", 5432);
        compose.start();
    }

}
