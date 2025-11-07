package com.devon.flashsale.service;

import com.devon.flashsale.entity.Event;
import com.devon.flashsale.enums.EventStatus;
import com.devon.flashsale.exceptions.ResourceNotFoundException;
import com.devon.flashsale.repository.EventRepository;
import com.devon.flashsale.service.impl.EventServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventServiceImpl;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldGetEventDetails_WhenExists() {
        Event event = new Event();
        event.setEventId(1L);
        event.setEventName("Concert");
        event.setTotalSeats(100);
        event.setRemainingSeats(50);
        event.setVersion(0L);
        event.setStartTime(LocalDateTime.now().minusHours(12));
        event.setEndTime(LocalDateTime.now().plusHours(12));
        event.setStatus(EventStatus.ACTIVE);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Event result = eventServiceImpl.getEventById(1L);
        assertEquals("Concert", result.getEventName());
        assertEquals(50, result.getRemainingSeats());
    }

    @Test
    void shouldThrowException_WhenEventNotFound() {
        when(eventRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> eventServiceImpl.getEventById(2L));
    }
}
