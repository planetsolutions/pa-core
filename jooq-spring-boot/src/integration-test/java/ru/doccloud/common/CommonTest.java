package ru.doccloud.common;


import org.apache.http.client.HttpClient;

import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CommonTest extends DockerComposeTest {

    @LocalServerPort
    public  int port;

    public  HttpClient httpClient;
    public  String jwtToken;

    public static final String DEFAULT_USER = "boot";
    public static final String DEFAULT_PASS = "boot";

    @Autowired
    public DataSource dataSource;

    public Connection connection;

    public void init() throws SQLException {
        httpClient = HttpClientBuilder.create().build();
        jwtToken = JWTMock.getJWT(DEFAULT_USER);
        connection = dataSource.getConnection();
//        try (Stream<Path> walk = Files.walk(Paths.get("/home/ilya/planeta_arcive_new/pa-core/jooq-spring-boot/target/test-classes"))) {
//
//            List<String> result = walk.filter(Files::isRegularFile)
//                    .map(Path::toString).collect(Collectors.toList());
//
//            result.forEach(System.out::println);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        ScriptUtils.executeSqlScript(connection, new ClassPathResource("clean_data.sql"));
        ScriptUtils.executeSqlScript(connection, new ClassPathResource("test_data.sql"));

    }

    @After
    public void cleanUp(){
        try {
            if(connection != null)
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
