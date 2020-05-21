package ru.doccloud.cmis;


import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.junit.Test;
import java.util.UUID;

import static org.junit.Assert.*;

public class CmisGetObjectTestIT extends CmisTest {

    @Test(expected = CmisRuntimeException.class)
    public void getObjectByRandomId_thenException(){
        final String objectId = UUID.randomUUID().toString();

        provider.getObjectService().getObject(REPOSITORY_NAME, objectId,
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);
    }

    // TODO: 29.04.2020 add test for objectId = null, for this case cmis will automatically redirected request to getObjectByPath with root path /

    @Test
    public void getObjectByZeroID_thenRootDocReturned(){
        final String objectId = "0";

        ObjectData myObject = provider.getObjectService().getObject(REPOSITORY_NAME, objectId,
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);
        assertNotNull(myObject);
        assertEquals(ROOT_ID, myObject.getId());
    }

    @Test
    public void getObjectById_thenObjectReturned(){
         String objectId = "1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e";

        ObjectData objectData = provider.getObjectService().getObject(REPOSITORY_NAME, objectId,
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);

        assertCriteria(objectData, objectId, "00000000-0000-0000-0000-000000000000", "child_1", "child_1", "tenant", "cmis child folder");

        objectId = "3f4a17ca-2200-4bb0-b7fe-973bd715baf7";
        objectData = provider.getObjectService().getObject(REPOSITORY_NAME, objectId,
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);

        assertCriteria(objectData, objectId, "1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e", "child_name", "child_name", "treeroot", "child 1 of child_1");

        objectId = "e3fefcc7-d6f1-479f-a844-4b8546753042";
        objectData = provider.getObjectService().getObject(REPOSITORY_NAME, objectId,
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);

        assertCriteria(objectData, objectId, "1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e", "child_name", "child_name", "treeroot", "child 2 of child_1");

//  for document type we don't return neither parent nor path
//        objectId = "0841d456-0eea-4409-8e87-d80707c36a89";
//        objectData = provider.getObjectService().getObject(REPOSITORY_NAME, objectId,
//                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);
////
//        assertCriteria(objectData, objectId, "3f4a17ca-2200-4bb0-b7fe-973bd715baf7", "child_doc_name_1", "child_doc_name_1", "document", "child_name_1_1_1");

    }










}
