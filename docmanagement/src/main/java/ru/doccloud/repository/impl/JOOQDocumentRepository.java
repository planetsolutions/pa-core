package ru.doccloud.repository.impl;


import static org.jooq.impl.DSL.*;
import static ru.doccloud.document.jooq.db.tables.Documents.DOCUMENTS;
import static ru.doccloud.document.jooq.db.tables.Links.LINKS;
import static ru.doccloud.repository.util.DataQueryHelper.*;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jooq.*;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.MappedTable;
import org.jooq.conf.RenderMapping;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.common.service.DateTimeService;
import ru.doccloud.common.util.JsonNodeParser;
import ru.doccloud.document.jooq.db.tables.Documents;
import ru.doccloud.document.jooq.db.tables.Links;
import ru.doccloud.document.jooq.db.tables.records.DocumentsRecord;
import ru.doccloud.document.jooq.db.tables.records.LinksRecord;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.Link;
import ru.doccloud.document.model.QueryParam;
import ru.doccloud.repository.DocumentRepository;

/**
 * @author Andrey Kadnikov
 */
@Repository
public class JOOQDocumentRepository extends AbstractJooqRepository implements DocumentRepository<Document>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(JOOQDocumentRepository.class);
    private static final String[] allFields={"all"};
    private enum CMIS_TYPES {
        CMIS_FOLDER ("cmis:folder"),
        CMIS_DOCUMENT("cmis:document");

        private String cmisType;

        CMIS_TYPES(String cmisType) {
            this.cmisType = cmisType;
        }

        public String getCmisType() {
            return cmisType;
        }
    }

    @Autowired
    public JOOQDocumentRepository(DateTimeService dateTimeService, DSLContext jooq) {
        super(jooq, dateTimeService);

    }

    @Transactional
    @Override
    @NoDefDSConnection
    public Document add(String type, Document documentEntry) {
        LOGGER.trace("entering add(documentEntry= {})", documentEntry);
        if (documentEntry.isLastVersion()==null){
        	documentEntry.setLastVersion(true);
        }
        Table<?> table = getTable(type);
        Record record = jooq.insertInto( 
                table, DOCUMENTS.SYS_DESC, DOCUMENTS.SYS_TITLE, DOCUMENTS.SYS_BASE_TYPE, DOCUMENTS.SYS_TYPE, DOCUMENTS.SYS_AUTHOR,
                DOCUMENTS.SYS_READERS, DOCUMENTS.DATA, DOCUMENTS.SYS_FILE_LENGTH, DOCUMENTS.SYS_FILE_MIME_TYPE,
                DOCUMENTS.SYS_FILE_NAME, DOCUMENTS.SYS_FILE_PATH, DOCUMENTS.SYS_VERSION, DOCUMENTS.SYS_FILE_STORAGE,
                DOCUMENTS.SYS_PARENT_UUID, DOCUMENTS.SYS_SOURCE_ID, DOCUMENTS.SYS_ACL, DOCUMENTS.SYS_RETENTION_POLICY,
                DOCUMENTS.VER_ISLAST,DOCUMENTS.VER_PARENT_UUID,DOCUMENTS.VER_SERIES_UUID, DOCUMENTS.SYS_SOURCE_PACKAGE)
                .values(
                        documentEntry.getDescription(), documentEntry.getTitle(), documentEntry.getBaseType(), documentEntry.getType(), documentEntry.getAuthor(),
                        documentEntry.getReaders(), documentEntry.getData(), documentEntry.getFileLength(), documentEntry.getFileMimeType(),
                        documentEntry.getFileName(), documentEntry.getFilePath(), documentEntry.getDocVersion(), documentEntry.getFileStorage(),
                        documentEntry.getParent(),documentEntry.getSourceId(),documentEntry.getAcl(), documentEntry.getRetentionPolicy(),
                        documentEntry.isLastVersion(),documentEntry.getVersionParent(),documentEntry.getVersionSeries(),documentEntry.getSourcePackage())
                .returning()
                .fetchOne();
            // DocumentsRecord persisted = (DocumentsRecord) record;
	        // Document returned = DocumentConverter.convertQueryResultToModelObject(persisted); 
       	Document returned = DocumentConverter.convertQueryResultToModelObject(record, allFields);
       
        LOGGER.trace("leaving add():  added document {}", returned);

        return returned;
        
    }

    private Table<Record> getTable(String type) {
    	Table<?> table = DOCUMENTS;
    	if ("rendition".equals(type)) {
    		table = getTable(jooq, "responses");
    		if (table==null) {
    			table = DOCUMENTS;
    		}
    	}
    	
		return (Table<Record>) table;
	}

    public Table<?> getTable(DSLContext dslContext, final String tableName){
        Meta meta = dslContext.meta();

        for (Table<?> table : meta.getTables()) {
            if (table.getName().equalsIgnoreCase(tableName)) {
                return table;
            }
        }
        LOGGER.error("Table not found: " + tableName);
        return null;
        
    }
	@Transactional
    @Override
    public Link addLink(UUID headId, UUID tailId, String type) {
        LOGGER.trace("entering addLink(headId = {}, tailId={})", headId, tailId);

        LinksRecord persisted = jooq.insertInto(LINKS,LINKS.HEAD_ID,LINKS.TAIL_ID,LINKS.LINK_TYPE)
                .values(headId,tailId,type)
                .returning()
                .fetchOne();

        Link returned = new Link(persisted.getHeadId(),persisted.getTailId());

        LOGGER.trace("leaving addLink():  added link {}", returned);

        return returned;
    }

    @Transactional
    @Override
    public Link deleteLink(UUID headId, UUID tailId) {
        LOGGER.trace("entering deleteLink(headId = {}, tailId={})", headId, tailId);

        int deleted = jooq.delete(LINKS)
                .where(LINKS.HEAD_ID.equal(headId).and(LINKS.TAIL_ID.equal(tailId)))
                .execute();

        LOGGER.trace("deleteLink(): {} link entry deleted", deleted);
        Link returned = new Link(headId,tailId); 

        LOGGER.trace("leaving deleteLink():  deleted link {}", returned);
        return returned;
    }


    @Transactional
    @Override
    @NoDefDSConnection
    public Document delete(String type, UUID id) {
        LOGGER.trace("entering delete(id={})", id);

        Document deleted = findById(type,id);

        LOGGER.trace("delete(): Document was found in database {}", deleted);

        if(deleted == null)
            throw new DocumentNotFoundException("The document with id was not found in database");

        int deletedRecordCount = jooq.delete(DOCUMENTS)
                .where(DOCUMENTS.SYS_UUID.equal(id))
                .execute();

        LOGGER.trace("delete(): {} document entries deleted", deletedRecordCount);

        LOGGER.trace("leaving delete(): Returning deleted Document entry: {}", deleted);

        return deleted;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Document> findAll() {
        LOGGER.debug("entering findAll()");

        List<DocumentsRecord> queryResults = jooq.selectFrom(DOCUMENTS).fetchInto(DocumentsRecord.class);

        LOGGER.debug("findAll(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.debug("leaving findAll(): Found {} Document entries", documentEntries);

        return documentEntries;
    }
    
    @Transactional(readOnly = true)
    @Override
    public List<Document> findAllVersions(UUID SeriesId) { 
        LOGGER.debug("entering findAllVersions()");
        Condition cond = DOCUMENTS.VER_SERIES_UUID.equal(SeriesId);
        cond=cond.or(DOCUMENTS.SYS_UUID.equal(SeriesId));
        List<DocumentsRecord> queryResults = jooq.selectFrom(DOCUMENTS)
        		.where(cond)
        		.fetchInto(DocumentsRecord.class);

        LOGGER.debug("findAllVersions(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.debug("leaving findAllVersions(): Found {} Document entries", documentEntries);

        return documentEntries;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Document> findAll(Pageable pageable, String query) {

        LOGGER.debug("entering findAll(pageSize = {}, pageNumber = {})", pageable.getPageSize(), pageable.getPageNumber());
        List<QueryParam> queryParams = getQueryParams(query, null);
        Condition cond = null;
        if (queryParams !=null){
            cond = DOCUMENTS.VER_ISLAST.isTrue();
            cond = extendConditions(cond, queryParams, DOCUMENTS, DOCUMENTS.DATA);
        }
        List<DocumentsRecord> queryResults = jooq.selectFrom(DOCUMENTS)
                .where(cond)
                .orderBy(getSortFields(pageable.getSort(), DOCUMENTS, DOCUMENTS.DATA, null))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(DocumentsRecord.class);

        LOGGER.debug("findAll(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.debug("findAll(): {} document entries for page: {} ",
                documentEntries.size(),
                pageable.getPageNumber()
        );
        long totalCount = 0;
        if (queryParams !=null){
            totalCount = findTotalCountByType(cond, DOCUMENTS);
        }else{
            totalCount = findTotalCount(DOCUMENTS);
        }

        LOGGER.trace("findAll(): {} document entries matches with the like expression: {}", totalCount);

        LOGGER.trace("leaving findAll(): Found {} Document entries", documentEntries);

        return new PageImpl<>(documentEntries, pageable, totalCount);
    }


    @Override
    @Cacheable(value = "docsByType", cacheManager = "springCM")
    @NoDefDSConnection
    public Page<Document> findAllByType(String type, String[] fields, Pageable pageable, String query, JsonNode typeData) {
        LOGGER.trace("entering findAllByType(type={}, fields={}, pageable={}, query={})", type, fields, pageable, query);

        ArrayList<SelectField<?>> selectedFields = new ArrayList<SelectField<?>>();
        //selectedFields.add(DOCUMENTS.ID);
        selectedFields=selectedDefault(selectedFields,DOCUMENTS);
        if (fields!=null){
            if (fields[0].equals("all")){
                selectedFields.add(DOCUMENTS.DATA);
            }else{
                for (String field : fields) {
                    selectedFields.add(jsonObject(DOCUMENTS.DATA, field).as(field));
                }
            }
        }
        LOGGER.trace("findAllByType(): selectedFields: {}", selectedFields);
        Map<String, String> propTypes = getPropertiesType(typeData);
        List<QueryParam> queryParams = getQueryParams(query,propTypes);
        Condition cond = DOCUMENTS.SYS_TYPE.equal(type);
        cond = cond.and(DOCUMENTS.VER_ISLAST.isTrue());
        cond = extendConditions(cond, queryParams, DOCUMENTS, DOCUMENTS.DATA);
        List<Record> queryResults = jooq.select(selectedFields).from("ONLY {0}", DOCUMENTS)
                .where(cond)
                .orderBy(getSortFields(pageable.getSort(), DOCUMENTS, DOCUMENTS.DATA, propTypes))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetch();//Into(DocumentsRecord.class);

        LOGGER.trace("findAllByType(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResults(queryResults, fields);

        long totalCount = pageable.getOffset()+(pageable.getPageSize()*2);
        if(documentEntries.size()<pageable.getPageSize()){
        	totalCount = pageable.getOffset()+documentEntries.size();
        }
        
        LOGGER.trace("findAllByType(): {} document entries matches with the like expression: {}", totalCount);

        LOGGER.trace("leaving findAllByType(): Found {} Documents", documentEntries);

        return new PageImpl<>(documentEntries, pageable, totalCount);

    }

    private ArrayList<SelectField<?>> selectedDefault(ArrayList<SelectField<?>> selectedFields, Documents documents) {
    	selectedFields.add(DOCUMENTS.SYS_TITLE);
        selectedFields.add(DOCUMENTS.SYS_AUTHOR);
        selectedFields.add(DOCUMENTS.SYS_DATE_CR);
        selectedFields.add(DOCUMENTS.SYS_DATE_MOD);
        selectedFields.add(DOCUMENTS.SYS_DESC);
        selectedFields.add(DOCUMENTS.SYS_MODIFIER);
        selectedFields.add(DOCUMENTS.SYS_TYPE);
        selectedFields.add(DOCUMENTS.SYS_BASE_TYPE);
        selectedFields.add(DOCUMENTS.SYS_UUID);
        selectedFields.add(DOCUMENTS.SYS_PARENT_UUID);
        selectedFields.add(DOCUMENTS.SYS_FILE_NAME);
        selectedFields.add(DOCUMENTS.SYS_FILE_PATH);
        selectedFields.add(DOCUMENTS.SYS_FILE_STORAGE);
        selectedFields.add(DOCUMENTS.SYS_FILE_MIME_TYPE);
        selectedFields.add(DOCUMENTS.SYS_FILE_LENGTH);
        selectedFields.add(DOCUMENTS.SYS_ACL);
        selectedFields.add(DOCUMENTS.SYS_SOURCE_ID);
        selectedFields.add(DOCUMENTS.SYS_SOURCE_PACKAGE);
        selectedFields.add(DOCUMENTS.SYS_VERSION);
        selectedFields.add(DOCUMENTS.VER_ISLAST);
        selectedFields.add(DOCUMENTS.VER_PARENT_UUID);
        selectedFields.add(DOCUMENTS.VER_SERIES_UUID);
        selectedFields.add(DOCUMENTS.VER_COMMENT);
		return selectedFields;
	}

	@Transactional(readOnly = true)
    @Override
    @NoDefDSConnection
    public Document findById(String type, UUID id) {
        LOGGER.trace("entering findById(id = {})", id);

        Record queryResult = findDocById(type, id);


        LOGGER.trace("leaving findById(): Found {}", queryResult);
        
        return queryResult != null ? DocumentConverter.convertQueryResultToModelObject(queryResult,allFields): null;
    }

    @Transactional(readOnly = true)
    @Override
    public Document findByUUID(String uuid) {
        LOGGER.debug("entering findByUUID(uuid = {})", uuid);

        DocumentsRecord queryResult = jooq.selectFrom(DOCUMENTS)
                .where(DOCUMENTS.SYS_UUID.equal( UUID.fromString(uuid)))
                .fetchOne();


        LOGGER.trace("leaving findByUUID(): Found {}", queryResult);
        return queryResult != null ?  DocumentConverter.convertQueryResultToModelObject(queryResult) : null;
    }
    
    @Transactional(readOnly = true)
    @Override
    public Document findBySourceID(String sourceId) {
        LOGGER.debug("entering findBysourceId(sourceId = {})", sourceId);

        DocumentsRecord queryResult = jooq.selectFrom(DOCUMENTS)
                .where(DOCUMENTS.SYS_SOURCE_ID.equal(sourceId))
                .fetchOne();

        if (queryResult == null) {
            throw new DocumentNotFoundException("No Document entry found with sourceId: " + sourceId);
        }
        LOGGER.trace("leaving findBysourceId(): Found {}", queryResult);
        return DocumentConverter.convertQueryResultToModelObject(queryResult);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<List<Document>> findByPath(String path) {
        LOGGER.trace("entering findByPath(path={})", path);

        final String docName = StringUtils.substringAfterLast(path, "/");

        final String parentFolderPath = path.startsWith("/") ? StringUtils.substringAfter(path,"/") : path;

        LOGGER.trace("findByPath(): docName is {} , parentFolderPath is {}", docName, parentFolderPath);
        Field<String> pathToParent = DOCUMENTS.SYS_TITLE.as("path");


        ArrayList<SelectField<?>> selectedFields = new ArrayList<SelectField<?>>();
        selectedDefault(selectedFields, DOCUMENTS);
        selectedFields.add(pathToParent);

        ArrayList<SelectField<?>> selectedFieldsUnion = new ArrayList<SelectField<?>>();
        selectedDefault(selectedFieldsUnion, DOCUMENTS);
        selectedFieldsUnion.add(DOCUMENTS.SYS_TITLE.concat("/").concat(pathToParent));

        SelectQuery<?> folderPathQuery = jooq.withRecursive("folderPath")
                .as(
                        select(selectedFields)
                                .from(DOCUMENTS)
                                .where(DOCUMENTS.SYS_TITLE.equal(docName))
                                .union(
                                        select(
                                                selectedFieldsUnion
                                        )
                                                .from(DOCUMENTS)
                                                .join(
                                                        table(
                                                                name("folderPath")
                                                        )
                                                )
                                                .on(
                                                        field(
                                                                name("folderPath", "sys_parent_uuid"), UUID.class).eq(DOCUMENTS.SYS_UUID)
                                                ).and(DOCUMENTS.SYS_PARENT_UUID.isNotNull())
                                )
                )
                .select().from(name("folderPath")).getQuery();

        Result<?> queryResult = folderPathQuery.fetch();

        LOGGER.trace("findByPath(): found {}", queryResult);

        boolean isFolderFound = queryResult.stream().anyMatch(r -> r.get("path").equals(parentFolderPath));

       if(isFolderFound) {

//           boolean needToFilterDocType = list..map(MyBean::getTitle).filter("some"::equals).unique().limit(2).count() > 1;
//           return list.filter(b -> "some".equals(b.title)).filter(b -> !needToFilterDocType || "Document".equals(b.type)).findAny().orElse(null);

           Function<String, Record> byName = name -> queryResult.stream()
                   .filter(r -> r.get("path").equals(name))
                   .findFirst().orElse(null);
           Record foundDoc = byName.apply(docName);

           Function<UUID, Record> byParentUUID = uuid -> queryResult.stream().filter(r -> r.get("sys_uuid").equals(uuid)).findFirst().orElse(null);

           Record parentDoc = byParentUUID.apply((UUID) foundDoc.get("sys_parent_uuid"));
           final String[] fields = new String[]{"all"};

           List<Document> documentList = Stream.of(foundDoc, parentDoc)
                   .limit(queryResult.size())
                   .filter(Objects::nonNull)
                   .map(r->DocumentConverter.convertQueryResultToModelObject(r, fields))
                   .collect(Collectors.toList());

           LOGGER.trace("leaving findByPath(): found {}", documentList);
           return Optional.of(documentList);
       } else {
           return Optional.empty();
//           throw new DocumentNotFoundException(String.format("Document with path %s wasn't found", path));
       }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Document> findBySearchTerm(String searchTerm, Pageable pageable) {
        LOGGER.trace("entering findBySearchTerm(searchTerm={}, pageable={})", searchTerm, pageable);

        String likeExpression = "%" + searchTerm + "%";

        List<DocumentsRecord> queryResults = jooq.selectFrom(DOCUMENTS)
                .where(createWhereConditions(likeExpression, DOCUMENTS.SYS_DESC, DOCUMENTS.SYS_TITLE))
                .orderBy(getSortFields(pageable.getSort(), DOCUMENTS, DOCUMENTS.DATA, null))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(DocumentsRecord.class);

        LOGGER.trace("findBySearchTerm(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        long totalCount = findCountByLikeExpression(likeExpression);
        LOGGER.trace("findBySearchTerm(): {} document entries matches with the like expression: {}", totalCount);
        LOGGER.trace("leaving findBySearchTerm(): Found {}", documentEntries);

        return new PageImpl<>(documentEntries, pageable, totalCount);
    }
    
    @Transactional(readOnly = true)
    @Override
    public Page<Document> findInPgBySearchTerm(String searchTerm, Pageable pageable) {
        LOGGER.trace("entering findBySearchTerm(searchTerm={}, pageable={})", searchTerm, pageable);

        Condition cond = DOCUMENTS.SYS_TITLE.equal(searchTerm);
        List<DocumentsRecord> queryResults = jooq.selectFrom(DOCUMENTS)
                .where(cond)
                .orderBy(getSortFields(pageable.getSort(), DOCUMENTS, DOCUMENTS.DATA, null))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(DocumentsRecord.class);

        LOGGER.trace("findBySearchTerm(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        long totalCount = findTotalCountByType(cond, DOCUMENTS);
        LOGGER.trace("findBySearchTerm(): {} document entries matches: {}", totalCount, searchTerm);
        LOGGER.trace("leaving findBySearchTerm(): Found {}", documentEntries);

        return new PageImpl<>(documentEntries, pageable, totalCount);
    }

    @Transactional
    @Override
    @NoDefDSConnection
    public Document update(String type, Document documentEntry) {
        LOGGER.trace("entering update(documentEntry={})", documentEntry);

        Timestamp currentTime = dateTimeService.getCurrentTimestamp();
        LOGGER.trace("update(): The current time is: {}", currentTime);
        
        Table<Record> table = getTable(type);
        UpdateSetMoreStep<Record> s = jooq.update(table)
                .set(DOCUMENTS.SYS_DATE_MOD, currentTime)
                .set(DOCUMENTS.SYS_MODIFIER, documentEntry.getModifier())
                .set(DOCUMENTS.SYS_VERSION, documentEntry.getDocVersion());

        if (documentEntry.getTitle() != null) s.set(DOCUMENTS.SYS_TITLE, documentEntry.getTitle());
        if (documentEntry.getDescription() != null) s.set(DOCUMENTS.SYS_DESC, documentEntry.getDescription());
        if (documentEntry.getData() != null) s.set(DOCUMENTS.DATA, documentEntry.getData());
        if (documentEntry.getType() != null) s.set(DOCUMENTS.SYS_TYPE, documentEntry.getType());
        if (documentEntry.getParent() != null) s.set(DOCUMENTS.SYS_PARENT_UUID, documentEntry.getParent());

        int updatedRecordCount = s.where(DOCUMENTS.SYS_UUID.equal(documentEntry.getId())).execute();

        LOGGER.trace("leaving update(): Updated {}", updatedRecordCount);
        //If you are using Firebird or PostgreSQL databases, you can use the RETURNING
        //clause in the update statement (and avoid the extra select clause):
        //http://www.jooq.org/doc/3.2/manual/sql-building/sql-statements/update-statement/#N11102

        return findById(documentEntry.getType(), documentEntry.getId());
    }

    @Transactional
    @Override
    @NoDefDSConnection
    public Document updateFileInfo(String type, Document documentEntry) {
        LOGGER.trace("entering updateFileInfo(documentEntry={})", documentEntry);
//todo check that such document exists in database
        Timestamp currentTime = dateTimeService.getCurrentTimestamp();
        Table<Record> table = getTable(type);
        int updatedRecordCount = jooq.update(table)
                .set(DOCUMENTS.SYS_FILE_PATH, documentEntry.getFilePath())
                .set(DOCUMENTS.SYS_DATE_MOD, currentTime)
                .set(DOCUMENTS.SYS_MODIFIER, documentEntry.getModifier())
                .set(DOCUMENTS.SYS_FILE_LENGTH, documentEntry.getFileLength())
                .set(DOCUMENTS.SYS_FILE_MIME_TYPE, documentEntry.getFileMimeType())
                .set(DOCUMENTS.SYS_FILE_NAME, documentEntry.getFileName())
                .set(DOCUMENTS.SYS_FILE_STORAGE, documentEntry.getFileStorage())
                .where(DOCUMENTS.SYS_UUID.equal(documentEntry.getId()))
                .execute();

        LOGGER.trace("leaving updateFileInfo(): Updated {} document entry", updatedRecordCount);
        //If you are using Firebird or PostgreSQL databases, you can use the RETURNING
        //clause in the update statement (and avoid the extra select clause):
        //http://www.jooq.org/doc/3.2/manual/sql-building/sql-statements/update-statement/#N11102

        return documentEntry;
    }

    @Transactional
    @Override
    public Document setParent(Document documentEntry) {
        LOGGER.trace("entering updateFileInfo(documentEntry={})", documentEntry);
        Timestamp currentTime = dateTimeService.getCurrentTimestamp();

        //todo check that such document exists in database
        Table<Record> table = getTable(documentEntry.getType());
        int updatedRecordCount = jooq.update(table)
                .set(DOCUMENTS.SYS_PARENT_UUID, documentEntry.getParent())
                .set(DOCUMENTS.SYS_DATE_MOD, currentTime)
                .set(DOCUMENTS.SYS_MODIFIER, documentEntry.getModifier())
                .where(DOCUMENTS.SYS_UUID.equal(documentEntry.getId()))
                .execute();

        LOGGER.trace("leaving updateFileInfo(): Updated {} document entry", updatedRecordCount);
        //If you are using Firebird or PostgreSQL databases, you can use the RETURNING
        //clause in the update statement (and avoid the extra select clause):
        //http://www.jooq.org/doc/3.2/manual/sql-building/sql-statements/update-statement/#N11102

        return findById(documentEntry.getType(), documentEntry.getId());
    }


    private static Field<Object> jsonObject(Field<?> field, String name) {
        return DSL.field("{0}->{1}", Object.class, field, inline(name));
    }

    private static Field<Object> jsonText(Field<?> field, String name) {
        return DSL.field("{0}->>{1}", Object.class, field, inline(name));
    }

    @Override
    public Page<Document> findAllByParent(UUID parent, Pageable pageable) {
        LOGGER.trace("entering findAllByParent(parent = {})", parent);

        if(parent== null)
            throw new DocumentNotFoundException("parentIs is null");
        Condition cond = DOCUMENTS.SYS_PARENT_UUID.equal(parent);
        cond = cond.and(DOCUMENTS.VER_ISLAST.isTrue());
        List<DocumentsRecord>  queryResults = jooq.selectFrom(DOCUMENTS)
                .where(cond)
                .orderBy(getSortFields(pageable.getSort(), DOCUMENTS, DOCUMENTS.DATA, null))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(DocumentsRecord.class);


        LOGGER.trace("findAllByParent(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.trace("leaving findAllByParent(): Found {}", documentEntries);

        long totalCount = findTotalCountByType(cond, DOCUMENTS);

        return new PageImpl<>(documentEntries, pageable, totalCount);
    }

    @Override
    public Page<Document> findAllByLinkHead(UUID head, String type, Pageable pageable) { 
        LOGGER.trace("entering findAllByLinkParent(head = {})", head);
        if(head == null)
            throw new DocumentNotFoundException("head id is null");

        Documents d = DOCUMENTS.as("d");
        Links l = LINKS.as("l");
        Documents t = DOCUMENTS.as("t");

        List<DocumentsRecord> queryResults = jooq.select(d.SYS_UUID, d.SYS_TITLE, d.SYS_AUTHOR, d.SYS_DATE_CR, d.SYS_DATE_MOD, d.SYS_DESC, d.SYS_MODIFIER, d.SYS_FILE_PATH, d.SYS_TYPE, d.SYS_FILE_NAME, d.SYS_UUID, d.DATA)
                .from(l.join(d)
                        .on(l.TAIL_ID.equal(d.SYS_UUID)))
                .where(l.HEAD_ID.equal(head).and(l.LINK_TYPE.equal(type)))
                .fetchInto(DocumentsRecord.class);

        LOGGER.trace("findAllByLinkParent(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.trace("leaving findAllByLinkParent(): Found {}", documentEntries);

        long totalCount = documentEntries.size();

        return new PageImpl<>(documentEntries, pageable, totalCount);
    }

    @Override
    public List<Document> findParents(UUID docId) {
        LOGGER.trace("entering findParents(docId = {})", docId);
        if(docId == null)
            throw new DocumentNotFoundException("documentId is null");

        Documents d = DOCUMENTS.as("d");
        Links l = LINKS.as("l");
        Documents t = DOCUMENTS.as("t");

        List<DocumentsRecord> queryResults = jooq.select(d.SYS_UUID, d.SYS_TITLE, d.SYS_AUTHOR, d.SYS_DATE_CR, d.SYS_DATE_MOD, d.SYS_DESC, d.SYS_MODIFIER, d.SYS_FILE_PATH, d.SYS_TYPE, d.SYS_FILE_NAME, d.SYS_UUID)
                .from(d
                        .join(l
                                .join(t)
                                .on(t.SYS_UUID.equal(l.TAIL_ID)))
                        .on(d.SYS_UUID.equal(l.HEAD_ID)))
                .where(t.SYS_UUID.equal(docId))
                .fetchInto(DocumentsRecord.class);

        LOGGER.trace("findParents(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.debug("leaving findParents(): Found: {}", documentEntries);

        return documentEntries;
    }



    private long findCountByLikeExpression(String likeExpression) {
        LOGGER.trace("entering findCountByLikeExpression(likeExpression={})", likeExpression);

        long resultCount = jooq.fetchCount(
                jooq.select()
                        .from(DOCUMENTS)
                        .where(createWhereConditions(likeExpression, DOCUMENTS.SYS_DESC, DOCUMENTS.SYS_TITLE))
        );

        LOGGER.trace("leaving findCountByLikeExpression(): Found search result count: {}", resultCount);

        return resultCount;
    }


    private static class DocumentConverter{
        private static Document convertQueryResultToModelObject(Record queryResult, String[] fields) {
            return  Document.getBuilder(queryResult.getValue(DOCUMENTS.SYS_TITLE))
                    .description(queryResult.getValue(DOCUMENTS.SYS_DESC))
                    .baseType(queryResult.getValue(DOCUMENTS.SYS_BASE_TYPE))
                    .type(queryResult.getValue(DOCUMENTS.SYS_TYPE))
                    .id(queryResult.getValue(DOCUMENTS.SYS_UUID))
                    .creationTime(queryResult.getValue(DOCUMENTS.SYS_DATE_CR))
                    .modificationTime(queryResult.getValue(DOCUMENTS.SYS_DATE_MOD))
                    .author(queryResult.getValue(DOCUMENTS.SYS_AUTHOR))
                    .modifier(queryResult.getValue(DOCUMENTS.SYS_MODIFIER))
                    .filePath(queryResult.getValue(DOCUMENTS.SYS_FILE_PATH))
                    .fileMimeType(queryResult.getValue(DOCUMENTS.SYS_FILE_MIME_TYPE))
                    .fileLength(queryResult.getValue(DOCUMENTS.SYS_FILE_LENGTH))
                    .fileName(queryResult.getValue(DOCUMENTS.SYS_FILE_NAME))
                    .fileStorage(queryResult.getValue(DOCUMENTS.SYS_FILE_STORAGE))
                    .docVersion(queryResult.getValue(DOCUMENTS.SYS_VERSION))
                    .uuid(queryResult.getValue(DOCUMENTS.SYS_UUID))
                    .parent(queryResult.getValue(DOCUMENTS.SYS_PARENT_UUID))
                    .acl(JsonNodeParser.buildObjectNode(queryResult, "sys_acl"))
                    .sourceId(queryResult.getValue(DOCUMENTS.SYS_SOURCE_ID))
                    .sourcePackage(queryResult.getValue(DOCUMENTS.SYS_SOURCE_PACKAGE))
                    .versionParent(queryResult.getValue(DOCUMENTS.VER_PARENT_UUID))
                    .versionSeries(queryResult.getValue(DOCUMENTS.VER_SERIES_UUID))
                    .lastVersion(queryResult.getValue(DOCUMENTS.VER_ISLAST))
                    .versionComment(queryResult.getValue(DOCUMENTS.VER_COMMENT))
                    .data(JsonNodeParser.buildObjectNode(queryResult, fields))
                    .build();
        }


        private static Document convertQueryResultToModelObject(DocumentsRecord queryResult) {
            return Document.getBuilder(queryResult.getSysTitle())
                    .creationTime(queryResult.getSysDateCr())
                    .description(queryResult.getSysDesc())
                    .baseType(queryResult.getSysBaseType())
                    .type(queryResult.getSysType())
                    .data(queryResult.getData())
                    .id(queryResult.getSysUuid())
                    .modificationTime(queryResult.getSysDateMod())
                    .author(queryResult.getSysAuthor())
                    .modifier(queryResult.getSysModifier())
                    .filePath(queryResult.getSysFilePath())
                    .fileMimeType(queryResult.getSysFileMimeType())
                    .fileLength(queryResult.getSysFileLength())
                    .fileStorage(queryResult.getSysFileStorage())
                    .fileName(queryResult.getSysFileName())
                    .docVersion(queryResult.getSysVersion())
                    .parent(queryResult.getSysParentUuid())
                    .uuid(queryResult.getSysUuid())
                    .acl(queryResult.getSysAcl())
                    .sourceId(queryResult.getSysSourceId())
                    .sourcePackage(queryResult.getSysSourcePackage())
                    .versionParent(queryResult.getVerParentUuid())
                    .versionSeries(queryResult.getVerSeriesUuid())
                    .lastVersion(queryResult.getVerIslast())
                    .versionComment(queryResult.getVerComment())
                    .build();
        }

        private static List<Document> convertQueryResultsToModelObjects(List<DocumentsRecord> queryResults) {
//            List<Document> documentEntries = new ArrayList<>();
//
//            for (DocumentsRecord queryResult : queryResults) {
//                Document documentEntry = DocumentConverter.convertQueryResultToModelObject(queryResult);
//                documentEntries.add(documentEntry);
//            }

            return queryResults.stream()
                    .map(DocumentConverter::convertQueryResultToModelObject)
                    .collect(Collectors.toList());
        }

        private static List<Document> convertQueryResults(List<Record> queryResults, String[] fields) {
//            List<Document> documentEntries = new ArrayList<>();
//
//            for (Record queryResult : queryResults) {
//                documentEntries.add(DocumentConverter.convertQueryResultToModelObject(queryResult, fields));
//            }
//
//            return documentEntries;
            return queryResults.stream()
                    .map(r->DocumentConverter.convertQueryResultToModelObject(r, fields))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Page<Document> findAllByParentAndType(UUID parentid, String type, Pageable pageable) {

        LOGGER.trace("entering findAllByParentAndType(parent = {}, type = {})", parentid , type);

        if(parentid == null)
            throw new DocumentNotFoundException("parentId is null");

        if(StringUtils.isBlank(type))
            throw new DocumentNotFoundException("document type is null");

        Table<?> table = getTable(type);
        String curTable = "documents";
        if ("rendition".equals(type)) {
        	curTable = "responses";
        }
        jooq.settings()
        	.withRenderMapping(new RenderMapping()
            	    .withSchemata(
            	        new MappedSchema().withInput("public")
            	                          .withTables(
            	         new MappedTable().withInput("documents")
            	                          .withOutput(curTable))));
        
        Condition cond = DOCUMENTS.SYS_PARENT_UUID.equal(parentid);
        cond = cond.and(DOCUMENTS.VER_ISLAST.isTrue());
        if (CMIS_TYPES.CMIS_FOLDER.getCmisType().equals(type)){
        	cond = cond.and(DOCUMENTS.SYS_BASE_TYPE.equal("folder"));
        }else if (CMIS_TYPES.CMIS_DOCUMENT.getCmisType().equals(type)){
        	cond = cond.and(DOCUMENTS.SYS_BASE_TYPE.equal("document"));
        }else{
        	cond = cond.and(DOCUMENTS.SYS_TYPE.equal(type));
        }
        
        List<Record>  queryResults = jooq.selectFrom(table)
                .where(cond)
                .orderBy(getSortFields(pageable.getSort(), DOCUMENTS, DOCUMENTS.DATA, null))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(DocumentsRecord.class);


        LOGGER.trace("findAllByParentAndType(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResults(queryResults, allFields);

        LOGGER.trace("leaving findAllByParentAndType(): Found {}", documentEntries);
        long totalCount = findTotalCountByType(cond, DOCUMENTS);
        jooq.settings().withRenderMapping(new RenderMapping());
        return new PageImpl<>(documentEntries, pageable, totalCount);
    }

    private DocumentsRecord findDocById(UUID id){
        return  jooq.selectFrom(DOCUMENTS)
                .where(DOCUMENTS.SYS_UUID.equal(id))
                .fetchOne();
    }
    private Record findDocById(String type, UUID id){
    	Table<?> table = getTable(type);
        return  jooq.selectFrom(table)
                .where(DOCUMENTS.SYS_UUID.equal(id))
                .fetchOne();
    }
	@Override
	public int processRetention(String policy, String dateField, Long period, String[] admins, String user) {
		Condition cond = DOCUMENTS.SYS_RETENTION_POLICY.equal(policy);
		cond=cond.and(DOCUMENTS.SYS_STATUS.notEqual("deleted_by_retention"));
		cond=cond.and(getTableField(dateField, DOCUMENTS, DOCUMENTS.DATA, true).lessOrEqual(DSL.timestampAdd(DSL.currentTimestamp(),DSL.val(-period),DatePart.YEAR)));
		int updatedRecordCount = jooq.update(DOCUMENTS)
                .set(DOCUMENTS.SYS_STATUS, "deleted_by_retention")
                .set(DOCUMENTS.SYS_READERS, admins)
                .set(DOCUMENTS.SYS_MODIFIER, user)
                .where(cond)
                .execute();
		
		return updatedRecordCount;
	}
	
	@Override
	public List<String> getDistinct(String field, String type, String query){
		Condition cond = DOCUMENTS.SYS_TYPE.equal(type);
		if (query!=null && query!="")
			cond=cond.and(getTableField(field, DOCUMENTS, DOCUMENTS.DATA, true).startsWith(query));
		return jooq.selectDistinct(getTableField(field, DOCUMENTS, DOCUMENTS.DATA, true))
				.from(DOCUMENTS)
				.where(cond)
				.fetchInto(String.class);
	}

	@Override
	public List<Document> findAllByIds(UUID[] ids, String[] fields) {
		ArrayList<SelectField<?>> selectedFields = new ArrayList<SelectField<?>>();
		selectedFields=selectedDefault(selectedFields,DOCUMENTS);
		if (fields!=null){
            if (fields[0].equals("all")){
                selectedFields.add(DOCUMENTS.DATA);
            }else{
                for (String field : fields) {
                    selectedFields.add(jsonObject(DOCUMENTS.DATA, field).as(field));
                }
            }
        }
		List<Record> queryResults = jooq.select(selectedFields).from(DOCUMENTS)
                .where(DOCUMENTS.SYS_UUID.in(ids))
                .fetch();


        LOGGER.trace("findAllByIds(): Found {} Document entries, they are going to convert to model objects", queryResults);

        return DocumentConverter.convertQueryResults(queryResults, fields);
	}
	
	@Override
	@Cacheable(value = "countByType", cacheManager = "springCM")
    @NoDefDSConnection
	public Long countAllByType(String type, String[] fields, Pageable pageable, String query, JsonNode typeData) {
		Map<String, String> propTypes = getPropertiesType(typeData);
        List<QueryParam> queryParams = getQueryParams(query, propTypes);
        Condition cond = DOCUMENTS.SYS_TYPE.equal(type);
        cond = cond.and(DOCUMENTS.VER_ISLAST.isTrue());
        cond = extendConditions(cond, queryParams, DOCUMENTS, DOCUMENTS.DATA);
        return findTotalCountByType(cond, DOCUMENTS);
	}


}