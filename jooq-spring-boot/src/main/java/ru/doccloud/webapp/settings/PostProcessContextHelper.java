package ru.doccloud.webapp.settings;

import org.apache.catalina.Context;
import org.apache.catalina.realm.DataSourceRealm;
import org.apache.catalina.realm.JDBCRealm;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Created by ilya on 7/10/18.
 */
public class PostProcessContextHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostProcessContextHelper.class);

    public static ContextResource buildDSContextResource(DataSourcesSettings.Datasource dataSourceSettings) {

        LOGGER.debug("buildDSContextResource(dataSourcesSettings ={})", dataSourceSettings);
        final ContextResource resource = new ContextResource();
        resource.setName(dataSourceSettings.getName());
        resource.setType(DataSource.class.getName());
        resource.setProperty("factory", dataSourceSettings.getFactory());
        resource.setProperty("driverClassName", dataSourceSettings.getDriverClassName());
        resource.setProperty("url", dataSourceSettings.getUrl());

        resource.setProperty("maxTotal", dataSourceSettings.getMaxTotal());
        resource.setProperty("maxIdle", dataSourceSettings.getMaxIdle());
        resource.setProperty("minIdle", "5");
        resource.setProperty("maxWaitMillis", dataSourceSettings.getMaxWaitMillis());

        resource.setProperty("username", dataSourceSettings.getUsername());
        resource.setProperty("password", dataSourceSettings.getPassword());

        resource.setProperty("testOnBorrow", dataSourceSettings.getTestOnBorrow());
        resource.setProperty("testWhileIdle", dataSourceSettings.getTestWhileIdle());
        resource.setProperty("testOnReturn", dataSourceSettings.getTestOnReturn());
        resource.setProperty("validationQuery", dataSourceSettings.getValidationQuery());
        resource.setProperty("removeAbandoned", dataSourceSettings.getRemoveAbandoned());
        resource.setProperty("removeAbandonedTimeout", dataSourceSettings.getRemoveAbandonedTimeout());

        resource.setAuth(dataSourceSettings.getAuthContainer());

        return resource;
    }

    public static DataSourceRealm buildDataSourceRealm(RealmDsSettings realmDsSettings) {

        LOGGER.debug("buildDataSourceRealm(realmDsSettings={})", realmDsSettings);
        final DataSourceRealm realmDS = new DataSourceRealm();

        realmDS.setDataSourceName(realmDsSettings.getName());
        realmDS.setUserTable(realmDsSettings.getUserTable());
        realmDS.setUserNameCol(realmDsSettings.getUserNameCol());
        realmDS.setUserCredCol(realmDsSettings.getUserCredCol());
        realmDS.setUserRoleTable(realmDsSettings.getUserRoleTable());
        realmDS.setRoleNameCol(realmDsSettings.getRoleNameCol());
        realmDS.setAllRolesMode(realmDsSettings.getAllRolesMode());

        return realmDS;
    }

    public static JDBCRealm buildJDBCRealm(RealmDsSettings realmDsSettings) {

        LOGGER.debug("buildJDBCRealm(realmDsSettings={})", realmDsSettings);
        final JDBCRealm jdbcRealm = new JDBCRealm();

        jdbcRealm.setDriverName(realmDsSettings.getDriverName());
        jdbcRealm.setConnectionURL(realmDsSettings.getConnectionURL());
        jdbcRealm.setConnectionName(realmDsSettings.getConnectionName());
        jdbcRealm.setConnectionPassword(realmDsSettings.getConnectionPassword());
        jdbcRealm.setUserTable(realmDsSettings.getUserTable());
        jdbcRealm.setUserNameCol(realmDsSettings.getUserNameCol());
        jdbcRealm.setUserCredCol(realmDsSettings.getUserCredCol());
        jdbcRealm.setUserRoleTable(realmDsSettings.getUserRoleTable());
        jdbcRealm.setRoleNameCol(realmDsSettings.getRoleNameCol());
        jdbcRealm.setAllRolesMode(realmDsSettings.getAllRolesMode());

        return jdbcRealm;
    }


    public static SecurityConstraint buildSecurityConstraint(Context context, LoginConfigSettings loginConfigSettings) {
        LOGGER.debug("buildSecurityConstraint(loginConfigSettings={})", loginConfigSettings);
        LoginConfig config = new LoginConfig();
        config.setAuthMethod(loginConfigSettings.getAuthMethod());
        context.setLoginConfig(config);
        context.addSecurityRole(loginConfigSettings.getSecurityRole());

        SecurityConstraint constraint = new SecurityConstraint();
        constraint.addAuthRole(loginConfigSettings.getAuthRole());

        SecurityCollection collection = new SecurityCollection();
        collection.addPattern(loginConfigSettings.getPattern());
        constraint.addCollection(collection);

        return constraint;
    }

}
