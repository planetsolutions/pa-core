package ru.doccloud.service.document.dto;

import java.util.UUID;

import org.jtransfo.DomainClass;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Andrey Kadnikov
 */
@DomainClass("ru.doccloud.document.model.Document")
public class DocumentDTO extends AbstractDocumentDTO{

    private String fileStorage;
    private String baseType;
    private String sourceId;
	private String sourceParent;
	private String sourcePackage;
	private String retentionPolicy;
	
	private UUID versionSeries;
	private UUID versionParent;
	private Boolean lastVersion;
    private String versionComment;

	protected JsonNode acl;
	protected JsonNode links;

	public String getRetentionPolicy() {
		return retentionPolicy;
	}

	public void setRetentionPolicy(String retentionPolicy) {
		this.retentionPolicy = retentionPolicy;
	}
	
	public UUID getVersionSeries() {
		return versionSeries;
	}

	public void setVersionSeries(UUID versionSeries) {
		this.versionSeries = versionSeries;
	}

	public UUID getVersionParent() {
		return versionParent;
	}

	public void setVersionParent(UUID versionParent) {
		this.versionParent = versionParent;
	}

	public Boolean isLastVersion() {
		return lastVersion;
	}

	public void setLastVersion(Boolean lastVersion) {
		this.lastVersion = lastVersion;
	}

	public String getVersionComment() {
		return versionComment;
	}

	public void setVersionComment(String versionComment) {
		this.versionComment = versionComment;
	}

	public Boolean getLastVersion() {
		return lastVersion;
	}

	public JsonNode getLinks() {
		return links;
	}

	public void setLinks(JsonNode links) {
		this.links = links;
	}

	public JsonNode getAcl() {
		return acl;
	}

	public void setAcl(JsonNode acl) {
		this.acl = acl;
	}

	public DocumentDTO() {
        super();
    }

    public DocumentDTO(String title, String type, String author) {
        super(title, type, author);
    }

    public String getFileStorage() {
        return fileStorage;
    }

    public void setFileStorage(String fileStorage) {
        this.fileStorage = fileStorage;
    }

    public String getBaseType() {
		return baseType;
	}

	public void setBaseType(String baseType) {
		this.baseType = baseType;
	}
	
    public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	
	public String getSourceParent() {
		return sourceParent;
	}

	public void setSourceParent(String sourceParent) {
		this.sourceParent = sourceParent;
	}

	@Override
	public String getDto4Audit(){

		return   "{\"id\":\"" + id + "\",\"type\":\"" + type + "\",\"title\":\"" + title + "\",\"source_id\":\"" + sourceId + "\",\"filepath\":" +  "\"" +  filePath + "\"}";
	}

	@Override
	public String getAuditIndexName() {
		return "document_audit";
	}

	public String getSourcePackage() {
		return sourcePackage;
	}

	public void setSourcePackage(String sourcePackage) {
		this.sourcePackage = sourcePackage;
	}

	@Override
    public String toString() {
        return "DocumentDTO{" +
                "fileStorage='" + fileStorage + "'" +
                "links='" + links + "'" +
        		super.toString() +
                '}';
    }
}
