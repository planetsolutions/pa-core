package ru.doccloud.document.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * @author Andrey Kadnikov
 */
public class Document extends AbstractDocument implements Serializable {

	private static final long serialVersionUID = 1L;

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

	protected transient JsonNode acl;
	protected JsonNode links;

	private Document(Builder builder) {
        this.id = builder.id;

        LocalDateTime creationTime = null;
        if (builder.creationTime != null) {
            creationTime = new LocalDateTime(builder.creationTime);
        }
        this.creationTime = creationTime;

        this.description = builder.description;

        LocalDateTime modificationTime = null;
        if (builder.modificationTime != null) {
            modificationTime = new LocalDateTime(builder.modificationTime);
        }
        this.modificationTime = modificationTime;

        this.title = builder.title;
        
        this.filePath = builder.filePath;
        
        this.fileMimeType = builder.fileMimeType;
        
        this.fileLength = builder.fileLength;

        this.fileName = builder.fileName;
        this.fileStorage = builder.fileStorage;
        
        this.author = builder.author;
        
        this.modifier = builder.modifier;
        
        this.type = builder.type;
        this.baseType = builder.baseType;
                
        this.parent = builder.parent;
        this.readers = builder.readers;
        
        this.data = builder.data;
        this.docVersion = builder.docVersion;
        this.uuid = builder.uuid;
        this.acl = builder.acl;
        this.sourceId = builder.sourceId;
        this.sourcePackage = builder.sourcePackage;
        
        this.retentionPolicy = builder.retentionPolicy;
        
        this.versionParent = builder.versionParent;
        this.versionSeries = builder.versionSeries;
        this.lastVersion = builder.lastVersion;
        this.versionComment = builder.versionComment;
    }

    public String getFileStorage() {
        return fileStorage;
    }
    
    public String getBaseType() {
        return baseType;
    }

    public JsonNode getAcl() {
        return acl;
    }
    
    public JsonNode getlinks() {
        return links;
    }
    
	public String getSourceParent() {
		return sourceParent;
	}
	
    public void setAcl(JsonNode acl) {
		this.acl = acl;
	}
    
	public UUID getVersionSeries() {
		return versionSeries;
	}

	public UUID getVersionParent() {
		return versionParent;
	}


	public Boolean isLastVersion() {
		return lastVersion;
	}

	public void setLastVersion(Boolean lastVersion) {
		this.lastVersion = lastVersion;
	}

	public void setVersionSeries(UUID versionSeries) {
		this.versionSeries = versionSeries;
	}

	public void setVersionParent(UUID versionParent) {
		this.versionParent = versionParent;
	}


	public String getVersionComment() {
		return versionComment;
	}

	public void setVersionComment(String versionComment) {
		this.versionComment = versionComment;
	}


	public static Builder getBuilder(String title) {
        return new Builder(title);
    }


	public String getSourceId() {
		return sourceId;
	}
	
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	
	public String getRetentionPolicy() {
		return retentionPolicy;
	}
	
	public void setRetentionPolicy(String retentionPolicy) {
		this.retentionPolicy = retentionPolicy;
	}
	
	public String getSourcePackage() {
		return sourcePackage;
	}

	public void setSourcePackage(String sourcePackage) {
		this.sourcePackage = sourcePackage;
	}
	
    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", creationTime=" + creationTime +
                ", author='" + author + '\'' +
                ", modificationTime=" + modificationTime +
                ", modifier='" + modifier + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileMimeType='" + fileMimeType + '\'' +
                ", fileLength=" + fileLength +
                ", type='" + type + '\'' +
                ", baseType='" + baseType + '\'' +
                ", docVersion='" + docVersion + '\'' +
                ", data=" + data +
                ", uuid=" + uuid +
                ", readers=" + readers +
                ", acl=" + acl +
                '}';
    }



	public static class Builder {

		private UUID id;

        private Timestamp creationTime;

        private String description;

        private Timestamp modificationTime;

		private String title;
		
		private String filePath;
		
		private Long fileLength;

		private String fileName;

		private String fileMimeType;
        
        private String type;
        
        private String baseType;
        
        private UUID parent;
        
        private String[] readers;
        
        private String author;
        
        private String modifier;
        
        private String sourceId;
        
        private String sourcePackage;
        
        private JsonNode data;

        private String docVersion;

        private UUID uuid;

        private String fileStorage;
        
        private JsonNode acl;
        
        private String retentionPolicy;
        
    	private UUID versionSeries;
    	
    	private UUID versionParent;
    	
    	private Boolean lastVersion;

    	private String versionComment;


        public Builder(String title) {
            this.title = title;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        
        public Builder type(String type) {
            this.type = type;
            return this;
        }
        
        public Builder baseType(String baseType) {
            this.baseType = baseType;
            return this;
        }
        
        public Builder parent(UUID parent) {
            this.parent = parent;
			return this;
        }
        
        public Builder readers(String[] readers) {
            this.readers = readers;
			return this;
        }
        
        public Builder modifier(String modifiedBy) {
            this.modifier = modifiedBy;
            return this;
        }
        
        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder fileName(String fileName){
            this.fileName = fileName;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }
        
        public Builder fileMimeType(String fileMimeType) {
            this.fileMimeType = fileMimeType;
            return this;
        }
        
        public Builder fileLength(Long fileLength) {
            this.fileLength = fileLength;
            return this;
        }

        public Builder fileStorage(String fileStorage) {
            this.fileStorage = fileStorage;
            return this;
        }


        public Builder creationTime(Timestamp creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }
        
        public Builder data(JsonNode data) {
            this.data = data;
            return this;
        }

        public Builder modificationTime(Timestamp modificationTime) {
            this.modificationTime = modificationTime;
            return this;
        }
        
        public Builder docVersion(String docVersion){
            this.docVersion = docVersion;
            return this;
        }

        public Builder uuid(UUID uuid){
            this.uuid = uuid;
            return this;
        }

        public Builder acl(JsonNode acl) {
            this.acl = acl;
            return this;
        }
        
        public Builder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }
        
        public Builder sourcePackage(String sourcePackage) {
            this.sourcePackage = sourcePackage;
            return this;
        }
        
        public Builder versionSeries(UUID versionSeries) {
            this.versionSeries = versionSeries;
            return this;
        }
        public Builder versionParent(UUID versionParent) {
            this.versionParent = versionParent;
            return this;
        }
        public Builder versionComment(String versionComment) {
            this.versionComment = versionComment;
            return this;
        }
        public Builder lastVersion(Boolean lastVersion) {
            this.lastVersion = lastVersion;
            return this;
        }
        
        public Builder retentionPolicy(String retentionPolicy) {
            this.retentionPolicy = retentionPolicy;
            return this;
        } 
        
        public Document build() {
            Document created = new Document(this);

            String title = created.getTitle();

            if (title == null || title.length() == 0) {
                throw new IllegalStateException("title cannot be null or empty");
            }

            return created;
        }

    }


}
