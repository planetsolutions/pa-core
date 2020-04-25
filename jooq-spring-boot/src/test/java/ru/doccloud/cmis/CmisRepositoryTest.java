package ru.doccloud.cmis;


import org.apache.chemistry.opencmis.client.SessionParameterMap;
import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.apache.commons.lang3.StringUtils;
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
import org.junit.Before;
import org.junit.Test;
import ru.doccloud.cmis.util.JWTMock;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class CmisRepositoryTest {

    private final static String LOGIN_PATH = "jooq/login";
    private final static String HTTP_SCHEMA_URL = "http";

    private final static String URL = "doccloud.ru";
    private final static int PORT = 8888;

    private HttpClient httpClient;

//    private static final String PROP_CUSTOM = "org.apache.chemistry.opencmis.binding.header.";

    @Before
    public void init(){
        httpClient = HttpClientBuilder.create().build();
    }

    @Test
    public void authTest(){

// TODO: 25.04.2020 use JWTMock.getJWT instead of getJWTToken after ading spring profile
        final String token = getJwtToken();
//        final String token = JWTMock.getJWT("boot");

        if(!StringUtils.isBlank(token)) {

            CmisBinding provider = getClientBindings("http://doccloud.ru:8888/jooq/browser", "boot", "boot", token);


            ObjectData myObject = provider.getObjectService().getObject("test", "0ff729c3-3b26-463f-8006-2fd79bdc124a",
                    "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);

            System.out.println("my Object " + myObject);


        }
    }

    private CmisBinding getClientBindings(String url, String user, String pwd, String token) {
        return createBrowserBinding(url, user, pwd, token);
    }

    private static void fillLoginParams(Map<String, String> parameters, String user, String password) {
        if (user != null && user.length() > 0) {
            parameters.put(SessionParameter.USER, user);
        }
        if (user != null && user.length() > 0) {
            parameters.put(SessionParameter.PASSWORD, password);
        }
    }

    private static CmisBinding createBrowserBinding(String url, String user, String password, String token) {

        // gather parameters
        Map<String, String> parameters = new HashMap<>();
        fillLoginParams(parameters, user, password);
//        fillCustomHeaders(parameters, token);

        // get factory and create binding
        CmisBindingFactory factory = CmisBindingFactory.newInstance();

        parameters.put(SessionParameter.BROWSER_URL, url);

        SessionParameterMap parameter = new SessionParameterMap();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            parameter.put(entry.getKey(), entry.getValue());
        }
        parameter.addHeader("cmisJwtAuthorization", token);
        return factory.createCmisBrowserBinding(parameter);
    }

    private String getJwtToken(){
        HttpResponse response = null;
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

                String accessToken = jObject.getString("access_token");

                System.out.println("JWT accessToken: " + accessToken);


                return accessToken;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String prepareLoginInfoAsJSON(){
        return "{\"username\":\"boot\",\"password\":\"boot\"}";
    }


    private final static String SERVER_URL = "http://localhost:8888/jooq/api/docs";
    private static final String DEFAULT_USER = "test";
    private static final String DEFAULT_PASS = "123456";

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

}
