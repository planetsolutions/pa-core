package ru.doccloud.cmis;


import org.apache.chemistry.opencmis.client.SessionParameterMap;
import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
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
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class CmisRepositoryTest extends CmisTest {

    @BeforeClass
    public static void init(){
        httpClient = HttpClientBuilder.create().build();
        jwtToken = getJwtToken();
    }

    @Test
    public void authTest(){

// TODO: 25.04.2020 use JWTMock.getJWT instead of getJWTToken after ading spring profile
//        final String token = getJwtToken();
//        final String token = JWTMock.getJWT("boot");

//        if(!StringUtils.isBlank(token)) {

            CmisBinding provider = getClientBindings("http://doccloud.ru:8888/jooq/browser", "boot", "boot", jwtToken);


            ObjectData myObject = provider.getObjectService().getObject("test", "0ff729c3-3b26-463f-8006-2fd79bdc124a",
                    "*", true, IncludeRelationships.BOTH, "cmis:none", true, true, null);

            System.out.println("my Object " + myObject);


//        }
    }










}
