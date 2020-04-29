package ru.doccloud.webapp.config;

import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.doccloud.webapp.config.exception.JOOQToSpringExceptionTransformer;
import ru.doccloud.webapp.settings.DataSourcesSettings;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
public class MultiDSConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDSConfiguration.class);

    @Autowired
    private  DataSourcesSettings dataSourcesSettings;

    /**
     * Defines the data source for the application
     * @return
     */
    @Bean
    public DataSource dataSource() {
        Map<Object,Object> resolvedDataSources = new HashMap<>();

        for (DataSourcesSettings.Datasource dataSourceSettings : dataSourcesSettings.getDatasources()) {

            LOGGER.trace("createDSMap(): datasourceSettings {}", dataSourceSettings);

            JndiDataSourceLookup dataSource = new JndiDataSourceLookup();
            dataSource.setResourceRef(true);
            final DataSource ds = dataSource.getDataSource(dataSourceSettings.getName());

            LOGGER.trace("dataSource(): {}", ds);
            if (ds == null)
                throw new RuntimeException("Datasource with jndi " + dataSourceSettings.getName() + " was not found. Please create datasource");
            LOGGER.trace("datasource {}", ds);
            resolvedDataSources.put(dataSourceSettings.getName(), ds);
        }

        LOGGER.trace("dataSource(): resolvedDataSources {}", resolvedDataSources);

        // Create the final multi-tenant source.
        // It needs a default database to connect to.
        // Make sure that the default database is actually an empty tenant database.
        // Don't use that for a regular tenant if you want things to be safe!
        MultiDocDataSource dataSource = new MultiDocDataSource();
        dataSource.setDefaultTargetDataSource(defaultDataSource());
        dataSource.setTargetDataSources(resolvedDataSources);

        // Call this to finalize the initialization of the data source.
        dataSource.afterPropertiesSet();

        return dataSource;
    }

    /**
     * Creates the default data source for the application
     * @return
     */
    private DataSource defaultDataSource() {

        final DataSourcesSettings.Datasource defaultDS = dataSourcesSettings.getDefaultDS();

        LOGGER.trace("defaultDataSource(): datasourceSettings {}", defaultDS);

        JndiDataSourceLookup dataSource = new JndiDataSourceLookup();
        dataSource.setResourceRef(true);
        final DataSource ds = dataSource.getDataSource(defaultDS.getName());

        if (ds == null)
            throw new RuntimeException("Datasource with jndi " + defaultDS.getName() + " was not found. Please create datasource");
        LOGGER.trace("defaultDataSource {}", ds);

        return ds;
    }

    @Bean
    public DataSourceTransactionManager transactionManager() throws Exception {
        return new DataSourceTransactionManager(new LazyConnectionDataSourceProxy(dataSource()));
    }

    @Bean
    public DataSourceConnectionProvider connectionProvider() throws Exception {
        return new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(new LazyConnectionDataSourceProxy(dataSource())));
    }

    @Bean
    public JOOQToSpringExceptionTransformer jooqToSpringExceptionTransformer() {
        return new JOOQToSpringExceptionTransformer();
    }

    @Bean
    public DefaultConfiguration configuration() throws Exception {
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();

        jooqConfiguration.set(connectionProvider());
        jooqConfiguration.set(new DefaultExecuteListenerProvider(
                jooqToSpringExceptionTransformer()
        ));

        jooqConfiguration.set(SQLDialect.POSTGRES);

        return jooqConfiguration;
    }

//
    @Bean
    public DefaultDSLContext jooq() throws Exception {
        return new DefaultDSLContext(configuration());
    }
}
