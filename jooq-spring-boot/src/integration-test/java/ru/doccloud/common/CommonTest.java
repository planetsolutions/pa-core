package ru.doccloud.common;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.http.client.HttpClient;

import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.doccloud.webapp.WebApplication;

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

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(classes = {WebApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public abstract class CommonTest extends DockerComposeTest {

    @LocalServerPort
    public  int port;

    public static final String DEFAULT_USER = "boot";
    public static final String DEFAULT_PASS = "boot";

    @Autowired
    public DataSource dataSource;

    public Connection connection;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws SQLException {

        connection = dataSource.getConnection();

        ScriptUtils.executeSqlScript(connection, new ClassPathResource("clean_data.sql"));
        ScriptUtils.executeSqlScript(connection, new ClassPathResource("test_data.sql"));

    }


//    // TODO: 08.05.2020 specify acceptance criteria
//    public abstract void  assertCriteria(ObjectData myObject, String expectedObjId, String expectedParentId,
//                        String expectedName, String expectedPath, String expectedType, String expectedDesc );

    @After
    public void cleanUp(){
        try {
            if(connection != null)
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public JsonNode getJsonFromString(String strToJson){
        JsonNode node = null;

        try {
            node = mapper.readTree(strToJson);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return node;
    }

}
