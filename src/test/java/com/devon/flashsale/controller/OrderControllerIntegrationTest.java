package com.devon.flashsale.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.devon.flashsale.FlashsaleTicketingApplication;
import com.devon.flashsale.entity.Event;
import com.devon.flashsale.enums.EventStatus;
import com.devon.flashsale.repository.EventRepository;

@SpringBootTest(classes = FlashsaleTicketingApplication.class)
@AutoConfigureMockMvc
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    private Long validEventId;

    @BeforeEach
    void setup() {        
        Event event = new Event();
        event.setEventName("Test Concert");
        event.setTotalSeats(100);
        event.setRemainingSeats(100);
        event.setStartTime(LocalDateTime.now().minusMinutes(10));
        event.setEndTime(LocalDateTime.now().plusHours(12));
        event.setStatus(EventStatus.ACTIVE);
        Event savedEvent = eventRepository.save(event);

        validEventId = savedEvent.getEventId();
    }
 
    @AfterEach
    void postTestSetup() {
    	eventRepository.deleteById(validEventId);
    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        mockMvc.perform(post("/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"eventId\": " + validEventId + 
                		", \"quantity\": 2 "+
                		", \"idempotencyKey\": \"key-123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnConflict_WhenOutOfStock() throws Exception {
        mockMvc.perform(post("/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"eventId\": " + validEventId 
                		+ ", \"quantity\": 9999"
                		+ ", \"idempotencyKey\": \"key-456\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnUnprocessableEntity_WhenSaleClosed() throws Exception {
        Event closedEvent = new Event();
        closedEvent.setEventName("Closed Event");
        closedEvent.setTotalSeats(50);
        closedEvent.setRemainingSeats(50);
        closedEvent.setStartTime(LocalDateTime.now().minusHours(2));
        closedEvent.setEndTime(LocalDateTime.now().minusHours(1));
        closedEvent.setStatus(EventStatus.CLOSED);
        Event savedClosedEvent = eventRepository.save(closedEvent);

        mockMvc.perform(post("/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"eventId\": " + savedClosedEvent.getEventId() 
                + ", \"quantity\": 1"
                + ", \"idempotencyKey\": \"key-789\"}"))
                .andExpect(status().isUnprocessableEntity());
        
        eventRepository.deleteById(savedClosedEvent.getEventId());
    }
}
