package ru.doccloud.webapp.audit.aspect.util;

import java.util.Set;

/**
 * Created by ilya on 3/13/18.
 */
public class AuditEventBean {

    private AuditEvents auditEvent;

    private Set<EventTypeBean> eventTypeBeanSet;

    public AuditEventBean(AuditEvents auditEvent, Set<EventTypeBean> eventTypeBeanSet) {
        this.auditEvent = auditEvent;
        this.eventTypeBeanSet = eventTypeBeanSet;
    }

    public AuditEvents getAuditEvent() {
        return auditEvent;
    }

    public Set<EventTypeBean> getEventTypeBeanSet() {
        return eventTypeBeanSet;
    }
}
