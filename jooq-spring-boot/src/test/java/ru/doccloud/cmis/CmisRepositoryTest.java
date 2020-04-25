package ru.doccloud.cmis;


import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;


public class CmisRepositoryTest extends CmisTest {


    @Test(expected = CmisRuntimeException.class)
    public void getObjectByRandomId_theObjectNotFound(){
        final String objectId = UUID.randomUUID().toString();

        ObjectData myObject = provider.getObjectService().getObject(REPOSITORY_NAME, objectId,
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);
    }

    @Test
    public void getObjectById_theObjectReturned(){
        final String objectId = "0ff729c3-3b26-463f-8006-2fd79bdc124a";

            ObjectData myObject = provider.getObjectService().getObject(REPOSITORY_NAME, objectId,
                    "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);

        assertNotNull(myObject);
        assertEquals(objectId, myObject.getId());

        assertNotNull(myObject.getProperties().getProperties().get("cmis:name").getValues());

        final String expectedName = "Documents";
        assertEquals(expectedName, myObject.getProperties().getProperties().get("cmis:name").getValues().get(0));
    }










}
