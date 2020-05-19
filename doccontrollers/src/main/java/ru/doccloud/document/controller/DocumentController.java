package ru.doccloud.document.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.common.util.JsonNodeParser;
import ru.doccloud.common.util.VersionHelper;
import ru.doccloud.document.controller.util.CSVutils;
import ru.doccloud.service.*;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.document.dto.SystemDTO;
import ru.doccloud.service.document.dto.ExportResultDTO;

/**
 * @author Andrey Kadnikov
 */
@RestController
@RequestMapping("/api/docs")
public class DocumentController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private ApplicationContext context;

    private final DocumentCrudService<DocumentDTO> crudService;
    private final DocumentSearchService searchService;
    private final SystemCrudService<SystemDTO> sysService;

    @Autowired
    public DocumentController(DocumentCrudService<DocumentDTO> crudService, DocumentSearchService searchService,
                              FileService fileService, SystemCrudService sysService) throws Exception {
        super(fileService, crudService);
        LOGGER.trace("DocumentController(crudService={}, searchService = {}, fileService={}, sysService={})", crudService, searchService, fileService, sysService);
        this.crudService = crudService;
        this.searchService = searchService;
        this.sysService = sysService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDTO add(HttpServletRequest request, @RequestBody @Valid DocumentDTO dto) throws Exception {
        LOGGER.debug("entering add(dto={}, requestUri={})",
                dto, request.getRequestURI());
        return addDoc(dto, request.getRemoteUser());
    }

    @RequestMapping(value = "/addcontent", headers = "content-type=multipart/*", method = RequestMethod.POST)
    public DocumentDTO addContent(MultipartHttpServletRequest request) throws Exception {

        LOGGER.info("addContent(): add new document from request uri {}", request.getRequestURI());
        final DocumentDTO dto = new DocumentDTO();
        Iterator<String> itr = request.getFileNames();
        if (!itr.hasNext())
            throw new Exception("Request does not contain any files");
        final MultipartFile mpf = request.getFile(itr.next());

        populateFilePartDto(dto, request, mpf);

        return writeContent(addDoc(dto, request.getRemoteUser()), mpf, request.getRemoteUser());
    }

    @RequestMapping(value = "/updatecontent/{id}", headers = "content-type=multipart/*", method = RequestMethod.POST)
    public DocumentDTO updateContent(MultipartHttpServletRequest request, @PathVariable("id") UUID id,
                                     @RequestParam(value = "type", required = false) String type) throws Exception {
        LOGGER.info("entering updateContent(requestURI={}, id={})", id, request.getRequestURI());

        final DocumentDTO dto = new DocumentDTO("FileInfo", type, request.getRemoteUser()); //crudService.findById(id, type, false);
        dto.setId(id);

        LOGGER.debug("updateContent(): dto = {}", dto);

        //if(dto == null)
        //    throw new Exception("The document with such id " + id + " was not found in database ");

        Iterator<String> itr = request.getFileNames();

        MultipartFile mpf = request.getFile(itr.next());
        populateFilePartDto(dto, request, mpf);
        return writeContent(dto, mpf, request.getRemoteUser());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public DocumentDTO update(HttpServletRequest request, @PathVariable("id") UUID id, @RequestBody @Valid DocumentDTO dto) throws Exception {
        dto.setId(id);
        LOGGER.info("update(id={})", id);
        dto.setDocVersion(VersionHelper.generateMinorDocVersion(dto.getDocVersion()));

        return crudService.update(dto, request.getRemoteUser());
    }

    @RequestMapping(value = "/updatefileinfo/{id}", method = RequestMethod.PUT)
    public DocumentDTO updateFileInfo(HttpServletRequest request, @PathVariable("id") UUID id, @RequestBody @Valid DocumentDTO dto) throws Exception {
        dto.setId(id);
        LOGGER.info("update file info (id={})", id);
        dto.setDocVersion(VersionHelper.generateMinorDocVersion(dto.getDocVersion()));

        return crudService.updateFileInfo(dto, request.getRemoteUser());
    }

    @RequestMapping(value = "/processretention", method = RequestMethod.GET)
    public JsonNode processRetention() throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return crudService.processRetention(request.getRemoteUser());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public DocumentDTO delete(@PathVariable("id") UUID id,
                              @RequestParam(value = "type", required = false) String type) throws Exception {
        LOGGER.info("delete(id={})", id);

        return crudService.delete(id, type);
    }

    @RequestMapping(value = "/getcontent/{id}", method = RequestMethod.GET)
    public void getContent(@PathVariable("id") UUID id,
                           @RequestParam(value = "type", required = false) String type,
                           HttpServletResponse resp) throws Exception {

        LOGGER.info("getContent (id = {})", id);
        final DocumentDTO dto = crudService.findById(id, type, false);

        resp.setHeader("Content-Disposition", "attachment; filename=\"" + dto.getFileName() + "\"");
        resp.setHeader("Content-Type", dto.getFileMimeType());
        resp.setHeader("Content-Length", String.valueOf(dto.getFileLength()));
        byte[] buffer = readContent(dto);
        resp.getOutputStream().write(buffer);

    }

    @RequestMapping(value = "/reviewcomplete/{id}", method = RequestMethod.GET)
    public DocumentDTO reviewComplete(HttpServletRequest request, @PathVariable("id") UUID id) throws Exception {

        String[] fieldsArr = "all".split(",");
        Page<DocumentDTO> revs = crudService.findAllByType("doc_review", fieldsArr, new PageRequest(0, Integer.MAX_VALUE), "{\"rules\": [{\"op\": \"eq\",\"data\": \"" + request.getRemoteUser() + "\",\"field\": \"reviewer\"},{\"op\": \"eq\",\"data\": \"" + id + "\",\"field\": \"sys_parent_uuid\"}],\"groupOp\": \"AND\"}");
        if (revs.hasContent()) {
            DocumentDTO myrev = revs.getContent().get(0);
            ObjectMapper mapper = new ObjectMapper();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
            Date now = new Date();
            JsonNode data = mapper.readTree("{\"status\":\"Complete\", \"completion_date\":\"" + fmt.format(now) + "\"}");
            myrev.setData(data);
            crudService.update(myrev, request.getRemoteUser());
            return myrev;
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/importfoldertree/{id}", method = RequestMethod.GET)
    public ObjectNode importFolderTree(HttpServletRequest request, @PathVariable("id") UUID id) throws Exception {

        final DocumentDTO dto = crudService.findById(id, null, false);
        byte[] input = readContent(dto);
        String s = new String(input);
        Integer totalErrors = 0;
        Integer totalRows = 0;
        StringBuffer errorLog = new StringBuffer();
        ObjectMapper mapper = new ObjectMapper();
        List<DocumentDTO> dtoArray = mapper.readValue(s, new TypeReference<List<DocumentDTO>>() {
        });
        //DocumentDTOList dtoArray = mapper.readValue(s, DocumentDTOList.class);
        for (DocumentDTO sourceDto : dtoArray) {
            totalRows++;
            try {
                sourceDto.setSourceId(sourceDto.getUuid().toString());
                sourceDto.setSourceParent(sourceDto.getParent().toString());
                addDoc(sourceDto, request.getRemoteUser());
            } catch (Exception e) {
                errorLog.append(sourceDto.getUuid().toString() + " - " + e.getMessage() + "\n");
                e.printStackTrace();
                totalErrors++;
            }
        }
        String item = "{}";
        ObjectNode resultNode = (ObjectNode) mapper.readTree(item);
        resultNode.put("totalRows", totalRows);
        resultNode.put("totalErrors", totalErrors);
        DocumentDTO resDoc = new DocumentDTO();
        Date now = new Date();
        resDoc.setTitle("Result from " + now.toString());
        resDoc.setType("import_json_result");
        resDoc.setData(resultNode);
        resDoc.setParent(id);
        DocumentDTO resDTO = crudService.add(resDoc, request.getRemoteUser());
        byte[] file = String.valueOf(errorLog).getBytes();
        resDTO.setFileLength((long) file.length);
        resDTO.setFileMimeType("text/plain");
        resDTO.setFileName("importResult.txt");
        writeContent(resDTO, file, request.getRemoteUser());
        return resultNode;
    }

    @RequestMapping(value = "/checkimport/{id}", method = RequestMethod.GET)
    public ObjectNode checkImport(HttpServletRequest request, @PathVariable("id") UUID id) throws Exception {

        final DocumentDTO dto = crudService.findById(id, null, false);
        byte[] input = readContent(dto);
        char sep = ',';
        if (dto.getData().get("separator") != null) {
            sep = dto.getData().get("separator").textValue().charAt(0);
        }
        char quote = '"';
        if (dto.getData().get("quote") != null) {
            quote = dto.getData().get("quote").textValue().charAt(0);
        }
        Reader targetReader = new InputStreamReader(new ByteArrayInputStream(input));
        Scanner scanner = new Scanner(targetReader);
        boolean isHeader = true;
        List<String> header = null;
        Integer totalErrors = 0;
        Integer totalRows = 0;
        Map<String, Integer> errorByField = new HashMap<String, Integer>();
        StringBuffer errorLog = new StringBuffer();
        while (scanner.hasNext()) {
            List<String> line = CSVutils.parseLine(scanner.nextLine(), sep, quote);
            if (isHeader) {
                header = line;
                isHeader = false;
            } else {
                boolean hasError = false;
                try {
                    DocumentDTO doc = crudService.findBySourceID(line.get(0));
                    if (doc != null) {
                        for (int i = 1; i < line.size(); i++) {
                            String target = doc.getData().get(header.get(i)).textValue().replace("\r\n", "").replace("\n", "").replace("    ", " ");
                            if (line.get(i).equals(target)) {
                                LOGGER.info("checkImport id = {}, value - {} - OK", line.get(0), header.get(i));
                            } else {
                                LOGGER.error("checkImport id = {}, value - {} - FAILED", line.get(0), header.get(i));
                                errorLog.append(line.get(0) + " - field " + header.get(i) + " - target value - " + target + ", source value - " + line.get(i) + "\n");
                                hasError = true;
                                errorByField = increaseCounter(errorByField, header.get(i));
                            }
                        }
                    }
                } catch (DocumentNotFoundException e) {
                    hasError = true;
                    errorByField = increaseCounter(errorByField, "DocumentNotFound");
                    errorLog.append(line.get(0) + " - DocumentNotFound" + "\n");
                }
                if (hasError) {
                    totalErrors++;
                }
                totalRows++;
            }

        }
        ObjectMapper mapper = new ObjectMapper();
        String item = "{\"errorsByFields\":[]}";
        ObjectNode resultNode = (ObjectNode) mapper.readTree(item);
        resultNode.put("totalRows", totalRows);
        resultNode.put("totalErrors", totalErrors);
        ArrayNode errorsNode = (ArrayNode) resultNode.path("errorsByFields");
        LOGGER.info("Completed with {} errors from {} documents", totalErrors, totalRows);
        for (String errField : errorByField.keySet()) {
            LOGGER.info("Error by field {} - {}", errField, errorByField.get(errField));
            ObjectNode sNode = mapper.createObjectNode();
            sNode.put(errField, errorByField.get(errField));
            errorsNode.add(sNode);
            errorLog.append("Error by field " + errField + " - " + errorByField.get(errField) + "\n");
        }
        DocumentDTO resDoc = new DocumentDTO();
        Date now = new Date();
        resDoc.setTitle("Result from " + now.toString());
        resDoc.setType("import_check_result");
        resDoc.setData(resultNode);
        resDoc.setParent(id);
        DocumentDTO resDTO = crudService.add(resDoc, request.getRemoteUser());
        byte[] file = String.valueOf(errorLog).getBytes();
        resDTO.setFileLength((long) file.length);
        resDTO.setFileMimeType("text/plain");
        resDTO.setFileName("importResult.txt");
        writeContent(resDTO, file, request.getRemoteUser());
        scanner.close();
        targetReader.close();

        return resultNode;

    }

    private Map<String, Integer> increaseCounter(Map<String, Integer> errorByField, String key) {
        if (errorByField.containsKey(key)) {
            errorByField.put(key, errorByField.get(key) + 1);
        } else {
            errorByField.put(key, 1);
        }
        return errorByField;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Page<DocumentDTO> findAll(Pageable pageable, @RequestParam(value = "filters", required = false) String query) throws Exception {
        LOGGER.info("findAll(pageSize= {}, pageNumber = {}) ",
                pageable.getPageSize(),
                pageable.getPageNumber()
        );

        return crudService.findAll(pageable, query);
    }

    @RequestMapping(value = "/type/{type}", method = RequestMethod.GET)
    public Page<DocumentDTO> findByType(@PathVariable("type") String type, @RequestParam(value = "fields", required = false) String fields,
                                        @RequestParam(value = "filters", required = false) String query, Pageable pageable) throws Exception {
        LOGGER.info("findByType(type = {}, fields={}, query={}, pageSize= {}, pageNumber = {})",
                type, fields, query,
                pageable.getPageSize(),
                pageable.getPageNumber()
        );
        String[] fieldsArr = null;
        if (fields != null) {
            fieldsArr = fields.split(",");
        }

        return crudService.findAllByType(type, fieldsArr, pageable, query);
    }

    @RequestMapping(value = "/exportbytype/{type}", method = RequestMethod.GET)
    public @ResponseBody
    String exportByType(@PathVariable("type") String type, @RequestParam(value = "fields", required = false) String fields,
                        @RequestParam(value = "filters", required = false) String query,
                        HttpServletResponse httpServletResponse) throws Exception {
        LOGGER.info("findByType(type = {}, fields={}, query={})",
                type, fields, query
        );
        String[] fieldsArr = new String[] { "all" };
        if (fields != null) {
            fieldsArr = fields.split(",");
        }
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"export.json\"");
        Page<DocumentDTO> res = crudService.findAllByType(type, fieldsArr, new PageRequest(0, Integer.MAX_VALUE), query);

        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();

        MediaType jsonMimeType = MediaType.APPLICATION_JSON;


        try {
            jsonConverter.write(res, jsonMimeType, new ServletServerHttpResponse(httpServletResponse));
        } catch (HttpMessageNotWritableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @RequestMapping(value = "/exportSelected/{config}", method = RequestMethod.POST)
    public @ResponseBody ExportResultDTO exportSelectedByConfig(
            @PathVariable("config") String configSysName,
            @RequestBody(required = false) JsonNode body,
            HttpServletRequest request, HttpServletResponse response) {

        LOGGER.info("Export by config called: {}", configSysName);
        try {
            SystemDTO exportConfig = sysService.findBySymbolicName(configSysName);
            if (exportConfig == null) {
                throw new RuntimeException("Unable to fetch config by symbolic name (" + configSysName + ")");
            }

            DocumentExportService serviceByType = BeanFactoryAnnotationUtils.qualifiedBeanOfType(
                    context.getAutowireCapableBeanFactory(), DocumentExportService.class, exportConfig.getType()
            );

            String[] fields = new String[0];

            // search composition fields are preferred
            if (body.has("searchId")) {
                DocumentDTO searchDoc = crudService.findById(UUID.fromString(body.get("searchId").asText()), null, false);
                ArrayList<String> fl = new ArrayList<>();
                searchDoc.getData().get("fields").forEach(jsonNode -> fl.add(jsonNode.get("name").asText()));
                fields = fl.toArray(fields);
            }

            // if no fields defined above, trying get the explicit list
            if (fields.length == 0 && body.has("fields")) {
                ArrayList<String> fl = new ArrayList<>();
                body.get("fields").forEach(jsonNode -> fl.add(jsonNode.asText()));
                fields = fl.toArray(fields);
            }

            UUID[] items = new UUID[0];
            if (body.has("selectedIds")) {
                ArrayList<UUID> ul = new ArrayList<>();
                body.get("selectedIds").forEach(jsonNode -> ul.add(UUID.fromString(jsonNode.asText())));
                items = ul.toArray(items);
            }

            ExportResultDTO result = serviceByType.exportSelectedDocuments(exportConfig, fields, items);

            if (result.isDownloadable()) { // means we need to send the file on the client
                response.setHeader("Content-Disposition", "attachment; filename=\"" + result.getDownloadFileName() + "\"");
                response.setHeader("Content-Type", result.getContentType());
                IOUtils.copy(result.getContentStream(), response.getWriter());
                return null;
            } else return result;         // or just return the information about the export result
        } catch (Exception e) {
            throw new RuntimeException("Unable to export documents", e);
        }
    }

    @RequestMapping(value = "/versions/{seriesid}", method = RequestMethod.GET)
    public List<DocumentDTO> findAllVersions(@PathVariable("seriesid") UUID seriesId) throws Exception {
        LOGGER.info("findAllVersions(seriesid = {})",
                seriesId
        );

        return crudService.findAllVersions(seriesId);
    }

    @RequestMapping(value = "/distinct", method = RequestMethod.GET)
    public JsonNode getDistinct(@RequestParam(value = "field", required = true) String field, @RequestParam(value = "type", required = true) String type, @RequestParam(value = "query", required = false) String query) throws Exception {
        LOGGER.info("getDistincts(field = {}, type - {}, query - {})",
                field, type, query
        );

        return crudService.getDistinct(field, type, query);
    }

    @RequestMapping(value = "/parent/{parentid}", method = RequestMethod.GET)
    public Page<DocumentDTO> findByParent(@PathVariable("parentid") UUID parentid, Pageable pageable) throws Exception {
        LOGGER.info("findByParent(parentid = {})", parentid);

        return crudService.findAllByParent(parentid, pageable);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public DocumentDTO findById(@PathVariable("id") UUID id, @RequestParam(value = "type", required = false) String type) throws Exception {
        LOGGER.info("findById(id= {})", id);

        return crudService.findById(id, type, true);
    }


    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public JsonNode findBySearchTerm(@RequestParam("searchTerm") String searchTerm,
                                     @RequestParam("resultConfigId") String searchCompId,
                                     Pageable pageable) throws Exception {
        LOGGER.info("findBySearchTerm(searchTerm = {}, pageSize= {}, pageNumber = {}) ",
                searchTerm,
                pageable.getPageSize(),
                pageable.getPageNumber()
        );
        //String searchCompId = "970ac89d-d49b-4a1a-b7db-9e58326b1ca9";
        UUID id = UUID.fromString(searchCompId);
        DocumentDTO searchDoc = crudService.findById(id, null, false);

        ArrayNode fieldsArr = null;
        if (searchDoc != null && searchDoc.getData() != null)
            fieldsArr = (ArrayNode) searchDoc.getData().get("fields");

        return searchService.findBySearchTerm(searchTerm, pageable, fieldsArr, null);
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public JsonNode findBySearchTermWithFilters(@RequestParam("searchTerm") String searchTerm,
                                                @RequestParam("resultConfigId") String searchCompId,
                                                HttpServletRequest request,
                                                Pageable pageable) throws Exception {
        LOGGER.info("findBySearchTerm(searchTerm = {}, pageSize= {}, pageNumber = {}) ",
                searchTerm,
                pageable.getPageSize(),
                pageable.getPageNumber()
        );
        //String searchCompId = "970ac89d-d49b-4a1a-b7db-9e58326b1ca9";
        UUID id = UUID.fromString(searchCompId);
        DocumentDTO searchDoc = crudService.findById(id, null, false);

        ArrayNode fieldsArr = null;
        if (searchDoc != null && searchDoc.getData() != null)
            fieldsArr = (ArrayNode) searchDoc.getData().get("fields");

        ObjectMapper mapper = new ObjectMapper();
        String body = IOUtils.toString(request.getInputStream());
        ObjectNode params = null;
        if (!"".equals(body)) {
            params = (ObjectNode) mapper.readTree(body);
        }

        return searchService.findBySearchTerm(searchTerm, pageable, fieldsArr, params);
    }

    private DocumentDTO addDoc(DocumentDTO dto, String user) throws Exception {

        LOGGER.debug("entering addDoc(dto={}, user= {}");
        //final String minorDocVersion = VersionHelper.generateMinorDocVersion(dto.getDocVersion());
        //LOGGER.debug("addDoc(): minorDocVersion {}", minorDocVersion);
        //dto.setDocVersion(minorDocVersion);
        if (dto.getDocVersion() != null) {
            if (dto.getDocVersion().split("\\|").length > 1) {
                String[] verInfo = dto.getDocVersion().split("\\|");
                String isLast = verInfo[0];
                String verNum = verInfo[1];
                String verSeries = verInfo[2];
                String verParent = verInfo[3];
                if (!verSeries.equals(dto.getSourceId())) {
                    DocumentDTO series = crudService.findBySourceID(verSeries);
                    dto.setVersionSeries(series.getId());
                    if (verSeries.equals(verParent)) {
                        dto.setVersionParent(series.getId());
                    } else if (verParent.equals("0000000000000000")) {
                        dto.setVersionParent(series.getId());
                    } else {
                        DocumentDTO verParDoc = crudService.findBySourceID(verParent);
                        dto.setVersionParent(verParDoc.getId());
                    }
                }
                if (isLast.equals("F")) {
                    dto.setLastVersion(false);
                } else {
                    dto.setLastVersion(true);
                }
                dto.setDocVersion(verNum);

            }
        }
        if (dto.getSourceParent() != null) {
            try {
                DocumentDTO par = crudService.findBySourceID(dto.getSourceParent());
                dto.setParent(par.getId());
            } catch (Exception e) {
                LOGGER.error("Can not find parent by source Id - {}", e.getMessage());
            }
        }

        final DocumentDTO documentDTO = crudService.add(dto, user);
        if (dto.getLinks() != null) {
            addLinks(dto, documentDTO.getId());
        }
        LOGGER.debug("leaving addDoc(): created document = {}", documentDTO);

        return documentDTO;
    }

    private void addLinks(DocumentDTO dto, UUID docUUID) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            LOGGER.debug("links - {}", mapper.writeValueAsString(dto.getLinks()));


            if (dto.getLinks().isArray()) {
                for (final JsonNode linkNode : dto.getLinks()) {
                    LOGGER.debug("link - {}", mapper.writeValueAsString(linkNode));
                    String type = "";
                    if (linkNode.has("type")) {
                        type = linkNode.get("type").textValue();
                    }
                    if (linkNode.has("sourceId")) {
                        try {
                            DocumentDTO linkedDoc = crudService.findBySourceID(linkNode.get("sourceId").textValue());
                            crudService.addLink(linkedDoc.getId(), docUUID, type);
                        } catch (Exception e) {
                            LOGGER.error("Error in links by source Id - {}", e.getMessage());
                        }
                    } else if (linkNode.has("uuid")) {
                        try {
                            crudService.addLink(UUID.fromString(linkNode.get("uuid").textValue()), docUUID, type);
                        } catch (Exception e) {
                            LOGGER.error("Error in links by UUID - {}", e.getMessage());
                        }
                    }
                }
            }
        } catch (JsonProcessingException e1) {
            e1.printStackTrace();
        }

    }

    private byte[] readContent(DocumentDTO dto) throws Exception {

        LOGGER.info("getContent(): Found Document {}", dto);

        if (dto == null)
            throw new Exception("The document was not found in database ");

        final String filePath = dto.getFilePath();
        if (StringUtils.isBlank(filePath)) {
            LOGGER.error("Filepath is empty. Content for document {} does not exist", dto);
            throw new Exception("Filepath is empty, content for document " + dto + "does not exist");
        }

        final JsonNode storageSettings = fileService.getStorageSettingByStorageAreaName(dto.getFileStorage());
        LOGGER.debug("getContent(): storageSettings {}", storageSettings);

        return fileService.readFile(storageSettings, filePath);
    }

    private DocumentDTO writeContent(DocumentDTO dto, MultipartFile mpf, String user) throws Exception {
        if (!checkMultipartFile(mpf))
            throw new Exception("The multipart file contains either empty content type or empty filename or does not contain data");
        LOGGER.debug("writeContent(): the document: {} has been added, starting write to storage", dto);

        return writeContent(dto, mpf.getBytes(), user);
    }

    private DocumentDTO writeContent(DocumentDTO dto, byte[] file, String user) throws Exception {
        LOGGER.debug("entering writeContent(dto={}, user={})", dto, user);
        try {
            final JsonNode storageSettings = fileService.getStorageSettingsByDocType(dto.getType());
            LOGGER.debug("writeContent(): storageSettings {}", storageSettings);
            final String filePath = fileService.writeContent(dto.getId(), file, storageSettings);
            LOGGER.debug("writeContent(): file has been saved, filePath {}", filePath);
            dto.setFilePath(filePath);
            dto.setFileStorage(JsonNodeParser.getStorageAreaName(storageSettings));
            DocumentDTO updated = crudService.updateFileInfo(dto, user);
            LOGGER.debug("leaving writeContent(): Dto object has been updated: {}", updated);
            return updated;
        } catch (Exception e) {

//            todo add custom Exception
            LOGGER.error("The exception has been occurred while addContent method is executing {} {}", e.getMessage(), e);
            crudService.delete(dto.getId(), dto.getType());
            throw new Exception("Error has been occurred " + e.getMessage());
        }
    }

}
