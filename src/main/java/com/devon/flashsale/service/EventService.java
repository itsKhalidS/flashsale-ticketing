package com.devon.flashsale.service;

import java.util.List;

import com.devon.flashsale.entity.Event;

public interface EventService {
	
	public Event createEvent(Event event);
	
	public List<Event> getAllEvents();
	
	public Event getEventById(Long eventId);
}
