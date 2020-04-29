package ru.doccloud.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * @author Andrey Kadnikov
 */
public interface SystemCrudService<SystemDTO> extends CommonService<SystemDTO> {


    @Transactional(readOnly = true)
    JsonNode findSettings(final String settingsKey);


    SystemDTO findById(final UUID id) throws Exception;

    SystemDTO findById(final String uuid)  throws Exception;
    
    @Transactional
    SystemDTO updateFileInfo(final SystemDTO dto)  throws Exception;

	SystemDTO findBySymbolicName(String symbolic)  throws Exception;

	Map<String, SystemDTO> getModifiedTypes()  throws Exception;

	void emptyModifiedTypes();

	JsonNode addParentSchema(SystemDTO typedoc, JsonNode schemaNode, int curLevel)  throws Exception;


	ArrayList<String> getDatasourcesNames();

}
