package ru.doccloud.document.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import ru.doccloud.common.util.VersionHelper;
import ru.doccloud.service.FileService;
import ru.doccloud.service.SystemCrudService;
import ru.doccloud.service.UserService;
import ru.doccloud.service.document.dto.GroupDTO;
import ru.doccloud.service.document.dto.SystemDTO;
import ru.doccloud.service.document.dto.UserDTO;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * @author Andrey Kadnikov
 */
@RestController
@RequestMapping("/api/system")
public class SystemController  extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemController.class);

	private static final String zerotype = "00000000-0000-0000-0000-000000000000";

    private final SystemCrudService<SystemDTO> crudService;
    private final UserService userService;

    @Autowired
    public SystemController(SystemCrudService<SystemDTO> crudService, FileService fileService, UserService userService) throws Exception {
        super(fileService, crudService);
        LOGGER.info("SystemController(crudService={}, storageAreaSettings= {}, storageManager={})", crudService, fileService);
        this.crudService = crudService;
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public SystemDTO add(HttpServletRequest request, @RequestBody @Valid SystemDTO dto) throws Exception {
        LOGGER.info("add(): add new document");

        return addDoc(dto, request.getRemoteUser());
    }


    @RequestMapping(value="/addcontent",headers="content-type=multipart/*",method=RequestMethod.POST)
    public SystemDTO addContent(MultipartHttpServletRequest request) throws Exception {

        LOGGER.info("addContent... add new document from request uri {}", request.getRequestURI());
        SystemDTO dto = new SystemDTO();
        Iterator<String> itr =  request.getFileNames();
        if(!itr.hasNext())
            return addDoc(dto, request.getRemoteUser());
        final MultipartFile mpf = request.getFile(itr.next());

        populateFilePartDto(dto, request, mpf);

        return writeContent(addDoc(dto, request.getRemoteUser()), mpf, request.getRemoteUser());
    }


    @RequestMapping(value="/updatecontent/{id}",headers="content-type=multipart/*",method=RequestMethod.POST)
    public SystemDTO updateContent(MultipartHttpServletRequest request,  @PathVariable("id") UUID id) throws Exception {
        LOGGER.info("updateContent... update document with id {} from request uri {}", id, request.getRequestURI());

        SystemDTO dto = crudService.findById(id);

        if(dto == null)
            throw new Exception("The document with such id " + id + " was not found in database ");

        Iterator<String> itr =  request.getFileNames();

        MultipartFile mpf = request.getFile(itr.next());

        return writeContent(dto, mpf, request.getRemoteUser());
    }

    @RequestMapping(value="/getcontent/{id}",headers="content-type=multipart/*",method=RequestMethod.GET)
    public byte[] getContent( @PathVariable("id") UUID id) throws Exception {

        LOGGER.info("getContent (id = {})", id);
        SystemDTO dto = crudService.findById(id);

        if(dto == null)
            throw new Exception("The document with such id " + id + " was not found in database ");

        LOGGER.info("getContent(): Found Document with id: {}", dto.getId());

        final String filePath = dto.getFilePath();
        if(StringUtils.isBlank(filePath)) {
            LOGGER.error("Filepath is empty. Content for document {} does not exist", dto);
            throw new Exception("Filepath is empty, content for document " + dto + "does not exist");
        }

        JsonNode storageSettings = fileService.getStorageSettingsByDocType(dto.getType());

        return fileService.readFile(storageSettings, filePath);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public SystemDTO delete(@PathVariable("id") UUID id, 
    		@RequestParam(value = "type", required=false) String type) throws Exception {
        LOGGER.info("delete(id={})", id);

        return crudService.delete(id, type);
    }

    /**@RequestMapping(method = RequestMethod.GET)
    public List<DocumentDTO> findAll() {
        LOGGER.info("Finding all Document entries");

        List<DocumentDTO> documentEntries = crudService.findAll();

        LOGGER.info("Found {} Document entries.");

        return documentEntries;
    }*/
    
    @RequestMapping(method = RequestMethod.GET)
    public Page<SystemDTO> findAll(Pageable pageable, @RequestParam(value = "filters",required=false) String query)  throws Exception {
        LOGGER.info("findAll(pageSize= {}, pageNumber = {}) ",
                pageable.getPageSize(),
                pageable.getPageNumber()
        );

        return crudService.findAll(pageable, query);
    }
    
    
    
    @RequestMapping(value = "/type/{type}", method = RequestMethod.GET)
    public Page<SystemDTO> findByType(@PathVariable("type") String type, @RequestParam(value = "fields",required=false) String fields,
                                      @RequestParam(value = "filters",required=false) String query,Pageable pageable)  throws Exception {
        LOGGER.info("findByType(type = {}, fields={}, query={}, pageSize= {}, pageNumber = {})",
                type, fields, query,
                pageable.getPageSize(),
                pageable.getPageNumber()
        );
        String[] fieldsArr = null;
        if (fields!=null){
        	fieldsArr = fields.split(",");
        }

        return crudService.findAllByType(type, fieldsArr, pageable, query);
    }
    
    @RequestMapping(value = "/types", method = RequestMethod.GET)
    public Page<SystemDTO> getTypes(@RequestParam(value = "parent",required=false) String parent, @RequestParam(value = "fields",required=false) String fields,
                                    @RequestParam(value = "filters",required=false) String query,Pageable pageable)  throws Exception {
        LOGGER.info("findByType(type = {}, fields={}, query={}, parent={}, pageSize= {}, pageNumber = {})",
                "type", fields, query, parent,
                pageable.getPageSize(),
                pageable.getPageNumber()
        );
        Page<SystemDTO> result;
        if (parent!=null){
        	if ("0".equals(parent)) parent=zerotype;
        	
        	result = crudService.findAllByParentAndType(UUID.fromString(parent), "type", pageable);
        }else{
	        String[] fieldsArr = null;
	        if (fields!=null){
	        	fieldsArr = fields.split(",");
	        }
	        result = crudService.findAllByType("type", fieldsArr, pageable, query);
        }

        return result;
    }
    
    @RequestMapping(value = "/groups", method = RequestMethod.GET)
    public List<GroupDTO> getGroups(@RequestParam(value = "query",required=false) String query)  throws Exception {

        return userService.getGroups(query);
    }
    
    @RequestMapping(value = "/datasources", method = RequestMethod.GET)
    public ArrayNode getDatasources()  throws Exception {
    	ObjectMapper mapper = new ObjectMapper();

		ObjectNode itemNode = (ObjectNode) mapper.readTree("{\"datasources\":[]}");
		ArrayNode nodes = (ArrayNode) itemNode.path("datasources");
    	for (String ds : crudService.getDatasourcesNames()){
    		nodes.add(ds);
    	}
        return nodes;
    }
    
    @RequestMapping(value = "/groups/{id}", method = RequestMethod.GET)
    public GroupDTO getGroup(HttpServletRequest request, @PathVariable("id") String id)  throws Exception {
    	return userService.getGroup(id);
    }
    
    @RequestMapping(value = "/groups/{id}", method = RequestMethod.PUT)
    public GroupDTO updateGroup(HttpServletRequest request, @PathVariable("id") String id, @RequestBody @Valid GroupDTO dto)  throws Exception {
    	dto.setId(id);
        return userService.updateGroup(dto);
    }
    
    @RequestMapping(value = "/groups", method = RequestMethod.POST)
    public GroupDTO addGroup(HttpServletRequest request, @RequestBody @Valid GroupDTO dto)  throws Exception{
        return userService.addGroup(dto);
    }
    
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public List<UserDTO> getUsers(@RequestParam(value = "query",required=false) String query)  throws Exception{

        return userService.getUsers(query);
    }
    
    @RequestMapping(value = "/users/{id}", method = RequestMethod.GET)
    public UserDTO getUser(HttpServletRequest request, @PathVariable("id") String id)  throws Exception{
    	return userService.getUserDto(id);
    }
    
    @RequestMapping(value = "/users/{id}", method = RequestMethod.PUT)
    public UserDTO updateUser(HttpServletRequest request, @PathVariable("id") String id, @RequestBody @Valid UserDTO dto)  throws Exception{
    	dto.setUserId(id);
        return userService.updateUser(dto);
    }
    
    @RequestMapping(value = "/userinfo", method = RequestMethod.GET)
    public UserDTO getUserInfo()  throws Exception {
    	HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return userService.getUserDto(request.getRemoteUser());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public SystemDTO findById(@PathVariable("id") UUID id) throws Exception {
        LOGGER.info("findById(id= {})", id);

        return crudService.findById(id);
    }

    @RequestMapping(value = "/uuid/{uuid}", method = RequestMethod.GET)
    public SystemDTO findById(@PathVariable("uuid") String uuid)  throws Exception{
        LOGGER.info("findById(uuid= {})", uuid);

        return crudService.findById(uuid);
    }

    @RequestMapping(value = "/s/{symbolic}", method = RequestMethod.GET)
    public SystemDTO findBySymbolicName(@PathVariable("symbolic") String symbolic)  throws Exception{
        LOGGER.info("findBySymbolicName(symbolic= {})", symbolic);

        return crudService.findBySymbolicName(symbolic);
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public SystemDTO update(HttpServletRequest request, @PathVariable("id") UUID id, @RequestBody @Valid SystemDTO dto) throws Exception {
        dto.setId(id);
        LOGGER.info("update(id={}, doc={})", id, dto);
        dto.setDocVersion(VersionHelper.generateMinorDocVersion(dto.getDocVersion()));

        return crudService.update(dto, request.getRemoteUser());
    }


    private SystemDTO addDoc(SystemDTO dto, String user) throws Exception {
        dto.setDocVersion(VersionHelper.generateMinorDocVersion(dto.getDocVersion()));
        return crudService.add(dto, user);
    }


    private SystemDTO writeContent(SystemDTO dto, MultipartFile mpf, String user) throws Exception {
        try {
            if(!checkMultipartFile(mpf))
                throw new Exception("The multipart file contains either empty content type or empty filename or does not contain data");
            LOGGER.debug("the document: {} has been added", dto);

            final JsonNode storageSettings = fileService.getStorageSettingsByDocType(dto.getType());
            LOGGER.debug("writeContent(): storageSettings {}", storageSettings);

            final String filePath = fileService.writeContent(dto.getUuid(), mpf.getBytes(), storageSettings);
            dto.setFilePath(filePath);
            SystemDTO updated = crudService.update(dto, user);
            LOGGER.debug("Dto object has been updated: {}", updated);
            return updated;
        }
        catch (Exception e) {

//            todo add custom Exception
            LOGGER.error("The exception has been occured while addContent method is executing {} {}", e.getMessage(), e);
            crudService.delete(dto.getId(), dto.getType());
            throw new Exception("Error has been occured " + e.getMessage());
        }
    }



}
