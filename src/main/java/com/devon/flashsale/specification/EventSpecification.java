package com.devon.flashsale.specification;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.devon.flashsale.entity.Event;
import com.devon.flashsale.enums.EventStatus;

public final class EventSpecification {

    public static Specification<Event> hasStatuses(List<EventStatus> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

}