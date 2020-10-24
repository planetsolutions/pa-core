package ru.doccloud.common.jwt;

public enum JWTTokenGenerator {
    INSTANCE;

//    private static final Logger LOGGER = LoggerFactory.getLogger(JWTTokenGenerator.class);

//    private String secretKey;

    private final String tokenPrefix = "Bearer";
    private final String jwtHeaderAuth = "cmisJwtAuthorization";
    private final String standardHeaderAuth = "Authorization";
    private final String jwtCookieName = "PA_JWT_TOKEN";
    private final String jwtRequestParam = "jwtToken";

    private long accessTokenExpirationTime; // 1 day
    private long refreshTokenExpirationTime; // 10 days
    private String signingKey;
    private boolean useJwtCookie = true;
    private boolean useRequestParam = false;

    private final String jwtRefreshHeader = "refresh_token";

//    public void generateRandomSecretKey(){
//        secretKey = UUID.randomUUID().toString();
//    }
//
//    synchronized String getSecretKey() {
//        return secretKey;
//    }

    public long getAccessTokenExpirationTime() {
        return accessTokenExpirationTime;
    }

    public long getRefreshTokenExpirationTime() {
        return refreshTokenExpirationTime;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public String getJwtHeaderAuth() {
        return jwtHeaderAuth;
    }

    public String getJwtRefreshHeader() {
        return jwtRefreshHeader;
    }

    public String getStandardHeaderAuth() {
        return standardHeaderAuth;
    }

    public void setAccessTokenExpirationTime(long accessTokenExpirationTime) {
        this.accessTokenExpirationTime = accessTokenExpirationTime;
    }

    public void setRefreshTokenExpirationTime(long refreshTokenExpirationTime) {
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    public String getSigningKey() {
        return signingKey;
    }

    public String getJwtCookieName() {
        return jwtCookieName;
    }

    public void setSigningKey(String signingKey) {
        this.signingKey = signingKey;
    }

    public void setUseJwtCookie(boolean useJwtCookie) {
        this.useJwtCookie = useJwtCookie;
    }

    public boolean isUseJwtCookie() {
        return useJwtCookie;
    }

    public String getJwtRequestParam() {
        return jwtRequestParam;
    }

    public boolean isUseRequestParam() {
        return useRequestParam;
    }

    public void setUseRequestParam(boolean useRequestParam) {
        this.useRequestParam = useRequestParam;
    }
}
