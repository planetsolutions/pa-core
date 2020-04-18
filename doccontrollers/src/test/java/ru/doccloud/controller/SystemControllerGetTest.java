package ru.doccloud.controller;


import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import ru.doccloud.controller.util.DocumentControllerTestsHelper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class SystemControllerGetTest {

    private final static String LOGIN_PATH = "jooq/login";
    private final static String HTTP_SCHEMA_URL = "http";

    private final static String URL = "localhost";
    private final static int PORT = 8888;
    private final static String BASIC_PATH = "jooq/api/system/";

    private final static String GET_CONTENT_PATH = BASIC_PATH + "getcontent/";

    private final static String GET_UUID_PATH = BASIC_PATH + "uuid/";

    private static final String PATH_TO_FILE = "/home/ilya/filenet_workspace/tomcat-users.xml";

    final UUID EXISTING_UUID = UUID.fromString("8b01505e-a0f7-4213-8743-efc494dc44d9");

    final UUID NON_EXISTING_UUID = UUID.randomUUID();




    private HttpClient httpClient;

    @Before
    public void init(){
        httpClient = HttpClientBuilder.create().build();
    }

    @Test
    public void getDocByIdTest(){
        try {
            final String token = getJwtToken();
            if(!StringUtils.isBlank(token)) {
                URIBuilder uriBuilder = new URIBuilder();
                uriBuilder.setScheme(HTTP_SCHEMA_URL).setHost(URL).setPort(PORT).setPath(BASIC_PATH + EXISTING_UUID);
                URI uri = uriBuilder.build();
                HttpGet httpGet = new HttpGet(uri);
                httpGet.addHeader("Authorization", token);
                HttpResponse response = httpClient.execute(httpGet, DocumentControllerTestsHelper.getHttpClientContext());
                int respStatus = response.getStatusLine().getStatusCode();
                System.out.println("response  status: " + respStatus + "os ok ? " + (respStatus == HttpStatus.SC_OK));

                if (respStatus == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    try {
                        entity.writeTo(os);
                    } catch (IOException e1) {
                    }
                    String contentString = new String(os.toByteArray());

                    System.out.println("contentString  \n " + contentString);
                }
            }

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
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
            response = httpClient.execute(httpPost, DocumentControllerTestsHelper.getHttpClientContext());
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

}