package com.devon.flashsale.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.devon.flashsale.entity.Event;
import com.devon.flashsale.exceptions.FlashSaleAppException;
import com.devon.flashsale.service.EventService;
import com.devon.flashsale.validation.EventValidator;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/event")
public class EventController {
	
	private static final Logger log = LoggerFactory.getLogger(EventController.class);
	
	private final EventService eventService;
	
	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	/**
	 * @return The List of all events
	 */
	@GetMapping
	@ResponseBody
	public List<Event> fetchAllEvents() {
		log.info("Fetching all events");
		return eventService.getAllEvents();
	}

	/**
	 * @param event : The event to create
	 * @return The created event
	 */
	@PostMapping("/create")
	@ResponseBody
	public Event createNewEvent(@RequestBody @Valid Event event) {
		log.info("Event Creation request received for Event: {}", event.getEventName());
		List<FlashSaleAppException> exceptions = EventValidator.validateNewEvent(event);
		if(exceptions.size() > 0) {
			throw exceptions.get(0);
		}
		return eventService.createEvent(event);
	}
	
	/**
	 * @param id : The eventId
	 * @return The event fetched
	 */
	@GetMapping("/{id}")
	@ResponseBody
	public Event fetchEventById(@PathVariable Long id) {
		log.info("Fetching event with EventId: {}", id);
		return eventService.getEventById(id);
	}
}
