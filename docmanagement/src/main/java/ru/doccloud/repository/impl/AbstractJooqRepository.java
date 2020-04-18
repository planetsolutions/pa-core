package ru.doccloud.repository.impl;

import java.util.ArrayList;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectField;
import org.jooq.impl.TableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import ru.doccloud.common.service.DateTimeService;

/**
 * Created by ilya on 6/3/17.
 */
public abstract class AbstractJooqRepository {

    final DSLContext jooq;

    final DateTimeService dateTimeService;

    AbstractJooqRepository(DSLContext jooq, DateTimeService dateTimeService) {
        this.jooq = jooq;
        this.dateTimeService = dateTimeService;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJooqRepository.class);

    @Transactional
    public void setUser(String userName) {
        LOGGER.trace("Current User - {}",userName);
        jooq.execute("SET my.username = '"+userName+"'");

        //jooq.execute("SELECT current_setting('my.username') FROM documents LIMIT 1;");
    }


    long findTotalCount(TableImpl<?> table) {
        LOGGER.trace("entering findTotalCount()");

        long resultCount = jooq.selectCount()
                .from(table)
                .fetchOne(0, long.class);

        LOGGER.trace("leaving findTotalCount(): Found search result count: {}", resultCount);

        return resultCount;
    }

    long findTotalCountByType(Condition cond, TableImpl<?> table) {
        LOGGER.trace("entering findTotalCountByType(cond={})", cond);
        //ArrayList<SelectField<?>> selectedFields = new ArrayList<SelectField<?>>();
        //selectedFields.add(field);
        // long resultCount = jooq.fetchCount(jooq.select(selectedFields).from(table).where(cond));
        long resultCount = jooq.selectCount()
        		   .from(table)
        		   .where(cond)
        		   .fetchOne(0, long.class);
        LOGGER.trace("leaving findTotalCountByType(): Found search result count: {}", resultCount);

        return resultCount;
    }
}
