package ru.doccloud.document.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import ru.doccloud.document.controller.util.Data;
import ru.doccloud.document.controller.util.queryParam;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.DocumentExportService;
import ru.doccloud.service.FileService;
import ru.doccloud.service.SystemCrudService;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.document.dto.ExportResultDTO;
import ru.doccloud.service.document.dto.SystemDTO;

/**
 * @author Andrey Kadnikov
 */
@RestController
@RequestMapping("/restapi/systemdata")
public class IAController  extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IAController.class);

	@Autowired
	private ApplicationContext context;

	private final DocumentCrudService<DocumentDTO> crudService;
	private final SystemCrudService<SystemDTO> sysService;

//    private final DocumentSearchService searchService;
    
    public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String base_url = "http://localhost:8080/jooq";
    
    private final DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(TIMESTAMP_PATTERN);

    @Value("${pa.export.limit}")
    private int exportLimit = 100;
	

    @Autowired
    public IAController(DocumentCrudService<DocumentDTO> crudService,
						FileService fileService, SystemCrudService<SystemDTO> sysService) throws Exception {
        super(fileService, crudService);
        LOGGER.info("DocumentController(crudService={}, searchService = {}, storageAreaSettings= {}, storageManager={})", crudService, fileService);
        this.crudService = crudService;
        this.sysService = sysService;
//        this.searchService = searchService;
    }

    @RequestMapping(value = "/tenants", method = RequestMethod.GET)
    public JsonNode findTenants(Pageable pageable) throws Exception {
        LOGGER.info("findTenants");
        String parentid = "00000000-0000-0000-0000-000000000000";
        
        return getIaJson(crudService.findAllByParentAndType(UUID.fromString(parentid),"tenant",pageable),"tenants");
    }
    private JsonNode getXForm(DocumentDTO doc){
    	ObjectMapper mapper = new ObjectMapper();
    	ObjectNode res = null;
    	String base_url = "http://localhost:8080/jooq";
    	String base = "{"
    			+ "\"_links\" : {"
	    			+ "\"self\" : {"
	    			+ "\"href\" : \""+base_url+"/restapi/systemdata/xforms\""
	    			+ "}"
    			+ "}"
    			+ "}";
    	try {
    		res = (ObjectNode) mapper.readTree(base);
			res.put("lastModifiedBy",doc.getModifier());
			res.put("lastModifiedDate",dateTimeFormat.print(doc.getModificationTime()));
			res.put("version",doc.getDocVersion());
			
			final String filePath = doc.getFilePath();
	        if(StringUtils.isNotBlank(filePath)) {
		        JsonNode storageSettings = fileService.getStorageSettingByStorageAreaName(doc.getFileStorage());
	
		        byte[] file = fileService.readFile(storageSettings, filePath);
		        String fileString = new String(file);
		        res.put("form", fileString);
	        }
	        if (doc.getData()!=null){
	        	res.put("searchData", doc.getData());
			}

    	} catch (Exception e) {
			LOGGER.error("getXForm(): Exception {}", e);
		}
		
		return res;
			
    }
    private JsonNode getIaJson(Page<DocumentDTO> docs, String domain){
    	ObjectMapper mapper = new ObjectMapper();
    	JsonNode res = null;
    	String base = "{"
    			+ "\"_embedded\" : {"
    				+"\""+domain+"\" : []"
    			+ "},"
    			+ "\"_links\" : {"
	    			+ "\"self\" : {"
	    			+ "\"href\" : \""+base_url+"/restapi/systemdata/"+domain+"\""
	    			+ "}"
    			+ "},"
    			+ "\"page\" : {"
	    			+"\"size\" : "+docs.getSize()+","
	    			+"\"totalElements\" : "+docs.getTotalElements()+","
	    			+"\"totalPages\" : "+docs.getTotalPages()+","
	    			+"\"number\" : "+docs.getNumber()
    			+ "}"
    			+ "}";
    	
        
    	LOGGER.info(base);
    	
		try {
			res = mapper.readTree(base);
			LOGGER.info(mapper.writeValueAsString(res));
			ArrayNode nodes = (ArrayNode) res.path("_embedded").path(domain);
			ObjectNode itemNode = null;
			for (DocumentDTO doc : docs){
				itemNode = getIaJsonObj(doc, domain);
				nodes.add(itemNode);
			}
		
			LOGGER.info(mapper.writeValueAsString(res));
		} catch (IOException e) {
			LOGGER.error("getIaJson(): Exception {}", e);
		}
		
		return res;
    }
    
    private JsonNode getSearchResultJson(Page<DocumentDTO> docs, String[] docFields){
    	ObjectMapper mapper = new ObjectMapper();
    	JsonNode res = null;
    	String base = "{"
    			+ "\"_embedded\" : {"
    				+"\"results\" : []"
    			+ "},"
    			+ "\"_links\" : {"
	    			+ "\"self\" : {"
	    			+ "\"href\" : \""+base_url+"/restapi/systemdata/results\""
	    			+ "}"
    			+ "},"
    			+ "\"page\" : {"
	    			+"\"size\" : "+docs.getSize()+","
	    			+"\"totalElements\" : "+docs.getTotalElements()+","
	    			+"\"totalPages\" : "+docs.getTotalPages()+","
	    			+"\"number\" : "+docs.getNumber()
    			+ "}"
    			+ "}";
    	
        
    	LOGGER.info(base);
    	
		try {
			res = mapper.readTree(base);
			LOGGER.info(mapper.writeValueAsString(res));
			ArrayNode nodes = (ArrayNode) res.path("_embedded").path("results");
			ObjectNode resNode = (ObjectNode) mapper.readTree("{\"rows\":[]}");
			resNode.put("totalElements", docs.getTotalElements());
			resNode.put("empty", false);
			resNode.put("executionTime", 100);
			
			ArrayNode rows = (ArrayNode) resNode.path("rows");
			for (DocumentDTO doc : docs){
				ObjectNode itemNode = getSearchResultJsonObj(doc, docFields);
				rows.add(itemNode);
			}
			nodes.add(resNode);

			LOGGER.info(mapper.writeValueAsString(res));
		} catch (IOException e) {
			LOGGER.error("getSearchResultJson(): Exception {}", e);
		}
		
		return res;
    }
    private ObjectNode getSearchResultJsonObj(DocumentDTO doc, String[] docFields) throws JsonProcessingException, IOException{
    	ObjectMapper mapper = new ObjectMapper();

		ObjectNode itemNode = (ObjectNode) mapper.readTree("{\"columns\":[]}");
		itemNode.put("id", doc.getId().toString());
		ArrayNode columns = (ArrayNode) itemNode.path("columns");
		
		for (String field: docFields){
			ObjectNode colNode = (ObjectNode) mapper.readTree("{}");
			colNode.put("name", field);
			colNode.put("cid", false);
			if (doc.getData()!=null && doc.getData().get(field)!=null){
				if(doc.getData().get(field).isNumber()){
					colNode.put("value", doc.getData().get(field).doubleValue());
				}else{
					colNode.put("value", doc.getData().get(field).textValue());
				}
			}else{
				colNode.put("value","");
			}
			columns.add(colNode);
		}
		String docjson = mapper.writeValueAsString(doc);
		ObjectNode docNode = (ObjectNode) mapper.readTree(docjson);
		Iterator<Entry<String, JsonNode>> dociter = docNode.fields();
		while (dociter.hasNext()) {
			  Map.Entry<String, JsonNode> propentry = (Map.Entry<String, JsonNode>) dociter.next();
			  if (!propentry.getKey().equals("data")){
				ObjectNode sysNode = (ObjectNode) mapper.readTree("{}");
				sysNode.put("name", propentry.getKey());
				sysNode.put("cid", false);
				sysNode.put("value",propentry.getValue());
				columns.add(sysNode);
			  }
		}
		return itemNode;
    }
    private ObjectNode getIaJsonObj(DocumentDTO doc, String domain) throws JsonProcessingException, IOException{
    	ObjectMapper mapper = new ObjectMapper();
    	String item = "{"
				+ "\"permission\" : {"
					+"\"groups\" : []"
				+"},"
				+"\"_links\" : {}}";
		ObjectNode itemNode = (ObjectNode) mapper.readTree(item);
		itemNode.put("createdBy",doc.getAuthor());
		itemNode.put("createdDate",dateTimeFormat.print(doc.getCreationTime()));
		itemNode.put("lastModifiedBy",doc.getModifier());
		itemNode.put("lastModifiedDate",dateTimeFormat.print(doc.getModificationTime()));
		itemNode.put("version",doc.getDocVersion());
		itemNode.put("name",doc.getTitle());
		itemNode.put("description",doc.getDescription());
		
		
		ObjectNode linksNode = (ObjectNode) itemNode.path("_links");
		ObjectNode sNode = mapper.createObjectNode();
		sNode.put("href",base_url+"/restapi/systemdata/"+domain+"/"+doc.getId());
		linksNode.put("self", sNode);
		
		ObjectNode jNode = mapper.createObjectNode();
		if (domain.equals("tenants")){
			jNode.put("href",base_url+"/restapi/systemdata/"+domain+"/"+doc.getId()+"/applications");
			linksNode.put("http://identifiers.emc.com/applications", jNode);
			if (doc.getData()!=null){
				itemNode.put("tenantData", doc.getData());
			}
		}
		if (domain.equals("applications")){
			itemNode.put("structuredDataStorageAllocationStrategy","DEFAULT");
			itemNode.put("type", "APP_DECOMM");
			itemNode.put("platform", "PG");
			itemNode.put("archiveType", "TABLE");
			itemNode.put("searchCreated", true);
			itemNode.put("xdbLibraryAssociated", true);
			itemNode.put("state", "IN_TEST");
			itemNode.put("viewStatus", true);
			
			if (doc.getData()!=null){
				itemNode.put("appData", doc.getData());
			}
			jNode.put("href",base_url+"/restapi/systemdata/"+domain+"/"+doc.getId()+"/searches");
			linksNode.put("http://identifiers.emc.com/searches", jNode);
		}
		if (domain.equals("searches")){
			jNode.put("href",base_url+"/restapi/systemdata/"+domain+"/"+doc.getId()+"/search-compositions");
			linksNode.put("http://identifiers.emc.com/search-compositions", jNode);
			itemNode.put("description", doc.getDescription());
			itemNode.put("nestedSearch",false);
			itemNode.put("state","PUBLISHED");
			itemNode.put("inUse",true);
		}
		if (domain.equals("searchCompositions")){
			jNode.put("href",base_url+"/restapi/systemdata/xforms/"+doc.getId());
			linksNode.put("http://identifiers.emc.com/xform", jNode);
			ObjectNode jNode1 = mapper.createObjectNode();
			jNode1.put("href",base_url+"/restapi/systemdata/result-masters/"+doc.getId());
			linksNode.put("http://identifiers.emc.com/result-master", jNode1);
			itemNode.put("searchName", doc.getDescription());
			if (doc.getData()!=null){
				itemNode.put("searchData", doc.getData());
			}
		}
		if (domain.equals("panels")){
			ObjectNode panelNode = (ObjectNode) mapper.readTree("{\"tabs\":[]}");
			panelNode.put("name", "Main Panel");
			panelNode.put("title", "null");
			panelNode.put("description", "null");
			ArrayNode tabsnodes = (ArrayNode) panelNode.path("tabs");
			ObjectNode tabNode = (ObjectNode) mapper.readTree("{}");
			tabNode.put("name", "_ia_Default_Main_tab_");
			tabNode.put("title", "null");
			tabNode.put("description", "null");
			ArrayNode colsnodes = tabNode.putArray("columns");
			if (doc.getData()!=null && doc.getData().get("fields")!=null)
				colsnodes.addAll((ArrayNode) doc.getData().get("fields"));
			
//			ArrayNode colsnodes = (ArrayNode) tabNode.path("columns");
			
//			String[] docFields = doc.getData().get("fields").textValue().split(",");
//	        for (String field: docFields){
//	        	ObjectNode colNode = (ObjectNode) mapper.readTree("{}");
//				colNode.put("name", field);
//	        	colNode.put("label", field);
//	        	colNode.put("dataType", "STRING");
//	        	colNode.put("hidden", false);
//	        	colNode.put("sortable", true);
//	        	colsnodes.add(colNode);
//	        }
			
			tabsnodes.add(tabNode);
			
			ArrayNode panels = itemNode.putArray("panels");
			panels.add(panelNode);
		}

		return itemNode;
    }
    
    @RequestMapping(value = "/tenants/{tenantId}/applications", method = RequestMethod.GET)
    public JsonNode findAplications(@PathVariable("tenantId") String tenantId,Pageable pageable) throws Exception {
        LOGGER.info("findApplications");
        return getIaJson(crudService.findAllByParentAndType(UUID.fromString(tenantId), "application",pageable),"applications"); 
    }
    
    @RequestMapping(value = "/applications/{appId}", method = RequestMethod.GET)
    public JsonNode getApplication(@PathVariable("appId") String appId) throws Exception {
        LOGGER.info("findApplications");
        UUID id = UUID.fromString(appId);
        ObjectNode res = null;
        try {
			res = getIaJsonObj(crudService.findById(id, "application", false),"applications");
		} catch (IOException e) {
			LOGGER.error("getApplication(): Exception {}", e);
		}
        return res;
    }
    
    @RequestMapping(value = "/applications/{appId}/searches", method = RequestMethod.GET)
    public JsonNode findSearches(@PathVariable("appId") String appId,Pageable pageable) throws Exception {
        LOGGER.info("findSearches");
        UUID parentid = UUID.fromString(appId);
        return getIaJson(crudService.findAllByParentAndType(parentid, "search",pageable),"searches");
    }
    
    @RequestMapping(value = "/applications/{appId}/treeroot", method = RequestMethod.GET)
    public JsonNode findTreeRoot(@PathVariable("appId") String appId,Pageable pageable) throws Exception {
        LOGGER.info("findSearches");
        UUID parentid = UUID.fromString(appId);
        return getIaJson(crudService.findAllByParentAndType(parentid, "treeroot",pageable),"treeroots");
    }
    
    @RequestMapping(value = "/searches/{searchId}", method = RequestMethod.GET)
    public JsonNode getSearch(@PathVariable("searchId") String searchId) throws Exception {
        LOGGER.info("getSearch");
        UUID id = UUID.fromString(searchId);
        ObjectNode res = null;
        try {
			res = getIaJsonObj(crudService.findById(id,"searche",false),"searches");
		} catch (IOException e) {
			LOGGER.error("getSearch(): Exception {}", e);
		}
        return res;
    }
    
    @RequestMapping(value = "/searches/{searchId}/search-compositions", method = RequestMethod.GET)
    public JsonNode findSearchCompositions(@PathVariable("searchId") String searchId,Pageable pageable) throws Exception {
        LOGGER.info("findApplications");
        UUID parentid = UUID.fromString(searchId);
        return getIaJson(crudService.findAllByParentAndType(parentid, "search-composition",pageable),"searchCompositions");
    }
    
    @RequestMapping(value = "/search-compositions/{searchCompId}", method = RequestMethod.GET)
    public JsonNode getSearchComposition(@PathVariable("searchCompId") String searchCompId) throws Exception {
        LOGGER.info("getSearchComposition");
        UUID id = UUID.fromString(searchCompId);
        ObjectNode res = null;
        try {
			res = getIaJsonObj(crudService.findById(id, "search-composition", false),"searchCompositions");
		} catch (IOException e) {
			LOGGER.error("getSearchComposition(): Exception {}", e);
		}
        return res;
    }
    
    @RequestMapping(value = "/result-masters/{searchCompId}", method = RequestMethod.GET)
    public JsonNode getReultMaster(@PathVariable("searchCompId") String searchCompId) throws Exception {
        LOGGER.info("getSearchComposition");
        UUID id = UUID.fromString(searchCompId);
        ObjectNode res = null;
        try {
			res = getIaJsonObj(crudService.findById(id, "search-composition", false),"panels");
		} catch (IOException e) {
			LOGGER.error("getReultMaster(): Exception {}", e);
		}
        return res;
    }
    
    @RequestMapping(value = "/test", 
    		method = RequestMethod.POST)
    public JsonNode testRequest(HttpServletRequest request) {
    	JsonNode res = null;
    	try {
	    	String body = IOUtils.toString( request.getInputStream());
	        LOGGER.info("test Request, Body = "+body);
	        ObjectMapper mapper = new ObjectMapper();
        	res = mapper.readTree("{}");
		} catch (IOException e) {
			LOGGER.error("testRequest(): Exception {}", e);
		}
        return res;
    }
    @RequestMapping(value = "/search-compositions/{searchCompId}", 
    		method = RequestMethod.POST)
    public JsonNode processSearchRequest(@PathVariable("searchCompId") String searchCompId, 
    		@RequestParam(value = "mode", required=false) String mode,
    		@RequestParam(value = "exportConfig",required=false) String exportConfigName,
    		HttpServletRequest request,
            HttpServletResponse httpServletResponse,
            Pageable pageable ) throws Exception {
        LOGGER.info("processSearchRequest");
        ObjectMapper mapper = new ObjectMapper();
        UUID id = UUID.fromString(searchCompId);
        DocumentDTO searchDoc = crudService.findById(id, "search-composition", false);
        String docType = searchDoc.getData().get("type").textValue();
        
        ArrayNode fieldsArr = (ArrayNode) searchDoc.getData().get("fields");
        List<String> fieldsNameList = new ArrayList<>();
        for (JsonNode node : fieldsArr) {
        	fieldsNameList.add(node.get("name").asText());
        }
        String[] docFields = fieldsNameList.toArray(new String[0]);
        Page<DocumentDTO> searchRes = null;
    	JsonNode res = null;
    	try {
    		ObjectNode params = (ObjectNode) mapper.readTree("{}");

			ArrayNode nodes = (ArrayNode) mapper.readTree("[]");
			
			XmlMapper xmlMapper = new XmlMapper();
			String body = IOUtils.toString( request.getInputStream());
			LOGGER.debug(body);
			String searchParamsJson = "";
			if(!"".equals(body.trim())){
		    Data value = xmlMapper.readValue(body, Data.class);
		    
		    if (value.getCriterions()!=null){
		    queryParam[] qparams = value.getCriterions();
		    for (queryParam par : qparams){
		    	LOGGER.debug("Query param - {} = {}",par.getField(),par.getValue());
		    
				
				ObjectNode itemNode = (ObjectNode) mapper.readTree("{}");
				String betweenStr = "BETWEEN";
				if (betweenStr.equals(par.getOperand())){
					LOGGER.debug("Query value from - {} to - {}",par.getFrom(),par.getTo());
					ObjectNode itemNode1 = (ObjectNode) mapper.readTree("{}");
					itemNode1.put("field", par.getField());
					itemNode1.put("op", "ge");
					itemNode1.put("data", par.getFrom().getValue());
					nodes.add(itemNode1);
					itemNode.put("field", par.getField());
					itemNode.put("op", "le");
					itemNode.put("data", par.getTo().getValue());
					
				}else if(par.getOperand()!=null && par.getOperand().startsWith("GREATER")){
					itemNode.put("field", par.getField());
					itemNode.put("op", operandConverter(par.getOperand()));
					itemNode.put("data", par.getFrom().getValue());
				}else if(par.getOperand()!=null && par.getOperand().startsWith("LESS")){
					itemNode.put("field", par.getField());
					itemNode.put("op",  operandConverter(par.getOperand()));
					itemNode.put("data", par.getTo().getValue());	
				}else{
					if (par.getValue()!=null){
						itemNode.put("field", par.getField());
						itemNode.put("op", operandConverter(par.getOperand()));
						itemNode.put("data", par.getValue());
					}
				}
				nodes.add(itemNode);
				
			}
			params.put("groupOp", "AND");
			params.put("rules", nodes);
			
			searchParamsJson = params.toString();
		    }
			}else if (searchDoc.getData().get("default_query")!=null){
				searchParamsJson = mapper.writeValueAsString(searchDoc.getData().get("default_query")).replace("$UserName", request.getRemoteUser());
			}
			LOGGER.info("searchParamsJson - {}", searchParamsJson);
    	
	    	if (mode != null && "export".equals(mode)){
	    		SystemDTO exportConfig = sysService.findBySymbolicName(exportConfigName);

				if (exportConfig == null) {
					throw new RuntimeException("Unable to fetch config by symbolic name (" + exportConfigName + ")");
				}

				DocumentExportService serviceByType = BeanFactoryAnnotationUtils.qualifiedBeanOfType(
						context.getAutowireCapableBeanFactory(), DocumentExportService.class, exportConfig.getType()
				);

				// search documents without paging
				searchRes = crudService.findAllByType(docType, docFields, new PageRequest(0, exportLimit), searchParamsJson);

				ExportResultDTO exportResult = serviceByType.exportExplicitDocumentObjects(exportConfig, docFields.length > 0 ? docFields : new String[0], searchRes);
				if (exportResult.isDownloadable()) { 		// means we need to send the file on the client
					httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + exportResult.getDownloadFileName() + "\"");
					httpServletResponse.setHeader("Content-Type", exportResult.getContentType());
					IOUtils.copy(exportResult.getContentStream(), httpServletResponse.getWriter());
					return null;
				} else {
					return mapper.valueToTree(exportResult);   // or just return the information about the export result as JSON
				}

		   	} else if (mode != null && "countOnly".equals(mode)) {
				ObjectNode resNode = (ObjectNode) mapper.readTree("{}");
				resNode.put("totalElements", crudService.countAllByType(docType, docFields, pageable, searchParamsJson));
				return resNode;
			} else {
		   		searchRes = crudService.findAllByType(docType, docFields, pageable, searchParamsJson);
		   		return getSearchResultJson(searchRes, docFields);
		   	}
    	} catch (IOException e) {
			LOGGER.error("processSearchRequest(): Exception {}", e);
		}
    	return null;

    }
    
    private String operandConverter(String iaOperand){
    	String res = "cn";
    	switch (iaOperand){
    	case "EQUAL": res = "eq"; break;
    	case "NOT_EQUAL": res = "ne"; break;
    	case "STARTS_WITH": res = "bw"; break;
    	case "BEGINS_WITH": res = "bw"; break;
    	case "END_WITH": res = "ew"; break;
    	case "CONTAINS": res = "cn"; break;
    	case "GREATER_OR_EQUAL": res = "ge"; break;
    	case "GREATER": res = "gt"; break;
    	case "LESS_OR_EQUAL": res = "le"; break;
    	case "LESS": res = "lt"; break;
    	default: res = "cn";
    	}
    	return res;
    }
    
    @RequestMapping(value = "/xforms/{searchCompId}", method = RequestMethod.GET)
    public JsonNode getSearchXForm(@PathVariable("searchCompId") String searchCompId) throws Exception {
        LOGGER.info("getSearchXForm");
        UUID id = UUID.fromString(searchCompId);
        return getXForm(crudService.findById(id, "search-composition", false));
    }


}
