package ru.doccloud.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;

import java.util.*;


public enum JwtTokenHelper {

    INSTANCE;
    private static final String ROLES_TOKEN_KEY = "roles";

    public String buildAccessToken(final String userName, final List<String> userRoles) {

        Claims accessClaims = Jwts.claims().setSubject(userName);


        accessClaims.put(ROLES_TOKEN_KEY, userRoles);

        return Jwts.builder()
                .setClaims(accessClaims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWTTokenGenerator.INSTANCE.getAccessTokenExpirationTime()))
                .signWith(SignatureAlgorithm.HS512, JWTTokenGenerator.INSTANCE.getSigningKey())
                .compact();
    }

    public String buildRefreshToken(final String userName, final List<String> userRoles){

        Claims refreshClaims = Jwts.claims().setSubject(userName);
        refreshClaims.put(ROLES_TOKEN_KEY, userRoles);

        return Jwts.builder()
                .setClaims(refreshClaims)
                .setIssuedAt(new Date())
                .setId(UUID.randomUUID().toString())
                .setExpiration(new Date(System.currentTimeMillis() + JWTTokenGenerator.INSTANCE.getRefreshTokenExpirationTime()))
                .signWith(SignatureAlgorithm.HS512, JWTTokenGenerator.INSTANCE.getSigningKey())
                .compact();
    }

    public UserCredentials getUserFromJWT(final String token) {
        Jws<Claims> jwsClaims = Jwts.parser()
                .setSigningKey(JWTTokenGenerator.INSTANCE.getSigningKey())
                .parseClaimsJws(token.replace(JWTTokenGenerator.INSTANCE.getTokenPrefix(), ""));

        if(!isTokenNotExpired(jwsClaims))
            return null;

        final String user = jwsClaims.getBody().getSubject();
        final List<String> userRoles = jwsClaims.getBody().get(ROLES_TOKEN_KEY, List.class);

        return new UserCredentials(user, userRoles);
    }

    public UserCredentials getUserFromRefreshToken(final String refreshToken) {
        Jws<Claims> jwsClaims = Jwts.parser()
                .setSigningKey(JWTTokenGenerator.INSTANCE.getSigningKey())
                .parseClaimsJws(refreshToken.replace(JWTTokenGenerator.INSTANCE.getTokenPrefix(), ""));

        if(!validateRefreshToken(jwsClaims))
            return null;

        final String user = jwsClaims.getBody().getSubject();
        final List<String> userRoles = jwsClaims.getBody().get(ROLES_TOKEN_KEY, List.class);

        return new UserCredentials(user, userRoles);
    }

    private boolean isTokenNotExpired(Jws<Claims> jwsClaims){
        final Date expireDate = jwsClaims.getBody().getExpiration();

        return expireDate.after(new Date());
    }

    private boolean validateRefreshToken(Jws<Claims> jwsClaims) {

        if (jwsClaims == null || jwsClaims.getBody().isEmpty())
            return false;

        final String iti = jwsClaims.getBody().getId();
        if (StringUtils.isBlank(iti))
            return false;
        final UUID id = UUID.fromString(iti);

        return id.toString().equals(iti) && isTokenNotExpired(jwsClaims);
    }

    /**
     * Created by ilya on 2/7/18.
     */
    public static class UserCredentials {
        private String userName;
        private List<String> userRoles;

        UserCredentials(String userName, List<String> userRoles) {
            this.userName = userName;
            this.userRoles = userRoles;
        }

        public String getUserName() {
            return userName;
        }

        public List<String> getUserRoles() {
            return userRoles;
        }

        @Override
        public String toString() {
            return "UserCredentials{" +
                    "userName='" + userName + '\'' +
                    ", userRoles=" + userRoles +
                    '}';
        }
    }
}
