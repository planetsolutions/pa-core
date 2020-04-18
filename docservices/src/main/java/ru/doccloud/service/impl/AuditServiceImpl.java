package ru.doccloud.service.impl;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.doccloud.common.elasticsearch.ElasticsearchSettingsBean;
import ru.doccloud.service.AuditService;
import ru.doccloud.service.util.ElasticSearchHelper;

/**
 * write to elastics search
 */
@Service
public class AuditServiceImpl implements AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditServiceImpl.class);

    public void sendResponse(String jsonResponse, String indexName)  {
        LOGGER.debug("entering sendResponse(jsonResponse = {}, documentId = {}, indexName= {}) ", jsonResponse, indexName);

        Client client = null;
        try {
            LOGGER.trace("sendResponse(): elasticSearchSettings {}", ElasticsearchSettingsBean.INSTANCE.toString());

            client = ElasticSearchHelper.buildClient();


            XContentBuilder b = XContentFactory.jsonBuilder().prettyPrint();
            try (XContentParser p = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, jsonResponse)) {
                b.copyCurrentStructure(p);
            }
            LOGGER.trace("sendResponse(): content string {}", b.string());


            IndexResponse response = null;

            response = client.prepareIndex(indexName, "document")
                    .setSource(jsonResponse, b.contentType()).execute().actionGet();
//
//            response = client.prepareIndex("documents_audit", "document")
//                    .setSource(jsonResponse, b.contentType()).execute().actionGet();

            LOGGER.debug("leaving sendResponse() : Response status {}", response.status());
        } catch (Exception e) {
            LOGGER.error("sendResponse() exception has been thrown {}", e.getMessage());
        }
        finally {
            if(client != null) {
//                read document
//                GetResponse response = client.prepareGet("documents_audit", "document", "0b8b69d2-31f8-41ce-ac15-87af4d8bd388").get();
//Map<String, Object> map = response.getSource();
                client.close();
            }
        }
    }




}
