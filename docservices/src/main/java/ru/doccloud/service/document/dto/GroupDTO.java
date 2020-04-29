package ru.doccloud.service.document.dto;


public class GroupDTO extends AbstractDTO {
	

	private String title;
    private String id;
    

	public GroupDTO() {

	}
    public GroupDTO(String title, String id) {
        this.title = title;
        this.id = id;
    }

	public String getTitle() {
		return title;
	}

	public String getId() {
		return id;
	}
    
    public void setTitle(String title) {
		this.title = title;
	}
	public void setId(String id) {
		this.id = id;
	}


    @Override
    public String getDto4Audit(){

        return "{\"groupId\":\" " + id + "\",\"title\":\"" +  "\"" + title + "\"}";
    }

    @Override
    public String getAuditIndexName() {
        return "group_audit";
    }

    @Override
    public String toString() {
        return "GroupDTO{" +
                "title='" + title + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
