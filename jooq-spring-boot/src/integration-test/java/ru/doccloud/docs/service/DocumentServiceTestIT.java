package ru.doccloud.docs.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.docs.CommonDocTest;
import ru.doccloud.document.model.Document;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.document.dto.DocumentDTO;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

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

        List<String> fields = Stream.of("title", "baseType", "uuid", "parent").collect(Collectors.toList());
        List<Object> values = Stream.of("child_folder", "folder",
                UUID.fromString("9936c332-f410-487d-9903-7d25083b489a"),
                UUID.fromString("b2f54af5-ca24-4724-a971-db1493802bda"))
                .collect(Collectors.toList());

        assertCriteria(documents.get(0), buildExpectedValuesMap(fields, values));

        values = Stream.of("child_name_3", "folder",
                UUID.fromString("b2f54af5-ca24-4724-a971-db1493802bda"),
                UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"))
                .collect(Collectors.toList());

        assertCriteria(documents.get(1), buildExpectedValuesMap(fields, values));

    }

    @Test
    public void getDocByPath_whenTwoDocsFound_thenDocumentTypeReturned(){

        String path = "/child_1/child_name/child_same_name_1";

        List<DocumentDTO> documents = documentService.findByPath(path);

        assertNotNull(documents);
        List<String> fields = Stream.of("title", "baseType", "uuid", "parent").collect(Collectors.toList());
        List<Object> values = Stream.of("child_same_name_1", "document",
                UUID.fromString("0841d456-0eea-4409-8e87-d80707c36a89"),
                UUID.fromString("3f4a17ca-2200-4bb0-b7fe-973bd715baf7"))
                .collect(Collectors.toList());

        assertCriteria(documents.get(0), buildExpectedValuesMap(fields, values));

        values = Stream.of("child_name", "folder",
                UUID.fromString("3f4a17ca-2200-4bb0-b7fe-973bd715baf7"),
                UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"))
                .collect(Collectors.toList());

        assertCriteria(documents.get(1), buildExpectedValuesMap(fields, values));
    }
    // TODO: 5/13/20 implement unit and integration tests for docservice.getById()

//    @Test
//    public void findByUUID_whenNonExistingUUID_thenNullReturned(){
//        Document document = documentService.findById(UUID.randomUUID().toString());
//        assertNull(document);
//    }
//
//    @Test
//    public void findByUUID_whenUUIDProvided_thenDocumentReturned(){
//        Document document = documentService.findById("9936c332-f410-487d-9903-7d25083b489a");
//
//        List<String> fields = Stream.of("title", "baseType", "uuid", "parent", "sourceId").collect(Collectors.toList());
//        List<Object> values = Stream.of("child_folder", "folder", UUID.fromString("9936c332-f410-487d-9903-7d25083b489a"), UUID.fromString("b2f54af5-ca24-4724-a971-db1493802bda"), "child_folder_source_id").collect(Collectors.toList());
//        assertCriteria(document, buildExpectedValuesMap(fields, values));
//    }
}
