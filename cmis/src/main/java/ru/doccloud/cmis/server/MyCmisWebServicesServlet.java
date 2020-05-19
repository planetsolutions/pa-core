package ru.doccloud.cmis.server;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.server.impl.webservices.CmisWebServicesServlet;
import org.apache.chemistry.opencmis.server.shared.AbstractCmisHttpServlet;
import org.apache.chemistry.opencmis.server.shared.CsrfManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@Component
public class MyCmisWebServicesServlet extends AstractWebServicesServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CmisWebServicesServlet.class.getName());

    @Autowired
    private ResourceLoader resourceLoader;

	@Override
	public void init(ServletConfig config) throws ServletException {

        // get CMIS version
        String cmisVersionStr = config.getInitParameter(PARAM_CMIS_VERSION);
        if (cmisVersionStr != null) {
            try {
                cmisVersion = CmisVersion.fromValue(cmisVersionStr);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("CMIS version is invalid! Setting it to CMIS 1.0.");
                cmisVersion = CmisVersion.CMIS_1_1;
            }
        } else {
            LOGGER.warn("CMIS version is not defined! Setting it to CMIS 1.0.");
            cmisVersion = CmisVersion.CMIS_1_1;
        }

        // initialize the call context handler
        callContextHandler = AbstractCmisHttpServlet.loadCallContextHandler(config);

        // set up WSDL and XSD documents
        docs = new HashMap<String, String>();

        try {

            final Resource wsdlResource = resourceLoader.getResource("classpath:cmis11/CMISWS-Service.wsdl.template");
            final Resource coreResource = resourceLoader.getResource("classpath:cmis11/CMIS-Core.xsd.template");
            final Resource msgResource = resourceLoader.getResource("classpath:cmis11/CMIS-Messaging.xsd.template");

            LOGGER.info("init(): wsdl resource {}", wsdlResource);
            LOGGER.info("init(): core resource {}", coreResource);
            LOGGER.info("init(): msg resource {}", msgResource);


            InputStream wsdlStream = wsdlResource.getInputStream();
            InputStream coreStream = coreResource.getInputStream();
            InputStream msgStream = msgResource.getInputStream();


            docs.put("wsdl", readFile(wsdlStream));
            docs.put("core", readFile(coreStream));
            docs.put("msg", readFile(msgStream));

        } catch (IOException e) {
            LOGGER.error("init(): no wsdl templates in classpath  {}", e.getMessage());
        }

        // set up CSRF manager
        csrfManager = new CsrfManager(config);

        super.init(config);
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        // set CMIS version and CSRF Manager
        request.setAttribute(CMIS_VERSION, cmisVersion);
        request.setAttribute(CSRF_MANAGER, csrfManager);

        super.handleRequest(request, response);
    }

    @Override
    protected void loadBus(ServletConfig servletConfig) {
        super.loadBus(servletConfig);
    }

}
