package ru.doccloud.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.JsonNode;

import ru.doccloud.document.model.Link;

/**
 * @author Andrey Kadnikov
 */
public interface DocumentRepository<Document>  extends CommonRepository<Document>  {


    public Page<Document> findAllByParent(UUID parent, Pageable pageable);

    public List<Document> findParents(UUID docId);

    public Link addLink(UUID headId, UUID tailId, String type);

    public Link deleteLink(UUID headId, UUID tailId);

    public Document setParent(Document documentEntry);

    public Page<Document> findAllByLinkHead(UUID head, String type, Pageable pageable);

    public Document findBySourceID(String sourceId);

    public Document findByPath(String path);

	public List<Document> findAllVersions(UUID seriesId);

	int processRetention(String policy, String dateField, Long period, String[] admins, String user);

	public List<String> getDistinct(String field, String type, String query);

	public Page<Document> findInPgBySearchTerm(String searchTerm, Pageable pageable);

	public List<Document> findAllByIds(UUID[] ids, String[] fields);

	public Long countAllByType(String type, String[] fields, Pageable pageable, String query, JsonNode typeData);

}