package ru.doccloud.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.document.dto.LinkDTO;

import java.util.List;
import java.util.UUID;

/**
 * @author Andrey Kadnikov
 */
public interface DocumentCrudService<DocumentDTO> extends CommonService<DocumentDTO> {

    public DocumentDTO addToFolder(final DocumentDTO todo, final String id, String user)  throws Exception;

	// TODO: 06.05.2020 test what is beter return list of object with paretn or add parent object to DocumentDto
	public List<DocumentDTO> findByPath(final String path);
//
    List<DocumentDTO> findParents(UUID docId)  throws Exception;

// 
    public DocumentDTO findById(UUID id, String type, boolean getAcl) throws Exception;


	public Page<DocumentDTO> findAllByParent(final UUID parentid, Pageable pageable)  throws Exception;
//

    DocumentDTO updateFileInfo(final DocumentDTO dto, String user)  throws Exception;

    LinkDTO addLink(UUID headId, UUID tailId, String type)  throws Exception;

    LinkDTO deleteLink(UUID headId, UUID tailId)  throws Exception;

    public DocumentDTO setParent(DocumentDTO dto, String user)  throws Exception;

	public DocumentDTO findBySourceID(String textValue)  throws Exception;

	public Page<DocumentDTO> findAllByLinkHead(UUID head, String type, Pageable pageable)  throws Exception;
	
	public JsonNode processRetention(String remoteUser)  throws Exception;
	
	public List<DocumentDTO> findAllVersions(UUID seriesUUID) throws Exception;

	public JsonNode getDistinct(String field, String type, String query);

	public String getRequestUser();
	
	public Page<DocumentDTO> findInPgBySearchTerm(String searchTerm, Pageable pageable);
	
	public String convertResultToCsv(List<DocumentDTO> searchRes, String[] docFields) throws Exception;
	
	public List<DocumentDTO> findAllByIds(UUID[] Ids, String[] fields);
	
	public Long countAllByType(String type, String[] fields, Pageable pageable, String query) throws Exception;
	

}
