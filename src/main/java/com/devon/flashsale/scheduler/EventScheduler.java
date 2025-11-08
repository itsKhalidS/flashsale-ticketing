package com.devon.flashsale.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.devon.flashsale.entity.Event;
import com.devon.flashsale.enums.EventStatus;
import com.devon.flashsale.service.EventService;

/**
 * Scheduler class : Runs in background and updates Event Status of ACTIVE and CLOSED events
 */
@Component
public class EventScheduler {

	private static final Logger log = LoggerFactory.getLogger(EventScheduler.class);
			
	private final EventService eventService;
	
	public EventScheduler(EventService eventService) {
		this.eventService = eventService;
	}
	
	@Scheduled(fixedDelay = 60000)
	public void run() {
		log.info("Executing scheduled Event Status updation");
		List<Event> eventList = eventService.getAllEvents();
		for(Event e: eventList) {
			if (e.getStatus() == EventStatus.ACTIVE) {
				LocalDateTime currentDateTime = LocalDateTime.now();
				if(currentDateTime.isBefore(e.getStartTime()) || currentDateTime.isAfter(e.getEndTime())) {
					eventService.updateEventStatus(e.getEventId(), EventStatus.CLOSED);
				}
			}
			else if(e.getStatus() == EventStatus.CLOSED) {
				LocalDateTime currentDateTime = LocalDateTime.now();
				if((currentDateTime.isEqual(e.getStartTime()) || currentDateTime.isAfter(e.getStartTime())) && 
						(currentDateTime.isBefore(e.getEndTime()) || currentDateTime.isEqual(e.getStartTime()))) {
					eventService.updateEventStatus(e.getEventId(), EventStatus.ACTIVE);
				}
			}
		}
		log.info("Ending scheduled Event Status updation");
	}
	
}
