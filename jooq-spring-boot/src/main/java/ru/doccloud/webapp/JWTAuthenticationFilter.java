package ru.doccloud.webapp;

import java.io.IOException;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

class JWTAuthenticationFilter extends GenericFilterBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTAuthenticationFilter.class);
    private Set<String> exceptUrls;
    
    JWTAuthenticationFilter(Set<String> exceptUrl){
    	this.exceptUrls=exceptUrl;
    }
    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain filterChain)
            throws IOException, ServletException {
    	boolean excludedRequestFound = false;
		if (this.exceptUrls != null && this.exceptUrls.size() > 0) {
    	    for (String pattern : this.exceptUrls) {
    	    	AntPathRequestMatcher matcher = new AntPathRequestMatcher(pattern);
    	        if (matcher.matches((HttpServletRequest) request)){
    	            excludedRequestFound = true;
    	            break;
    	        }
    	    }
    	}
    	
    	if (!excludedRequestFound) {
        try {
            Authentication authentication = TokenAuthenticationService
                    .getAuthentication((HttpServletRequest) request);

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
            
        }
        catch (Exception e) {
            LOGGER.error("JWT token already expired {}", e);
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token already expired");
        }
    	}
        filterChain.doFilter(request, response);
    }
}
