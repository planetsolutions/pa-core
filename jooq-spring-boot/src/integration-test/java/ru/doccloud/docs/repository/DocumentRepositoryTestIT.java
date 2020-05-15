package ru.doccloud.docs.repository;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.docs.CommonDocTest;
import ru.doccloud.document.model.Document;
import ru.doccloud.repository.DocumentRepository;
import ru.doccloud.service.document.dto.DocumentDTO;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class DocumentRepositoryTestIT extends CommonDocTest {

    @Autowired
    DocumentRepository<Document> documentRepository;

    @Test
    public void getDocByPath_whenNotFound_thenEmptyOptional(){
        Optional<List<Document>> listOptional = documentRepository.findByPath("/sdfsd/sdfds/dsfds/fs");
        assertFalse(listOptional.isPresent());
    }

    @Test
    public void getDocByPath_when_NotNullPath_thenDocReturned(){

        String path = "/child_1/child_name_3/child_folder";

        Optional<List<Document>> optionalDocuments = documentRepository.findByPath(path);

        assertTrue(optionalDocuments.isPresent());

        List<Document> documents = optionalDocuments.get();

        assertNotNull(documents);
        assertEquals(2, documents.size());

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

        Optional<List<Document>> optionalDocuments = documentRepository.findByPath(path);

        assertNotNull(optionalDocuments);
        List<Document> documents = optionalDocuments.get();

        assertNotNull(documents);
        assertEquals(2, documents.size());

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

//    @Test(expected = DocumentNotFoundException.class)
//    public void findByUUID_whenUUIDIsNull_thenDocumentNotFoundException(){
//        Document document = documentRepository.findByUUID(null);
//        assertNotNull(document);
//    }

    @Test
    public void findByUUID_whenNonExistingUUID_thenNullReturned(){
        Document document = documentRepository.findByUUID(UUID.randomUUID().toString());
        assertNull(document);
    }

    @Test
    public void findByUUID_whenUUIDProvided_thenDocumentReturned(){
        Document document = documentRepository.findByUUID("9936c332-f410-487d-9903-7d25083b489a");

        List<String> fields = Stream.of("title", "baseType", "uuid", "parent", "sourceId").collect(Collectors.toList());
        List<Object> values = Stream.of("child_folder", "folder", UUID.fromString("9936c332-f410-487d-9903-7d25083b489a"), UUID.fromString("b2f54af5-ca24-4724-a971-db1493802bda"), "child_folder_source_id").collect(Collectors.toList());
        assertCriteria(document, buildExpectedValuesMap(fields, values));
    }


    @Test(expected = DocumentNotFoundException.class)
    public void findBySourceId_whenSourceIdIsNull_thenDocumentNotFoundException(){
        Document document = documentRepository.findBySourceID(null);
        assertNull(document);
    }

    @Test(expected = DocumentNotFoundException.class)
    public void findBySourceId_whenNothingFound_thenDocumentNotFoundException(){

        String sourceId = "fsdfsd";
        Document document = documentRepository.findBySourceID(sourceId);
        assertNull(document);
    }

    @Test
    public void findBySourceId_whenSourceIdProvided_thenListDocumentsReturned(){
        Document document = documentRepository.findByUUID("9936c332-f410-487d-9903-7d25083b489a");

        List<String> fields = Stream.of("title", "baseType", "uuid", "parent", "sourceId").collect(Collectors.toList());
        List<Object> values = Stream.of("child_folder", "folder", UUID.fromString("9936c332-f410-487d-9903-7d25083b489a"), UUID.fromString("b2f54af5-ca24-4724-a971-db1493802bda"), "child_folder_source_id").collect(Collectors.toList());
        assertCriteria(document, buildExpectedValuesMap(fields, values));
    }

    @Test
    public void findAll_whenFindAllCalled_AllDocumentsReturned() {
        List<Document> documentDTOList = documentRepository.findAll();

        assertNotNull(documentDTOList);
        assertEquals(12, documentDTOList.size());
    }

    @Test
    public void findAllVersions_whenNothingFound_thenEmptyListReturned(){
        List<Document> documentList = documentRepository.findAllVersions(UUID.randomUUID());
        assertNotNull(documentList);
        assertEquals(0, documentList.size());
    }

    @Test
    public void findAllVersions_whenDocsFound_thenListDocsReturned(){

        UUID seriesId = UUID.fromString("5163a159-d04c-4496-8501-0ea252c9f912");
        List<Document> documentList = documentRepository.findAllVersions(seriesId);
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
