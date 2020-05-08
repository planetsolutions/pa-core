package ru.doccloud.docs;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import ru.doccloud.common.CommonTest;

public abstract class CommonDocTest extends CommonTest {
    @Override
    public void assertCriteria(ObjectData myObject, String expectedObjId, String expectedParentId, String expectedName, String expectedPath, String expectedType, String expectedDesc) {
        // TODO: 08.05.2020 implement this for documents and system
    }
}
