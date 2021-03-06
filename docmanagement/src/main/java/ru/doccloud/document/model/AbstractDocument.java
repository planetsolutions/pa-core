package ru.doccloud.document.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;

import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by ilya on 5/27/17.
 */
public abstract class AbstractDocument  implements Serializable{

	private static final long serialVersionUID = 1L;

	protected  UUID id;

    protected  LocalDateTime creationTime;

    protected  String author;

    protected  LocalDateTime modificationTime;

    protected  String modifier;

    protected  String title;

    protected  String description;

    protected  String filePath;

    protected  String fileName;

    protected  String fileMimeType;

    protected  Long fileLength;

    protected  String type;
    
    protected  UUID parent;
    
    protected  String[] readers;

    protected  String docVersion;

    protected  transient JsonNode data;

    protected  UUID uuid;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if(this.data == null){
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            new ObjectMapper().configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false).writeValue((OutputStream) out, this.data);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if(in.readBoolean()){
            this.data = new ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false).readValue((InputStream) in, JsonNode.class);
        }     
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getModificationTime() {
        return modificationTime;
    }

    public String getModifier() {
        return modifier;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileMimeType() {
        return fileMimeType;
    }

    public Long getFileLength() {
        return fileLength;
    }

    public String getType() {
        return type;
    }

    public UUID getParent() {
		return parent;
	}

    public String[] getReaders() {
		return readers;
	}
    
	public void setParent(UUID parent) {
		this.parent = parent;
	}

	public String getDocVersion() {
        return docVersion;
    }

    public JsonNode getData() {
        return data;
    }

    public UUID getUuid() {
        return uuid;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractDocument)) return false;

        AbstractDocument that = (AbstractDocument) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return uuid != null ? uuid.equals(that.uuid) : that.uuid == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AbstractDocument{" +
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
                ", docVersion='" + docVersion + '\'' +
                ", data=" + data +
                ", uuid=" + uuid +
                '}';
    }
}
