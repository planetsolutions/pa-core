package ru.doccloud.webapp.audit.aspect.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.doccloud.service.document.dto.AbstractDTO;

import java.io.IOException;

public enum EventType {
    ERROR("error") {
        @Override
        public void getEventAuditJson(ObjectNode objectNode, Object auditInfo) {
            if(auditInfo == null || !(auditInfo instanceof String))
                return ;

            objectNode.put("error", String.valueOf(auditInfo));
        }
    },
    EVENT("event") {
        @Override
        public void getEventAuditJson(ObjectNode objectNode, Object auditInfo) {
            if(auditInfo == null)
                return;

            AbstractDTO dto = (AbstractDTO) auditInfo;

            try {
                LOGGER.info("getEventAuditJson(): dto {}", dto.getDto4Audit());
                ObjectNode objectNode1 = new ObjectMapper().readValue(dto.getDto4Audit(), ObjectNode.class);
                objectNode.set("dto", objectNode1);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    },
    TIMING("timing") {
        @Override
        public void getEventAuditJson(ObjectNode objectNode, Object auditInfo) {
            if(auditInfo == null || !(auditInfo instanceof Long))
                return;

            objectNode.put("elapsedTime", String.valueOf(auditInfo));

        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(EventType.class);

    private String eventType;

    EventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public abstract void getEventAuditJson(ObjectNode objectNode, Object auditInfo);
}
