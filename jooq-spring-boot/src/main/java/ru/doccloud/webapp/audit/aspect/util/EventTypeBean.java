package ru.doccloud.webapp.audit.aspect.util;

import java.util.Objects;

/**
 * Created by ilya on 3/13/18.
 */
public class EventTypeBean {
    private EventType eventType;
    private boolean isEnabled;

    EventTypeBean(EventType eventType, boolean isEnabled) {
        this.eventType = eventType;
        this.isEnabled = isEnabled;
    }

    public EventType getEventType() {
        return eventType;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventTypeBean)) return false;
        EventTypeBean that = (EventTypeBean) o;
        return isEnabled == that.isEnabled &&
                eventType == that.eventType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType);
    }

    @Override
    public String toString() {
        return "EventTypeBean{" +
                "eventType=" + eventType +
                ", isEnabled=" + isEnabled +
                '}';
    }
}
