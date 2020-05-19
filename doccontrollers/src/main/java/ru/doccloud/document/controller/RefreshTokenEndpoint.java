package ru.doccloud.document.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.doccloud.common.jwt.JWTTokenGenerator;
import ru.doccloud.common.jwt.JwtTokenHelper;
import ru.doccloud.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * RefreshTokenEndpoint
 * 
 *
 * Aug 17, 2016
 */
@RestController
@RequestMapping("/api/token")
public class RefreshTokenEndpoint {
    @Autowired
    private UserService userService;

    /**
     * returns new accessJwtToken by refresh token
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws ServletException
     */
    @RequestMapping(value="/refresh", method= RequestMethod.POST, produces={ MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody
    String refreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception {

        final String refreshToken = request.getHeader(JWTTokenGenerator.INSTANCE.getJwtRefreshHeader());

        final JwtTokenHelper.UserCredentials userCredentials = JwtTokenHelper.INSTANCE.getUserFromRefreshToken(refreshToken);

        if(userCredentials == null)
            throw new Exception("refresh token is not valid. Please re-login to get new token");


        return JwtTokenHelper.INSTANCE.buildAccessToken(userCredentials.getUserName(), userCredentials.getUserRoles());

    }
}
