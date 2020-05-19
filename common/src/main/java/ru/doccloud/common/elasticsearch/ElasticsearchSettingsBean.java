package ru.doccloud.common.elasticsearch;

/**
 * Created by ilya on 3/12/18.
 */
public enum ElasticsearchSettingsBean {

    INSTANCE;

    private String host;
    private String port;
    private String clusterName;
    private String clientTransportSniff;

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getClientTransportSniff() {
        return clientTransportSniff;
    }

    public void initSettings(final String host, final String port, final String clusterName, final String clientTransportSniff) {
        this.host = host;
        this.port = port;
        this.clusterName = clusterName;
        this.clientTransportSniff = clientTransportSniff;
    }

    @Override
    public String toString() {
        return "ElasticsearchSettingsBean{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", clientTransportSniff='" + clientTransportSniff + '\'' +
                '}';
    }
}
