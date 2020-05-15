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

    @Test(expected = DocumentNotFoundException.class)
    public void findByUUID_whenNothingFound_thenDocumentNotFoundException() throws Exception {
        DocumentDTO document = documentService.findById(UUID.randomUUID(), "document", false);
//        assertNull(document);
    }

    @Test
    public void findByUUID_whenACLIsFalse_thenDocumentWithoutAclReturned() throws Exception {
        DocumentDTO document = documentService.findById(UUID.fromString("5163a159-d04c-4496-8501-0ea252c9f912"), "folder", false);

        List<String> fields = Stream.of("title", "baseType", "uuid", "parent").collect(Collectors.toList());
        List<Object> values = Stream.of("child_name_1_1_1", "folder", UUID.fromString("5163a159-d04c-4496-8501-0ea252c9f912"), UUID.fromString("e3fefcc7-d6f1-479f-a844-4b8546753042")).collect(Collectors.toList());
        assertCriteria(document, buildExpectedValuesMap(fields, values));
    }

    @Test
    public void findByUUID_whenACLIsTrue_thenDocumentWithAclReturned() throws Exception {
        DocumentDTO document = documentService.findById(UUID.fromString("5163a159-d04c-4496-8501-0ea252c9f912"), "folder", false);

        String rolesFromAcl = "\"full\",\"delete\",\"modify_security\",\"change_content\",\"edit_prop\",\"view_prop\",\"read\"";

        List<String> fields = Stream.of("title", "baseType", "uuid", "parent",  "acl").collect(Collectors.toList());
        List<Object> values = Stream.of("child_name_1_1_1", "folder", UUID.fromString("5163a159-d04c-4496-8501-0ea252c9f912"),
                UUID.fromString("e3fefcc7-d6f1-479f-a844-4b8546753042"), getJsonFromString(rolesFromAcl)
                ).collect(Collectors.toList());
        assertCriteria(document, buildExpectedValuesMap(fields, values));
    }


    @Test(expected = DocumentNotFoundException.class)
    public void findBySourceId_whenSourceIdIsNull_thenDocumentNotFoundException() throws Exception {
        DocumentDTO document = documentService.findBySourceID(null);
        assertNull(document);
    }

    @Test(expected = DocumentNotFoundException.class)
    public void findBySourceId_whenNOthingFound_thenDocumentNotFoundException() throws Exception {

        String sourceId = "fsdfsd";
        DocumentDTO document = documentService.findBySourceID(sourceId);
        assertNull(document);
    }

    @Test
    public void findBySourceId_whenSourceIdProvided_thenListDocumentsReturned() throws Exception {

        DocumentDTO document = documentService.findBySourceID("child_folder_source_id");

        List<String> fields = Stream.of("title", "baseType", "uuid", "parent", "sourceId").collect(Collectors.toList());
        List<Object> values = Stream.of("child_folder", "folder", UUID.fromString("9936c332-f410-487d-9903-7d25083b489a"), UUID.fromString("b2f54af5-ca24-4724-a971-db1493802bda"), "child_folder_source_id").collect(Collectors.toList());
        assertCriteria(document, buildExpectedValuesMap(fields, values));
    }


    @Test
    public void findAll_whenFindAllCalled_AllDocumentsReturned() throws Exception {
        List<DocumentDTO> documentDTOList = documentService.findAll();

        assertNotNull(documentDTOList);
        assertEquals(12, documentDTOList.size());
    }


    @Test
    public void findAllVersions_whenNothingFound_thenEmptyListReturned() throws Exception {
        List<DocumentDTO> documentList = documentService.findAllVersions(UUID.randomUUID());
        assertNotNull(documentList);
        assertEquals(0, documentList.size());
    }

    @Test
    public void findAllVersions_whenDocsFound_thenListDocsReturned() throws Exception {

        UUID seriesId = UUID.fromString("5163a159-d04c-4496-8501-0ea252c9f912");
        List<DocumentDTO> documentList = documentService.findAllVersions(seriesId);
        assertNotNull(documentList);
        assertEquals(2, documentList.size());

        List<String> fields = Stream.of("title", "baseType", "uuid", "parent", "versionSeries").collect(Collectors.toList());
        List<Object> values = Stream.of("child_name_1_1_1", "folder",
                UUID.fromString("5163a159-d04c-4496-8501-0ea252c9f912"),
                UUID.fromString("e3fefcc7-d6f1-479f-a844-4b8546753042"),
                UUID.fromString("5163a159-d04c-4496-8501-0ea252c9f912"))
                .collect(Collectors.toList());

        assertCriteria(documentList.get(0), buildExpectedValuesMap(fields, values));

        values = Stream.of("child_doc_name", "document",
                UUID.fromString("4c33cae4-9847-479a-859b-5ff6cb0b45b0"),
                UUID.fromString("3f4a17ca-2200-4bb0-b7fe-973bd715baf7"),
                UUID.fromString("5163a159-d04c-4496-8501-0ea252c9f912"))
                .collect(Collectors.toList());

        assertCriteria(documentList.get(1), buildExpectedValuesMap(fields, values));
    }

}
