package ru.doccloud.common;

import ru.doccloud.common.jwt.JwtTokenHelper;

import java.util.ArrayList;
import java.util.List;

public class JWTMock {

    private static final List<String> userRoles;


    // TODO: 6/5/19 rewrite it using @BeforeClass annotation
    static {
        userRoles = new ArrayList<>();
        userRoles.add("admin");
        userRoles.add("readwrite");
        userRoles.add("tomcat");
    }

//     UserCredentials{userName='boot', userRoles=[admin, readwrite, tomcat]}
// boot, roles from token [admin, readwrite, tomcat]

    public static String getJWT(String userName){
        final String accessToken = JwtTokenHelper.INSTANCE.buildAccessToken(userName, userRoles);

        return accessToken;
    }

//    public static void main(String[] args) {
//        System.out.println(ru.doccloud.common.JWTMock.getJWT("boot"));
//    }
}
