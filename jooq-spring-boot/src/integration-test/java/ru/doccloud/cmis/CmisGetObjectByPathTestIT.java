package ru.doccloud.cmis;


import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CmisGetObjectByPathTestIT extends CmisTest {

    @Test
    public void getObjectByPath_whenPathBlank_thenRootDocReturned(){
        ObjectData myObject = provider.getObjectService().getObjectByPath(REPOSITORY_NAME, null,
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);
        assertNotNull(myObject);
        assertEquals(ROOT_ID, myObject.getId());
    }

    @Test
    public void getObjectByPath_whenRootPath_thenRootDocReturned(){
        ObjectData myObject = provider.getObjectService().getObjectByPath(REPOSITORY_NAME, "/",
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);
        assertNotNull(myObject);
        assertEquals(ROOT_ID, myObject.getId());
    }

    @Test(expected = CmisRuntimeException.class)
    public void getObjectByPath_whenNonExistingPath_thenCmisRuntimeException(){
        ObjectData myObject = provider.getObjectService().getObjectByPath(REPOSITORY_NAME, "fdsfsfdvxcv",
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);
        assertNotNull(myObject);
    }
//
    @Test
    public void getObjectByPath_whenObjectFound_thenObjectReturned(){
//      try to path folder
        String path = "/child_1/child_name_3/child_folder";

        ObjectData objectData = provider.getObjectService().getObjectByPath(REPOSITORY_NAME, path,
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);
        assertCriteria(objectData, "9936c332-f410-487d-9903-7d25083b489a",
                "b2f54af5-ca24-4724-a971-db1493802bda", "child_folder", "child_folder", "folder", "child folder of child_name_3");
    }

    @Test
    public void getObjectByPath_whenTwoObjectFound_thenDocTypeReturned(){
//      try to path folder
        String path = "/child_1/child_name/child_same_name_1";

        ObjectData objectData = provider.getObjectService().getObjectByPath(REPOSITORY_NAME, path,
                "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);
        assertCriteria(objectData, "0841d456-0eea-4409-8e87-d80707c36a89",
                "3f4a17ca-2200-4bb0-b7fe-973bd715baf7", "child_same_name_1", "child_same_name_1", "cmis:document", "child_name_1_1_1");

    }










}
