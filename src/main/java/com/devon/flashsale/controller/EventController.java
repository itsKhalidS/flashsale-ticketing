package com.devon.flashsale.controller;

import java.util.List;

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

@RestController
@RequestMapping("/event")
public class EventController {
	
	private final EventService eventService;
	
	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	@GetMapping
	@ResponseBody
	public List<Event> fetchAllEvents() {
		return eventService.getAllEvents();
	}

	@PostMapping("/create")
	@ResponseBody
	public Event createNewEvent(@RequestBody Event event) {
		List<FlashSaleAppException> exceptions = EventValidator.validateNewEvent(event);
		if(exceptions.size() > 0) {
			throw exceptions.get(0);
		}
		return eventService.createEvent(event);
	}
	
	@GetMapping("/{id}")
	@ResponseBody
	public Event fetchEventById(@PathVariable Long id) {
		return eventService.getEventById(id);
	}
}
