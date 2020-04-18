package ru.doccloud.service.util;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import ru.doccloud.common.elasticsearch.ElasticsearchSettingsBean;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ElasticSearchHelper {
    public static Client buildClient() throws Exception {

        //see https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.6/transport-client.html for custom elastic parameters
        try {
            final Settings settings = Settings.builder()
                    .put("cluster.name", ElasticsearchSettingsBean.INSTANCE.getClusterName())
                    .put("client.transport.sniff", Boolean.valueOf(ElasticsearchSettingsBean.INSTANCE.getClientTransportSniff()))
                    .build();


            final TransportAddress inetAddress = new InetSocketTransportAddress(InetAddress.getByName(ElasticsearchSettingsBean.INSTANCE.getHost()),
                    Integer.parseInt(ElasticsearchSettingsBean.INSTANCE.getPort()));

            return new PreBuiltTransportClient(settings)
                    .addTransportAddress(inetAddress);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public static Client buildReadClient() throws UnknownHostException {
                return new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ElasticsearchSettingsBean.INSTANCE.getHost()),
                        Integer.parseInt(ElasticsearchSettingsBean.INSTANCE.getPort())));
    }
}
