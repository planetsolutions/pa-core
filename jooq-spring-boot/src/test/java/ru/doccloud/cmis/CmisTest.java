package ru.doccloud.cmis;

import org.apache.chemistry.opencmis.client.SessionParameterMap;
import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import ru.doccloud.common.CommonTest;

import java.util.HashMap;
import java.util.Map;

public abstract class CmisTest extends CommonTest {
    CmisBinding getClientBindings(String url, String user, String pwd, String token) {
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
}
