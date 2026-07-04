package com.devon.flashsale.specification;

import org.springframework.data.jpa.domain.Specification;

import com.devon.flashsale.entity.Event;
import com.devon.flashsale.enums.EventStatus;

public final class EventSpecification {

    public static Specification<Event> isOngoingOrUpcomingEvent() {
        return (root, query, cb) -> root.get("status").in(EventStatus.ACTIVE, EventStatus.INACTIVE);
    }
    
    public static Specification<Event> nameContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like( cb.lower(root.get("eventName")), "%" + keyword.toLowerCase() + "%");
    }

}