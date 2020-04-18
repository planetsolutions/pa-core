package ru.doccloud.webapp.audit.aspect.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;


public class AuditHelper {

    private static final Set<String> technicalParamSet;

    private static final String PAGEABLE_PARAM = "pageable";
    private static final String JSON_FIELDS = "fields";
    private static final String HTTP_REQUEST = "request";


    static {
        technicalParamSet = new HashSet<>();
        technicalParamSet.add(PAGEABLE_PARAM);
        technicalParamSet.add(JSON_FIELDS);
        technicalParamSet.add(HTTP_REQUEST);
    }

    public static Set<ParamBean> getParamValues(String[] paramNames, Object[] paramValues){
        if(paramNames == null)
            return null;

        Set<ParamBean> paramBeen = new HashSet<>();

        for(int i = 0; i < paramNames.length; i++) {
            final String paramName = paramNames[i];
            if(technicalParamSet.contains(paramName))
                continue;
            paramBeen.add(new ParamBean(paramName, paramValues[i]));
        }

        return paramBeen;
    }

    public static Set<EventTypeBean> getAuditPossibleEvents(JsonNode jsonNode, AuditEvents auditEvent)  {
        final JsonNode eventsJson = jsonNode.get(auditEvent.getAuditEvent());

        return getEventTypesFromJson(eventsJson);
    }


    public static Map<String, Set<AuditEventBean>> getAvailableAuditEvents(final String docType, final JsonNode auditEventsJson)  {
        Map<String, Set<AuditEventBean>> auditEventMap = new HashMap<>();


        Set<AuditEventBean> auditEventBeanSet = new HashSet<>();


        for(AuditEvents auditEvent : AuditEvents.values()) {

            final JsonNode eventsJson = auditEventsJson.get(auditEvent.getAuditEvent());
            final Set<EventTypeBean> eventTypeBeanSet = getEventTypesFromJson(eventsJson);

            auditEventBeanSet.add(new AuditEventBean(auditEvent, eventTypeBeanSet));
        }
        auditEventMap.put(docType, auditEventBeanSet);

        return auditEventMap;

    }

    public static String buildJsonResponse(AuditEvents read, Set<ParamBean> paramBeen,
                                           String login, String error, Date eventDate){

        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode root = factory.objectNode();

        ObjectNode list = factory.objectNode();
        list.put("event", read.getAuditEvent());

        //params json
        if(paramBeen != null && paramBeen.size() > 0) {
            ObjectNode paramList = factory.objectNode();
            for(ParamBean paramBean : paramBeen) {
                paramList.put(paramBean.getParamName(), String.valueOf(paramBean.getParamValue()));
            }
            list.set("params", paramList);
        }

        if(error != null) {
            list.put("error", error);
        }

        //user
        if(login != null) {
            list.put("login", login);
        }

        if(eventDate != null) {
            list.put("date", String.valueOf(eventDate));
        }

        root.set("audit", list);

        return root.toString();
    }



    public static String buildJsonResponse(AuditEvents read, Set<ParamBean> paramBeen,
                                           Set<EventTypeBean> eventTypeBeanSet,
                                           Object dto, String login,
                                           String error, Long timInMillisecond, Date eventDate){

        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode root = factory.objectNode();

        ObjectNode list = factory.objectNode();
        list.put("event", read.getAuditEvent());

        //params json
        if(paramBeen != null && paramBeen.size() > 0) {
            ObjectNode paramList = factory.objectNode();
            for(ParamBean paramBean : paramBeen) {
                paramList.put(paramBean.getParamName(), String.valueOf(paramBean.getParamValue()));
            }
            list.set("params", paramList);
        }

        if(error != null) {
            list.put("error", error);
        }

        //user
        if(login != null) {
            list.put("login", login);
        }

        if(eventDate != null) {
            list.put("date", String.valueOf(eventDate));
        }


        // eventTypes
        if(eventTypeBeanSet != null) {
            for (EventTypeBean eventTypeBean : eventTypeBeanSet) {
                if (!eventTypeBean.isEnabled()) {
                    continue;
                }
                final Object auditObj = getAuditInfoByEventType(eventTypeBean.getEventType(), error, dto, timInMillisecond);

                eventTypeBean.getEventType().getEventAuditJson(list, auditObj);
            }
        }
        root.set("audit", list);

        return root.toString();
    }


    private  static Object getAuditInfoByEventType(EventType eventType, String errorMsg, Object dto, Long timeInMillis) {
        if(eventType.equals(EventType.ERROR))
            return errorMsg;
        else if(eventType.equals(EventType.EVENT))
            return dto;
        else if(eventType.equals(EventType.TIMING))
            return timeInMillis;

        return null;
    }

    private static Set<EventTypeBean> getEventTypesFromJson(final JsonNode eventNode)  {
        Set<EventTypeBean> eventTypeBeanSet = new HashSet<>();

        for(EventType eventType : EventType.values()) {
            String jsonValue = getValueJsonNode(eventNode, eventType.getEventType());
            if(jsonValue != null) {
                eventTypeBeanSet.add(new EventTypeBean(eventType, Boolean.parseBoolean(jsonValue)));
            }
        }

        return eventTypeBeanSet;
    }

    public static Set<EventTypeBean> getFullEventTypeBeanSet() {
        Set<EventTypeBean> eventTypeBeanSet = new HashSet<>();

        for(EventType eventType : EventType.values()) {
            eventTypeBeanSet.add(new EventTypeBean(eventType, true));
        }

        return eventTypeBeanSet;
    }


    private static String getValueJsonNode(final JsonNode settingsNode, final String paramName)  {
//        if(paramName == null)
//            throw new Exception("paramName is empty");

        JsonNode value = settingsNode.findValue(paramName);
//        if(value == null)
//            throw new Exception("value for key " + paramName + "was not found in json settings");

        return value != null ? String.valueOf(value.asText()) : null;
    }
}
