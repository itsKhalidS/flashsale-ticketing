package com.devon.flashsale.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.devon.flashsale.dto.EventCreationDto;
import com.devon.flashsale.dto.EventResponseDto;
import com.devon.flashsale.entity.Event;
import com.devon.flashsale.enums.EventStatus;
import com.devon.flashsale.exceptions.FlashSaleAppException;
import com.devon.flashsale.exceptions.ResourceNotFoundException;
import com.devon.flashsale.repository.EventRepository;
import com.devon.flashsale.service.EventService;
import com.devon.flashsale.service.storage.StorageService;
import com.devon.flashsale.validation.EventValidator;

import jakarta.transaction.Transactional;

@Service
public class EventServiceImpl implements EventService {
	
	private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);
	
	private final EventRepository eventRepository;
	private final StorageService storageService;
	
	public EventServiceImpl(EventRepository eventRepository, StorageService storageService) {
		this.eventRepository = eventRepository;
		this.storageService = storageService;
	}

	private Event convertEventCreationDtoToEvent(EventCreationDto eventDto) {
		Event event = new Event();
		event.setEventName(eventDto.getEventName());
		event.setDescription(eventDto.getDescription());
		event.setTotalSeats(eventDto.getTotalSeats());
		event.setStartTime(eventDto.getStartTime());
		event.setEndTime(eventDto.getEndTime());
		return event;
	}
	
	private EventResponseDto convertEventToEventResponseDto(Event event) {
		EventResponseDto eventDto = new EventResponseDto();
		eventDto.setEventId(event.getEventId());
		eventDto.setEventName(event.getEventName());
		eventDto.setDescription(event.getDescription());
		eventDto.setTotalSeats(event.getTotalSeats());
		eventDto.setRemainingSeats(event.getRemainingSeats());
		eventDto.setStartTime(event.getStartTime());
		eventDto.setEndTime(event.getEndTime());
		eventDto.setStatus(event.getStatus());
		eventDto.setImageUrl(event.getImageUrl());
		return eventDto;
	}

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public EventResponseDto createEvent(EventCreationDto eventDto, MultipartFile eventImage) {
		Event event = convertEventCreationDtoToEvent(eventDto);
		List<FlashSaleAppException> exceptions = EventValidator.validateNewEvent(event);
		if(exceptions.size() > 0) {
			throw exceptions.get(0);
		}
		String imageUrl = storageService.uploadEventImage(eventImage);
		try {
			event.setImageUrl(imageUrl);
			Event savedEvent = eventRepository.save(event);
			log.info("Event with Event Id: {} created", savedEvent.getEventId());
			return convertEventToEventResponseDto(savedEvent);
		}catch (Exception exp) {
			log.error("Failed to create event {}", eventDto.getEventName(), exp);
			storageService.deleteEventImage(imageUrl);
		    throw exp;
		}
	}

	@Override
	public List<EventResponseDto> getAllEvents() {
		return eventRepository.findAll().stream().map(this::convertEventToEventResponseDto).toList();
	}

	@Override
	public EventResponseDto getEventById(Long eventId) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new ResourceNotFoundException("No Event Found for id ["+eventId+"]") );
		log.info("Event with Event Id: {} found", event.getEventId());
		return convertEventToEventResponseDto(event);
	}
	
	@Override
	@Transactional
	public int updateEventStatus(Long eventId, EventStatus status) {
		int rowEffected = eventRepository.updateEventStatus(eventId, status);
		if(rowEffected == 1)
			log.info("Status updated to [{}] for Event with Event Id: {}", status, eventId);
		return rowEffected;
	}

}
