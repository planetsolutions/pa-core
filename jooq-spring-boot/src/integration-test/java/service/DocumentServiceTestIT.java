package service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.doccloud.common.CommonTest;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.document.model.Document;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.document.dto.DocumentDTO;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DocumentServiceTestIT extends CommonTest {
    @Autowired
    private DocumentCrudService<DocumentDTO> documentService;

    @Test(expected = DocumentNotFoundException.class)
    public void getDocByPath_whenNotFound_thenDocNotFoundException(){
        documentService.findByPath("/sdfsd/sdfds/dsfds/fs");
    }

    @Test
    public void getDocByPath_when_NotNullPath_thenDocReturned(){

        String path = "/child_1/child_name_3/child_folder";

        List<DocumentDTO> document = documentService.findByPath(path);

        assertNotNull(document);

        // TODO: 04.05.2020 add testCriteria
    }

    @Test
    public void getDocByPath_whenTwoDocsFound_thenDocumentTypeReturned(){

        String path = "/child_1/child_name_3/child_folder";

        List<DocumentDTO> document = documentService.findByPath(path);

        assertNotNull(document);
//        assertEquals("document", document.getBaseType());

        // TODO: 04.05.2020 add testCriteria
    }
}
