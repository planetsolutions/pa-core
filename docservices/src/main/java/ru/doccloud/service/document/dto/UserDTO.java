package ru.doccloud.service.document.dto;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class UserDTO extends AbstractDTO {
    private String userId;
   	private String password;
    private String fullName;
    private String email;
    private JsonNode details;
    private String[] groups;
    private List<UserRoleDTO> userRoleList;
    
	public UserDTO() {

    }
    public UserDTO(String userId, String password, String fullName, String email, JsonNode details, String[] groups, List<UserRoleDTO> userRoleList) {
        this.userId = userId;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.groups = groups;
        this.details = details;
        this.userRoleList = userRoleList;
    }
    public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public void setDetails(JsonNode details) {
		this.details = details;
	}
	public void setGroups(String[] groups) {
		this.groups = groups;
	}
    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
    
    public JsonNode getDetails() {
        return details;
    }
    

    public String[] getGroups() {
		return groups;
	}
    
    public List<UserRoleDTO> getUserRoleList() {
        return userRoleList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDTO userDTO = (UserDTO) o;

        if (!userId.equals(userDTO.userId)) return false;
        return password.equals(userDTO.password);

    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }

    @Override
    public String getDto4Audit(){

        return "{\"userId\":\" " + userId + "\",\"email\":\"" + email + "\",\"fullName\":" +  "\"" + fullName + "\"}";
    }

    @Override
    public String getAuditIndexName() {
        return "users_audit";
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "userId='" + userId + '\'' +
                ", password lenght='" +  (!StringUtils.isBlank(password) ? password.length() : 0) + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", details='" + details + '\'' +
                ", groups='" + groups + '\'' +
                ", userRoleList=" + userRoleList +
                '}';
    }
}
