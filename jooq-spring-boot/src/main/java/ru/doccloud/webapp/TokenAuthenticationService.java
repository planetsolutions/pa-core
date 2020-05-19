package ru.doccloud.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.doccloud.common.jwt.JWTTokenGenerator;
import ru.doccloud.common.jwt.JwtTokenHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

// see this nice tutorial to see how to work with jwt http://www.svlada.com/jwt-token-authentication-with-spring-boot/#jwt-authentication
class TokenAuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationService.class);

    static void addAuthentication(HttpServletResponse res, String username, Collection<? extends GrantedAuthority> authorities) {

//todo check all claims decribed in spec https://tools.ietf.org/html/rfc7519#section-4.1.6

        List<String> userRoles = authorities.stream().map(Object::toString).collect(Collectors.toList());

        LOGGER.trace("addAuthentication(): userRoles {}", userRoles);


        final String accessToken = JwtTokenHelper.INSTANCE.buildAccessToken(username, userRoles);
        LOGGER.trace("addAuthentication(): accessToken {}", accessToken);

        final String refreshToken = JwtTokenHelper.INSTANCE.buildRefreshToken(username, userRoles);
        LOGGER.trace("addAuthentication(): refreshToken {}", refreshToken);
		try {
            String loginResponse = new ObjectMapper().writeValueAsString(new LoginResponse(accessToken, refreshToken));

	        LOGGER.trace("addAuthentication(): login response {}", loginResponse);
            res.addHeader(JWTTokenGenerator.INSTANCE.getStandardHeaderAuth(), JWTTokenGenerator.INSTANCE.getTokenPrefix()
                    + " " + accessToken);
	        res.setHeader("Accept", "application/json");
	        res.setHeader("Content-type", "application/json");
	
	        res.getWriter().write(loginResponse);
	        res.getWriter().flush();
	        res.getWriter().close();
         
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    static Authentication getAuthentication(HttpServletRequest request) throws IOException {
            String token = request.getHeader(JWTTokenGenerator.INSTANCE.getJwtHeaderAuth());
            if (StringUtils.isBlank(token)) {
                token = request.getHeader(JWTTokenGenerator.INSTANCE.getStandardHeaderAuth());
            }

            LOGGER.trace("getAuthentication(): jwtToken {}", token);
            if (token != null) {
                // parse the token.
                final JwtTokenHelper.UserCredentials userCredentials = JwtTokenHelper.INSTANCE.getUserFromJWT(token);

                LOGGER.trace("getAuthentication(): userCredentials {}", userCredentials);

                if (userCredentials == null) {
                    LOGGER.error("Authentication Token has been expired");
                    return null;
                }

                final List<GrantedAuthority> authorities = userCredentials.getUserRoles().stream()
                        .map(authority -> new SimpleGrantedAuthority(authority))
                        .collect(Collectors.toList());

                LOGGER.trace("getAuthentication(): user from token {}, roles from token {}", userCredentials.getUserName(), authorities);
                return userCredentials.getUserName() != null ?
                        new UsernamePasswordAuthenticationToken(userCredentials.getUserName(), null, authorities) :
                        null;
            }

        return null;
    }



    
    @SuppressWarnings("unused")
    private static class LoginResponse {
        public String access_token;
        public String refresh_token;
        public String token_type;
        public Integer expires_in;
        public String scope;

        public LoginResponse(final String accessToken, final String refreshToken) {
            LOGGER.trace("LoginResponse accessToken: {}", accessToken);
            this.access_token = accessToken;
            this.refresh_token= refreshToken;
            this.token_type="Bearer";
            this.expires_in=2000;
            this.scope="administration compliance search";
        }

        @Override
        public String toString() {
            return "LoginResponse{" +
                    "token='" + access_token + '\'' +
                    '}';
        }
    }
}