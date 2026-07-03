package com.devon.flashsale.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.devon.flashsale.dto.EventCreationDto;
import com.devon.flashsale.dto.EventResponseDto;
import com.devon.flashsale.dto.PageResponse;
import com.devon.flashsale.enums.EventStatus;

public interface EventService {
	
	
	/** Create a new event
	 * @param event
	 * @return The event created
	 */
	public EventResponseDto createEvent(EventCreationDto eventDto, MultipartFile file);
	
	/** Fetches all events
	 * @return
	 */
	public PageResponse<EventResponseDto> getAllEventsPaginated(int page, int size, String sortBy, String direction);
	
	/** Fetches event for the particular event id.
	 * @param eventId 
	 * @return
	 */
	public EventResponseDto getEventById(Long eventId);
	
	public List<EventResponseDto> getAllEvents();
	
	/** Update the Status of a particular event
	 * @param eventId : The eventId of the Event
	 * @param status : The status to update
	 * @return : The number of rows effected
	 */
	public int updateEventStatus(Long eventId, EventStatus status);
}
