package com.devon.flashsale.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.devon.flashsale.config.FlashSaleMetricsConfig;
import com.devon.flashsale.entity.Event;
import com.devon.flashsale.entity.Order;
import com.devon.flashsale.enums.EventStatus;
import com.devon.flashsale.exceptions.OutOfStockException;
import com.devon.flashsale.repository.EventRepository;
import com.devon.flashsale.repository.OrderRepository;
import com.devon.flashsale.service.impl.OrderServiceImpl;

public class OrderServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private FlashSaleMetricsConfig flashSaleMetricsConfig;

    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateOrder_WhenSeatsAvailable() {
        Event event = new Event();
        event.setEventId(1L);
        event.setEventName("Concert");
        event.setTotalSeats(100);
        event.setRemainingSeats(100);
        event.setVersion(0L);
        event.setStartTime(LocalDateTime.now().minusHours(12));
        event.setEndTime(LocalDateTime.now().plusHours(12));
        event.setStatus(EventStatus.ACTIVE);
        
        Order order = new Order();
        order.setQty(2);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order created = orderServiceImpl.createOrder(1L, 2, "uniqueKey");

        assertNotNull(created);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(flashSaleMetricsConfig, times(1)).incrementOrdersCreated();
    }

    @Test
    void shouldThrowOutOfStockException_WhenSeatsNotAvailable() {
        Event event = new Event();
        event.setEventId(1L);
        event.setEventName("Concert");
        event.setTotalSeats(100);
        event.setRemainingSeats(1);
        event.setVersion(0L);
        event.setStartTime(LocalDateTime.now().minusHours(12));
        event.setEndTime(LocalDateTime.now().plusHours(12));
        event.setStatus(EventStatus.ACTIVE);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(OutOfStockException.class,
                () -> orderServiceImpl.createOrder(1L, 5, "uniqueKey"));
    }

    @Test
    void shouldReturnSameOrder_WhenIdempotencyKeyUsedAgain() {
        Event event = new Event();
        event.setEventId(1L);
        event.setEventName("Concert");
        event.setTotalSeats(100);
        event.setRemainingSeats(100);
        event.setVersion(0L);
        event.setStartTime(LocalDateTime.now().minusHours(12));
        event.setEndTime(LocalDateTime.now().plusHours(12));
        event.setStatus(EventStatus.ACTIVE);
        Order existing = new Order();
        existing.setIdempotencyKey("uniqueKey");

        when(orderRepository.findByIdempotencyKey("uniqueKey"))
                .thenReturn(Optional.of(existing));

        Order result = orderServiceImpl.createOrder(1L, 2, "uniqueKey");
        assertEquals(existing, result);
    }
}
