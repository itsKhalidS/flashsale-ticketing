package com.devon.flashsale.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.devon.flashsale.entity.Event;
import com.devon.flashsale.exceptions.ResourceNotFoundException;
import com.devon.flashsale.repository.EventRepository;
import com.devon.flashsale.service.EventService;

@Service
public class EventServiceImpl implements EventService {

	
	private final EventRepository eventRepository;
	
	public EventServiceImpl(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}
	
	@Override
	public Event createEvent(Event event) {
		return eventRepository.save(event);
	}

	@Override
	public List<Event> getAllEvents() {
		return eventRepository.findAll();
	}

	@Override
	public Event getEventById(Long eventId) {
		return eventRepository.findById(eventId)
				.orElseThrow(() -> new ResourceNotFoundException("No Event Found for id ["+eventId+"]") );
	}

}
