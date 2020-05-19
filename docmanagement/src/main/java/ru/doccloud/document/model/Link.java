package ru.doccloud.document.model;

import java.util.UUID;

public class Link {
	private final UUID head_id;
	
	private final UUID tail_id;

	public Link(UUID head_id, UUID tail_id) {
		super();
		this.head_id = head_id;
		this.tail_id = tail_id;
	}

	public UUID getTail_id() {
		return tail_id;
	}

	public UUID getHead_id() {
		return head_id;
	}
	
}
