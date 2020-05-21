package ru.doccloud.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jtransfo.JTransfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.User;
import ru.doccloud.repository.DocumentRepository;
import ru.doccloud.repository.UserRepository;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.SystemCrudService;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.document.dto.SystemDTO;
import ru.doccloud.service.impl.AbstractService;
import ru.doccloud.service.impl.RepositoryDocumentCrudService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class DocumentCRUDServiceFindUT {

    private DocumentCrudService<DocumentDTO> documentCrudService;

    private static final String SYS_ACL = "{\"full\":[\"all\"],\"read\":[\"all\"],\"edit_prop\":[\"all\"],\"view_prop\":[\"all\"],\"modify_security\":[\"all\"],\"change_content\":[\"all\"],\"delete\":[\"all\"]}";

    private ObjectMapper mapper = new ObjectMapper();


    @Mock
    private DocumentRepository<Document> repository;

    @Mock
    private SystemCrudService<SystemDTO> sysService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JTransfo transformer;


    @Before
    public void setupMock(){
        MockitoAnnotations.initMocks(this);

        documentCrudService = new RepositoryDocumentCrudService(repository, sysService, userRepository, transformer);
    }

    @Test(expected = DocumentNotFoundException.class)
    public void shouldDocumentNOtFoundEx_whenFindByIdIsCalled() throws Exception {

        String type = "document";
        UUID id = java.util.UUID.randomUUID();

        when(repository.findById(type, id)).thenReturn(null);

        documentCrudService.findById(id, type, false);
    }

    @Test
    public void shouldReturnDocumentWithoutACL_whenFindByIdWithoutAclIsCalled() throws Exception {

        String type = "document";
        UUID id = java.util.UUID.randomUUID();

        Document document = mockDocument(id, false);

        DocumentDTO documentDTO = mockDocumentDTO(id, false);

        when(repository.findById(type, id)).thenReturn(document);
        when(transformer.convert(document, new DocumentDTO())).thenReturn(documentDTO);

        DocumentDTO result = documentCrudService.findById(id, type, false);

        assertEquals(documentDTO, result);

    }


    @Test
    public void shouldReturnDocumentWithoutACL_whenFindByIdWithoutAclIsCalledAndDocumentWithAclFound() throws Exception {

        String type = "document";
        UUID id = java.util.UUID.randomUUID();

        Document document = mockDocument(id, true);

        DocumentDTO documentDTO = mockDocumentDTO(id, false);

        when(repository.findById(type, id)).thenReturn(document);
        when(transformer.convert(document, new DocumentDTO())).thenReturn(documentDTO);

        DocumentDTO result = documentCrudService.findById(id, type, false);

        assertEquals(documentDTO, result);

    }

    @Test
    public void shouldReturnDocumentWithACL_whenFindByIdWithoutAclIsCalledAndDocumentWithAclFound() throws Exception {

        String type = "document";
        UUID id = java.util.UUID.randomUUID();

        Document document = mockDocument(id, true);

        DocumentDTO documentDTO = mockDocumentDTO(id, true);

        DocumentCrudService<DocumentDTO> documentCrudService1 = spy(documentCrudService);
        when(repository.findById(type, id)).thenReturn(document);
        when(documentCrudService1.getRequestUser()).thenReturn("boot");
        when(userRepository.getUser("boot")).thenReturn(mockUser());
        when(transformer.convert(document, new DocumentDTO())).thenReturn(documentDTO);

//        to be capable of mocking call of getRequestUser() we have to use spy to mock the same class as we testing
        DocumentDTO result = documentCrudService1.findById(id, type, true);

        assertEquals(documentDTO, result);

    }


    @Test(expected = DocumentNotFoundException.class)
    public void shouldDocumentNotFoundEx_whenFindByPathIsCalledAndNothingFound()  {

        String path = "/sdfsd/sdfds/dsfds/fs";

        when(repository.findByPath(path)).thenReturn(Optional.empty());

        documentCrudService.findByPath(path);
    }


    @Test
    public void shouldReturnDocumentAndHisParent_whenFindByPathIsCalled() {

        String path = "/child_1/child_name_3/child_folder";
        UUID id = UUID.randomUUID();

        Document document = mockDocument(id, false);

        Document parent = mockDocument(id, false);

        List<Document> documentList = Stream.of(document, parent).collect(Collectors.toList());

        DocumentDTO documentDTO = mockDocumentDTO(id, false);
        DocumentDTO parentDTO = mockDocumentDTO(id, false);

        List<DocumentDTO> expectedList = Stream.of(documentDTO, parentDTO).collect(Collectors.toList());

        when(repository.findByPath(path)).thenReturn(Optional.of(documentList));
        when(transformer.convertList(documentList, DocumentDTO.class)).thenReturn(expectedList);

        List<DocumentDTO> resultList = documentCrudService.findByPath(path);

        assertEquals(expectedList, resultList);

    }

    @Test(expected = DocumentNotFoundException.class)
    public void shouldDocumentNotFoundEx_whenFindBySourceIDCalledAndNothingFound() throws Exception {

        String sourceId = "sourceId";

        when(repository.findBySourceID(sourceId)).thenThrow(DocumentNotFoundException.class);

        documentCrudService.findBySourceID(sourceId);
    }

    @Test
    public void shouldReturnDocumentDTO_whenFindBySourceIDCalled() throws Exception {

        String sourceId = "sourceId";
        UUID id = UUID.randomUUID();

        Document document = mockDocument(id, false);

        DocumentDTO documentDTO = mockDocumentDTO(id, false);

        when(repository.findBySourceID(sourceId)).thenReturn(document);
        when(transformer.convert(document, new DocumentDTO())).thenReturn(documentDTO);

        DocumentDTO result = documentCrudService.findBySourceID(sourceId);

        assertEquals(documentDTO, result);
    }


    @Test
    public void shouldReturnListDocuments_whenFindAllIsCalled() throws Exception {

        UUID id = UUID.randomUUID();

        Document document = mockDocument(id, false);

        Document document1 = mockDocument(id, false);

        List<Document> documentList = Stream.of(document, document1).collect(Collectors.toList());

        DocumentDTO documentDTO = mockDocumentDTO(id, false);
        DocumentDTO documentDTO1 = mockDocumentDTO(id, false);

        List<DocumentDTO> expectedList = Stream.of(documentDTO, documentDTO1).collect(Collectors.toList());

        when(repository.findAll()).thenReturn(documentList);
        when(transformer.convertList(documentList, DocumentDTO.class)).thenReturn(expectedList);

        List<DocumentDTO> resultList = documentCrudService.findAll();

        assertEquals(expectedList, resultList);
    }

    // TODO: 5/15/20 write tests for findAll with Pageable


    private Document mockDocument(UUID id, boolean isAclRequired){
        Document.Builder documentBuilder = Document.getBuilder("title")
                .id(id)
                .description("description")
                .sourceId("sourceId")
                .parent(id);

        if(isAclRequired)
            documentBuilder.acl(getJsonFromString(SYS_ACL));

        return documentBuilder.build();
    }

    private DocumentDTO mockDocumentDTO(UUID id, boolean isAclReq){
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setTitle("title");
        documentDTO.setId(id);
        documentDTO.setDescription("description");
        documentDTO.setParent(id);
        documentDTO.setSourceId("sourceId");

        if(isAclReq){
            String rolesFromAcl = "\"full\",\"delete\",\"modify_security\",\"change_content\",\"edit_prop\",\"view_prop\",\"read\"";
            documentDTO.setAcl(getJsonFromString(rolesFromAcl));
        }


        return documentDTO;
    }

    private User mockUser(){
        String[] groups = {"editors", "all", "admins", "boot"};
        User.Builder userBuilder = User.getBuilder("boot")
                .groups(groups);

        return userBuilder.build();
    }


    private JsonNode getJsonFromString(String strToJson){
        JsonNode node = null;

        try {
            node = mapper.readTree(strToJson);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return node;
    }
}
