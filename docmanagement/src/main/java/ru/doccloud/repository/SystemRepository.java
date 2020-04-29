package ru.doccloud.repository;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Andrey Kadnikov
 */
public interface SystemRepository<SystemDocument> extends CommonRepository<SystemDocument> {

    @Transactional(readOnly = true)
    SystemDocument findSettings(final String settingsKey);

	public SystemDocument findBySymbolicName(String symbolic);

}
