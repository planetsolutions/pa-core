package ru.doccloud.service.impl;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.jtransfo.JTransfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ru.doccloud.repository.DocumentRepository;
import ru.doccloud.service.DocumentSearchService;
import ru.doccloud.service.UserService;
import ru.doccloud.service.document.dto.UserDTO;
import ru.doccloud.service.util.ElasticSearchHelper;

/**
 * @author Andrey Kadnikov
 */
@Service
public class RepositoryDocumentSearchService implements DocumentSearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryDocumentSearchService.class);

    private DocumentRepository repository;

    private JTransfo transformer;
    
    private UserService userService;

    @Autowired
    public RepositoryDocumentSearchService(DocumentRepository repository, JTransfo transformer, UserService userService) {
        this.repository = repository;
        this.transformer = transformer;
        this.userService = userService;
    }

    @Override
    public JsonNode findBySearchTerm(String searchTerm, Pageable pageable, ArrayNode fieldsArr, ObjectNode params) {
        LOGGER.debug("entering findBySearchTerm(searchTerm={}, pageSize= {}, pageNumber = {})",
                searchTerm,
                pageable.getPageSize(),
                pageable.getPageNumber()
        );
        //List<DocumentDTO> dtos = new ArrayList<DocumentDTO>();
        JsonNode res =null;
        long total = 0;
		try {

			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			LOGGER.info("httpservlet request from findBySearchTerm {} ", request);

			final String username = request.getRemoteUser();

			final UserDTO userDTO = userService.getUserDto(username);
			//List<String> readersArr = new ArrayList<String>();
	        //readersArr.add("test");
	        //readersArr.add("admins");
	        final String[] readers = userDTO.getGroups();//readersArr.toArray(new String[0]);
	        LOGGER.debug("username {}, groups {}",username,String.join(",",readers));
	        
			BoolQueryBuilder query = QueryBuilders.boolQuery()
					.must(QueryBuilders.queryStringQuery(searchTerm))
					.filter(QueryBuilders.termsQuery("readers", readers));
			
			if (params!=null){
				JsonNode must = params.path("query").path("bool").path("filter").path("bool").path("must");
				if (must!=null && must.isArray()){
					for (JsonNode node : must){
						Iterator<String> itr = node.get("terms").fieldNames();
		                while (itr.hasNext()) {  
		                	String termField = itr.next();
		                	JsonNode  termValues = node.get("terms").get(termField);
		                	if (termValues!=null && termValues.isArray()){
		                		for (JsonNode termValue : termValues){
				                	LOGGER.info("Filter on {} - {}",termField, termValue.textValue());
				                	query.filter(QueryBuilders.termsQuery(termField, termValue.textValue()));
				                
		                		}
		                	}
		                }
						
					}
				}
			}
			String sortField = "_score";
			SortOrder sortDirection = SortOrder.DESC;
			if (pageable.getSort()!=null){
			for (Sort.Order specifiedField : pageable.getSort()) {
				if (!sortField.equals(specifiedField.getProperty())){
					sortField = "data."+specifiedField.getProperty()+".keyword";
					if (specifiedField.getDirection().equals(Sort.Direction.ASC)) sortDirection = SortOrder.ASC;
				}
			}
			}
			final String[] includes = {};
		    final String[] excludes = {"attachment"};
		    SearchRequestBuilder rb = ElasticSearchHelper.buildClient().prepareSearch("doccloud")
			        //.setTypes("609")
			        .addSort(sortField, sortDirection)
			        //.setScroll(new TimeValue(60000))
			        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(query)
			        .setFetchSource(includes, excludes)
			        .setSize(pageable.getPageSize());

	        for (JsonNode node : fieldsArr) {
	        	if (node.get("faceted")!=null && node.get("faceted").asBoolean())
	        		rb.addAggregation(AggregationBuilders.terms(node.get("label").asText()).field("data."+node.get("name").asText()+".keyword"));
	        }
			final SearchResponse scrollResp = rb.get();
			LOGGER.debug("hits found - {}",scrollResp.getHits().getHits().length);
			LOGGER.info(scrollResp.toString());
			ObjectMapper mapper = new ObjectMapper();
//			for (JsonNode node : fieldsArr) {
//	        	if (node.get("faceted")!=null && node.get("faceted").asBoolean()){
//	        		Terms term = scrollResp.getAggregations().get(node.get("label").asText());
//	    			for (Terms.Bucket bucket : term.getBuckets()) {
//	    				LOGGER.info("Aggregations - {} - {} - {}",node.get("label").asText(),bucket.getKey(),bucket.getDocCount());
//	    			}
//	        	}
//			}
//	        		
//			total  = scrollResp.getHits().getTotalHits();
//			    for (SearchHit hit : scrollResp.getHits().getHits()) {
//			    	LOGGER.debug("hit - {} {} {}",hit.getId(), hit.getType(), hit.getSourceAsString());
//			    	dtos.add(mapper.readValue(hit.getSourceAsString(),DocumentDTO.class));
//			       
//			    }
//			LOGGER.debug("leaving findBySearchTerm(): found {}", dtos);
			
			res = mapper.readValue(scrollResp.toString(),JsonNode.class);
		} catch (Exception e) {
			LOGGER.error("findBySearchTerm(): exception {}", e);
		}
//		return new PageImpl<>(dtos,
//                new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort()),
//                total
//        );
		return res;
		
    }
}
