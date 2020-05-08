package ru.doccloud.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;


public class JsonNodeParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonNodeParser.class);


    public static String getStorageAreaName(JsonNode storageSettings) throws Exception {
        LOGGER.trace("entering getStorageAreaName(storageSettings= {})", storageSettings);
        final String storageName = JsonNodeParser.getValueJsonNode(storageSettings, "symbolicName");
        LOGGER.trace("leaving getStorageAreaName(): storageName {}", storageName);

        return storageName;
    }


    public static String getValueJsonNode(final JsonNode settingsNode, final String paramName) throws Exception {
        LOGGER.debug("entering  getValueJsonNode(settingsNode={}, paramName={})", settingsNode, paramName);
        if(StringUtils.isBlank(paramName))
            throw new Exception("paramName is empty");

        LOGGER.debug("getValueJsonNode(): settingsNode {}", settingsNode);
        JsonNode value = settingsNode.findValue(paramName);
        if(value == null)
            throw new Exception("value for key " + paramName + "was not found in json settings");

        String rootFolder = String.valueOf(value.asText());
        LOGGER.debug("leaving getValueJsonNode(): repository for save file {}", rootFolder);
        return rootFolder;
    }



    public static JsonNode mergeJson(JsonNode mainNode, JsonNode updateNode) {

        Iterator<String> fieldNames = mainNode.fieldNames();

        while (fieldNames.hasNext()) {
            String updatedFieldName = fieldNames.next();
            JsonNode oldValue = mainNode.get(updatedFieldName);
            JsonNode updatedValue = updateNode.get(updatedFieldName);
            if (updatedValue == null ){
                if (updateNode instanceof ObjectNode) {
                    ((ObjectNode) updateNode).set(updatedFieldName, oldValue);
                }
            }else{
                if ("null".equals(updatedValue.textValue())){
                    if (updateNode instanceof ObjectNode) {
                        ((ObjectNode) updateNode).remove(updatedFieldName);
                    }
                }
            }
        }
        return updateNode;
    }
    public static JsonNode mergeJsonDeep(JsonNode mainNode, JsonNode updateNode) {

        Iterator<String> fieldNames = updateNode.fieldNames();

        while (fieldNames.hasNext()) {
            String updatedFieldName = fieldNames.next();
            JsonNode valueToBeUpdated = mainNode.get(updatedFieldName);
            JsonNode updatedValue = updateNode.get(updatedFieldName);

            // If the node is an @ArrayNode
            if (valueToBeUpdated != null && valueToBeUpdated.isArray() &&
                    updatedValue.isArray()) {
                // running a loop for all elements of the updated ArrayNode
                for (int i = 0; i < updatedValue.size(); i++) {
                    JsonNode updatedChildNode = updatedValue.get(i);
                    // Create a new Node in the node that should be updated, if there was no corresponding node in it
                    // Use-case - where the updateNode will have a new element in its Array
                    if (valueToBeUpdated.size() <= i) {
                        ((ArrayNode) valueToBeUpdated).add(updatedChildNode);
                    }
                    // getting reference for the node to be updated
                    JsonNode childNodeToBeUpdated = valueToBeUpdated.get(i);
                    mergeJsonDeep(childNodeToBeUpdated, updatedChildNode);
                }
                // if the Node is an @ObjectNode
            } else if (valueToBeUpdated != null && valueToBeUpdated.isObject()) {
                mergeJsonDeep(valueToBeUpdated, updatedValue);
            } else {
                if (mainNode instanceof ObjectNode) {
                    ((ObjectNode) mainNode).replace(updatedFieldName, updatedValue);
                }
            }
        }
        return mainNode;
    }
}
