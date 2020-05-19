package ru.doccloud.cmis.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MyCmisAtomPubServlet extends AstractAtomPubServlet {

    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }
    
	 @Override
	    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	            IOException {
			super.service(request, response);
	 
	 }
}
