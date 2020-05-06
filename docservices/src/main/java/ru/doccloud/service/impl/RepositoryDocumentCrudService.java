package ru.doccloud.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.jtransfo.JTransfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.doccloud.common.exception.AccessViolationException;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.Link;
import ru.doccloud.document.model.User;
import ru.doccloud.repository.DocumentRepository;
import ru.doccloud.repository.UserRepository;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.SystemCrudService;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.document.dto.LinkDTO;
import ru.doccloud.service.document.dto.SystemDTO;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static ru.doccloud.common.ProjectConst.ENTRY_TYPE_KEY;
import static ru.doccloud.common.ProjectConst.RETENTION_POLICY_TYPE;
import static ru.doccloud.common.util.JsonNodeParser.mergeJson;

/**
 * @author Andrey Kadnikov
 */
@Service
public class RepositoryDocumentCrudService extends AbstractService  implements DocumentCrudService<DocumentDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryDocumentCrudService.class);



    private final DocumentRepository<Document>  repository;

    private final SystemCrudService<SystemDTO> sysService;
    
    private final UserRepository userRepository;
    

    
    private final JTransfo transformer;

    @Autowired
    public RepositoryDocumentCrudService(DocumentRepository<Document> repository, SystemCrudService<SystemDTO> sysService,
                                         UserRepository userRepository, JTransfo transformer) {

        super(repository);

        LOGGER.trace("RepositoryDocumentCrudService(transformer={})", transformer);
        this.repository = repository;
        this.sysService = sysService;
        this.transformer = transformer;
        this.userRepository = userRepository;
    }

    public DocumentRepository getRepository(){
        return this.repository;
    }

    @Override
    public DocumentDTO add(final DocumentDTO dto, final String user) throws Exception {
        LOGGER.debug("entering add(dto = {}, user = {})", dto, user);
        if (dto.getType().startsWith("abstract_")){
        	throw new Exception("Document can not be created based on abstract type.");
        }
        repository.setUser(user);
        dto.setAuthor(user);
        
        List<String> readersArr = new ArrayList<>();
        readersArr.add(user);
        
        final SystemDTO typedoc = sysService.findBySymbolicName(dto.getType());

        LOGGER.debug(" add(): typeDoc {}", typedoc);
        JsonNode accessFromType = null;
        if (typedoc!=null){
            final JsonNode retentionPolicy = typedoc.getData().get(RETENTION_POLICY_TYPE);

        	if (retentionPolicy!=null)
        		dto.setRetentionPolicy(retentionPolicy.asText());
        	
        	accessFromType = typedoc.getData().get("access");
        	
        	if (dto.getReaders()!=null){
        		Collections.addAll(readersArr,dto.getReaders());
        		if (accessFromType instanceof ObjectNode){
        			ObjectMapper mapper = new ObjectMapper();
        			ArrayNode areaders = (ArrayNode) mapper.readTree("[]");
        			for (String sreader: readersArr){
        				areaders.add(sreader);
        			}
        			((ObjectNode) accessFromType).put("sourceReaders", areaders);
        		}
        	}
        	
        	if (accessFromType instanceof ArrayNode){
		        if (accessFromType.isArray()){
			        for (JsonNode acc: accessFromType){
			        	LOGGER.debug("add(): reader - {}",acc.asText());
			        	readersArr.add(acc.asText());
			        }
		        }
	        }else{
	        	ArrayNode readersFromAcl = (ArrayNode) accessFromType.get("read");
	        	if (readersFromAcl.isArray()){
			        for (JsonNode acc: readersFromAcl){
			        	LOGGER.debug("add(): reader from ACL- {}",acc.asText());
			        	readersArr.add(acc.asText());
			        }
		        }
	        }
	        validateSchema(typedoc,dto);
	        
        }
        
        String[] readers = readersArr.toArray(new String[0]);
        LOGGER.debug("add(): readers {}", readers);
        dto.setReaders(readersArr.toArray(new String[0]));
        dto.setAcl(accessFromType);
        
        
        if (dto.getBaseType() == null) dto.setBaseType("document");
        LOGGER.info("Package from DTO - "+dto.getSourcePackage());
        LOGGER.debug("add(): dto = {}", dto);

        final Document document = createModel(dto);
        LOGGER.info("Package - "+document.getSourcePackage());
        LOGGER.debug("add(): documentEntry= {}", document);
        Document persisted = repository.add(dto.getType(), document);
        
        LOGGER.debug("leaving add(): Added Document entry {}", persisted);

        return transformer.convert(persisted, new DocumentDTO());
    }
    
	private void validateSchema(SystemDTO typedoc, DocumentDTO dto) throws Exception {
    	if(dto.getData()!=null){
            JsonNode schemaNode = typedoc.getData().get("schema");
            schemaNode = sysService.addParentSchema(typedoc, schemaNode, 1);
            if (!schemaNode.isNull()){
                ObjectMapper mapper = new ObjectMapper();
                try {
                    LOGGER.debug("validateSchema(): Schema - {}",mapper.writeValueAsString(schemaNode));

                    JsonNode props=schemaNode.get("properties");
                    Iterator<Entry<String, JsonNode>> iter = props.fields();
                    if(dto.getData() instanceof ObjectNode){
                        final ObjectNode dataNode = (ObjectNode) dto.getData();
                        if(dataNode!=null){
                            LOGGER.debug("validateSchema(): dataNode origin - {}",dataNode);
                            boolean hasChanges = false;
                            while (iter.hasNext()) {
                                  Map.Entry<String, JsonNode> entry = iter.next();

                                  if(entry.getValue()==null)
                                      continue;

                                  final JsonNode typeNode = entry.getValue().get(ENTRY_TYPE_KEY);

                                  if (typeNode !=null){
                                      final String typeNodeValue = typeNode.textValue();

                                      LOGGER.debug("validateSchema(): entry {} - {}",entry.getKey(), typeNodeValue);

                                      try {
                                          final JsonNode node = dataNode.get(entry.getKey());
                                          final String strProp = node != null ? dataNode.get(entry.getKey()).textValue() : null;
                                          if(strProp != null) {
                                              if ("integer".equals(typeNodeValue)) {
                                                  final Long longProp = Long.parseLong(strProp);
                                                  dataNode.put(entry.getKey(),longProp);
                                              } else if("number".equals(typeNodeValue)) {
                                                  final Double longProp = Double.parseDouble(strProp);
                                                  dataNode.put(entry.getKey(),longProp);
                                              }
                                              hasChanges=true;
                                              break;
                                          }
                                      } catch (Exception e){
                                          LOGGER.error("validateSchema(): Exception: {}", e);
                                      }


//                                      String strProp = "";

    //                                  if ("integer".equals(typeNodeValue)){
    //                                      try{
    //                                          if(dataNode.get(entry.getKey())!=null){
    //                                              strProp = dataNode.get(entry.getKey()).textValue();
    //                                              if (strProp!=null){
    //                                                  Long longProp = Long.parseLong(strProp);
    //                                                  dataNode.put(entry.getKey(),longProp);
    //                                                  hasChanges=true;
    //                                              }
    //                                          }
    //                                      }catch (Exception e){
    //                                          LOGGER.error("validateSchema(): Exception: {}", e);
    //                                      }
    //                                  }
    //                                  if("number".equals(typeNodeValue)){
    //                                      try{
    //                                          if(dataNode.get(entry.getKey())!=null){
    //                                              strProp = dataNode.get(entry.getKey()).textValue();
    //                                              if (strProp!=null){
    //                                                  Double longProp = Double.parseDouble(strProp);
    //                                                  dataNode.put(entry.getKey(),longProp);
    //                                                  hasChanges=true;
    //                                              }
    //                                          }
    //                                      }catch (Exception e){
    //                                          LOGGER.error("validateSchema(): Exception: {}", e);
    //                                      }
    //
    //
    //                                  }
                                  }
                            }


                            if (hasChanges)	{
                                LOGGER.debug("validateSchema(): dataNode has changed - {}",dataNode);
                                dto.setData(dataNode);

                            }
                        }
                    }

                    JSONObject rawSchema = new JSONObject(mapper.writeValueAsString(schemaNode));
                    Schema schema = SchemaLoader.load(rawSchema);
                    LOGGER.debug("validateSchema(): Data - {}",mapper.writeValueAsString(dto.getData()));
                    schema.validate(new JSONObject(mapper.writeValueAsString(dto.getData())));
                } catch (JSONException | JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    
    }

	@Override
	public DocumentDTO findBySourceID(String sourceId)  throws Exception {
		Document persisted = repository.findBySourceID(sourceId);
		return transformer.convert(persisted, new DocumentDTO());
	}
	
    @Override
    public DocumentDTO addToFolder(final DocumentDTO dto, final String folderId, String user) throws Exception {
        LOGGER.debug("entering addToFolder(dto = {}, folderId={})", dto, folderId);

        Document persisted = null;
//        try to find document in database
        if(dto.getId() != null) {
            persisted = repository.findById(dto.getType(),dto.getId());
        }

        if(persisted == null)
            persisted = repository.add(dto.getType(), createModel(dto));

        dto.setParent(UUID.fromString(folderId)); 
        setParent(dto, user);
        //Link link = repository.addLink(folderId, persisted.getId());

        //LOGGER.debug("leaving addToFolder(): Added Document entry  {} with link {}", persisted, link);

        return transformer.convert(persisted, new DocumentDTO());
    }

    @Override
    public List<DocumentDTO> findByPath(String path)  {
        LOGGER.debug("entering findDocumentByPath(path = {})", path);
        List<Document> documents = repository.findByPath(path);

        LOGGER.debug("leaving findDocumentByPath(): found {}", documents);
        return transformer.convertList(documents, DocumentDTO.class);
    }

    @Override
    public DocumentDTO delete(final UUID id, String type) throws Exception {
        LOGGER.debug("entering delete(id ={})", id);

        Document deleted = null;
        DocumentDTO doc = findById(id, type, true);
        ArrayNode roles = (ArrayNode) doc.getAcl();
        if (!hasRole(roles,"delete")){
        	throw new AccessViolationException("User don't have permissions to delete document.");
        }
        
        LinkDTO deletedLink = null;
        
        try {

            DocumentDTO parent = findById(doc.getParent(), null, false);

            LOGGER.debug("deleteObject(): parent document {}", parent);

            if (parent != null) {
                deletedLink = deleteLink(parent.getId(), doc.getId());
                LOGGER.debug("deleteObject(): link has been deleted {}", deletedLink);
            }

            // delete doc
            deleted = repository.delete(type, id);
            LOGGER.debug("leaving deleteObject(): document {} has been deleted successfully", deleted);
        } catch (Exception e){
            LOGGER.error("deleteObject(): exception {} ", e.getMessage());
            if(deletedLink != null)
                addLink(deletedLink.getHead_id(), deletedLink.getTail_id(),"");
        }

        LOGGER.debug("leaving delete(): Deleted Document  {}", deleted);

        return transformer.convert(deleted, new DocumentDTO());
    }

    @Override
    public List<DocumentDTO> findAll()  throws Exception {
        LOGGER.debug("entering findAll() ");

        List<Document> docEntries = repository.findAll();

        LOGGER.debug("leaving findAll(): Found {} Documents", docEntries.size());

        return transformer.convertList(docEntries, DocumentDTO.class);
    }
    
    @Override
    public List<DocumentDTO> findAllVersions(UUID seriesUUID)  throws Exception {
        LOGGER.debug("entering findAllVersions() ");

        List<Document> docEntries = repository.findAllVersions(seriesUUID);

        LOGGER.debug("leaving findAllVersions(): Found {} Documents", docEntries.size());

        return transformer.convertList(docEntries, DocumentDTO.class);
    }

    @Override
    public Page<DocumentDTO> findAll(final Pageable pageable, String query)  throws Exception {
        LOGGER.debug("entering findAll(pageable = {})", pageable);

        Page<Document> searchResults = repository.findAll(pageable, query);
        List<DocumentDTO> dtos = transformer.convertList(searchResults.getContent(), DocumentDTO.class);

        LOGGER.debug("leaving findAll(): Found {} Documents", searchResults.getNumber());

        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }

    @Override
    public List<DocumentDTO> findParents(UUID docId)  throws Exception{
        LOGGER.debug("entering findParents(docId = {})", docId);
        final List<Document> docEntries = repository.findParents(docId);

        LOGGER.debug("leaving findParents(): Found: {}", docEntries);

        return docEntries == null ? null : transformer.convertList(docEntries, DocumentDTO.class);
    }
    
    @Override
    public Page<DocumentDTO> findAllByLinkHead(UUID head, String type, Pageable pageable)  throws Exception{
        LOGGER.debug("entering findAllByLinkHead(head = {})", head);
        final Page<Document> docEntries = repository.findAllByLinkHead(head, type, pageable);

        LOGGER.debug("leaving findAllByLinkHead(): Found: {}", docEntries);
        List<DocumentDTO> dtos = transformer.convertList(docEntries.getContent(), DocumentDTO.class);

        LOGGER.debug("leaving findAllByLinkHead(): Found {} Documents", docEntries.getNumber());

        return new PageImpl<>(dtos,
                new PageRequest(docEntries.getNumber(), docEntries.getSize(), docEntries.getSort()),
                docEntries.getTotalElements()
        );
    }

//todo remove this use only RepositoryDocumentSearchService
    @Override
    public List<DocumentDTO> findBySearchTerm(String searchTerm, Pageable pageable)  throws Exception{
        LOGGER.debug("entering findBySearchTerm(searchTerm={}, pageable={})", searchTerm, pageable);
        Page<Document> docPage = repository.findBySearchTerm(searchTerm, pageable);
        LOGGER.debug("leaving findBySearchTerm(): Found {}", docPage);
        return  transformer.convertList(docPage.getContent(), DocumentDTO.class);
    }

//    @Override
//    public DocumentDTO findById(final UUID id) throws Exception {
//    	return getDocumentById(id, null, false);
//    }
    		
    @Override
    public DocumentDTO findById(final UUID id, String type, boolean getAcl) throws Exception {
       return getDocumentById(id, type, getAcl);
    }

    private DocumentDTO getDocumentById(final UUID id, String type, boolean getAcl) throws Exception{
        LOGGER.debug("entering getDocumentById(id = {})", id);

        Document found = repository.findById(type, id);

        if (found == null) {
            throw new DocumentNotFoundException("No Document entry found with uuid: " + id);
        }

//        if(found != null) {
            if (found.getAcl() != null && !found.getAcl().isNull()) {
                if (getAcl) {
                    found.setAcl(convertACLtoRoles(found.getAcl()));
                } else {
                    found.setAcl(null);
                }
            }
//        } else {
//            LOGGER.debug("getDocumentById(): no document found with id {}", id);
//        }

        LOGGER.debug("leaving getDocumentById(): Found {}", found);

        return transformer.convert(found, new DocumentDTO());
    }

    private enum AccessLevel {
    	full,
        delete,
        modify_security,
        change_content,
        edit_prop,
        view_prop,
        read
    }

    private JsonNode convertACLtoRoles(JsonNode Acl){
    	ObjectMapper mapper = new ObjectMapper();
    	ArrayNode roles =  mapper.createArrayNode();
    	String userName = getRequestUser();
    	
    	if (userName!=null){
	    	User ur = userRepository.getUser(userName);
	    	String[] userGroups = ur.getGroups();
	    	boolean hasRole = false;
	    	for (AccessLevel level : AccessLevel.values()) {
	    	//while (rolesNames.hasNext()) {
		    //    String roleName = rolesNames.next();
	    		String roleName = level.toString();
	    		if(!hasRole){
			        JsonNode roleValue = Acl.get(roleName);
			        if (roleValue!=null && roleValue.isArray()){
			    	    for (final JsonNode roleInList : roleValue) {
			    	    	for (String userRole : userGroups) {
			    	    		if (userRole.equals(roleInList.textValue())) hasRole = true;
			    	    	}
			    	    }
			        }
	    		}
		        LOGGER.debug("Has role {} - {}",roleName,hasRole);
		        if (hasRole) roles.add(roleName);
		    }
    	}
	    return roles;
    }
    
    private boolean hasRole(ArrayNode arr, String role){
    	LOGGER.debug("Has role - {}", role);
    	
    	if (arr.isArray()){
	        for (JsonNode acc: arr){
	        	LOGGER.debug("Check role - {}",acc.asText());
	        	if (acc.asText().equals(role)) return true;
	        }
        }
    	return false;
    }
//    @Override
//    public DocumentDTO findById(final String uuid) {
//        LOGGER.debug("entering findById(uuid = {})", uuid);
//
//        Document found = repository.findByUUID(uuid);
//
//        LOGGER.debug("findById(): Found {}", found);
//
//        if (found == null) {
//            throw new DocumentNotFoundException("No Document entry found with uuid: " + uuid);
//        }
//
//        return transformer.convert(found, new DocumentDTO());
//    }

    @Override
    public DocumentDTO update(final DocumentDTO dto, final String user) throws Exception, AccessViolationException {
        LOGGER.debug("entering update(dto={}, user={})", dto, user);

        DocumentDTO fromDb = getDocumentById(dto.getId(), dto.getType(),true);
        if (fromDb.getAcl()!=null){
        ArrayNode roles = (ArrayNode) fromDb.getAcl();
        if (dto.getType() != null && !dto.getType().equals(fromDb.getType())){
        	if (!hasRole(roles,"full")){
        		dto.setType(fromDb.getType());
        	}
        }
        
        if (!hasRole(roles,"edit_prop")){
        	throw new AccessViolationException("User don't have permissions to edit document properties.");
        }
        }
        dto.setModifier(user);
        
        if (dto.getData() != null){ 
			JsonNode defaults = fromDb.getData();
	    	JsonNode merged = mergeJson(defaults,dto.getData());
			dto.setData(merged);
		}
        final SystemDTO typedoc = sysService.findBySymbolicName(dto.getType());
        if (typedoc!=null){
	        validateSchema(typedoc,dto);
	        
        }
        List<String> readersArr = new ArrayList<String>();
        readersArr.add(user);
        dto.setReaders(readersArr.toArray(new String[0]));
        
        Document updated = repository.update(dto.getType(), createModel(dto));

        LOGGER.debug("leaving update(): Updated {}", updated);

        return transformer.convert(updated, new DocumentDTO());
    }

    @Override
    public DocumentDTO updateFileInfo(final DocumentDTO dto, String user){
        LOGGER.debug("entering updateFileInfo(dto={})", dto);

        /*DocumentDTO fromDb = getDocumentById(dto.getId(),true);
        ArrayNode roles = (ArrayNode) fromDb.getAcl();
        if (!hasRole(roles,"change_content")){
        	throw new AccessViolationException("User don't have permissions to edit document content.");
        }*/
        
        dto.setModifier(user);
        
        final Document updated = repository.updateFileInfo(dto.getType(), createModel(dto));

        LOGGER.debug("leaving updateFileInfo(): Updated {}", updated);

        return transformer.convert(updated, new DocumentDTO());
    }
    
	@Override
	public JsonNode processRetention(String remoteUser)  throws Exception {
		String[] fields = {"all"};
		String[] admins = {"admins"};
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode res = mapper.createObjectNode();
		Page<SystemDTO> policies = sysService.findAllByType(RETENTION_POLICY_TYPE, fields, new PageRequest(0, Integer.MAX_VALUE), null);
		for (SystemDTO policy : policies) {
			int updatedRecordCount = repository.processRetention(policy.getSymbolicName(),policy.getData().get("date_field").asText(),policy.getData().get("years_before_retention").asLong(),admins,remoteUser);
			res.put(policy.getSymbolicName(), updatedRecordCount);
		}
		return res;
	}
    
    @Override
    public DocumentDTO setParent(final DocumentDTO dto, String user)  throws Exception{
        LOGGER.debug("entering setParent(dto={})", dto);
        
        dto.setModifier(user);
        
        final Document updated = repository.setParent(createModel(dto));

        LOGGER.debug("leaving setParent(): Updated {}", updated);

        return transformer.convert(updated, new DocumentDTO());
    }

    @Override
    public LinkDTO addLink(UUID headId, UUID tailId, String type)  throws Exception {
        LOGGER.debug("entering addLink(headId={}, tailId = {})", headId, tailId);

        Link link = repository.addLink(headId, tailId, type);
        LOGGER.debug("leaving addLink(): Link {}", link);
        return transformer.convert(link, new LinkDTO());
    }
    @Override
    public LinkDTO deleteLink(UUID headId, UUID tailId)  throws Exception {
        LOGGER.debug("entering deleteLink(headId={}, tailId = {})", headId, tailId);
        Link link = repository.deleteLink(headId, tailId);
        LOGGER.debug("leaving deleteLink(): Link {}", link);
        return transformer.convert(link, new LinkDTO());
    }


    @Override
    public Page<DocumentDTO> findAllByType(final String type, final String[] fields, final Pageable pageable, final String query)  throws Exception {

        LOGGER.debug("entering findAllByType(type={}, fields={}, pageable={}, query={})", type, fields, pageable, query);
        final SystemDTO typedoc = sysService.findBySymbolicName(type);
        Page<Document> searchResults = repository.findAllByType(type, fields, pageable, query, typedoc.getData());

        List<DocumentDTO> dtos = transformer.convertList(searchResults.getContent(), DocumentDTO.class);

        LOGGER.debug("leaving findAllByType(): Found {} Documents", searchResults.getNumber());
        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }

    @Override
    public Page<DocumentDTO> findAllByParent(final UUID parentid, Pageable pageable)  throws Exception {
        LOGGER.debug("entering findAllByParent(parentId = {})", parentid);

        Page<Document> searchResults = repository.findAllByParent(parentid, pageable);

        List<DocumentDTO> dtos = transformer.convertList(searchResults.getContent(), DocumentDTO.class);

        LOGGER.debug("leaving findAllByParentAndType(): Found {} Documents", dtos);

        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }
    
    @Override
    public JsonNode getDistinct(String field, String type, String query){
    	ObjectMapper mapper = new ObjectMapper();
	    JsonNode result = null;
		try {
			result = mapper.readTree("{\"result\":[]}");
		
		    ArrayNode resarr = (ArrayNode) result.path("result");
		    List<String> res = repository.getDistinct(field, type, query);
			for (String queryResult : res) {
				resarr.add(queryResult);
			}
		} catch (JsonProcessingException e) {
			LOGGER.error("getDistinct JsonProcessingException - {}", e.getMessage());
		} catch (IOException e) {
			LOGGER.error("getDistinct IOException - {}", e.getMessage());
		}
		return result;
    }

    @Override
    public String getRequestUser() {
        return super.getRequestUser();
    }

    @Override
    public Page<DocumentDTO> findAllByParentAndType(final UUID parentid, String type, final Pageable pageable)  throws Exception {
        LOGGER.debug("entering findAllByParentAndType(parentId = {}, type = {})", parentid, type); 

        Page<Document> searchResults = repository.findAllByParentAndType(parentid, type, pageable);

        List<DocumentDTO> dtos = transformer.convertList(searchResults.getContent(), DocumentDTO.class);

        LOGGER.debug("leaving findAllByParentAndType(): Found {} Documents", dtos);

        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }
    
    @Override
    public Page<DocumentDTO> findInPgBySearchTerm(String searchTerm, Pageable pageable) {
        Page<Document> searchResults = repository.findInPgBySearchTerm(searchTerm, pageable);

        List<DocumentDTO> dtos = transformer.convertList(searchResults.getContent(), DocumentDTO.class);

        LOGGER.debug("leaving findBySearchTerm(): found {}", dtos);

        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }

    @Override
    public String convertResultToCsv(List<DocumentDTO> searchRes, String[] docFields) throws Exception {
    	CsvMapper csvMapper = new CsvMapper();
    	Builder schemaBuilder = CsvSchema.builder(); 
    	for (String field : docFields) {
        	schemaBuilder.addColumn(field);
        }
        //csvMapper.schemaFor(DocumentDTO.class).withHeader();
        CsvSchema schema = schemaBuilder.build().withHeader();
        List<JsonNode> dataList = new ArrayList<>();
        for (DocumentDTO doc : searchRes){
        	dataList.add(doc.getData());
        }
        String csvRes = csvMapper.writer(schema).writeValueAsString(dataList);
        LOGGER.info("csvRes - {}",csvRes);
        return csvRes;
    }
    
    public List<DocumentDTO> findAllByIds(UUID[] Ids, String[] fields){
    	LOGGER.debug("entering findAllByIds(Ids = {})", Ids.toString());
        final List<Document> docEntries = repository.findAllByIds(Ids, fields);
        List<Document> finalEntries = getFolderContent(docEntries);
        LOGGER.debug("leaving findAllByIds(): Found: {}", finalEntries);
        return transformer.convertList(finalEntries, DocumentDTO.class);
    }
    
    private List<Document> getFolderContent(List<Document> docEntries){
    	ListIterator<Document> docIter = docEntries.listIterator();
    	List<Document> finalEntries = new ArrayList<Document>();
        while(docIter.hasNext()){
        	Document doc = docIter.next();
        	finalEntries.add(doc);
        	if (doc.getBaseType().equals("folder")){
        		Page<Document> searchResults = repository.findAllByParent(doc.getId(), new PageRequest(0, Integer.MAX_VALUE));
        		finalEntries.addAll(getFolderContent(searchResults.getContent()));
        	}
        }
        return finalEntries;
    }
    @Override
    public Long countAllByType(final String type, final String[] fields, final Pageable pageable, final String query)  throws Exception {
    	final SystemDTO typedoc = sysService.findBySymbolicName(type);
        return repository.countAllByType(type, fields, pageable, query, typedoc.getData()); 
    }

    
    private Document createModel(DocumentDTO dto) {
        return Document.getBuilder(dto.getTitle())
                .description(dto.getDescription())
                .type(dto.getType())
                .baseType(dto.getBaseType())
                .parent(dto.getParent())
                .readers(dto.getReaders())
                .data(dto.getData())
                .id(dto.getId())
                .author(dto.getAuthor())
                .modifier(dto.getModifier())
                .fileLength(dto.getFileLength())
                .fileMimeType(dto.getFileMimeType())
                .fileName(dto.getFileName())
                .filePath(dto.getFilePath())
                .docVersion(dto.getDocVersion())
                .fileStorage(dto.getFileStorage())
                .sourceId(dto.getSourceId())
                .sourcePackage(dto.getSourcePackage())
                .acl(dto.getAcl())
                .retentionPolicy(dto.getRetentionPolicy())
                .versionParent(dto.getVersionParent())
                .versionSeries(dto.getVersionSeries())
                .lastVersion(dto.isLastVersion())
                .uuid(dto.getUuid())
                .build();
    }

}
