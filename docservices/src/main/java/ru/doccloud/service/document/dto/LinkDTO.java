package ru.doccloud.service.document.dto;

import java.util.UUID;

import org.jtransfo.DomainClass;

@DomainClass("ru.doccloud.document.model.Link")
public class LinkDTO {
    private  UUID head_id;

    private UUID tail_id;

    public LinkDTO() {
    }

    public LinkDTO(UUID head_id, UUID tail_id) {
        this.head_id = head_id;
        this.tail_id = tail_id;
    }

    public UUID getHead_id() {
        return head_id;
    }

    public void setHead_id(UUID head_id) {
        this.head_id = head_id;
    }

    public UUID getTail_id() {
        return tail_id;
    }

    public void setTail_id(UUID tail_id) {
        this.tail_id = tail_id;
    }

    @Override
    public String toString() {
        return "LinkDTO{" +
                "head_id=" + head_id +
                ", tail_id=" + tail_id +
                '}';
    }
}
