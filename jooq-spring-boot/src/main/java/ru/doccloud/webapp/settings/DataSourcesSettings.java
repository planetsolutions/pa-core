package ru.doccloud.webapp.settings;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "pa.persistence")
@Component
public class DataSourcesSettings {
    private List<Datasource> datasources;

    public List<Datasource> getDatasources() {
        return datasources;
    }

    public void setDatasources(List<Datasource> datasources) {
        this.datasources = datasources;
    }


    public Datasource getDefaultDS(){
        for (Datasource dataSourceSettings : getDatasources()) {
            if(!StringUtils.isBlank(dataSourceSettings.getDefaultDS()) &&
                    Boolean.valueOf(dataSourceSettings.getDefaultDS()).equals(true))
                return dataSourceSettings;
        }
        throw new RuntimeException("NO Default datasource defined");
    }


    @Override
    public String toString() {
        return "DataSourcesSettings{" +
                "listDs=" + datasources +
                '}';
    }

    public static class Datasource {

        private String name;
        private String factory;
        private String driverClassName;
        private String url;
        private String maxTotal;
        private String maxIdle;
        private String minIdle;
        private String maxWaitMillis;
        private String username;
        private String password;
        private String testOnBorrow;
        private String testWhileIdle;
        private String testOnReturn;
        private String validationQuery;
        private String removeAbandoned;
        private String defaultDS;
        private String removeAbandonedTimeout;

        private String authContainer;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFactory() {
            return factory;
        }

        public void setFactory(String factory) {
            this.factory = factory;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMaxTotal() {
            return maxTotal;
        }

        public void setMaxTotal(String maxTotal) {
            this.maxTotal = maxTotal;
        }

        public String getMaxIdle() {
            return maxIdle;
        }

        public void setMaxIdle(String maxIdle) {
            this.maxIdle = maxIdle;
        }

        public String getMinIdle() {
            return minIdle;
        }

        public void setMinIdle(String minIdle) {
            this.minIdle = minIdle;
        }

        public String getMaxWaitMillis() {
            return maxWaitMillis;
        }

        public void setMaxWaitMillis(String maxWaitMillis) {
            this.maxWaitMillis = maxWaitMillis;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getTestOnBorrow() {
            return testOnBorrow;
        }

        public void setTestOnBorrow(String testOnBorrow) {
            this.testOnBorrow = testOnBorrow;
        }

        public String getTestWhileIdle() {
            return testWhileIdle;
        }

        public void setTestWhileIdle(String testWhileIdle) {
            this.testWhileIdle = testWhileIdle;
        }

        public String getTestOnReturn() {
            return testOnReturn;
        }

        public void setTestOnReturn(String testOnReturn) {
            this.testOnReturn = testOnReturn;
        }

        public String getValidationQuery() {
            return validationQuery;
        }

        public void setValidationQuery(String validationQuery) {
            this.validationQuery = validationQuery;
        }

        public String getRemoveAbandoned() {
            return removeAbandoned;
        }

        public void setRemoveAbandoned(String removeAbandoned) {
            this.removeAbandoned = removeAbandoned;
        }

        public String getRemoveAbandonedTimeout() {
            return removeAbandonedTimeout;
        }

        public void setRemoveAbandonedTimeout(String removeAbandonedTimeout) {
            this.removeAbandonedTimeout = removeAbandonedTimeout;
        }

        public String getAuthContainer() {
            return authContainer;
        }

        public void setAuthContainer(String authContainer) {
            this.authContainer = authContainer;
        }

        public String getDefaultDS() {
            return defaultDS;
        }

        public void setDefaultDS(String defaultDS) {
            this.defaultDS = defaultDS;
        }

        @Override
        public String toString() {
            return "Datasource{" +
                    "name='" + name + '\'' +
                    ", factory='" + factory + '\'' +
                    ", driverClassName='" + driverClassName + '\'' +
                    ", url='" + url + '\'' +
                    ", maxTotal='" + maxTotal + '\'' +
                    ", maxIdle='" + maxIdle + '\'' +
                    ", minIdle='" + minIdle + '\'' +
                    ", maxWaitMillis='" + maxWaitMillis + '\'' +
                    ", username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", testOnBorrow='" + testOnBorrow + '\'' +
                    ", testWhileIdle='" + testWhileIdle + '\'' +
                    ", testOnReturn='" + testOnReturn + '\'' +
                    ", validationQuery='" + validationQuery + '\'' +
                    ", removeAbandoned='" + removeAbandoned + '\'' +
                    ", defaultDS='" + defaultDS + '\'' +
                    ", removeAbandonedTimeout='" + removeAbandonedTimeout + '\'' +
                    ", authContainer='" + authContainer + '\'' +
                    '}';
        }
    }
}
