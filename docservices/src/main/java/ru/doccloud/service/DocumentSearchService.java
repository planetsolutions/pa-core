package ru.doccloud.service;

import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Andrey Kadnikov
 */
public interface DocumentSearchService {

    public JsonNode findBySearchTerm(String searchTerm, Pageable pageable, ArrayNode fieldsArr, ObjectNode params);
}
