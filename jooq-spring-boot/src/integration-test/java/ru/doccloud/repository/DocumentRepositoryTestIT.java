package ru.doccloud.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.doccloud.common.CommonTest;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.document.model.Document;
import ru.doccloud.webapp.WebApplication;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(classes = {WebApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class DocumentRepositoryTestIT extends CommonTest {

    @Autowired
    DocumentRepository<Document> documentRepository;

//    @Test(expected = DocumentNotFoundException.class)
//    public void getDocByPath_whenNotFound_thenDocNotFoundException(){
//        documentRepository.findByPath("/sdfsd/sdfds/dsfds/fs");
//    }

    @Test
    public void getDocByPath_when_NotNullPath_thenDocReturned(){

        String path = "/child_1/child_name_3/child_folder";

        Document document = documentRepository.findByPath(path);

        assertNotNull(document);

        // TODO: 04.05.2020 add testCriteria
    }

//    @Test
//    public void getDocByPath_whenTwoDocsFound_thenDocumentTypeReturned(){
//
//        String path = "/child_1/child_name_3/child_folder";
//
//        Document document = documentRepository.findByPath(path);
//
//        assertNotNull(document);
//        assertEquals("document", document.getBaseType());
//
//        // TODO: 04.05.2020 add testCriteria
//    }

}
