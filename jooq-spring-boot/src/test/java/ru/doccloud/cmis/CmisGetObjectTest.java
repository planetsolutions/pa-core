package ru.doccloud.cmis;


import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class CmisGetObjectTest extends CmisTest {

    @Test(expected = CmisRuntimeException.class)
    public void getObjectByRandomId_thenException(){
        final String objectId = UUID.randomUUID().toString();

        provider.getObjectService().getObject(REPOSITORY_NAME, objectId,
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);
    }

    @Test
    public void getObjectByNullID_thenRootDocReturned(){
        final String objectId = null;

        ObjectData myObject = provider.getObjectService().getObject(REPOSITORY_NAME, objectId,
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);
        assertNotNull(myObject);
        assertEquals(ROOT_ID, myObject.getId());

        assertNotNull(myObject.getProperties().getProperties().get(objectNameKey).getValues());

        final String expectedName = "Root folder";
        assertEquals(expectedName, myObject.getProperties().getProperties().get(objectNameKey).getValues().get(0));


        assertNotNull(myObject.getProperties().getProperties().get(parentKey).getValues());
        assertEquals(0, myObject.getProperties().getProperties().get(parentKey).getValues().size());

        assertNotNull(myObject.getProperties().getProperties().get(pathKey).getValues());

        final String expectedPath = "Root folder";
        assertEquals(expectedPath, myObject.getProperties().getProperties().get(pathKey).getValues().get(0));
    }

    @Test
    public void getObjectById_thenObjectReturned(){
        final String objectId = "0ff729c3-3b26-463f-8006-2fd79bdc124a";

            ObjectData myObject = provider.getObjectService().getObject(REPOSITORY_NAME, objectId,
                    "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);

        assertNotNull(myObject);
        assertEquals(objectId, myObject.getId());

        assertNotNull(myObject.getProperties().getProperties().get(objectNameKey).getValues());

        final String expectedName = "Documents";
        assertEquals(expectedName, myObject.getProperties().getProperties().get(objectNameKey).getValues().get(0));


        assertNotNull(myObject.getProperties().getProperties().get(parentKey).getValues());

        final String expectedParentId = "ce4feb70-aa7b-4cf3-90f0-45d1caef62fd";
        assertEquals(expectedParentId, myObject.getProperties().getProperties().get(parentKey).getValues().get(0));

        assertNotNull(myObject.getProperties().getProperties().get(pathKey).getValues());

        final String expectedPath = "Documents";
        assertEquals(expectedPath, myObject.getProperties().getProperties().get(pathKey).getValues().get(0));
    }










}
