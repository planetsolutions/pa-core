package ru.doccloud.webapp.audit.aspect.util;

/**
 * Created by ilya on 3/13/18.
 */
public enum AuditEvents {
    READ("read"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete");

    private String auditEvent;

    AuditEvents(String auditEvent) {
        this.auditEvent = auditEvent;
    }

    public String getAuditEvent() {
        return auditEvent;
    }
}
