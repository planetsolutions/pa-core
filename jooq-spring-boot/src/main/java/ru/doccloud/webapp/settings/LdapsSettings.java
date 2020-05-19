package ru.doccloud.webapp.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
@ConfigurationProperties(prefix = "security.openldap")
public class LdapsSettings {

    private List<Ldap> ldaps;

    public List<Ldap> getLdaps() {
        return ldaps;
    }

    public void setLdaps(List<Ldap> ldaps) {
        this.ldaps = ldaps;
    }

    public static class Ldap {
        //
        private String userSearchBase;
        private String userSearchFilter;
        private String groupSearchBase;
        private String url;
        private String managerDn;
        private String managerPassword;
        private String passwordAttribute;

        public String getUserSearchBase() {
            return userSearchBase;
        }

        public void setUserSearchBase(String userSearchBase) {
            this.userSearchBase = userSearchBase;
        }

        public String getUserSearchFilter() {
            return userSearchFilter;
        }

        public void setUserSearchFilter(String userSearchFilter) {
            this.userSearchFilter = userSearchFilter;
        }

        public String getGroupSearchBase() {
            return groupSearchBase;
        }

        public void setGroupSearchBase(String groupSearchBase) {
            this.groupSearchBase = groupSearchBase;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getManagerDn() {
            return managerDn;
        }

        public void setManagerDn(String managerDn) {
            this.managerDn = managerDn;
        }

        public String getManagerPassword() {
            return managerPassword;
        }

        public void setManagerPassword(String managerPassword) {
            this.managerPassword = managerPassword;
        }

        public String getPasswordAttribute() {
            return passwordAttribute;
        }

        public void setPasswordAttribute(String passwordAttribute) {
            this.passwordAttribute = passwordAttribute;
        }

        @Override
        public String toString() {
            return "LdapSettings{" +
                    "userSearchBase='" + userSearchBase + '\'' +
                    ", userSearchFilter='" + userSearchFilter + '\'' +
                    ", groupSearchBase='" + groupSearchBase + '\'' +
                    ", url='" + url + '\'' +
                    ", managerDn='" + managerDn + '\'' +
                    ", managerPassword='" + managerPassword + '\'' +
                    ", passwordAttribute='" + passwordAttribute + '\'' +
                    '}';
        }
    }
}
