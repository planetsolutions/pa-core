package ru.doccloud.common;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class CommonTest {
    public final static String LOGIN_PATH = "jooq/login";
    public final static String HTTP_SCHEMA_URL = "http";

    public final static String URL = "doccloud.ru";
    public final static int PORT = 8888;

    public static HttpClient httpClient;
    public static String jwtToken;

    public final static String SERVER_URL = "http://doccloud.ru:8888/jooq/api/docs";
    public static final String DEFAULT_USER = "boot";
    public static final String DEFAULT_PASS = "boot";

    @BeforeClass
    public static void init(){
        httpClient = HttpClientBuilder.create().build();
        jwtToken = getJwtToken();
    }

    // TODO: 25.04.2020 use JWTMock.getJWT instead of getJWTToken after ading spring profile
    public static String getJwtToken(){
        HttpResponse response;
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme(HTTP_SCHEMA_URL).setHost(URL).setPort(PORT).setPath(LOGIN_PATH);
            uriBuilder.addParameter("username", "boot").addParameter("password", "boot");
            URI uri = uriBuilder.build();
            HttpPost httpPost = new HttpPost(uri );

            httpPost.setEntity(new StringEntity(
                    prepareLoginInfoAsJSON(),
                    ContentType.create("application/json")));
            response = httpClient.execute(httpPost, getHttpClientContext());
            int statusLoginCode = response.getStatusLine().getStatusCode();

            if(statusLoginCode == HttpStatus.SC_OK) {

                HttpEntity entity = response.getEntity();

                String content = EntityUtils.toString(entity);

                JSONObject jObject = new JSONObject(content);

                return jObject.getString("access_token");
            }

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HttpClientContext getHttpClientContext(){
        HttpHost targetHost = new HttpHost(SERVER_URL);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(DEFAULT_USER, DEFAULT_PASS));

        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());

// Add AuthCache to the execution context
        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        return context;
    }

    private static String prepareLoginInfoAsJSON(){
        return "{\"username\":\"boot\",\"password\":\"boot\"}";
    }
}
