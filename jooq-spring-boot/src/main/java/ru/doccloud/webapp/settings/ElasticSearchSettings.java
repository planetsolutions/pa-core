package ru.doccloud.webapp.settings;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pa.elasticsearch")
public class ElasticSearchSettings {
    private String host;
    private String port;
    private String clusterName;
    private String clientTransportSniff;

    public ElasticSearchSettings() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setClientTransportSniff(String clientTransportSniff) {
        this.clientTransportSniff = clientTransportSniff;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getClientTransportSniff() {
        return clientTransportSniff;
    }

    @Override
    public String toString() {
        return "ElasticSearchSettings{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", clientTransportSniff='" + clientTransportSniff + '\'' +
                '}';
    }
}
