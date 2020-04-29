package ru.doccloud.common;

import org.junit.ClassRule;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

public class DockerComposeTest {
    @ClassRule
    public static DockerComposeContainer compose =
            new DockerComposeContainer(
                    new File("src/integration-test/resources/docker-compose-test.yml"))
                    .withExposedService("hazelcast-test", 5701)
//            .withExposedService("hazelcast-man-test", 8088)
            .withExposedService("docclouddb-test", 5432);
}
