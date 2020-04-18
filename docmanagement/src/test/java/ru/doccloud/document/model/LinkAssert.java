package ru.doccloud.document.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.assertj.core.api.AbstractAssert;

public class LinkAssert extends AbstractAssert<LinkAssert, Link> {

    private LinkAssert(Link actual) {
        super(actual, LinkAssert.class);
    }

    public static LinkAssert assertThatLink(Link actual) {
        return new LinkAssert(actual);
    }

    public LinkAssert hasHeadId(UUID headId) {
        isNotNull();

        assertThat(actual.getHead_id())
                .overridingErrorMessage("Expected headId to be <%s> but was <%s>", headId, actual.getHead_id())
                .isEqualTo(headId);

        return this;
    }

    public LinkAssert hasTailId(UUID tailId) {
        isNotNull();

        assertThat(actual.getTail_id())
                .overridingErrorMessage("Expected tailId to be <%s> but was <%s>", tailId, actual.getTail_id())
                .isEqualTo(tailId);

        return this;
    }
}
