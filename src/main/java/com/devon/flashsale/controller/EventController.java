package com.devon.flashsale.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.devon.flashsale.dto.EventCreationDto;
import com.devon.flashsale.dto.EventResponseDto;
import com.devon.flashsale.dto.PageResponse;
import com.devon.flashsale.service.EventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/event")
public class EventController {
	
	private static final Logger log = LoggerFactory.getLogger(EventController.class);
	
	private static final int pageSize = 12;
	private static final String sortBy = "startTime";
	private static final String sortDirection = "asc";
	
	private final EventService eventService;
	
	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	/**
	 * @return The List of all events
	 */
	@GetMapping
	@ResponseBody	
	public PageResponse<EventResponseDto> fetchAllEvents(@RequestParam(defaultValue = "0") int page, @RequestParam(required = false) String search) {
	    log.info("Fetching events for page={}, size={}, sortBy={}, direction={} and searchKeyword={}",page, pageSize, sortBy, sortDirection, search);
	    return eventService.getAllEventsPaginated(page, pageSize, sortBy, sortDirection, search);
	}
	
	/**
	 * @param id : The eventId
	 * @return The event fetched
	 */
	@GetMapping("/{id}")
	@ResponseBody
	public EventResponseDto fetchEventById(@PathVariable Long id) {
		log.info("Fetching event with EventId: {}", id);
		return eventService.getEventById(id);
	}
	
	/**
	 * 
	 * @param eventDto : The event to create
	 * @param image : The event image
	 * @return The created event
	 */
	@PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public EventResponseDto createNewEvent( @RequestPart("event") @Valid EventCreationDto eventDto, @RequestPart("image") MultipartFile image){
		log.info("Event Creation request received for Event: {}", eventDto.getEventName());
		return eventService.createEvent(eventDto, image);
	}
}
