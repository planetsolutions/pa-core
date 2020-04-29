package ru.doccloud.cmis;


import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.doccloud.webapp.WebApplication;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(classes = {WebApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class CmisGetObjectByPathTestIT extends CmisTest {

    @Test(expected = CmisInvalidArgumentException.class)
    public void getObjectByPath_whenPathBlank_thenCmisInvalidArgumentException(){
        // TODO: 25.04.2020 implement test
    }

    @Test(expected = CmisInvalidArgumentException.class)
    public void getObjectByPath_whenInvalidPath_thenCmisInvalidArgumentException(){
        // TODO: 25.04.2020 implement test
    }

    @Test
    public void getObjectByPath_thenObjectReturned(){
        final String path = "/Test Tenant2/New App/Documents/Test_1";

            ObjectData myObject = provider.getObjectService().getObjectByPath(REPOSITORY_NAME, path,
                    "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);
        // TODO: 25.04.2020 add test criteria
        assertNotNull(myObject);
        final String expectedObjId = "8c755855-5159-49cf-8ed8-84e1cb30d17f";
        assertEquals(expectedObjId, myObject.getId());
    }










}
