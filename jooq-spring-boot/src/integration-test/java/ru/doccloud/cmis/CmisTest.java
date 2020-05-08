package ru.doccloud.cmis;

import org.apache.chemistry.opencmis.client.SessionParameterMap;
import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.junit.Before;
import ru.doccloud.common.CommonTest;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class CmisTest extends CommonTest {
    private static final String CMIS_JWT_AUTH_HEADER = "cmisJwtAuthorization";

    final  String objectIdKey = "cmis:objectId";

    final  String objectNameKey = "cmis:name";

    final String descriptionKey = "cmis:description";

    final String versionSeriesIdKey = "cmis:versionSeriesId";

    final String parentKey = "cmis:parentId";

    final String pathKey = "cmis:path";

    final String objTypeKey = "cmis:objectTypeId";

    final  String ROOT_ID = "00000000-0000-0000-0000-000000000000";

    final String cmisBaseType = "cmis:baseTypeId";

    final String cmisContentStreamId = "cmis:contentStreamId";

    final String cmisContentStreamFileName= "cmis:contentStreamFileName";

    final String cmisContentStreamFileLenght = "cmis:contentStreamLength";

    static CmisBinding provider;
    static final String REPOSITORY_NAME = "test";

    @Before
    public void setUp() throws SQLException {
        super.setUp();
         provider = getClientBindings(createURLWithPort("/jooq/browser", port), DEFAULT_USER, DEFAULT_PASS, jwtToken);
    }

    static CmisBinding getClientBindings(String url, String user, String pwd, String token) {
        return createBrowserBinding(url, user, pwd, token);
    }

    static void fillLoginParams(Map<String, String> parameters, String user, String password) {
        if (user != null && user.length() > 0) {
            parameters.put(SessionParameter.USER, user);
        }
        if (user != null && user.length() > 0) {
            parameters.put(SessionParameter.PASSWORD, password);
        }
    }

    static CmisBinding createBrowserBinding(String url, String user, String password, String token) {

        // gather parameters
        Map<String, String> parameters = new HashMap<>();
        fillLoginParams(parameters, user, password);

        // get factory and create binding
        CmisBindingFactory factory = CmisBindingFactory.newInstance();

        parameters.put(SessionParameter.BROWSER_URL, url);

        SessionParameterMap parameter = new SessionParameterMap();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            parameter.put(entry.getKey(), entry.getValue());
        }
        parameter.addHeader(CMIS_JWT_AUTH_HEADER, token);
        return factory.createCmisBrowserBinding(parameter);
    }

    // TODO: 08.05.2020 define abstract method and override it for cmis and for rest of functionality
    public void assertCriteria(ObjectData myObject, String expectedObjId, String expectedParentId,
                               String expectedName, String expectedPath, String expectedType, String expectedDesc){
        assertNotNull(myObject);
        assertEquals(expectedObjId, myObject.getId());

        assertNotNull(myObject.getProperties().getProperties().get(objectNameKey).getValues());

        assertEquals(expectedName, myObject.getProperties().getProperties().get(objectNameKey).getValues().get(0));

        // parentKey and path  are always null for document type
        if(!"cmis:document".equals(expectedType)) {
            assertNotNull(myObject.getProperties().getProperties().get(parentKey).getValues());

            assertEquals(expectedParentId, myObject.getProperties().getProperties().get(parentKey).getValues().get(0));

            assertNotNull(myObject.getProperties().getProperties().get(pathKey).getValues());

            assertEquals(expectedPath, myObject.getProperties().getProperties().get(pathKey).getValues().get(0));
        }

        assertNotNull(myObject.getProperties().getProperties().get(objTypeKey).getValues());

        assertEquals(expectedType, myObject.getProperties().getProperties().get(objTypeKey).getValues().get(0));

        assertNotNull(myObject.getProperties().getProperties().get(descriptionKey).getValues());

        assertEquals(expectedDesc, myObject.getProperties().getProperties().get(descriptionKey).getValues().get(0));
    }

     static String createURLWithPort(String uri, int port) {
        return "http://localhost:" + port + uri;
    }
}
