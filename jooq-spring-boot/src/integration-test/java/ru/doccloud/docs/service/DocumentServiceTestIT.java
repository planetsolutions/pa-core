package ru.doccloud.docs.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.docs.CommonDocTest;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.webapp.WebApplication;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(classes = {WebApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class DocumentServiceTestIT extends CommonDocTest {
    @Autowired
    private DocumentCrudService<DocumentDTO> documentService;

    @Test(expected = DocumentNotFoundException.class)
    public void getDocByPath_whenNotFound_thenDocNotFoundException(){
        documentService.findByPath("/sdfsd/sdfds/dsfds/fs");
    }

    @Test
    public void getDocByPath_when_NotNullPath_thenDocReturned(){

        String path = "/child_1/child_name_3/child_folder";

        List<DocumentDTO> documents = documentService.findByPath(path);

        assertNotNull(documents);
        assertEquals(2, documents.size());
        assertEquals("child_folder", documents.get(0).getTitle());
        assertEquals("folder", documents.get(0).getBaseType());
        assertEquals("9936c332-f410-487d-9903-7d25083b489a", documents.get(0).getUuid().toString());
        assertEquals("b2f54af5-ca24-4724-a971-db1493802bda", documents.get(0).getParent().toString());

        assertEquals("child_name_3", documents.get(1).getTitle());
        assertEquals("folder", documents.get(1).getBaseType());
        assertEquals("b2f54af5-ca24-4724-a971-db1493802bda", documents.get(1).getUuid().toString());
        assertEquals("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e", documents.get(1).getParent().toString());

    }

    @Test
    public void getDocByPath_whenTwoDocsFound_thenDocumentTypeReturned(){

        String path = "/child_1/child_name/child_same_name_1";

        List<DocumentDTO> documents = documentService.findByPath(path);

        assertNotNull(documents);
        assertEquals(2, documents.size());
        assertEquals("child_same_name_1", documents.get(0).getTitle());
        assertEquals("document", documents.get(0).getBaseType());
        assertEquals("0841d456-0eea-4409-8e87-d80707c36a89", documents.get(0).getUuid().toString());
        assertEquals("3f4a17ca-2200-4bb0-b7fe-973bd715baf7", documents.get(0).getParent().toString());

        assertEquals("child_name", documents.get(1).getTitle());
        assertEquals("folder", documents.get(1).getBaseType());
        assertEquals("3f4a17ca-2200-4bb0-b7fe-973bd715baf7", documents.get(1).getUuid().toString());
        assertEquals("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e", documents.get(1).getParent().toString());
    }
}
