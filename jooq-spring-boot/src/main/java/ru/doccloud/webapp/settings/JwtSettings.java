package ru.doccloud.webapp.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.doccloud.common.jwt.JWTTokenGenerator;


@Configuration
@ConfigurationProperties(prefix = "security.jwt")
public class JwtSettings {
//
    public long getAccessTokenExpirationTime() {
        return JWTTokenGenerator.INSTANCE.getAccessTokenExpirationTime();
    }

    public void setAccessTokenExpirationTime(long accessTokenExpirationTime) {
        JWTTokenGenerator.INSTANCE.setAccessTokenExpirationTime(accessTokenExpirationTime);
    }

    public long getRefreshTokenExpirationTime() {
        return  JWTTokenGenerator.INSTANCE.getRefreshTokenExpirationTime();
    }

    public void setRefreshTokenExpirationTime(long refreshTokenExpirationTime) {
        JWTTokenGenerator.INSTANCE.setRefreshTokenExpirationTime(refreshTokenExpirationTime);
    }

    public String getSigningKey() {
        return JWTTokenGenerator.INSTANCE.getSigningKey();
    }

    public void setSigningKey(String signingKey){
        JWTTokenGenerator.INSTANCE.setSigningKey(signingKey);
    }

}
