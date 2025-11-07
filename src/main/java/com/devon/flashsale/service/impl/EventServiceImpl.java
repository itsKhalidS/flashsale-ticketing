package com.devon.flashsale.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.devon.flashsale.entity.Event;
import com.devon.flashsale.enums.EventStatus;
import com.devon.flashsale.exceptions.ResourceNotFoundException;
import com.devon.flashsale.repository.EventRepository;
import com.devon.flashsale.service.EventService;

import jakarta.transaction.Transactional;

@Service
public class EventServiceImpl implements EventService {
	
	private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);
	
	private final EventRepository eventRepository;
	
	public EventServiceImpl(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@Override
	public Event createEvent(Event event) {
		Event savedEvent = eventRepository.save(event);
		log.info("Event with Event Id: {} created", savedEvent.getEventId());
		return savedEvent;
	}

	@Override
	public List<Event> getAllEvents() {
		return eventRepository.findAll();
	}

	@Override
	public Event getEventById(Long eventId) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new ResourceNotFoundException("No Event Found for id ["+eventId+"]") );
		log.info("Event with Event Id: {} found", event.getEventId());
		return event;
	}
	
	@Override
	@Transactional
	public int updateEventStatus(Long eventId, EventStatus status) {
		int rowEffected = eventRepository.updateEventStatus(eventId, status);
		if(rowEffected == 1)
			log.info("Status updated to [{}] for Event with Event Id: {} found", status, eventId);
		return rowEffected;
	}

}
