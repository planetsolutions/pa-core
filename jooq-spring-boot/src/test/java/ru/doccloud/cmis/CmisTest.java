package ru.doccloud.cmis;

import org.apache.chemistry.opencmis.client.SessionParameterMap;
import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.junit.BeforeClass;
import ru.doccloud.common.CommonTest;

import java.util.HashMap;
import java.util.Map;

public abstract class CmisTest extends CommonTest {
    private static final String CMIS_JWT_AUTH_HEADER = "cmisJwtAuthorization";

    static CmisBinding provider;
    static final String REPOSITORY_NAME = "test";

    @BeforeClass
    public static void initBinding(){
         provider = getClientBindings("http://doccloud.ru:8888/jooq/browser", DEFAULT_USER, DEFAULT_PASS, jwtToken);
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
}
