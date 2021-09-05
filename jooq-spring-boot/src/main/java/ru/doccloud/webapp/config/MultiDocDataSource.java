package ru.doccloud.webapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import ru.doccloud.common.jooq.DocDsContext;

public class MultiDocDataSource extends AbstractRoutingDataSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDocDataSource.class);
    @Override
    protected Object determineCurrentLookupKey() {

        Object currentDs =  DocDsContext.getCurrentDS();

        LOGGER.debug("determineCurrentLookupKey() current DS {}", currentDs);

        return currentDs;
    }
}
