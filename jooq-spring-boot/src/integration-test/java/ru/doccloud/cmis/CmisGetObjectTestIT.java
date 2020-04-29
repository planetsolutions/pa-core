package ru.doccloud.cmis;


import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.doccloud.common.JWTMock;
import ru.doccloud.webapp.WebApplication;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(classes = {WebApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class CmisGetObjectTestIT extends CmisTest {

    @Test(expected = CmisRuntimeException.class)
    public void getObjectByRandomId_thenException(){
        final String objectId = UUID.randomUUID().toString();

        provider.getObjectService().getObject(REPOSITORY_NAME, objectId,
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);
    }

    // TODO: 29.04.2020 add test for objectId = null, for this case cmis will automatically redirected request to getObjectByPath with root path /

    @Before
    public void setUp() throws SQLException {
        super.setUp();

    }

    @Test
    public void getObjectByZeroID_thenRootDocReturned(){
        final String objectId = "0";

        ObjectData myObject = provider.getObjectService().getObject(REPOSITORY_NAME, objectId,
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);
        assertNotNull(myObject);
        assertEquals(ROOT_ID, myObject.getId());

//        assertNotNull(myObject.getProperties().getProperties().get(objectNameKey).getValues());
//
//        final String expectedName = "Root folder";
//        assertEquals(expectedName, myObject.getProperties().getProperties().get(objectNameKey).getValues().get(0));
//
//
//        assertNotNull(myObject.getProperties().getProperties().get(parentKey).getValues());
//        assertEquals(0, myObject.getProperties().getProperties().get(parentKey).getValues().size());
//
//        assertNotNull(myObject.getProperties().getProperties().get(pathKey).getValues());
//
//        final String expectedPath = "Root folder";
//        assertEquals(expectedPath, myObject.getProperties().getProperties().get(pathKey).getValues().get(0));
    }

    @Test
    public void getObjectById_thenObjectReturned(){
        final String objectId = "5d05e877-a493-4fc9-9e5f-8fefaa84b437";

            ObjectData myObject = provider.getObjectService().getObject(REPOSITORY_NAME, objectId,
                    "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);

        assertNotNull(myObject);
        assertEquals(objectId, myObject.getId());

        assertNotNull(myObject.getProperties().getProperties().get(objectNameKey).getValues());

        final String expectedName = "Тенант";
        assertEquals(expectedName, myObject.getProperties().getProperties().get(objectNameKey).getValues().get(0));


        assertNotNull(myObject.getProperties().getProperties().get(parentKey).getValues());

        final String expectedParentId = "00000000-0000-0000-0000-000000000000";
        assertEquals(expectedParentId, myObject.getProperties().getProperties().get(parentKey).getValues().get(0));

        assertNotNull(myObject.getProperties().getProperties().get(pathKey).getValues());

        final String expectedPath = "Тенант";
        assertEquals(expectedPath, myObject.getProperties().getProperties().get(pathKey).getValues().get(0));
    }










}
