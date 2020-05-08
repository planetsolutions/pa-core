package ru.doccloud.repository.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonFromRecordBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonFromRecordBuilder.class);

    public static ObjectNode buildObjectNodeFromRecord(Record queryResult, String field){
        ObjectNode data = JsonNodeFactory.instance.objectNode();
        ObjectMapper mapper = new ObjectMapper();
        try {
            final Object fieldValue = queryResult.getValue(field);
            if (fieldValue!=null){
                final String fieldValueStr = fieldValue.toString();
                if(!"null".equals(fieldValueStr)) {
                    LOGGER.debug("field node - {}", fieldValueStr);
                    final JsonNode json = mapper.readTree(fieldValueStr);
                    if (!json.isNull() && json.getClass() != NullNode.class) {
                        data = (ObjectNode) json;
                    }
                }
            } else {
                LOGGER.debug("field node {} is null", field);
            }
        } catch (IllegalArgumentException | IOException e) {
            LOGGER.error("buildObjectNodeFromRecord(): exception {}", e.getMessage());
        }

        return data;
    }

    public static ObjectNode buildObjectNodeFromRecord(Record queryResult, String[] fields){
        ObjectNode data = JsonNodeFactory.instance.objectNode();
        ObjectMapper mapper = new ObjectMapper();
        if (fields!=null && fields.length >0){
            if ("all".equals(fields[0])){
                try {
                    final Object dataValue = queryResult.getValue("data");
                    if (dataValue!=null){
                        final String dataValueStr = dataValue.toString();
                        if(!"null".equals(dataValueStr)) {
                            LOGGER.debug("Data node - {}", dataValueStr);
                            JsonNode json = mapper.readTree(dataValueStr);
                            if (!json.isNull() && json.getClass() != NullNode.class) {
                                data = (ObjectNode) json;
                            }
                        } else {
                            LOGGER.debug("data is null");
                        }
                    }
                } catch (IllegalArgumentException | IOException e) {
                    LOGGER.error("buildObjectNodeFromRecord(): exception {}", e.getMessage());
                }
            }else{
                for (String field : fields) {
                    if (queryResult.getValue(field)!=null){
                        try {
                            final String fieldValue = queryResult.getValue(field).toString();
                            JsonNode node = mapper.readTree(fieldValue);
                            data.set(field, node);
                        } catch (IllegalArgumentException | IOException e) {
                            LOGGER.error("buildObjectNodeFromRecord(): exception {}", e.getMessage());
                        }
                    }
                }
            }
        }
        return data;
    }
}
