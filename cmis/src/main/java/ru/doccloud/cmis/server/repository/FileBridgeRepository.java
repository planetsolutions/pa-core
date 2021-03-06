
package ru.doccloud.cmis.server.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.*;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.*;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import ru.doccloud.cmis.server.FileBridgeTypeManager;
import ru.doccloud.cmis.server.util.FileBridgeUtils;
import ru.doccloud.common.exception.AccessViolationException;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.common.util.JsonNodeParser;
import ru.doccloud.common.util.VersionHelper;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.FileService;
import ru.doccloud.service.document.dto.DocumentDTO;

import java.io.*;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

import static ru.doccloud.cmis.server.util.FileBridgeUtils.*;


public class FileBridgeRepository extends AbstractFileBridgeRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBridgeRepository.class);

    private static final String CMIS_READ_WRITE_ROLE_NAME = "readwrite";

	private static final String zeroUUID = "00000000-0000-0000-0000-000000000000";

    private final DocumentCrudService<DocumentDTO> crudService;

    private FileService fileService;


    public FileBridgeRepository(final String repositoryId, String rootPath,
                                final FileBridgeTypeManager typeManager,  DocumentCrudService<DocumentDTO> crudService,
                                FileService fileService) throws Exception {
        super(repositoryId, typeManager, rootPath);

        LOGGER.trace("entering FileBridgeRepository(repositoryId={}, rootPath = {}, typeManager={},  crudService= {}, fileService={})",
                repositoryId, rootPath, typeManager, crudService, fileService);

        this.crudService = crudService;
        this.fileService = fileService;

        LOGGER.trace("leaving FileBridgeRepository()");
    }

    // --- CMIS operations ---

    /**
     * CMIS getTypesChildren.
     */
    public TypeDefinitionList getTypeChildren(CallContext context, String typeId, Boolean includePropertyDefinitions,
                                              BigInteger maxItems, BigInteger skipCount) throws Exception {
        checkUser(context, false);

        return typeManager.getTypeChildren(context, typeId, includePropertyDefinitions, maxItems, skipCount);
    }

    /**
     * CMIS getTypesDescendants.
     */
    public List<TypeDefinitionContainer> getTypeDescendants(CallContext context, String typeId, BigInteger depth,
                                                            Boolean includePropertyDefinitions) throws Exception {
        checkUser(context, false);

        return typeManager.getTypeDescendants(context, typeId, depth, includePropertyDefinitions);
    }

    /**
     * CMIS getTypeDefinition.
     */
    public TypeDefinition getTypeDefinition(CallContext context, String typeId) throws Exception {
        checkUser(context, false);

        return typeManager.getTypeDefinition(context, typeId);
    }

    /**
     * Create* dispatch for AtomPub.
     */
    public ObjectData create(CallContext context, Properties properties, String folderId, ContentStream contentStream,
                             VersioningState versioningState, ObjectInfoHandler objectInfos) throws Exception {
        LOGGER.debug("entering create(context={}, properties = {}, folderId={}, versionState={}, objectInfos={})", context, properties, folderId, versioningState, objectInfos);
        boolean userReadOnly = checkUser(context, true);

        final String typeId = getObjectTypeId(properties);
        LOGGER.debug("create(): typeId {}", typeId);
        if (StringUtils.isBlank(typeId)) {
            throw new CmisInvalidArgumentException("Type Id is not set!");
        }
        TypeDefinition type = getTypeDefinitionByTypeId(typeId);
        LOGGER.debug("create(): TypeDefinition {}", type);
        DocumentDTO doc;
        if (type.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
            doc = createDocument(context, properties, folderId, contentStream, versioningState, type);
        } else if (type.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
            doc = createFolder(context, properties, folderId, type);
        } else {
            throw new CmisObjectNotFoundException("Cannot create object of type '" + typeId + "'!");
        }

        DocumentDTO parent = null;
        if (doc.getParent()!=null)  parent = getParentDocument(doc.getParent().toString());
        LOGGER.debug("create(): parent document {}", parent);

        LOGGER.debug("leaving create(): document with id has been created {}", doc);
        return compileObjectData(context, doc, parent, null, false, false, userReadOnly, objectInfos);
    }

    /**
     * CMIS createDocument.
     */
    private DocumentDTO createDocument(CallContext context, Properties properties, String folderId,
                                       ContentStream contentStream, VersioningState versioningState, TypeDefinition type) throws Exception {
        LOGGER.trace("entering createDocument(context={}, properties = {}, folderId={}, versionState={}, type= {})", context, properties, folderId, versioningState, type);
        checkUser(context, true);

        // get parent
        final DocumentDTO parent = getParentDocument(folderId);
        LOGGER.trace("createDocument(): parent is {}", parent);
        if (parent == null || !isFolder(parent.getBaseType())) {
            throw new CmisObjectNotFoundException("Parent is not a folder!");
        }
        DocumentDTO doc = null;
        try{
            checkNewProperties(properties, BaseTypeId.CMIS_DOCUMENT, type);

            final String name = FileBridgeUtils.getStringProperty(properties, PropertyIds.NAME);
            final String description = FileBridgeUtils.getStringProperty(properties, PropertyIds.DESCRIPTION);

            LOGGER.trace("createDocument(): name is {}, description is {}", name, description);

            doc = new DocumentDTO(name, type.getId(), getUserNameFromJwtToken(context));
            if (description!=null ) doc.setDescription(description);
            
            ObjectMapper mapper = new ObjectMapper();
            String base = "{}";
            ObjectNode res = (ObjectNode) mapper.readTree(base);
            for (PropertyData prop : properties.getPropertyList()){
            	if (!prop.getId().startsWith("cmis:")){
	        		if (prop instanceof PropertyInteger) {
	        			res.put(prop.getId(), ((PropertyInteger) prop).getFirstValue().intValue());
	        		}
	        		if (prop instanceof PropertyDecimal) {
	        			res.put(prop.getId(), ((PropertyDecimal) prop).getFirstValue().doubleValue());
	        		}
	        		if (prop instanceof PropertyString) {
	        			res.put(prop.getId(), ((PropertyString) prop).getFirstValue());
	        		}
	        		if (prop instanceof PropertyDateTime) {
	        			res.put(prop.getId(), getDateFromProperty((PropertyDateTime) prop));
	        		}
            	}
            }
        	doc.setData(res);
            doc.setDocVersion(VersionHelper.generateMinorDocVersion(doc.getDocVersion()));
            doc = crudService.add(doc, getUserNameFromJwtToken(context));

            LOGGER.trace("createDocument(): Document has been created {}", doc);
            crudService.addToFolder(doc, parent.getId().toString(), getUserNameFromJwtToken(context));

            LOGGER.trace("createDocument(): contentStream  {} ", getContentStreamInfo(contentStream));
            writeContentFromStream(doc, contentStream, getUserNameFromJwtToken(context));



        }catch (Exception e){
        	LOGGER.error("createDocumentFromSource(): Exception {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
         LOGGER.debug("leaving createDocument(): created document {}", doc);
         return doc;
    }


    /**
     * CMIS createDocumentFromSource.
     */
    public String createDocumentFromSource(CallContext context, String sourceId, Properties properties,
                                           String folderId, VersioningState versioningState) throws Exception {
        LOGGER.debug("entering createDocumentFromSource(context={}, sourceId= {}, properties = {}, folderId={}, versionState={})",
                context, sourceId, properties, folderId, versioningState);
        checkUser(context, true);

        // get parent
        final DocumentDTO parent = getParentDocument(folderId);

        LOGGER.debug("createDocumentFromSource(): parent document is {}", parent);

        if (parent == null || !isFolder(parent.getBaseType())) {
            throw new CmisObjectNotFoundException("Parent is not a folder!");
        }
        DocumentDTO doc = null;
        try {
            // get source
            final DocumentDTO source = getDocument(sourceId);
            LOGGER.debug("createDocumentFromSource(): source document is {}", source);

            doc = crudService.add(new DocumentDTO(source.getTitle(), "document", getUserNameFromJwtToken(context)), getUserNameFromJwtToken(context));

            LOGGER.debug("createDocumentFromSource(): Document has been created {}", doc);
            crudService.addToFolder(doc, parent.getId().toString(), getUserNameFromJwtToken(context));

            // copy content
            final JsonNode storageSettings = fileService.getStorageSettingsByDocType(doc.getType());
            String filePath = writeContent(doc, new FileInputStream(source.getFilePath()), storageSettings);
            LOGGER.debug("createDocumentFromSource(): content was written filePath {}, storage = {}", filePath);
            if(filePath != null) {
                doc.setFilePath(filePath);
                doc.setFileMimeType(source.getFileMimeType());
                doc.setFileLength(source.getFileLength());
                doc.setFileName(source.getFileName());
                doc.setModifier(getUserNameFromJwtToken(context));
                doc.setFileStorage(JsonNodeParser.getStorageAreaName(storageSettings));
                doc = crudService.update(doc, getUserNameFromJwtToken(context));
            }

            return getId(doc.getId().toString());

        } catch (Exception e){
            if(doc != null && doc.getId() != null) {
                //crudService.deleteLink(parent.getId(), doc.getId());
                crudService.delete(doc.getId(), doc.getType());
            }
            LOGGER.error("createDocumentFromSource(): Exception {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * CMIS createFolder.
     */
    private DocumentDTO createFolder(CallContext context, Properties properties, String folderId, TypeDefinition type) throws Exception {
        checkUser(context, true);
        LOGGER.debug("entering createFolder(context={}, properties = {}, folderId={}, type={})", context, properties, folderId, type);
        // get parent
        if ("0".equals(folderId)) folderId=zeroUUID;
        DocumentDTO parent = getParentDocument(folderId);
        LOGGER.debug("createFolder(): parent is {}", parent);

        // check properties
        checkNewProperties(properties, BaseTypeId.CMIS_FOLDER, type);
        if (parent == null || !isFolder(parent.getBaseType())) {
            throw new CmisObjectNotFoundException("Parent is not a folder!");
        }

        // create the folder
        String name = FileBridgeUtils.getStringProperty(properties, PropertyIds.NAME);

        LOGGER.debug("createFolder(): name is {}", name);
        DocumentDTO doc = null;
        
        doc = new DocumentDTO(name, type.getId(), getUserNameFromJwtToken(context));
        doc.setBaseType("folder");
        doc.setDocVersion(VersionHelper.generateMinorDocVersion(doc.getDocVersion()));
        doc = crudService.add(doc, getUserNameFromJwtToken(context));
        crudService.addToFolder(doc, folderId, getUserNameFromJwtToken(context));

        LOGGER.debug("leaving createFolder(): created folder {}", doc);
        return doc;
    }


    /**
     * CMIS moveObject.
     */
    public ObjectData moveObject(CallContext context, Holder<String> objectId, String targetFolderId,
                                 ObjectInfoHandler objectInfos) throws Exception {
        LOGGER.debug("entering moveObject(context={}, objectId={}, targetFolderId={}, objectInfos = {})", context, objectId, targetFolderId, objectInfos);
        boolean userReadOnly = checkUser(context, true);

        if (objectId == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        // get the file and parent
        final DocumentDTO doc = getDocument(objectId.getValue());

        LOGGER.debug(" moveObject(): document for move {}", doc);
        if(doc == null || doc.getId() == null)
            throw new IllegalStateException(String.format("Document with id %s was not found", objectId));

        if ("0".equals(targetFolderId)) targetFolderId=zeroUUID;
        doc.setParent(UUID.fromString(targetFolderId));
        crudService.setParent(doc, getUserNameFromJwtToken(context));

        DocumentDTO parent = null;
        if (doc.getParent()!=null)  parent = getParentDocument(doc.getParent().toString());

        LOGGER.debug(" moveObject(): parent document {}", parent);

        if (parent!=null){
            LOGGER.debug("moveObject(): removing exiting link with headId {} and tailId {}", parent.getId(), doc.getId());
            //LinkDTO deletedLink = crudService.deleteLink(parent.getId(), doc.getId());
            //LOGGER.debug("moveObject(): existing link {} has been deleted", deletedLink);
        }
        //LinkDTO link = crudService.addLink(Long.parseLong(targetFolderId), doc.getId());

        //LOGGER.debug("leaving moveObject(): new link {} has been created for object {}", link, doc);

        return compileObjectData(context, doc, parent, null, false, false, userReadOnly, objectInfos);
    }

    /**
     * CMIS setContentStream, deleteContentStream, and appendContentStream.
     */
    public void changeContentStream(CallContext context, Holder<String> objectId, Boolean overwriteFlag,
                                    ContentStream contentStream, boolean append) throws Exception {
        LOGGER.debug("entering changeContentStream(context={}, objectId={}, overwriteFlag={}, contentStream = {}, append = {})", context, objectId, overwriteFlag, contentStream, append);
        try {
            checkUser(context, true);

            if (objectId == null) {
                throw new CmisInvalidArgumentException("Id is not valid!");
            }

            final String docId = objectId.getValue();
            LOGGER.trace("changeContentStream(): document ID: {}", docId);

            if(StringUtils.isBlank(docId))
                throw new Exception("Document id is blank");
            DocumentDTO doc = getDocument(docId);
            LOGGER.trace("changeContentStream(): document {}", doc);

            if(doc == null)
                throw new Exception("Document with objectId " + objectId + " was not found in database");

            writeContentFromStream(doc, contentStream, getUserNameFromJwtToken(context));
        } catch (Exception e) {
            throw new CmisStorageException("Could not write content: " + e.getMessage(), e);
        }
    }

    /**
     * CMIS deleteObject.
     */
    public void deleteObject(CallContext context, String objectId) throws Exception {

        LOGGER.debug("entering deleteObject(context={}, objectId = {})", context, objectId);
        checkUser(context, true);

        DocumentDTO doc = getDocument(objectId);

        LOGGER.debug("deleteObject(): document for deleting {}", doc);
        // check if it is a folder and if it is empty
        if (isFolder(doc.getBaseType())) {
        	Pageable pageable = new PageRequest(0, 1);
            if (crudService.findAllByParent(doc.getId(), pageable).getTotalElements() > 0) {
                throw new CmisConstraintException("Folder is not empty!");
            }
        }
        
        DocumentDTO deleted = null;
		try {
			deleted = crudService.delete(doc.getId(), doc.getType());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        LOGGER.debug(" deleteFolder(): deleted object {} : ", deleted);
        
        
    }

    /**
     * CMIS deleteTree.
     */
    public FailedToDeleteData deleteTree(CallContext context, String folderId, Boolean continueOnFailure) throws Exception {
        LOGGER.debug("entering deleteTree(context={}, folderId = {}, continueOnFailure={})", context, folderId, continueOnFailure);
        checkUser(context, true);

        boolean cof = FileBridgeUtils.getBooleanParameter(continueOnFailure, false);


        // get the doc
        DocumentDTO doc = getDocument(folderId);

        LOGGER.debug("deleteTree(): document for delete {}", doc);

        FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();
        result.setIds(new ArrayList<>());

        // if it is a folder, remove it recursively
        if (isFolder(doc.getBaseType())) {
            deleteFolder(doc, cof, result);
        } else {
            throw new CmisConstraintException("Object is not a folder!");
        }

        LOGGER.debug("leaving deleteTree() : result {}", result);
        return result;
    }


    /**
     * Writes the content to disc.
     */
    private String writeContent(DocumentDTO doc, InputStream stream, JsonNode storageSettings) throws Exception {
        try {
            LOGGER.trace("entering writeContent(doc={}, storageSettings={}, storages={})", doc, storageSettings);

            final String filePath = fileService.writeContent(doc.getUuid(), org.apache.commons.io.IOUtils.toByteArray(stream), storageSettings);
            LOGGER.debug("writeContent(): File has been saved on the disc, path to file {}", filePath);
            doc.setFilePath(filePath);

            return filePath;
        } catch (IOException e) {

            throw new CmisStorageException("Could not write content: " + e.getMessage(), e);
        }

    }

    /**
     * Removes a folder and its content.
     */
    private boolean deleteFolder(DocumentDTO doc, boolean continueOnFailure, FailedToDeleteDataImpl ftd) throws Exception {
        LOGGER.debug("entering deleteFolder(doc={}, continueOnFailure={}, ftd={})", doc, continueOnFailure, ftd);
        boolean success = true;
        Pageable pageable = new PageRequest(0, Integer.MAX_VALUE);
        Page<DocumentDTO> docList = crudService.findAllByParent(doc.getId(),pageable);
        for (DocumentDTO childDoc : docList) {
            LOGGER.debug(" deleteFolder(): childDoc {} : ", childDoc);
            if (isFolder(childDoc.getBaseType())) {
                if (!deleteFolder(childDoc, continueOnFailure, ftd)) {
                    if (!continueOnFailure) {
                        return false;
                    }
                    success = false;
                }
            } else {
                
                DocumentDTO childDeleted = null;
				try {
					childDeleted = crudService.delete(childDoc.getId(),childDoc.getType());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                LOGGER.debug(" deleteFolder(): childDeleted {} : ", childDeleted);
                
            }
        }

        
        DocumentDTO deleted = null;
		try {
			deleted = crudService.delete(doc.getId(), doc.getType());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        LOGGER.debug(" deleteFolder(): deleted object {} : ", deleted);
        

        return success;
    }

    /**
     * Removes a folder and its content.
     */
    private boolean deleteFolder(File folder, boolean continueOnFailure, FailedToDeleteDataImpl ftd) {
        if(folder == null){
            LOGGER.warn("deleteFolder(): Folder is null");
            return false;
        }

        boolean success = true;

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                if (!deleteFolder(file, continueOnFailure, ftd)) {
                    if (!continueOnFailure) {
                        return false;
                    }
                    success = false;
                }
            } else {
                if (!file.delete()) {
                    ftd.getIds().add(getId(file));
                    if (!continueOnFailure) {
                        return false;
                    }
                    success = false;
                }
            }
        }

        if (!folder.delete()) {
            ftd.getIds().add(getId(folder));
            success = false;
        }

        return success;
    }

    /**
     * CMIS updateProperties.
     */
    public ObjectData updateProperties(CallContext context, Holder<String> objectId, Properties properties,
                                       ObjectInfoHandler objectInfos) throws Exception {
        LOGGER.debug("entering updateProperties(context={}, objectId={}, properties={}, objectInfos ={})", context, objectId, properties, objectInfos);
        boolean userReadOnly = checkUser(context, true);

        // check object id
        if (objectId == null || objectId.getValue() == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }
        if(properties == null)
            throw new CmisInvalidArgumentException("Properties is null");
        // get the file or folder
        final DocumentDTO doc = getDocument(objectId.getValue());

        LOGGER.debug("updateProperties(): document for update: {}", doc);
        boolean isDirectory = isFolder(doc.getBaseType());
        // check the properties
        String typeId = (isDirectory ? BaseTypeId.CMIS_FOLDER.value() : BaseTypeId.CMIS_DOCUMENT.value());
        String customType = doc.getType();
        if (!isDirectory && customType!=null && customType!="" && customType!="document"){
        	typeId = customType;
        }
        LOGGER.debug("updateProperties(): typeId is : {}", typeId);

        if (StringUtils.isBlank(typeId)) {
            throw new CmisInvalidArgumentException("Type Id is not set!");
        }
        TypeDefinition type = getTypeDefinitionByTypeId(typeId);

        LOGGER.debug("updateProperties(): type definition is : {}", type);
        checkUpdateProperties(properties, typeId, type);

        // get and check the new name
        final String newName = FileBridgeUtils.getStringProperty(properties, PropertyIds.NAME);
        LOGGER.debug("updateProperties(): new name : {}", newName);

        final String description = FileBridgeUtils.getStringProperty(properties, PropertyIds.DESCRIPTION);
        LOGGER.debug("updateProperties(): new description : {}", description);
        boolean isRename = (newName != null) && (!newName.equals(doc.getTitle()));
        boolean isUpdateDescription = description != null && !description.equals(doc.getDescription());

        boolean isUpdate = false;
        if (isRename) {
            doc.setTitle(newName);
            isUpdate = true;
        }
        if(isUpdateDescription){
            doc.setDescription(description);
            isUpdate = true;
        }
        
		try {
			ObjectMapper mapper = new ObjectMapper();
	        String base = "{}";
	        ObjectNode res = (ObjectNode) mapper.readTree(base);
	        boolean isDataChanged = false;
	        for (PropertyData<?> prop : properties.getPropertyList()){
	        	if (!prop.getId().startsWith("cmis:")){
	        		if (prop instanceof PropertyInteger) {
	        			res.put(prop.getId(), ((PropertyInteger) prop).getFirstValue().intValue());
	        		}
	        		if (prop instanceof PropertyDecimal) {
	        			res.put(prop.getId(), ((PropertyDecimal) prop).getFirstValue().doubleValue());
	        		}
	        		if (prop instanceof PropertyString) {
	        			res.put(prop.getId(), ((PropertyString) prop).getFirstValue());
	        		}
	        		if (prop instanceof PropertyDateTime) {
                        res.put(prop.getId(), getDateFromProperty((PropertyDateTime) prop));
	        		}
	        		isDataChanged = true;
	        	}
	        }
	        LOGGER.debug("updateProperties(): new data : {}", res.toString());
	        if (isDataChanged){
	        	doc.setData(res);
	        	isUpdate=true;
	        }
		} catch (IOException e) {
			LOGGER.error("updateProperties(): Exception {}", e.getMessage());
		}
        if (isUpdate)
			try {
				crudService.update(doc ,getUserNameFromJwtToken(context));
			} catch (Throwable e){
				if (e instanceof AccessViolationException){
					throw new CmisPermissionDeniedException(e.getMessage());
				}else{
					e.printStackTrace();
				}
			}

        DocumentDTO parent = null;
        if (doc.getParent()!=null)  parent = getParentDocument(doc.getParent().toString());
        LOGGER.debug("updateProperties(): parent document {}", parent);

        LOGGER.debug("leaving updateProperties(context={}, objectId={}, properties={}, objectInfos ={})", context, objectId, properties, objectInfos);
        return compileObjectData(context, doc, parent, null, false, false, userReadOnly, objectInfos);
    }

    /**
     * CMIS bulkUpdateProperties.
     */
    public List<BulkUpdateObjectIdAndChangeToken> bulkUpdateProperties(CallContext context,
                                                                       List<BulkUpdateObjectIdAndChangeToken> objectIdAndChangeToken, Properties properties,
                                                                       ObjectInfoHandler objectInfos) throws Exception {
        checkUser(context, true);

        if (objectIdAndChangeToken == null) {
            throw new CmisInvalidArgumentException("No object ids provided!");
        }

        List<BulkUpdateObjectIdAndChangeToken> result = new ArrayList<BulkUpdateObjectIdAndChangeToken>();

        for (BulkUpdateObjectIdAndChangeToken oid : objectIdAndChangeToken) {
            if (oid == null) {
                // ignore invalid ids
                continue;
            }
            try {
                Holder<String> oidHolder = new Holder<String>(oid.getId());
                updateProperties(context, oidHolder, properties, objectInfos);

                result.add(new BulkUpdateObjectIdAndChangeTokenImpl(oid.getId(), oidHolder.getValue(), null));
            } catch (CmisBaseException e) {
                // ignore exceptions - see specification
            }
        }

        return result;
    }

    /**
     * CMIS getObject.
     */
    public ObjectData getObject(CallContext context, String objectId, String versionServicesId, String filter,
                                Boolean includeAllowableActions, Boolean includeAcl, ObjectInfoHandler objectInfos) throws Exception {
        LOGGER.debug("entering getObject(objectId={}, versionServicesId = {}, filter= {}, includeAllowableActions={}, includeACL={}, objectInfos={})", objectId, versionServicesId,
                filter, includeAllowableActions, includeAcl, objectInfos);
        boolean userReadOnly = checkUser(context, false);


        // check id
        if (objectId == null && versionServicesId == null) {
            throw new CmisInvalidArgumentException("Object Id must be set.");
        }

        if (objectId == null) {
            // this works only because there are no versions in a file system
            // and the object id and version series id are the same
            objectId = versionServicesId;
        }
        if ("0".equals(objectId)) objectId=zeroUUID;
        final DocumentDTO doc = getDocument(objectId);

        // set defaults if values not set
        boolean iaa = FileBridgeUtils.getBooleanParameter(includeAllowableActions, false);
        boolean iacl = FileBridgeUtils.getBooleanParameter(includeAcl, false);

        // split filter
        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);

        DocumentDTO parent = null;
        if (doc.getParent()!=null)  parent = getParentDocument(doc.getParent().toString());
        LOGGER.debug("getObject(): parent document {}", parent);

        LOGGER.debug("leaving getObject(): found object {}", doc);
        // gather properties
        return compileObjectData(context, doc, parent, filterCollection, iaa, iacl, userReadOnly, objectInfos);
    }

    /**
     * CMIS getAllowableActions.
     */
    public AllowableActions getAllowableActions(CallContext context, String objectId) {
        boolean userReadOnly = checkUser(context, false);

        // get the file or folder
        File file = getFile(objectId);
        if (!file.exists()) {
            throw new CmisObjectNotFoundException("Object not found!");
        }

        return compileAllowableActions(file, userReadOnly);
    }

    /**
     * CMIS getACL.
     */
    public Acl getAcl(CallContext context, String objectId) {
        boolean readOnly = checkUser(context, false);

        // get the file or folder
        File file = getFile(objectId);
        if (!file.exists()) {
            throw new CmisObjectNotFoundException("Object not found!");
        }

        return compileAcl(file, getUserNameFromJwtToken(context), readOnly);
    }

    /**
     * CMIS getContentStream.
     */
    public ContentStream getContentStream(CallContext context, String objectId, BigInteger offset, BigInteger length) throws Exception {
        LOGGER.trace("entering getContentStream(objectId={}, offset = {}, length= {})", objectId, offset, length);
        checkUser(context, false);

        // get the file
        final DocumentDTO doc = getDocument(objectId);
        LOGGER.trace("getContentStream(): document {}", doc);

        if(doc == null)
            throw new Exception("Document with objectId " + objectId + " was not found in database");
        if (StringUtils.isBlank(doc.getFilePath()))
            throw new CmisConstraintException("Document has no content!");

        if(StringUtils.isBlank(doc.getFileStorage()))
            throw new CmisConstraintException("Document has no filestorage!");

        JsonNode settingsNode = fileService.getStorageSettingByStorageAreaName(doc.getFileStorage());

        byte[] contentByteArr = fileService.readFile(settingsNode, doc.getFilePath());

        LOGGER.trace("getContentStream(): contentByte {}", contentByteArr != null ? contentByteArr.length : 0);
        // compile data
        ContentStreamImpl result;
        if ((offset != null && offset.longValue() > 0) || length != null) {
            result = new PartialContentStreamImpl();
        } else {
            result = new ContentStreamImpl();
        }

        result.setFileName(doc.getFileName());
        result.setLength(BigInteger.valueOf(doc.getFileLength()));
        result.setMimeType(MimeTypes.getMIMEType(doc.getFileMimeType()));
        result.setStream(contentByteArr != null ? new ByteArrayInputStream(contentByteArr): null);


        LOGGER.trace("leaving getContentStream(): contentStream  {}", getContentStreamInfo(result));
        return result;
    }

    /**
     * CMIS getChildren.
     */
    public ObjectInFolderList getChildren(CallContext context, String objectId, String filter, String orderBy,
                                          Boolean includeAllowableActions, Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount,
                                          ObjectInfoHandler objectInfos) throws Exception {
        LOGGER.debug("entering getChildren(objectId={},  filter= {}, includeAllowableActions={}, includePathSegment={}, maxItems={}, skipCount={}, objectInfos={})",
                objectId, filter, includeAllowableActions, includePathSegment, maxItems, skipCount, objectInfos);
        boolean userReadOnly = checkUser(context, false);



        // set defaults if values not set
        boolean iaa = FileBridgeUtils.getBooleanParameter(includeAllowableActions, false);
        boolean ips = FileBridgeUtils.getBooleanParameter(includePathSegment, false);

     // skip and max
        int skip = (skipCount == null ? 0 : skipCount.intValue());
        if (skip < 0) {
            skip = 0;
        }

        int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
        if (max < 0) {
            max = Integer.MAX_VALUE;
        }
        
        List<Order> ordList = new ArrayList<Order>();
        ordList.add(new Order(Direction.ASC, "SYS_TITLE"));
        Sort sort = new Sort(ordList);
        if (orderBy!=null){
        	String[] orderByArr = orderBy.split(",");
        	ordList = new ArrayList<Order>();
        	for (String orderByStr: orderByArr){
	        	Direction orderByDir = Direction.ASC;
	        	if (orderByStr.contains(" ") && orderByStr.split(" ")[1].equals("DESC")) orderByDir = Direction.DESC;
	        	String orderByProp = orderByStr.split(" ")[0];
                switch (orderByProp) {
                    case "cmis:name":
                        orderByProp = "SYS_TITLE";
                        break;
                    case "cmis:objectTypeId":
                        orderByProp = "SYS_TYPE";
                        break;
                    case "cmis:baseTypeId":
                        orderByProp = "SYS_BASE_TYPE";
                        break;
                    case "cmis:createdBy":
                        orderByProp = "SYS_AUTHOR";
                        break;
                    case "cmis:creationDate":
                        orderByProp = "SYS_DATE_CR";
                        break;
                    case "cmis:lastModifiedBy":
                        orderByProp = "SYS_MODIFIER";
                        break;
                    case "cmis:lastModificationDate":
                        orderByProp = "SYS_DATE_MOD";
                        break;
                }
	        	Order ord = new Order(orderByDir, orderByProp);
	        	LOGGER.debug("getChildren(): orderByDir: {}, orderByProp: {}", orderByDir, orderByProp);
	        	ordList.add(ord);
        	}
        	sort = new Sort(ordList);
        }
        Pageable pageable = new PageRequest(skip/max, max, sort);
        
        LOGGER.debug("getChildren(): Folder ID: {}", objectId);
        if ("0".equals(objectId)) objectId=zeroUUID;
        final UUID parentId = UUID.fromString(objectId);
        Page<DocumentDTO> docList = null;
        if ("cmis:folder".equals(filter)){
        	docList = crudService.findAllByParentAndType(parentId, filter, pageable);
        	filter = null;
        }else if (filter!=null && filter.startsWith("type:")){
        	docList = crudService.findAllByParentAndType(parentId, filter.split(":")[1], pageable);
        	filter = null;
        }else if (filter!=null && filter.startsWith("link:")){
        	docList = crudService.findAllByLinkHead(parentId, filter.split(":")[1], pageable); 
        	filter = null;
        }else{
        	docList = crudService.findAllByParent(parentId, pageable);
        	LOGGER.debug("getChildren(): filter {} ", filter);
        }
        
        // split filter
        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);
        LOGGER.debug("getChildren(): filterCollection: {}", filterCollection);
        
        LOGGER.debug("getChildren(): Found {} children.", docList != null ? docList.getTotalElements() : null);
        ObjectInFolderListImpl result = new ObjectInFolderListImpl();
        if(docList == null){
            LOGGER.debug("getChildren(): document with parentid {} does not have children", parentId);
            return result;
        }

        

        final DocumentDTO curdoc = getDocument(objectId);

        LOGGER.debug("getChildren(): current document {}.", curdoc);

        if (context.isObjectInfoRequired()) {
            DocumentDTO parent = null;
            if (curdoc.getParent()!=null)  parent = getParentDocument(curdoc.getParent().toString());
            LOGGER.debug("getChildren(): parent document {}", parent);
            compileObjectData(context, curdoc, parent, null, false, false, userReadOnly, objectInfos);
        }

        // prepare result

        result.setObjects(new ArrayList<>());
        result.setHasMoreItems(docList.hasNext());
        int count = 0;

        LOGGER.debug("getChildren(): adding children to result...");
        for (DocumentDTO doc : docList){
            LOGGER.debug("getChildren(): child document {}", doc);
            count++;

//            if (skip > 0) {
//                skip--;
//                continue;
//            }
//
//            if (result.getObjects().size() >= max) {
//                result.setHasMoreItems(true);
//                continue;
//            }

            // build and add child object
            ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();

            final DocumentDTO parent = curdoc; //getParentDocument(doc.getParent());
            LOGGER.debug("getChildren(): parent document {}", parent);
            objectInFolder.setObject(compileObjectData(context, doc, parent, filterCollection, iaa, false, userReadOnly,
                    objectInfos));
            if (ips) {
                objectInFolder.setPathSegment(doc.getTitle());
            }

            result.getObjects().add(objectInFolder);
        }


        result.setNumItems(BigInteger.valueOf(docList.getTotalElements()));
        LOGGER.debug("leaving getChildren(): result {}.", result);
        return result;
    }

    /**
     * CMIS getDescendants.
     */
    public List<ObjectInFolderContainer> getDescendants(CallContext context, String folderId, BigInteger depth,
                                                        String filter, Boolean includeAllowableActions, Boolean includePathSegment, ObjectInfoHandler objectInfos,
                                                        boolean foldersOnly) throws IOException {
        LOGGER.debug("entering getDescendants(folderId={}, depth={},  filter= {}, includeAllowableActions={}, includePathSegment={}, objectInfos={}, foldersOnly={})",
                folderId, depth, filter, includeAllowableActions, includePathSegment, objectInfos, foldersOnly);
        boolean userReadOnly = checkUser(context, false);

        // check depth
        int d = (depth == null ? 2 : depth.intValue());
        if (d == 0) {
            throw new CmisInvalidArgumentException("Depth must not be 0!");
        }
        if (d < -1) {
            d = -1;
        }

        // split filter
        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);
        LOGGER.debug("getDescendants(): filterCollection: {}", filterCollection);
        // set defaults if values not set
        boolean iaa = FileBridgeUtils.getBooleanParameter(includeAllowableActions, false);
        boolean ips = FileBridgeUtils.getBooleanParameter(includePathSegment, false);

        // get the folder
        File folder = getFile(folderId);
        LOGGER.debug("getDescendants(): folder name: {}, isDirectory {}", folder.getName(), folder.isDirectory());
        if (!folder.isDirectory()) {
            throw new CmisObjectNotFoundException("Not a folder!");
        }

        // set object info of the the folder
        if (context.isObjectInfoRequired()) {
            compileObjectData(context, folder, null, false, false, userReadOnly, objectInfos);
        }

        // get the tree
        List<ObjectInFolderContainer> result = new ArrayList<ObjectInFolderContainer>();
        gatherDescendants(context, folder, result, foldersOnly, d, filterCollection, iaa, ips, userReadOnly,
                objectInfos);


        LOGGER.debug("leaving getDescendants(): result {}.", result);
        return result;
    }

    /**
     * CMIS getFolderParent.
     */
    public ObjectData getFolderParent(CallContext context, String folderId, String filter, ObjectInfoHandler objectInfos) throws Exception {
        List<ObjectParentData> parents = getObjectParents(context, folderId, filter, false, false, objectInfos);

//        if (parents.isEmpty()) {
//            throw new CmisInvalidArgumentException("The root folder has no parent!");
//        }

        return !parents.isEmpty() ? parents.get(0).getObject() : null;
    }

    /**
     * CMIS getObjectParents.
     */
    public List<ObjectParentData> getObjectParents(CallContext context, String objectId, String filter,
                                                   Boolean includeAllowableActions, Boolean includeRelativePathSegment, ObjectInfoHandler objectInfos) throws Exception {
        LOGGER.debug("entering getObjectParents(objectId={},  filter= {}, includeAllowableActions={}, includeRelativePathSegment={}, objectInfos={})",
                objectId, filter, includeAllowableActions, includeRelativePathSegment, objectInfos);
        boolean userReadOnly = checkUser(context, false);

        // split filter
        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);
        LOGGER.debug("getObjectParents(): filterCollection: {}", filterCollection);

        // set defaults if values not set
        boolean iaa = FileBridgeUtils.getBooleanParameter(includeAllowableActions, false);
        boolean irps = FileBridgeUtils.getBooleanParameter(includeRelativePathSegment, false);

        // get the file or folder
        final DocumentDTO doc = getDocument(objectId);

        LOGGER.debug("getObjectParents():  document {}", doc);

        // don't climb above the root folder
//        if (zeroUUID.equals(doc.getId().toString())) {
//            return Collections.emptyList();
//        }
        if (zeroUUID.equals(doc.getId().toString())) {

            return buildObjectData(context, doc, filterCollection, iaa, irps, userReadOnly, objectInfos);
//            ObjectData object = compileObjectData(context, doc, null, filterCollection, iaa, false, userReadOnly, objectInfos);
//
//            ObjectParentDataImpl result = new ObjectParentDataImpl();
//            result.setObject(object);
//            if (irps) {
//                result.setRelativePathSegment(doc.getTitle());
//            }
//            LOGGER.info("leaving getObjectParents(): result {}.", result);
//            return Collections.<ObjectParentData> singletonList(result);
        }

        DocumentDTO parent = null;
        if (doc.getParent()!=null)  parent = getParentDocument(doc.getParent().toString());
        LOGGER.debug("getObjectParents(): parent document {}", parent);
        // set object info of the the object
        if (context.isObjectInfoRequired()) {

            compileObjectData(context, doc, parent, null, false, false, userReadOnly, objectInfos);
        }

        // get parent folder

//        LOGGER.debug("leaving getObjectParents()");

        return buildObjectData(context, parent, filterCollection, iaa, irps, userReadOnly, objectInfos);
//        ObjectData object = compileObjectData(context, parent, null, filterCollection, iaa, false, userReadOnly, objectInfos);
//
//        ObjectParentDataImpl result = new ObjectParentDataImpl();
//        result.setObject(object);
//        if (irps) {
//            result.setRelativePathSegment(doc.getTitle());
//        }
//        LOGGER.debug("leaving getObjectParents(): result {}.", result);
//        return Collections.<ObjectParentData> singletonList(result);
    }

    private List<ObjectParentData> buildObjectData(CallContext context, DocumentDTO doc, Set<String> filterCollection, boolean iaa, boolean irps,
                                                   boolean userReadOnly, ObjectInfoHandler objectInfos){
        ObjectData object = compileObjectData(context, doc, null, filterCollection, iaa, false, userReadOnly, objectInfos);

        ObjectParentDataImpl result = new ObjectParentDataImpl();
        result.setObject(object);
        if (irps) {
            result.setRelativePathSegment(doc.getTitle());
        }
        LOGGER.info("leaving getObjectParents(): result {}.", result);
        return Collections.<ObjectParentData> singletonList(result);
    }

    /**
     * CMIS getObjectByPath.
     */
    public ObjectData getObjectByPath(CallContext context, String folderPath, String filter,
                                      boolean includeAllowableActions, boolean includeACL, ObjectInfoHandler objectInfos) throws Exception {
        LOGGER.debug("entering getObjectByPath(folderPath={},  filter= {}, includeAllowableActions={}, includeACL={}, objectInfos={})",
                folderPath, filter, includeAllowableActions, includeACL, objectInfos);
        boolean userReadOnly = checkUser(context, false);

        // check path
        if (folderPath == null || folderPath.length() == 0) {
            throw new CmisInvalidArgumentException("Invalid folder path!");
        }

        // split filter
        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);
        LOGGER.debug("getObjectByPath(): filterCollection: {}", filterCollection);

        DocumentDTO doc = crudService.findById(UUID.fromString(zeroUUID), "folder", false);

        if (!folderPath.equals("/")){
        	Pageable pageable = new PageRequest(0, 1);
            Page<DocumentDTO> dtos = crudService.findInPgBySearchTerm(folderPath.substring(folderPath.lastIndexOf("/")+1, folderPath.length()), pageable);
            if (dtos.iterator().hasNext()){
            	doc = dtos.iterator().next();
            }else{
            	throw new CmisInvalidArgumentException("Can not find document by path!");
            }
        }
        
        LOGGER.debug("getObjectByPath():  document {}", doc);
        DocumentDTO parent = null;
        if (doc.getParent()!=null)  parent = getParentDocument(doc.getParent().toString());
        LOGGER.debug("getObjectByPath(): parent document {}", parent);
        return compileObjectData(context, doc, parent, filterCollection, includeAllowableActions, includeACL, userReadOnly,
                objectInfos);
    }

    /**
     * CMIS query (simple IN_FOLDER queries only)
     */
    public ObjectList query(CallContext context, String statement, Boolean includeAllowableActions,
                            BigInteger maxItems, BigInteger skipCount, ObjectInfoHandler objectInfos) throws IOException {
    	 LOGGER.debug("entering query(user ={})", context.getUsername());
    	boolean userReadOnly = checkUser(context, false);
        LOGGER.debug("entering query(statement ={})", statement);
        Matcher matcher = IN_FOLDER_QUERY_PATTERN.matcher(statement.toLowerCase().trim());

        if (!matcher.matches()) {
            throw new CmisInvalidArgumentException("Invalid or unsupported query.");
        }
        String filter = matcher.group(1);
        LOGGER.debug("filter ={}", filter);
        String typeId = matcher.group(2);
        LOGGER.debug("typeId ={}", typeId);
        String folderId = matcher.group(3);
        LOGGER.debug("folderId ={}", folderId);
        TypeDefinition type = getTypeDefinitionByTypeId(typeId);

        boolean queryFiles = (type.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT);

        if (folderId.length() == 0) {
            throw new CmisInvalidArgumentException("Invalid folder id.");
        }

        // set defaults if values not set
        boolean iaa = FileBridgeUtils.getBooleanParameter(includeAllowableActions, false);
        boolean ips = false;

        // skip and max
       int skip = (skipCount == null ? 0 : skipCount.intValue());
       if (skip < 0) {
           skip = 0;
       }

       int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
       if (max < 0) {
           max = Integer.MAX_VALUE;
       }
       List<Order> ordList = new ArrayList<Order>();
       ordList.add(new Order(Direction.ASC, "SYS_TITLE"));
       Sort sort = new Sort(ordList);
       Pageable pageable = new PageRequest(skip/max, max, sort);
       // prepare result
        ObjectListImpl res = new ObjectListImpl();
        if ("0".equals(folderId)) folderId=zeroUUID;
        final UUID parentId = UUID.fromString(folderId);
        Page<DocumentDTO> docList = null;
        try {
	        if ("cmis:folder".equals(typeId)){
	        	docList = crudService.findAllByParentAndType(parentId, "cmis:folder", pageable);
	        }else{
	        	docList = crudService.findAllByParent(parentId, pageable);
	        	LOGGER.debug("query(): filter {} ", filter);
	        }
	        
	        // split filter
	        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);
	        LOGGER.debug("query(): filterCollection: {}", filterCollection);
	        
	        LOGGER.debug("query(): Found {} children.", docList != null ? docList.getTotalElements() : null);
	        if(docList == null){
	            LOGGER.debug("query(): document with parentid {} does not have children", parentId);
	            return res;
	        }

        
        
	        DocumentDTO curdoc = getDocument(folderId);
			
	
	        LOGGER.debug("query(): current document {}.", curdoc);
	
	        if (context.isObjectInfoRequired()) {
	            DocumentDTO parent = null;
	            if (curdoc.getParent()!=null)  parent = getParentDocument(curdoc.getParent().toString());
	            LOGGER.debug("getChildren(): parent document {}", parent);
	            compileObjectData(context, curdoc, parent, null, false, false, userReadOnly, objectInfos);
	        }
	
	        // prepare result
	
	        res.setObjects(new ArrayList<>());
	        res.setHasMoreItems(docList.hasNext());
	        int count = 0;
	
	        LOGGER.debug("query(): adding children to result...");
	        for (DocumentDTO doc : docList){
	            LOGGER.debug("query(): child document {}", doc);
	            count++;
	
	            final DocumentDTO parent = curdoc; 
	            LOGGER.debug("query(): parent document {}", parent);
	            ObjectData object = compileObjectData(context, doc, parent, filterCollection, iaa, false, userReadOnly, objectInfos);
	
	            res.getObjects().add(object);
	        }
        } catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
        return res;
    }

    // --- helpers ---


    private DocumentDTO getDocument(final String objectId) throws Exception {

        LOGGER.debug("entering getDocument(objectId ={})", objectId);


        if(StringUtils.isBlank(objectId))
            throw new IllegalArgumentException("Object id is null");

        final UUID uuidKey = UUID.fromString(objectId);

        LOGGER.debug("entering getDocument(): uuidKey {} ", uuidKey);

        DocumentDTO found = crudService.findById(uuidKey, null, false);



        LOGGER.debug("getDocument(): Found Document entry: {}", found);

        if (found == null)
            throw new DocumentNotFoundException("Document was not found in database");



        return found;
    }




    private DocumentDTO getParentDocument(final String objectId) throws Exception {
        LOGGER.debug("entering getParentDocument(objectId ={})", objectId);
        return objectId != null ? getDocument(objectId) : null;
    }


    /**
     * Checks if the user in the given context is valid for this repository and
     * if the user has the required permissions.
     */
    boolean checkUser(CallContext context, boolean writeRequired) {

        LOGGER.trace("entering checkUser(context={},writeRequired={})", context, writeRequired);
        if (context == null) {
            throw new CmisPermissionDeniedException("No user context!");
        }

        final String jwtToken = getJwtTokenFromCallContext(context);
        boolean readOnly = true;
        String userName = getUserNameFromJwtToken(context);
        LOGGER.trace( "jwt token - {}", jwtToken);
        if(StringUtils.isBlank(jwtToken)) {
            //throw new CmisPermissionDeniedException("Unknown user!");
        	readOnly = false;
        	LOGGER.trace("checkUser(): JWT is blank");
        }else if (jwtToken.startsWith("Basic")){
        	readOnly = false;
        	LOGGER.trace("checkUser(): Basic Auth");
        }else{

	        LOGGER.trace("checkUser(): jwtToken lenght {}", jwtToken.length());
	
	        final List<String> userRoles = getUserRolesFromToken(jwtToken);
	        userName = getUserNameFromJwtToken(context);
	        
	        if(userRoles != null){
	            for (String userRole : userRoles) {
	                if(userRole.equals(CMIS_READ_WRITE_ROLE_NAME)){
	                    readOnly = false;
	                    break;
	                }
	            }
	        }
        }
        if (readOnly && writeRequired) {
            throw new CmisPermissionDeniedException("No write permission!");
        }


        LOGGER.trace("checkUser(): check user {}", userName);

        crudService.setUser(userName);


        LOGGER.trace("leaving checkUser(): is user {} readOnly? {}", userName, readOnly);
        return readOnly;
    }

    private String getContentStreamInfo(ContentStream contentStream){
        return contentStream != null ? ("ContentStream { " + " mimeType " + contentStream.getMimeType() +
                " fileName " + contentStream.getFileName() +
                " inputstream " + contentStream.getStream() +
                " length " + contentStream.getLength() + " }") : null;
    }


    private void writeContentFromStream(DocumentDTO doc, ContentStream contentStream, String userName) throws Exception {
        // write content, if available
        LOGGER.trace("entering writeContentFromStream(doc={}, contentStream={}, userName={})", doc, contentStream, userName);
        try {
            if (contentStream != null && contentStream.getStream() != null && contentStream.getLength() >0 ) {
                final JsonNode storageSettings = fileService.getStorageSettingsByDocType(doc.getType());

                final String filePath = writeContent(doc, contentStream.getStream(), storageSettings);

                LOGGER.debug("writeContentFromStream(): content was written filePath {}, storage={}", filePath);
                if (!StringUtils.isBlank(filePath)) {
                    BigInteger fileLength = contentStream.getBigLength();
                    String mimeType = contentStream.getMimeType();
                    String fileName = contentStream.getFileName();
                    LOGGER.debug("writeContentFromStream(): Uploaded file - {} - {} - {} - {}",filePath, fileLength, mimeType, fileName);
                    if (fileLength == null) {
                        doc.setFileLength(0L);
                    } else {
                        doc.setFileLength(fileLength.longValue());
                    }
                    doc.setFilePath(filePath);
                    doc.setFileMimeType(mimeType);
                    doc.setModifier(userName);
                    doc.setFileName(fileName);
                    doc.setFileStorage(JsonNodeParser.getStorageAreaName(storageSettings));
                    crudService.updateFileInfo(doc, userName);
                }
            }
            LOGGER.trace("leaving writeContentFromStream(): content stream was added {}", doc);
        } catch (Exception e){
            if(doc != null && doc.getId() != null) {
//                todo te needs to determine behaviour when any problems is happening while writecontent is being executed
                crudService.delete(doc.getId(), doc.getType());
            }
            LOGGER.error("writeContentFromStream(): exception has been thrown {}", e);
            throw new Exception(e.getMessage());
        }
    }


    private String getDateFromProperty(PropertyDateTime prop) {
        SimpleDateFormat fmt = new SimpleDateFormat(STORAGE_DATE_FORMAT);
        GregorianCalendar calendar = (prop).getFirstValue();
        fmt.setCalendar(calendar);

        return fmt.format(calendar.getTime());
    }
}