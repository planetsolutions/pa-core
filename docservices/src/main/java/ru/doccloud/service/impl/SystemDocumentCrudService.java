package ru.doccloud.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jtransfo.JTransfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ru.doccloud.common.datasources.DatasourceSettingsBean;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.document.model.SystemDocument;
import ru.doccloud.repository.SystemRepository;
import ru.doccloud.service.SystemCrudService;
import ru.doccloud.service.document.dto.SystemDTO;

import java.util.*;
import java.util.Map.Entry;

import static ru.doccloud.common.ProjectConst.*;

/**
 * @author Andrey Kadnikov
 */
@Service
public class SystemDocumentCrudService extends AbstractService implements SystemCrudService<SystemDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemDocumentCrudService.class);

	private static final Object zerotype = "00000000-0000-0000-0000-000000000000";

    private final SystemRepository<SystemDocument> repository;
    
    private final Map<String, SystemDTO> modifiedTypes;

    private final JTransfo transformer;

    @Autowired
    public SystemDocumentCrudService(SystemRepository<SystemDocument> repository, JTransfo transformer) {
        super(repository);
        this.repository = repository;
        this.transformer = transformer;
        modifiedTypes = new HashMap<>();
    }

    public SystemRepository getRepository(){
        return this.repository;
    }
    
    @Override
    public Map<String, SystemDTO> getModifiedTypes()  throws Exception{
        return this.modifiedTypes;
    }
    
    @Override
    public ArrayList<String> getDatasourcesNames (){
    	return DatasourceSettingsBean.INSTANCE.getDatasources();
    }
    
    @Override
    public void emptyModifiedTypes() {
        this.modifiedTypes.clear();
    }

    @Override
    public SystemDTO add(final SystemDTO dto, final String user)  throws Exception {
        LOGGER.debug("entering add(dto = {}, user = {})", dto, user);

        repository.setUser(user);
        dto.setAuthor(user);
        SystemDocument persisted = repository.add(dto.getType(), createModel(dto));

        LOGGER.debug("leaving add(): Added Document entry {}", persisted);

        SystemDTO resDTO = transformer.convert(persisted, new SystemDTO());
        if (ENTRY_TYPE_KEY.equals(resDTO.getType())) modifiedTypes.put(resDTO.getSymbolicName(), resDTO);
        return resDTO;
    }


    @Override
    public SystemDTO delete(final UUID id, String type) {
        LOGGER.debug("entering delete(id ={})", id);

        SystemDocument deleted = repository.delete(type, id);

        LOGGER.debug("leaving delete(): Deleted Document  {}", deleted);

        return transformer.convert(deleted, new SystemDTO());
    }

    @Override
    public List<SystemDTO> findAll() {
        LOGGER.debug("entering findAll() ");

        List<SystemDocument> docEntries = repository.findAll();

        LOGGER.debug("leaving findAll(): Found {} Documents", docEntries.size());

        return transformer.convertList(docEntries, SystemDTO.class);
    }

    @Override
    public Page<SystemDTO> findAll(final Pageable pageable, String query) {
        LOGGER.debug("entering findAll(pageable = {})", pageable);

        Page<SystemDocument> searchResults = repository.findAll(pageable, query);

        List<SystemDTO> dtos = transformer.convertList(searchResults.getContent(), SystemDTO.class);

        LOGGER.debug("leaving findAll(): Found {} Documents", searchResults.getNumber());

        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }


    @Override
    public List<SystemDTO> findBySearchTerm(String searchTerm, Pageable pageable){
        LOGGER.debug("entering findBySearchTerm(searchTerm={}, pageable={})", searchTerm, pageable);
        Page<SystemDocument> docPage = repository.findBySearchTerm(searchTerm, pageable);
        LOGGER.debug("leaving findBySearchTerm(): Found {}", docPage);
        return  transformer.convertList(docPage.getContent(), SystemDTO.class);
    }

    @Override
    public SystemDTO findById(final UUID id) {
        LOGGER.debug("entering findById(id = {})", id);

        SystemDocument found = repository.findById(null, id);

        LOGGER.debug("findById(): Found {}", found);

        if (found == null) {
            throw new DocumentNotFoundException("No Document entry found with uuid: " + id);
        }


        return transformer.convert(found, new SystemDTO());
    }

    @Override
    public SystemDTO findById(final String uuid) {
        LOGGER.debug("entering findById(uuid = {})", uuid);

        SystemDocument found = repository.findByUUID(uuid);

        LOGGER.debug("findById(): Found {}", found);

        if (found == null) {
            throw new DocumentNotFoundException("No Document entry found with uuid: " + uuid);
        }

        return transformer.convert(found, new SystemDTO());
    }

    @Override
    public SystemDTO findBySymbolicName(final String symbolic) {
        LOGGER.debug("entering findBySymbolicName(symbolic = {})", symbolic);

        SystemDocument found = repository.findBySymbolicName(symbolic);
        SystemDTO res = transformer.convert(found, new SystemDTO());
        ObjectNode data = (ObjectNode) found.getData();
        if (found.getType().equals(ENTRY_TYPE_KEY)){
        	JsonNode schemaNode = data.get(SCHEMA_KEY);
        	schemaNode = addParentSchema(res, schemaNode, 1);
        	data.put(SCHEMA_KEY, schemaNode);
        }

        LOGGER.debug("leaving findBySymbolicName(): Found {}", found);
        
        res.setData(data);
        return res;
    }
    
    @Override
    public JsonNode addParentSchema(SystemDTO typedoc, JsonNode schemaNode, int curLevel) { 
    	if (typedoc.getParent()!=null) {
            if (!zerotype.equals(typedoc.getParent().toString())) {
                LOGGER.debug("Parent - {}", typedoc.getParent());
                final SystemDTO parenttype = findById(typedoc.getParent());
                if (parenttype != null) {
                    JsonNode parentSchema = parenttype.getData().get("schema");
                    ObjectNode props = (ObjectNode) schemaNode.get(PROPERTIES_KEY);
                    ObjectNode parentprops = (ObjectNode) parentSchema.get(PROPERTIES_KEY);
                    Iterator<Entry<String, JsonNode>> iter = parentprops.fields();
                    while (iter.hasNext()) {
                        Map.Entry<String, JsonNode> entry = iter.next();
                        ObjectNode propValue = (ObjectNode) entry.getValue();
                        propValue.put("level", curLevel);
                        LOGGER.debug("Level for {} - {}", entry.getKey(), curLevel);
                        props.put(entry.getKey(), propValue);
                    }
                    ArrayNode reqProps = (ArrayNode) schemaNode.get(REQUIRED_PROP);
                    ArrayNode parentReqProps = (ArrayNode) parentSchema.get(REQUIRED_PROP);
                    if (reqProps == null && parentReqProps != null) {
                        ((ObjectNode) schemaNode).put(REQUIRED_PROP, parentReqProps);
                    }
                    if (reqProps != null && parentReqProps != null) {
                        reqProps.addAll(parentReqProps);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        LOGGER.debug("schemaNode - {}", mapper.writeValueAsString(schemaNode));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    schemaNode = addParentSchema(parenttype, schemaNode, curLevel++);
                }
            }

        }
		return schemaNode;
	}

    @Override
    public JsonNode findSettings(final String settingsKey) {
        LOGGER.debug("entering findSettings(settingsKey={})", settingsKey);
        SystemDocument found = repository.findSettings(settingsKey);
        LOGGER.debug("leaving findSettings(): Found {}", found);

        return found.getData();
    }


    @Override
    public SystemDTO update(final SystemDTO dto, final String user) {
        LOGGER.debug("entering update(dto={}, user={})", dto, user);

        dto.setModifier(user);
        SystemDocument updated = repository.update(dto.getType(), createModel(dto));

        LOGGER.debug("leaving update(): Updated {}", updated);

        SystemDTO resDTO = transformer.convert(updated, new SystemDTO());
        if (ENTRY_TYPE_KEY.equals(resDTO.getType())) modifiedTypes.put(resDTO.getSymbolicName(), resDTO);
        return resDTO;
    }

    @Override
    public SystemDTO updateFileInfo(final SystemDTO dto){
        LOGGER.debug("entering updateFileInfo(dto={})", dto);
        final SystemDocument updated = repository.updateFileInfo(dto.getType(), createModel(dto));

        LOGGER.debug("leaving updateFileInfo(): Updated {}", updated);

        return transformer.convert(updated, new SystemDTO());
    }


    @Override
    public Page<SystemDTO> findAllByType(final String type, final String[] fields, final Pageable pageable, final String query) {

        LOGGER.debug("entering findAllByType(type={}, fields={}, pageable={}, query={})", type, fields, pageable, query);
        final SystemDTO typedoc = findBySymbolicName(type);
        Page<SystemDocument> searchResults = repository.findAllByType(type, fields, pageable, query, typedoc.getData());

        List<SystemDTO> dtos = transformer.convertList(searchResults.getContent(), SystemDTO.class);

        LOGGER.debug("leaving findAllByType(): Found {} Documents", searchResults.getNumber());
        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }
    
    @Override
    public Page<SystemDTO> findAllByParentAndType(final UUID parentid, String type, final Pageable pageable) {
        return findAllByParentAndTypeLoc(parentid, type, pageable);
    }


    private Page<SystemDTO> findAllByParentAndTypeLoc(final UUID parentid, String type, final Pageable pageable) {
        LOGGER.debug("entering findAllByParentAndType(parentId = {}, type = {})", parentid, type);

        Page<SystemDocument> searchResults = repository.findAllByParentAndType(parentid, type, pageable);

        List<SystemDTO> dtos = transformer.convertList(searchResults.getContent(), SystemDTO.class);

        LOGGER.debug("leaving findAllByParentAndType(): Found {} Documents", dtos);

        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }



    private SystemDocument createModel(SystemDTO dto) {
        return SystemDocument.getBuilder(dto.getTitle())
                .description(dto.getDescription())
                .type(dto.getType())
                .data(dto.getData())
                .id(dto.getId())
                .author(dto.getAuthor())
                .modifier(dto.getModifier())
                .fileLength(dto.getFileLength())
                .fileMimeType(dto.getFileMimeType())
                .fileName(dto.getFileName())
                .filePath(dto.getFilePath())
                .docVersion(dto.getDocVersion())
                .symbolicName(dto.getSymbolicName())
                .parent(dto.getParent())
                .build();
    }

}
