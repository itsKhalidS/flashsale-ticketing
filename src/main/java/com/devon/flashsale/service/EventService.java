package com.devon.flashsale.service;

import java.util.List;

import com.devon.flashsale.entity.Event;
import com.devon.flashsale.enums.EventStatus;

public interface EventService {
	
	
	/** Create a new event
	 * @param event
	 * @return The event created
	 */
	public Event createEvent(Event event);
	
	/** Fetches all events
	 * @return
	 */
	public List<Event> getAllEvents();
	
	/** Fetches event for the particular event id.
	 * @param eventId 
	 * @return
	 */
	public Event getEventById(Long eventId);
	
	/** Update the Status of a particular event
	 * @param eventId : The eventId of the Event
	 * @param status : The status to update
	 * @return : The number of rows effected
	 */
	public int updateEventStatus(Long eventId, EventStatus status);
}
