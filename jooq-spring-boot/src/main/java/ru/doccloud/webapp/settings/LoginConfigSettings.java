package ru.doccloud.webapp.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "security.loginconfig")
public class LoginConfigSettings {
//
    private String authMethod;
    private String securityRole;
    private String authRole;
    private String pattern;

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public String getSecurityRole() {
        return securityRole;
    }

    public void setSecurityRole(String securityRole) {
        this.securityRole = securityRole;
    }

    public String getAuthRole() {
        return authRole;
    }

    public void setAuthRole(String authRole) {
        this.authRole = authRole;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        return "LoginConfigSettings{" +
                "authMethod='" + authMethod + '\'' +
                ", securityRole='" + securityRole + '\'' +
                ", authRole='" + authRole + '\'' +
                ", pattern='" + pattern + '\'' +
                '}';
    }
}
