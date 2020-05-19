package ru.doccloud.webapp.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "pa.persistence.realmds")
public class RealmDsSettings {
//
    private String name;

    private String driverName;
    private String connectionURL;
    private String connectionName;
    private String connectionPassword;
    private String userTable;
    private String userNameCol;
    private String userCredCol;
    private String userRoleTable;
    private String roleNameCol;
    private String allRolesMode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserTable() {
        return userTable;
    }

    public void setUserTable(String userTable) {
        this.userTable = userTable;
    }

    public String getUserNameCol() {
        return userNameCol;
    }

    public void setUserNameCol(String userNameCol) {
        this.userNameCol = userNameCol;
    }

    public String getUserCredCol() {
        return userCredCol;
    }

    public void setUserCredCol(String userCredCol) {
        this.userCredCol = userCredCol;
    }

    public String getUserRoleTable() {
        return userRoleTable;
    }

    public void setUserRoleTable(String userRoleTable) {
        this.userRoleTable = userRoleTable;
    }

    public String getRoleNameCol() {
        return roleNameCol;
    }

    public void setRoleNameCol(String roleNameCol) {
        this.roleNameCol = roleNameCol;
    }

    public String getAllRolesMode() {
        return allRolesMode;
    }

    public void setAllRolesMode(String allRolesMode) {
        this.allRolesMode = allRolesMode;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public String getConnectionPassword() {
        return connectionPassword;
    }

    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }

    @Override
    public String toString() {
        return "DatasourceSettings{" +
                "name='" + name + '\'' +
                ", driverName='" + driverName + '\'' +
                ", connectionURL='" + connectionURL + '\'' +
                ", connectionName='" + connectionName + '\'' +
                ", connectionPassword='" + connectionPassword + '\'' +
                ", userTable='" + userTable + '\'' +
                ", userNameCol='" + userNameCol + '\'' +
                ", userCredCol='" + userCredCol + '\'' +
                ", userRoleTable='" + userRoleTable + '\'' +
                ", roleNameCol='" + roleNameCol + '\'' +
                ", allRolesMode='" + allRolesMode + '\'' +
                '}';
    }
}
